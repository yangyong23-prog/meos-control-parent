## Why

线上经常反馈设备状态"一直卡在开启中(2)或关闭中(3)"，产品文档明确要求"定时查询runStatus补偿机制"
作为MQTT消息丢失的兜底方案，但现有代码完全不存在此机制。当MQTT消息丢失时，设备会永久停留在过渡状态，影响用户体验和问题排查。

## What Changes

- **新增标记驱动机制**：在 `ComputeIotDataHandler.pushMessage()` 中，当推送 `rcRunStatus=2`（开启中）或 `rcRunStatus=3`
  （关闭中）时，将设备ID写入Redis Set作为跟踪标记
- **新增定时补偿任务**：创建每30秒执行的定时任务，检查标记列表中的设备，查询其 `runStatus` 信息点：
    - 当 `runStatus=1` 时，更新 `rcRunStatus=1`（运行）并推送，清除标记
    - 当 `runStatus=0` 时，更新 `rcRunStatus=0`（停止）并推送，清除标记
    - 当 `runStatus` 为空或其他值时，保留标记，下次继续检查
- **新增XXL-Job定时任务**：在管理后台可控制任务的启停和执行周期

## Capabilities

### New Capabilities

- `rc-runstatus-compensation`: rcRunStatus状态补偿能力，通过标记驱动机制和定时任务，自动修复"卡在开启中/关闭中"的设备状态

### Modified Capabilities

<!-- 无现有能力需求变更 -->

## Impact

**影响的代码**：

- `common/config/RedisKey.java`
    - 新增 `buildRcRunStatusTrackingKey(projectId)` 方法

- `local/meos-control-compute-edge/websocket/handler/ComputeIotDataHandler.java`
    - `pushMessage()` 方法中添加标记写入逻辑

- `local/meos-control-compute-edge/task/` (新建)
    - `RcRunStatusCompensationTask.java` - XXL-Job定时补偿任务

**影响的系统**：

- 边缘计算模块（meos-control-compute-edge）
- Redis（新增跟踪标记Set）

**性能影响**：

- 极小：每30秒批量查询少量设备的runStatus
- 标记列表大小取决于当前处于过渡状态的设备数量（通常几十个）

**风险评估**：

- 低风险：纯补偿机制，不影响主计算逻辑
- 可回滚：通过XXL-Job管理后台即可禁用任务
