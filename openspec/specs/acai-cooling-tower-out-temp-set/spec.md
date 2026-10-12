## ADDED Requirements

### Requirement: ACAI Excel 模板新增冷却塔出水温度设定配置行

ACAI 系统详情页 Excel 模板（`【AIR_CONDITIONING_C_H_SOURCE-DETAIL】系统详情页模版.xlsx`）的 `heatDissipationControlLRY` 分组中 SHALL 新增 `INFO_CODE_SET_EXTEND_CONFIG` 和 `IOT_SETTING` 两行配置，用于加载和展示冷却塔出水温度设定（`coolingTowerOutTempSet`）的 `ExtendSetting`（含 manualAutoSet、ΔT、minValue）和 IoT 实时值。配置模式 MUST 与 ACAC 的 `heatDissipationControl` 分组中 `coolingTowerOutTempSet` 配置完全一致。

#### Scenario: ACAI Excel 模板包含 INFO_CODE_SET_EXTEND_CONFIG 行

- **WHEN** 查看 ACAI Excel 模板的 `heatDissipationControlLRY` 分组
- **THEN** SHALL 存在一行 `INFO_CODE_SET_EXTEND_CONFIG` 类型配置，其 configJson 为 `{"infoCode":"coolingTowerOutTempSet"}`，用于从 `info_code_set_extend` 表加载 `ExtendSetting`

#### Scenario: ACAI Excel 模板包含 IOT_SETTING 行

- **WHEN** 查看 ACAI Excel 模板的 `heatDissipationControlLRY` 分组
- **THEN** SHALL 存在一行 `IOT_SETTING` 类型配置，其 infoCode 为 `coolingTowerOutTempSet`，且 relateConfig 引用同分组的 `INFO_CODE_SET_EXTEND_CONFIG` 行，用于展示冷却塔出水温度的 IoT 实时值

#### Scenario: 新增配置行排在分组第一位

- **WHEN** 查看 ACAI Excel 模板的 `heatDissipationControlLRY` 分组各行排序
- **THEN** `INFO_CODE_SET_EXTEND_CONFIG` 行排序 SHALL 为 1，`IOT_SETTING` 行排序 SHALL 为 2，现有 `coolWaterReturnTempSet` 和 `coolWaterDetaTempSet` 行排序相应后移

### Requirement: ACAI 系统首次访问展示默认冷却塔出水温度设定配置

当 ACAI 系统首次访问 `listInfoCodeConfigsByObjectId` 接口且 `info_code_set_extend` 表中无 `coolingTowerOutTempSet` 记录时，系统 SHALL 创建默认配置，默认 `manualAutoSet` 为 `MANUAL`（人工），与 PRD-260801"默认人工"要求一致。

#### Scenario: 无 DB 记录时创建默认 MANUAL 配置

- **WHEN** ACAI 系统调用 `listInfoCodeConfigsByObjectId` 接口，且 `info_code_set_extend` 表中无该系统的 `coolingTowerOutTempSet` 记录
- **THEN** `InfoCodeSetExtendConfigAnalyzeStrategy` SHALL 通过 `createDefaultConfig` 创建默认 `ExtendSetting`，其中 `manualAutoSet` 为 `MANUAL`

#### Scenario: ACAC 默认值不受影响

- **WHEN** ACAC 系统调用 `listInfoCodeConfigsByObjectId` 接口，且 `info_code_set_extend` 表中无该系统的 `coolingTowerOutTempSet` 记录
- **THEN** 系统 SHALL 创建默认 `ExtendSetting`，其中 `manualAutoSet` 为 `AUTO`（保持原有行为不变）

#### Scenario: ACAI 其他信息点默认值不受影响

- **WHEN** `createDefaultConfig` 方法被调用，`runningMonitorType` 为 `AIR_CONDITIONING_C_H_SOURCE`，`infoCode` 为 `ZDYairConditioningCapacitySet` 或 `runModeSet`
- **THEN** 系统 SHALL 创建默认 `ExtendSetting`，其中 `manualAutoSet` 为 `AUTO`（保持原有行为不变）

### Requirement: ACAI 通过 extendInfoCodeValueConfig 推送 ExtendSetting 到 BDTP

ACAI 系统通过现有 `extendInfoCodeValueConfig` API 推送完整 `ExtendSetting`（含 manualAutoSet、ΔT、minValue）到 BDTP，推送逻辑 MUST 与 ACAC 一致，无需新增或修改推送代码。

#### Scenario: 推送完整 ExtendSetting 到 BDTP

- **WHEN** 前端调用 `extendInfoCodeValueConfig` API，传入 ACAI 系统的 `coolingTowerOutTempSet` 配置（含 manualAutoSet、ΔT、minValue）
- **THEN** 系统 SHALL 将完整 `ExtendSetting` 保存到 `info_code_set_extend` 表，并通过 `commandSend` 推送到 BDTP

#### Scenario: BDTP 自动模式下定时重算

- **WHEN** `manualAutoSet` 为 `AUTO`，BDTP 收到 `ExtendSetting` 推送
- **THEN** BDTP SHALL 根据湿球温度 + ΔT 计算冷却塔出水温度，且计算结果 MUST ≥ minValue，每 5 分钟定时重算
