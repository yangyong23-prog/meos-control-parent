# 技术设计：反馈调节空间新增回路置未配置

## 1. 问题根因

`DemandLightingCircuitSettingModeEnum.getSettingMode(parentSettingMode, circuitSettingMode)` 是"新增回路状态继承"的唯一决策点：当已保存配置中不存在该回路（`circuitSettingMode == null`，即新增回路）时，按上级功能类型状态继承。

其中 `FEEDBACK_ADJUSTMENT → FEEDBACK_ADJUSTMENT` 分支是 5.241202.0（空间级统一条件时代）加入的：当时反馈条件是空间级一份，回路继承"反馈调节"即可生效，语义合理。7.260801.0 条件下沉到回路级后，新增回路没有回路级条件，继承为"反馈调节 + null 条件"成为脏数据，与代码中"反馈条件不需要继承"的既有设计意图（`baseResultMapBuilding` 注释）冲突。

## 2. 现状调用点盘点（getSettingMode 共 4 处）

| # | 调用点 | 所属链路 | 新增回路第二参 | 修复后行为 |
|---|---|---|---|---|
| 1 | `DemandConfigLightingComputeServiceImpl.merge()` | 干预计算（`interventionResultCompute`） | `circuit2setting.get(id)` 为 null | → NOT_CONFIGURED ✅ |
| 2 | `DemandLightingStrategyImpl.configBuild()` | 配置预览（`listAllSpaceSceneObjectSettings`）、边缘端干预装配（`DemandCommandTask.getStringListMap`） | 恒为 null | 见 §3.2，需同步改造 |
| 3 | `DemandConfigLightingHandler.globalConfigSetting()` (L881) | 配置编辑页-全局配置 VO 组装 | `circuitId2setting.get(id)` 为 null | → NOT_CONFIGURED ✅ |
| 4 | `DemandConfigLightingHandler.buildingFuncTypeSetting()` (L1013) | 配置编辑页-空间配置 VO 组装 | `circuitId2settingMode.get(id)` 为 null | → NOT_CONFIGURED ✅ |

已保存回路在所有调用点均命中 `getSettingMode` 第一分支"当前配置存在，直接返回当前配置状态"，不受修改影响（保障 R2）。

## 3. 修复方案

### 3.1 改动点一：枚举继承分支（核心）

**文件**: `common/.../structure/demand/enumeration/lighting/DemandLightingCircuitSettingModeEnum.java`

`getSettingMode()` 中删除（或改为返回 NOT_CONFIGURED 并注释说明）`FEEDBACK_ADJUSTMENT → FEEDBACK_ADJUSTMENT` 分支，使反馈调节功能类型下的新增回路落到默认 `NOT_CONFIGURED`。

保留显式分支 + 注释的写法，表达"反馈调节不继承"的意图，避免后续误加回继承逻辑。

### 3.2 改动点二：configBuild 反馈调节清空重建分支（关联修复）

**文件**: `common/.../structure/demand/strategy/lighting/DemandLightingStrategyImpl.java` `configBuild()` 内 L1509-L1517

现状分支：功能类型为 FEEDBACK_ADJUSTMENT 时清空 `circuitSettings` 后 `break`，随后全部交付回路（`subtract`）按 null 条件重建。

问题：
- 该分支是旧模型（空间级统一条件、不存回路明细）的展开逻辑；新模型下 `circuitSettings` 持久化存储"已配置条件的回路"（`checkAndSet()` 中 FEEDBACK_ADJUSTMENT case 移除 NOT_CONFIGURED 回路），清空重建会**丢弃已保存的回路级条件**；
- 且 `break` 会跳过其余功能类型的筛选注册；
- 若只改枚举不改这里，该功能类型下**所有**回路（含已配置）都会被重建为 NOT_CONFIGURED，比现状更糟。

方案：删除该特殊分支，反馈调节功能类型走通用筛选逻辑：
- 已保存回路：正常筛选（移除交付已删除的）→ 保留状态与条件（DTO 拷贝携带 `feedbackAdjustSetting`）；
- 新增回路：`subtract` 阶段经 `getSettingMode(FEEDBACK_ADJUSTMENT, null)` → NOT_CONFIGURED；
- 分支删除后 `settingMode` 局部变量失去用途，一并清理。

### 3.3 不需要改动的部分

- `merge()`：只重建 circuitId + settingMode，不携带条件——干预计算只消费 settingMode，无影响。
- `DemandConfigLightingHandler` L881/L1013：仅补 settingMode；条件回显由结果视图链路（`getCircuitModeBySceneSettings` → `setFeedbackAdjustSetting`，L317/L558）负责，不在本次范围。
- DTX 交付变更处理（`DemandLightingDtxPublishChangeContext`）：只做减法清理，新增回路本就不入库，由查询/计算时动态补全，职责不变。
- 需求计算 `putCircuitIdResults`：NOT_CONFIGURED 时取消配置来源（`sourceObjId = null`），天然满足 AC4，无需改动。

## 4. 影响面分析

| 链路 | 修复前（新增回路） | 修复后（新增回路） |
|---|---|---|
| 配置预览 / 空间配置查询 | 反馈调节 + null 条件 | 未配置 |
| 配置编辑页（全局/空间 VO） | 反馈调节 | 未配置 |
| 干预计算（运行监控） | 反馈调节 | 未配置 |
| 边缘端干预装配 | 反馈调节 + null 条件 | 未配置 |
| 边缘端需求下发 | 计入反馈调节，可能整组判为"3=反馈" | 不计入反馈调节，功能类型不统一时空间写"99=其他"，提醒补充配置 |

存量数据：无迁移；已保存配置不受影响。7.260801.0 前的空间级反馈调节旧数据在本版已由配置模型改造处理，不在本次范围。

## 5. 风险与回滚

- 风险：反馈调节功能类型整组新增（如全新交付的功能类型）时，所有回路显示未配置，需逐个补充——符合产品预期行为。
- 回滚：两个改动点均为局部代码修改，`git revert` 即可恢复原继承行为。
