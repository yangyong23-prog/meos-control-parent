# 通风需求保存后非通风空间生成配置结果修复 - 技术设计

## 问题边界

事件驱动刷新路径 `DemandConfigResultRefreshRepositoryImpl.refreshByInstanceAndChildren` 按物理层级展开下级实例（项目 → 全部建筑/楼层/空间），随后 `refreshInstances` 稳定计算时未校验需求空间。通风为 FIELD 字段继承，非通风空间也会从上级继承出非空 `finalConfig`，被写入 `demand_config_result`。

定时任务全量刷新路径（`refreshByTemplateType` → `toDemandScopeInstanceTypeAndInstanceIds`）先通过 `DemandStrategy.listDemandSpace(templateType)` 查询需求空间，口径正确。两条路径不一致是缺陷根源。

## 通用性定位（用户强调：预计算是通用功能）

`demand_config_result` 预计算是所有需求类型共用的能力。正确解法不是修补某一个调用方（通风事件路径），而是把**"结果表只写入需求空间及其关联层级"这一不变量内建到实例级刷新的统一入口**，让所有调用方（现在的事件刷新、黑名单移出刷新、Debug 单实例刷新，以及未来的照明/安防接入）自动受保护。

### 写入口汇聚结构（代码证据）

- 实例级入口统一汇聚到 `refreshInstances(templateId, sceneId, instances)`（`DemandConfigResultRefreshRepositoryImpl.java:330`）：
  - `refreshInstance`（Debug 单实例，131 行）
  - `refreshByInstanceAndChildren`（配置事件刷新，154 行）
  - `refreshByDtxChangedInstances`（黑名单移出刷新，241 行）
- 范围级入口统一汇聚到 `refreshByQueryParam`（373/392 行）：
  - `refreshByTemplate`（模板 INSERT，510 行）
  - `refreshByTemplateType`（定时任务全量，535 行）——调用方已通过 `toDemandScopeInstanceTypeAndInstanceIds`（618-658 行）按 `DemandStrategy.listDemandSpace` 预过滤需求空间，口径正确且通用，无需改动。

### 各需求策略的范围过滤支持度（决定过滤口径）

各策略对"按实例范围过滤需求空间"的支持度不一致，因此统一过滤必须采用各策略都成立的全项目口径：

1. 空调 `DemandAirconditioningStrategyImpl.listDemandSpace`：`param.queryObject(...)` 不过滤，全项目口径。
2. 通风 `DemandVentilationStrategyImpl.listDemandSpace`：经 `BaseRunningMonitorStrategy.listSpaces(RunningMonitorSpaceQueryParam)` 委托到老版三参方法，其 `toInstance(param)` **仅支持单实例**（多元素抛 `ValidationException`），且 SPACE 实例不设置查询 ID、过滤不生效。
3. 照明 `RunningMonitorLightingStrategy.listSpaces(RunningMonitorSpaceQueryParam)`：自行重写接口方法（`listCircuits(...)` 链路），行为独立。
4. 安防 `DemandSecurityStrategyImpl.getSpacesByEquipment`：走 `SpaceQueryParam.queryObject(...)`，支持多实例范围过滤。

结论：过滤统一使用**全项目需求空间查询**（`DemandSpacesQueryParam(templateType)` 不设范围过滤），再与本次刷新实例集合求交，不依赖任何策略的范围过滤能力。

## 方案

### 采用：在 `refreshInstances` 统一入口内建需求空间过滤

`refreshInstances` 开头新增通用前置过滤步骤 `filterInstancesByDemandSpaces(templateId, instances)`：

1. 从 `instances` 中收集 SPACE / FLOOR / BUILDING 实例；三种都没有（如仅 PROJECT）时直接返回原集合。
2. 通过 `queryTemplate(templateId)` 获取 `templateType`，调用 `listDemandSpaces(templateType)` 查询该模板类型**全项目需求空间**（带 `id`、`floorId`、`buildingId` 列，不设范围/黑名单/屏蔽过滤）。
3. 过滤规则（逐实例判断）：
   - SPACE：仅保留在全项目需求空间集合中的空间；
   - FLOOR：仅保留需求空间关联的楼层（非空 `floorId` 推导）；
   - BUILDING：仅保留需求空间关联的建筑（非空 `buildingId` 推导）；
   - PROJECT：保留（项目级配置结果始终有效）。
