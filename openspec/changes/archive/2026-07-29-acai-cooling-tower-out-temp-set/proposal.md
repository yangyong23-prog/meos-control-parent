## Why

空调冷热源（ACAI）系统详情页缺少"冷却塔出水温度设定"功能。空调冷源（ACAC）已实现该功能，支持人工/自动模式设定冷却塔出水温度，自动模式下通过 ΔT（散热趋近温差）和 minValue（出水温度最低值）由 BDTP 自动计算并定时下发。ACAI 需要新增此功能，与现有 `coolWaterReturnTempSet` 并存，逻辑与 ACAC 完全一致。

## What Changes

- 在 ACAI 详情页 Excel 模板（`【AIR_CONDITIONING_C_H_SOURCE-DETAIL】系统详情页模版.xlsx`）的 `heatDissipationControlLRY` 分组中新增 2 行配置：
  - `INFO_CODE_SET_EXTEND_CONFIG` 行：通过 `{"infoCode":"coolingTowerOutTempSet"}` 从 `info_code_set_extend` 表加载 `ExtendSetting`（含 manualAutoSet、ΔT、minValue）
  - `IOT_SETTING` 行：展示冷却塔出水温度的 IoT 实时值
- 调整该分组现有行的排序，使新增配置排在第一位
- 在 `InfoCodeSetExtendConfigAnalyzeStrategy` 中新增 ACAI 的默认配置判断，使 ACAI 系统首次访问时能展示默认配置（默认 AUTO，与 ACAC 一致）
- 复用现有 `extendInfoCodeValueConfig` API 推送完整 `ExtendSetting` 到 BDTP，推送逻辑零改动

## Capabilities

### New Capabilities
- `acai-cooling-tower-out-temp-set`: ACAI 系统详情页新增冷却塔出水温度设定功能，包括 INFO_CODE_SET_EXTEND_CONFIG + IOT_SETTING 两行 Excel 配置、默认配置创建逻辑、以及通过 extendInfoCodeValueConfig API 推送 ExtendSetting 到 BDTP 的复用机制

### Modified Capabilities
（无）

## Impact

- **Excel 模板**: `【AIR_CONDITIONING_C_H_SOURCE-DETAIL】系统详情页模版.xlsx`（ACAI sheet）新增 2 行 + 排序调整
- **Java 代码**: `InfoCodeSetExtendConfigAnalyzeStrategy.java` 新增 1 个判断方法 + 1 行调用
- **数据库**: 无变更（`info_code_set_extend` 表已存在，`InfoCodeEnum.COOLING_TOWER_OUT_TEMP_SET` 已定义）
- **API**: 无变更（`extendInfoCodeValueConfig` API 已支持完整 `ExtendSetting` 推送）
- **BDTP**: 无变更（BDTP 负责自动计算：湿球温度 + ΔT，且 ≥ minValue，5min 定时重算）
- **前端**: 需配合展示冷却塔出水温度设定 UI（人工/自动模式切换、ΔT 调整、minValue 调整、调整原因输入）
