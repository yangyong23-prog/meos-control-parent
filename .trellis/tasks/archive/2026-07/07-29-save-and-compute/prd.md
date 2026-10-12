# 保存接口与计算适配

## Goal

适配保存接口参数和配置计算逻辑，使回路级反馈条件能正确传递到存储和计算结果。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

- **前置**: `07-29-config-model`（需要回路值对象的 `feedbackAdjustSetting` 字段）

## Scope

### 保存参数适配

- `DemandLightingTemplateSceneConfigSaveParam`:
  - `spaceFeedbackAdjustSettings` 字段废弃条件值
  - 回路条件通过 `circuitSettings[].feedbackAdjustSetting` 传递
  - `DemandLightingFuncTypeSettingDTO.spaceFeedbackAdjustSettings` 废弃条件值

### 计算结果适配

- `DemandConfigLightingResultDataDTO`:
  - `feedbackAdjustSettings` 字段类型从 `DemandLightingSpaceFeedbackAdjustSettingDTO`（空间级）改为 `DemandLightingCircuitFeedbackAdjustSettingDTO`（回路级）
- `DemandConfigLightingComputeNowServiceImpl.putCircuitIdResults()`:
  - 将回路级 `feedbackAdjustSetting` 从配置传递到计算结果（当前传 `null`，改为传回路条件）

### 保存逻辑适配

- `DemandLightingServiceImpl.editSceneObjectSettings()`:
  - 适配回路级条件的存储（新增/编辑路径）

## Files

- `common/.../model/param/demand/lighting/DemandLightingTemplateSceneConfigSaveParam.java`
- `common/.../model/dto/demand/lighting/DemandLightingFuncTypeSettingDTO.java`
- `common/.../model/dto/demand/lighting/DemandConfigLightingResultDataDTO.java`
- `common/.../service/demand/lighting/DemandConfigLightingComputeNowServiceImpl.java`
- `saas/meos-control-common/.../service/demand/impl/DemandLightingServiceImpl.java`

## Acceptance Criteria

- [ ] 保存接口能正确接收回路级 `feedbackAdjustSetting` 条件
- [ ] `spaceFeedbackAdjustSettings` 不再携带条件值
- [ ] `DemandConfigLightingResultDataDTO.feedbackAdjustSettings` 类型改为回路级
- [ ] `putCircuitIdResults()` 正确传递回路级条件到计算结果
- [ ] 保存 → 同步计算 → 回路结果正确携带条件
- [ ] 编译通过
