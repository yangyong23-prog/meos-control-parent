# 配置预览与二开接口

## Goal

适配配置预览返回回路级反馈条件，提供二开接口（临时需求增删改查、空间回路查询）。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

- **前置**: `07-29-config-model`（需要回路值对象的 `feedbackAdjustSetting` 字段）
- **前置**: `07-29-save-and-compute`（需要计算结果携带回路级条件）

## Scope

### 配置预览适配

- 配置预览查询返回回路级 `feedbackAdjustSetting` 条件
- `settingMode = FEEDBACK_ADJUSTMENT` 的回路展示自动开启/关闭条件
- 废弃 `feedbackAdjustmentConfig` 字段，改为回路级 `feedbackAdjustSetting` 对象

### 回路配置 VO 适配

- `DemandLightingCircuitSettingVO` 新增 `feedbackAdjustSetting` 字段
- 废弃 `feedbackAdjustmentConfig` 字段

### 二开接口

- 临时需求：增、删、改、查（定时执行），适配回路级反馈条件参数
- 查询空间及空间下的回路、查询编组的接口

## Files

- `common/.../model/vo/demand/lighting/DemandLightingCircuitSettingVO.java`
- 配置预览相关查询接口（根据实际代码定位）
- 临时需求接口（根据实际代码定位）
- 空间回路查询接口

## Acceptance Criteria

- [ ] 配置预览正确返回回路级 `feedbackAdjustSetting` 条件
- [ ] `feedbackAdjustmentConfig` 废弃，改为 `feedbackAdjustSetting`
- [ ] 二开接口（临时需求增删改查）可用
- [ ] 空间回路查询接口可用
- [ ] 编译通过
