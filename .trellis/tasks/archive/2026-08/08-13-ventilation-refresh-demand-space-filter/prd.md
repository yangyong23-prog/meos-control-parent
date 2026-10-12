# 通风需求保存后非通风空间生成配置结果修复

## Goal

修复测试提出的缺陷：通风需求配置保存后，需求配置结果预计算表（`demand_config_result`）中出现大量不属于通风需求的空间记录，导致通风运行监测等按空间读取预计算结果的链路拿到错误数据。

## Background

### 缺陷症状

- 保存通风需求配置（项目/建筑/楼层级）后，很多"通风不存在的空间"（无通风设备服务、功能类型不匹配的空间）生成了需求配置结果。
- 这些空间本不属于通风需求空间（机房后勤 `SFT-02.`、停车场 `SFT-04.`、卫生间 `SFT-01.06.`、穹顶 `SFT-01.05.`）。

### 已确认的根因（代码证据）

保存配置后的事件刷新链路缺少需求空间过滤：

1. 保存入口 `POST /demand/addDemandConfig` → `DemandConfigGatewayImpl.addOrUpdateConfig`（`common/.../structure/demand/gateway/DemandConfigGatewayImpl.java:804`）在事务内发布 `DemandConfigChangeEvent`。
2. 事件监听器 `DemandConfigResultRefreshEventListener.onDemandConfigChangeEvent`（`common/.../structure/demand/listener/DemandConfigResultRefreshEventListener.java:78-103`）按实例类型调用 `DemandConfigResultRefreshRepository.refreshByInstanceAndChildren(templateId, sceneId, instanceType, instanceIds)`。
3. `DemandConfigResultRefreshRepositoryImpl.expandInstancesAndChildren`（`common/.../structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:1069-1088`）通过 `InstanceService.listSpaceIdsByInstance` **按物理层级展开**：项目 → 项目下全部建筑/楼层/空间，不区分该模板类型的需求空间。
4. `refreshInstances` → `listStableConfigsByInstances` 稳定计算链路（`DemandConfigAggregateInitByInstanceContext`）对传入实例不做需求空间校验，通风为 FIELD 字段继承（`InheritConfigTypeEnum.FIELD`），每个无自身配置的空间都会从上级继承出非空 `finalConfig`。
5. 最终 `toUpsertParams` 将全部展开空间 upsert 进 `demand_config_result`。

### 对比：定时任务路径是正确的

- 定时任务全量刷新 `refreshByTemplateType` → `toDemandScopeInstanceTypeAndInstanceIds`（`DemandConfigResultRefreshRepositoryImpl.java:618-658`）先通过 `demandStrategy.listDemandSpace(templateType)` 查询需求空间，再进入刷新。
- 通风需求空间由 `RunningMonitorVentilationStrategy.listSpaces`（`common/.../structure/common/strategy/impl/RunningMonitorVentilationStrategy.java:114-185`）过滤：必须有通风设备（ACVTSF 送/排风机等）服务，且功能类型匹配。
- 空调 `DemandAirconditioningStrategyImpl.listDemandSpace`（`.../demand/strategy/airconditioning/DemandAirconditioningStrategyImpl.java:60-70`）不做特殊过滤（基本覆盖全部空间），因此同一缺陷在空调上不明显。

### 脏数据自愈能力（已确认）

- 定时任务路径按模板+场景边界走 `DemandConfigResultRepositoryImpl.syncRefreshScope`（`common/.../structure/demand/repository/DemandConfigResultRepositoryImpl.java:122-159`），在范围锁内删除不在新结果业务键集合中的旧行。云端定时任务全量刷新执行后，非需求空间的脏结果会被自动清理。

## Requirements

1. 实例级刷新统一入口（`DemandConfigResultRefreshRepositoryImpl.refreshInstances`）内建需求空间过滤，作为预计算的通用不变量：
   - 仅刷新该模板类型的需求空间（含来源实例自身）；
   - 楼层/建筑层级仅保留需求空间关联的楼层/建筑（与 `toDemandScopeInstanceTypeAndInstanceIds` 口径一致）；
   - 配置事件刷新、黑名单移出刷新、Debug 单实例刷新等所有实例级入口统一受保护，未来接入新需求类型时自动生效。
2. 修复覆盖所有受支持需求类型（空调 + 通风），通过 `SUPPORTED_DEMAND_TYPES` / `DemandStrategy.listDemandSpace` 统一实现，不针对通风硬编码；方案不依赖各策略对"按实例范围过滤"的特殊支持，采用全项目需求空间统一口径，未来接入照明/安防等新需求类型时事件刷新自动生效。
3. 保存通风配置（项目/建筑/楼层/空间级）后，非通风需求空间不得再写入或残留需求配置结果。

## Acceptance Criteria

- [ ] 保存项目级通风配置后，`demand_config_result` 中该通风模板类型下的 SPACE 记录仅包含通风需求空间（有通风设备服务且功能类型匹配）。
- [ ] 保存建筑/楼层级通风配置后，结果表同样只写入该范围内真正的通风需求空间及其关联楼层/建筑层级结果。
- [ ] 保存空间级通风配置后，仅刷新该空间及关联上级层级，行为不回归。
- [ ] 空调配置保存的事件刷新行为不回归（空调需求空间口径不变）。
- [ ] 事件刷新路径与定时任务路径的需求空间口径一致（同一模板类型、同一项目下两者刷出的 SPACE 集合相同）。
- [ ] 代码编译通过；需求配置结果刷新相关既有单测/行为不回归。

## Out of Scope

- 定时任务全量刷新逻辑改动（脏数据由既有全量刷新自愈）。
- 测试环境已有脏数据的清理（测试人员自行处理）。
- `demand_config_result` 表结构与迁移脚本变更。
- 通风/空调需求空间判定规则本身（`RunningMonitorVentilationStrategy.listSpaces` 等）的修改。
- 前端改动。