4. 需求空间为空时，SPACE/FLOOR/BUILDING 全部剔除，仅保留 PROJECT 等上级实例。
5. 过滤后的集合为空时直接返回，不执行稳定计算。
6. 后续稳定计算、差集删除（`toMissingDeleteParams`）、upsert 全部基于过滤后的集合。

黑名单/屏蔽不在本步处理：仍由稳定链路的 `removeInvalidInstances` 与屏蔽事件监听器负责，与现状一致。

### 各入口影响评估

- 配置事件刷新（`refreshByInstanceAndChildren`）：`expandInstancesAndChildren` 物理展开**保持原样**，非需求空间在 `refreshInstances` 统一入口被剔除，不再写入结果表。bug 根因消除。
- 黑名单移出刷新（`refreshByDtxChangedInstances`）：传入实例本就是需求空间，过滤无损，仅增加一次全项目需求空间查询（频率低，可接受）。
- Debug 单实例（`refreshInstance`）：非需求空间被过滤不写结果，符合预计算通用不变量。
- 模板/定时任务范围级刷新：调用方已按需求空间预过滤，本方案不改动。
- 空调：需求空间口径为全项目空间，过滤后行为与修复前基本一致。

### 放弃的方案

1. 仅修事件路径（在 `expandInstancesAndChildren` 中过滤）：只保护一个调用方，未来照明/安防接入或新增刷新入口时需各自适配，违背通用功能定位。
2. 按来源实例范围循环查询需求空间：依赖各策略不一致的范围过滤能力（通风单实例限制、SPACE 失效），与策略实现耦合。

## 数据流

```
配置变更 / 黑名单移出 / Debug
  → refreshByInstanceAndChildren / refreshByDtxChangedInstances / refreshInstance
      → refreshInstances(templateId, sceneId, instances)          [改造]
          → filterInstancesByDemandSpaces(templateId, instances)  [新增·统一前置过滤]
              ├─ queryTemplate(templateId) → templateType
              ├─ listDemandSpaces(templateType) 全项目需求空间    [新增]
              └─ SPACE/FLOOR/BUILDING 按需求空间口径过滤, PROJECT 保留
          → listStableConfigsByInstances 稳定计算（过滤后实例）
          → syncResults(delete, upsert) 差集同步结果表
```

## 兼容性

- 空调：需求空间口径不变（全项目空间），事件刷新行为与修复前基本一致。
- 通风：修复后仅需求空间及其关联层级写入结果，符合定时任务口径。
- 未来需求类型（照明/安防等）：接入预计算后所有实例级刷新入口自动按全项目需求空间口径过滤，无需额外适配。
- 结果表查询端（`listConfigResultsByInstances` 按 SPACE 查询）不需要改动。

## 性能

- 实例级刷新每次新增 1 次全项目需求空间查询（通风策略较重：设备+关系查询）+ 1 次模板查询；实例级刷新均为低频事件（配置保存、黑名单变更、Debug 手动触发），可接受。
- 事件路径的物理展开（建筑/楼层/空间 3 次 RWD）保持不变，不新增开销。

## 风险与回滚

- 风险：通风策略 `listDemandSpace` 依赖 RWD 设备/关系查询，若测试环境 RWD 数据异常导致需求空间为空，SPACE/FLOOR/BUILDING 结果不再写入，仅保留 PROJECT。该行为与定时任务路径一致（无需求空间时删除整类结果），属于正确收敛而非回归。
- 回滚：改动集中在 `DemandConfigResultRefreshRepositoryImpl` 的 `refreshInstances` 及新增私有方法，`git checkout` 单文件即可回滚。

## 涉及文件

- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java`
  - `refreshInstances`：开头调用通用前置过滤，后续逻辑基于过滤后的实例集合。
  - 新增私有方法 `filterInstancesByDemandSpaces(templateId, instances)`：实例级刷新的统一需求空间过滤入口。
  - 新增私有方法 `listDemandSpaces(templateType)`：查询模板类型全项目需求空间（带 `id`、`floorId`、`buildingId` 列）。
  - `expandInstancesAndChildren` / `refreshByInstanceAndChildren` / `refreshByDtxChangedInstances` / `refreshInstance`：保持原样。
  - `InstanceService instanceService` 字段保留（物理展开仍使用）。
