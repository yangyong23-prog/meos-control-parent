# 多空间一致性校验

## Goal

保存时校验同一照明回路在多个空间的反馈条件一致性，支持并发保存冲突检测。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

- **前置**: `07-29-config-model`（需要回路值对象的 `feedbackAdjustSetting` 字段）

## Scope

### 一致性校验服务

新增 `DemandLightingFeedbackConsistencyValidator`：
- 提取当前保存参数中所有 `settingMode = FEEDBACK_ADJUSTMENT` 的回路及条件
- 查询同模板+同场景下其他空间的配置
- 对每个反馈调节回路：查询其服务的其他空间，检查是否也配置为 `FEEDBACK_ADJUSTMENT`
- 若都为 `FEEDBACK_ADJUSTMENT`，比较 `feedbackAdjustSetting` 条件是否一致
- 不一致则抛出异常，返回冲突回路名称和各空间配置详情

### 校验规则

- 条件类型一致（自动开启/关闭条件是否都配置）
- 自动开启条件阈值和持续时间一致
- 自动关闭条件阈值和持续时间一致
- 不参与反馈调节的回路不纳入校验

### 集成到保存流程

- `DemandLightingServiceImpl.editSceneObjectSettings()` 中配置校验后、持久化前调用
- 并发保存冲突：事务内查询最新数据再次校验

### 错误响应

返回冲突回路 ID、回路名称、冲突空间 ID、空间名称及各自 `feedbackAdjustSetting` 配置。

## Files

- 新增 `saas/meos-control-common/.../service/demand/validator/DemandLightingFeedbackConsistencyValidator.java`
- `saas/meos-control-common/.../service/demand/impl/DemandLightingServiceImpl.java`

## Acceptance Criteria

- [ ] 同一回路在多空间均配置为反馈调节时，条件一致性校验正确
- [ ] 不参与反馈调节的空间不纳入校验
- [ ] 不一致时返回冲突回路和各空间配置详情
- [ ] 并发保存冲突时，后端基于最新配置再次校验并返回冲突信息
- [ ] 编译通过
