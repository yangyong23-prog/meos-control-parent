## ADDED Requirements

### Requirement: 标记驱动机制

当设备 rcRunStatus 处于过渡状态时，系统 SHALL 将设备 ID 写入 Redis Set 作为跟踪标记。

#### Scenario: 推送开启中状态时添加标记

- **WHEN** 推送 `rcRunStatus=2`（开启中）
- **THEN** 系统 SHALL 将 objectId 写入 Redis Set `apc:rc_runstatus_tracking:{projectId}`

#### Scenario: 推送关闭中状态时添加标记

- **WHEN** 推送 `rcRunStatus=3`（关闭中）
- **THEN** 系统 SHALL 将 objectId 写入 Redis Set `apc:rc_runstatus_tracking:{projectId}`

#### Scenario: 推送稳定状态时不添加标记

- **WHEN** 推送 `rcRunStatus=0`（停止）或 `rcRunStatus=1`（运行）或 `rcRunStatus=-1`（无效）
- **THEN** 系统 SHALL NOT 添加跟踪标记

### Requirement: 定时补偿任务

系统 SHALL 通过 XXL-Job 定时任务检查跟踪标记列表中的设备，并根据 runStatus 信息点更新 rcRunStatus。

#### Scenario: runStatus 为运行时更新 rcRunStatus

- **WHEN** 定时任务执行且设备的 `runStatus=1`
- **THEN** 系统 SHALL 更新 `rcRunStatus=1`（运行）
- **AND** 系统 SHALL 推送更新到前端
- **AND** 系统 SHALL 从 Redis Set 中清除该设备的标记

#### Scenario: runStatus 为停止时更新 rcRunStatus

- **WHEN** 定时任务执行且设备的 `runStatus=0`
- **THEN** 系统 SHALL 更新 `rcRunStatus=0`（停止）
- **AND** 系统 SHALL 推送更新到前端
- **AND** 系统 SHALL 从 Redis Set 中清除该设备的标记

#### Scenario: runStatus 为空时保留标记

- **WHEN** 定时任务执行且设备的 `runStatus` 为空（null 或不存在）
- **THEN** 系统 SHALL 保留该设备的标记
- **AND** 系统 SHALL 在下次任务执行时继续检查

#### Scenario: runStatus 为其他值时保留标记

- **WHEN** 定时任务执行且设备的 `runStatus` 不为 0、1 或空（如 -1、2、3 等）
- **THEN** 系统 SHALL 保留该设备的标记
- **AND** 系统 SHALL 在下次任务执行时继续检查

### Requirement: 补偿日志记录

系统 SHALL 通过实现 LogComponent 接口记录补偿过程的日志，便于问题排查。

#### Scenario: 补偿成功日志

- **WHEN** 补偿任务成功更新并推送 rcRunStatus
- **THEN** 系统 SHALL 通过 logProjectInstance().info() 记录日志，包含设备ID、runStatus值、rcRunStatus新值、推送成功结果

#### Scenario: 补偿失败日志

- **WHEN** 补偿任务推送失败
- **THEN** 系统 SHALL 通过 logProjectInstance().error() 记录错误日志，包含设备ID、runStatus值、rcRunStatus新值、推送失败结果及异常信息

#### Scenario: 跳过补偿日志

- **WHEN** 补偿任务检测到 runStatus 为空或无效值
- **THEN** 系统 SHALL 通过 logProjectInstance().info() 记录日志，包含设备ID、runStatus值、跳过原因

### Requirement: XXL-Job 任务自动注册

系统 SHALL 在应用启动时自动将补偿任务注册到 XXL-Job 管理后台。

#### Scenario: 应用启动时自动注册任务

- **WHEN** 应用启动且 XXL-Job 配置启用
- **THEN** 系统 SHALL 在 `InitXxlJob.init()` 中调用 `xxlJobService.addJob()` 注册任务
- **AND** 任务 Handler 名称为 `rcRunStatusCompensationHandler`
- **AND** 默认 Cron 表达式为 `0/30 * * * * ?`（每30秒执行）
- **AND** 阻塞策略为 `DISCARD_LATER`
