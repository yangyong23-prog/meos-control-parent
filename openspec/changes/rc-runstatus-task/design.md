## Context

**背景**：边缘计算模块（meos-control-compute-edge）在推送 `rcRunStatus` 时，当设备处于过渡状态（开启中=2，关闭中=3），如果 MQTT
消息丢失，设备将永久停留在该状态。

**现状**：

- `ComputeIotDataHandler.pushMessage()` 负责推送 rcRunStatus
- 已有 `iotDataCacheService.queryIotRealData()` 可查询设备 runStatus 信息点
- 项目使用 XXL-Job 作为定时任务框架，`InitXxlJob` 在启动时自动注册任务到管理后台

**约束**：

- 补偿逻辑必须轻量，不影响主计算流程
- 使用 Redis Set 存储跟踪标记
- 通过 XXL-Job 管理后台控制任务启停

## Goals / Non-Goals

**Goals：**

- 实现标记驱动机制：推送过渡状态时写入 Redis Set
- 实现 XXL-Job 定时任务：每30秒检查标记列表，补偿修复
- 查询设备 runStatus，根据值决定是否更新 rcRunStatus
- 推送成功后清除标记，失败或无效值保留标记继续检查

**Non-Goals：**

- 不修改主计算逻辑 `computeInfoCodeData()`
- 不新增配置类，使用 XXL-Job 管理后台控制
- 不区分设备原始是"开启中"还是"关闭中"

## Decisions

### 1. 标记存储方案：Redis Set

**选择**：Redis Set 存储 objectId 列表

**理由**：

- Set 自动去重，同一设备多次推送过渡状态不会产生重复标记
- `SMEMBERS` 获取全部成员，`SREM` 移除成员，操作简单
- 内存占用极小（通常几十个设备）

**Key 格式**：`apc:rc_runstatus_tracking:{projectId}`

**备选方案**：

- Redis List：需要手动去重
- Redis Hash：需要额外字段，过度设计

### 2. 定时任务框架：XXL-Job

**选择**：使用现有 XXL-Job 框架

**理由**：

- 项目已集成 XXL-Job，无额外依赖
- 管理后台可动态控制：启停、执行周期、手动触发
- 支持 `DISCARD_LATER` 策略，防止任务堆积

**任务配置**：

- Handler 名称：`rcRunStatusCompensationHandler`
- 默认 Cron：`0/30 * * * * ?`（每30秒）
- 阻塞策略：`DISCARD_LATER`
- 自动注册：在 `InitXxlJob.init()` 中添加 `XxlJobVo` 配置，启动时自动调用 `xxlJobService.addJob()` 注册任务

### 3. 补偿逻辑设计

```
┌─────────────────────────────────────────────────────────────────┐
│                    标记写入点                                     │
│  ComputeIotDataHandler.pushMessage()                            │
│  ─────────────────────────────────────                          │
│  if (rcRunStatus == "2" || rcRunStatus == "3")                  │
│      → SADD apc:rc_runstatus_tracking:{projectId} {objectId}    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    定时补偿任务 (每30秒)                          │
│  RcRunStatusCompensationTaskHandler.execute()                   │
│  ─────────────────────────────────────                          │
│  1. objectIds = SMEMBERS apc:rc_runstatus_tracking:{projectId}  │
│  2. for each objectId:                                          │
│       runStatus = queryIotRealData(objectId, "runStatus")       │
│       if (runStatus == "1") →                                   │
│           updateAndPush(objectId, rcRunStatus="1")              │
│           SREM ... {objectId}                                   │
│       else if (runStatus == "0") →                              │
│           updateAndPush(objectId, rcRunStatus="0")              │
│           SREM ... {objectId}                                   │
│       else →                                                    │
│           保留标记，下次继续检查                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4. 更新与推送逻辑

**选择**：复用现有推送逻辑

**实现**：

- 调用 `iotDataCacheService.insert()` 更新缓存
- 调用 `iWSMessageService.pushMessage()` 推送消息
- 实现 `LogComponent` 接口，使用 `logProjectInstance().info/error` 记录补偿日志

**日志格式**：

```
[rcRunStatus-补偿] 设备:{objectId} | runStatus={runStatus} | rcRunStatus={新值} | 结果:推送成功/失败
```

## Risks / Trade-offs

| 风险                       | 缓解措施                                        |
|--------------------------|---------------------------------------------|
| runStatus 始终为空，标记永不清理    | 极低概率。设备离线时 runStatus 通常有值（-1或0）。可后续添加超时清理机制 |
| 批量查询大量设备导致性能问题           | 标记列表通常几十个设备，每30秒执行一次，性能影响极小                 |
| 补偿推送失败                   | 保留标记，下次重试。XXL-Job 重试机制兜底                    |
| 并发问题：主流程同时更新 rcRunStatus | 补偿逻辑幂等，推送相同值无害                              |

## Migration Plan

**部署步骤**：

1. 在 `InitXxlJob.init()` 中添加任务配置（启动时自动注册到 XXL-Job）
2. 部署代码到测试环境，任务自动注册
3. 在 XXL-Job 管理后台确认任务已创建，根据需要调整 Cron
4. 手动触发测试验证
5. 启用任务，观察日志

**回滚方案**：

- 在 XXL-Job 管理后台停止/删除任务即可
- 无需代码回滚

## Open Questions

无待解决问题，需求已明确。
