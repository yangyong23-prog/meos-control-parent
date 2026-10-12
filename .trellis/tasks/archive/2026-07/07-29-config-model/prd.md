# 配置模型与校验

## Goal

扩展回路配置值对象，新增 `feedbackAdjustSetting` 对象字段，携带回路级反馈调节条件。废弃空间级统一条件字段。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

无前置依赖，为最底层变更。

## Scope

### 新增 DTO

- 新增 `DemandLightingCircuitFeedbackAdjustSettingDTO`，包含：
  - `underOutdoorIlluminance`（BigDecimal）— 自动开启条件照度阈值，0-10000 正整数
  - `underDuration`（Integer）— 自动开启条件持续时间，0-300 整数
  - `overtopOutdoorIlluminance`（BigDecimal）— 自动关闭条件照度阈值，0-10000 正整数
  - `overtopDuration`（Integer）— 自动关闭条件持续时间，0-300 整数

### 扩展值对象

- `DemandConfigLightingCircuitValueObject` 新增 `feedbackAdjustSetting` 字段（类型 `DemandLightingCircuitFeedbackAdjustSettingDTO`）
- 新增 `checkAndSet()` 方法：
  - `FEEDBACK_ADJUSTMENT` 时：`feedbackAdjustSetting` 非空，自动开启和关闭条件最少配一项，阈值大小关系校验
  - `OPEN`/`CLOSE` 时：`feedbackAdjustSetting` 置空

### 扩展 DTO

- `DemandLightingCircuitSettingDTO` 同步新增 `feedbackAdjustSetting` 字段

### 调整功能类型值对象

- `DemandConfigLightingFuncTypeValueObject.checkAndSet()` 中 `FEEDBACK_ADJUSTMENT` case：
  - 移除 `allFeedback` 判断逻辑
  - 改为遍历 `circuitSettings`，对 `FEEDBACK_ADJUSTMENT` 的回路调用回路级 `checkAndSet()`
  - 移除 `spaceFeedbackAdjustSettings` 的非空校验

### 废弃空间级条件

- `DemandConfigLightingFeedbackAdjustDetailValueObject` 的 `allFeedback` + 4 个条件字段标注 `@Deprecated`

## Files

- `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingCircuitValueObject.java`
- `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingFeedbackAdjustDetailValueObject.java`
- `common/.../structure/demand/model/valueobject/lighting/children/DemandConfigLightingFuncTypeValueObject.java`
- `common/.../model/dto/demand/lighting/DemandLightingCircuitSettingDTO.java`
- 新增 `common/.../model/dto/demand/lighting/DemandLightingCircuitFeedbackAdjustSettingDTO.java`

## Acceptance Criteria

- [ ] `DemandLightingCircuitFeedbackAdjustSettingDTO` 已创建，包含 4 个条件字段
- [ ] `DemandConfigLightingCircuitValueObject` 新增 `feedbackAdjustSetting` 字段 + `checkAndSet()` 校验
- [ ] `DemandLightingCircuitSettingDTO` 同步新增字段
- [ ] `DemandConfigLightingFuncTypeValueObject.checkAndSet()` 移除 `allFeedback` 逻辑，改为回路级校验
- [ ] `DemandConfigLightingFeedbackAdjustDetailValueObject` 废弃字段标注 `@Deprecated`
- [ ] 编译通过
