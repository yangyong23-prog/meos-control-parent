# 下发任务重构

## Goal

重构 `LightingFeedbackAdjustTask`，从空间维度单一写入改为空间+回路双维度写入，4 个条件信息点改为写入回路对象 LTLTCC。

## Parent

`07-29-prd-260801-illum-feedback-circuit`

## Dependencies

- **前置**: `07-29-config-model`（需要回路值对象的 `feedbackAdjustSetting` 字段）
- **前置**: `07-29-save-and-compute`（需要计算结果携带回路级条件）

## Scope

### 空间维度写入（illumFbCtrType，保持不变）

- 解析空间 `spaceFeedbackAdjustType`（室内=1, 室外=2, 无反馈=3）
- 增量对比：上个 5min vs 当前 5min 的场景配置变化
- 写入空间对象

### 回路维度写入（4 个条件信息点，新增）

- 新增 `parseCircuitFeedbackAdjustSettings()` 从空间配置中提取回路级条件
- 新增 `parseCircuitOrders()` 解析回路条件为 4 个信息点指令：
  - `outdoorIlluminationValueToTurnOnGeneralLighting`
  - `feedbackofLightingTurnonLastTime`
  - `outdoorIlluminationValueToTurnOffGeneralLighting`
  - `feedbackofLightingTurnoffLastTime`
- 回路指令 `objectId` = 回路 ID（LTLTCC）
- 增量对比：按回路 ID 对比上个 5min vs 当前
  - 有→有：按当前条件写入
  - 有→无：清空回路条件
  - 无→有：按当前条件写入

### Redis 缓存

- 新增 `lightingCircuitFeedbackAdjustRecord(projectId)` 回路级缓存 key
- 保留原空间级 key

## Files

- `local/meos-control-compute-edge/.../task/lighting/LightingFeedbackAdjustTask.java`
- `common/.../model/base/CachePersistenceKey.java`

## Acceptance Criteria

- [ ] 空间维度 `illumFbCtrType` 写入逻辑保持不变
- [ ] 回路维度 4 个条件信息点写入回路对象 LTLTCC
- [ ] 5min 增量对比按回路维度正确执行（有→有、有→无、无→有）
- [ ] Redis 缓存新增回路级 key
- [ ] 两批指令分别调用 `dtpTwinClientService.autoCommandBatchSend()` 下发
- [ ] 编译通过
