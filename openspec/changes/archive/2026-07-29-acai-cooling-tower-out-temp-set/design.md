## Context

空调冷源（ACAC）系统详情页已实现"冷却塔出水温度设定"功能（`coolingTowerOutTempSet`），通过 Excel 模板中 `INFO_CODE_SET_EXTEND_CONFIG` + `IOT_SETTING` 两行配合模式，从 `info_code_set_extend` 表加载 `ExtendSetting`（含 manualAutoSet、ΔT、minValue），并通过 `extendInfoCodeValueConfig` API 推送完整 `ExtendSetting` 到 BDTP。

空调冷热源（ACAI）系统详情页当前 `heatDissipationControlLRY` 分组仅有 `coolWaterReturnTempSet` 和 `coolWaterDetaTempSet` 两个 IOT_SETTING 行，缺少冷却塔出水温度设定功能。ACAI 对应的运行监控类型为 `AIR_CONDITIONING_C_H_SOURCE`。

`InfoCodeSetExtendConfigAnalyzeStrategy` 中已有 `createDefaultConfig` 机制，在无 DB 记录时根据 `runningMonitorType` + `infoCode` 判断是否创建默认配置（默认 AUTO）。当前仅覆盖 ACAC（`AIR_CONDITIONING_COLD_SOURCE`），ACAI（`AIR_CONDITIONING_C_H_SOURCE`）未覆盖。

## Goals / Non-Goals

**Goals:**
- ACAI 系统详情页新增"冷却塔出水温度设定"功能，逻辑与 ACAC 完全一致
- ACAI 系统首次访问时能展示默认配置（默认 AUTO）
- 复用现有 `extendInfoCodeValueConfig` API 推送 `ExtendSetting` 到 BDTP，推送逻辑零改动
- 改动最小化

**Non-Goals:**
- 不修改 `ExtendSetting` DTO 结构
- 不修改 `extendInfoCodeValueConfig` API 的推送逻辑
- 不修改 BDTP 自动计算逻辑（湿球温度 + ΔT，且 ≥ minValue，5min 定时重算）
- 不修改 `buildDefaultInfoCodeSetExtend` 默认值（已为 AUTO，与 ACAC 一致）
- 不新增数据库表或字段

## Decisions

### 1. Excel 配置采用 INFO_CODE_SET_EXTEND_CONFIG + IOT_SETTING 两行配合模式

**选择**: 与 ACAC 完全一致的两行配合模式。

**理由**: ACAC 已验证此模式可行，`InfoCodeSetExtendConfigAnalyzeStrategy` 已支持该配置类型的解析，无需新增策略类。

**替代方案**: 将 ΔT 和 minValue 作为独立 infoCode 配置。否决，因为这会改变数据模型，且与 ACAC 不一致。

### 2. 默认配置判断新增 ACAI 类型，复用 buildDefaultInfoCodeSetExtend

**选择**: 在 `InfoCodeSetExtendConfigAnalyzeStrategy` 中新增 `isAirConditioningCHSourceExtendConfigDefault` 方法，判断 `runningMonitorType == AIR_CONDITIONING_C_H_SOURCE` 且 `infoCode == coolingTowerOutTempSet`，并在 `createDefaultConfig` 中增加调用。

**理由**: 最小改动，仅需 1 个方法 + 1 行调用。`buildDefaultInfoCodeSetExtend` 默认 AUTO，与 ACAC 一致，无需修改。

**替代方案**: 修改 `isAirConditioningColdSourceExtendConfigDefault` 方法直接加入 ACAI 判断。否决，因为方法名语义不符，且不利于后续维护。

### 3. 推送路径复用 extendInfoCodeValueConfig，不走 outsideCoolWaterReturnTempCfg

**选择**: ACAI 走 `extendInfoCodeValueConfig` 路径推送完整 `ExtendSetting` 到 BDTP。

**理由**: ACAI 的 classCode 不是 `COLD_SYSTEM`，不会走 `outsideCoolWaterReturnTempCfg` 路径。`extendInfoCodeValueConfig` 已支持完整 `ExtendSetting` 推送（含 manualAutoSet、ΔT、minValue），零改动可复用。

### 4. ACAI Excel 模板中 coolingTowerOutTempSet 排在 heatDissipationControlLRY 分组第一位

**选择**: 新增的 `INFO_CODE_SET_EXTEND_CONFIG` 行排序为 1，`IOT_SETTING` 行排序为 2，现有行排序后移。

**理由**: 与 ACAC 模板中 `coolingTowerOutTempSet` 在 `heatDissipationControl` 分组中的排序一致。

## Risks / Trade-offs

- **[风险] ACAI 系统可能有多个冷却塔** → 复用 ACAC 的单系统单配置模式，`info_code_set_extend` 表以 `obj_id + info_code` 为键，与 ACAC 一致，无额外风险。
- **[风险] 前端未同步适配 ACAI 的 coolingTowerOutTempSet UI** → 前端需配合展示人工/自动模式切换、ΔT 调整、minValue 调整、调整原因输入，需与前端团队确认排期。
- **[取舍] 默认配置仅覆盖 coolingTowerOutTempSet** → 与 ACAC 一致，仅对 `coolingTowerOutTempSet` 创建默认配置，其他 infoCode 不受影响。
