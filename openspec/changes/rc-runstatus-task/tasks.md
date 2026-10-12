## 1. 基础设施

- [x] 1.1 在 `RedisKey.java` 中新增 `buildRcRunStatusTrackingKey(projectId)` 方法定义 Redis Key

## 2. 标记写入机制

- [x] 2.1 在 `ComputeIotDataHandler.pushMessage()` 中添加标记写入逻辑：当 rcRunStatus=2 或 rcRunStatus=3 时写入 Redis Set

## 3. 定时补偿任务

- [x] 3.1 创建 `RcRunStatusCompensationTaskHandler.java`，实现 LogComponent 接口
- [x] 3.2 实现补偿逻辑：获取标记列表、遍历设备、查询 runStatus
- [x] 3.3 实现 runStatus=1 或 runStatus=0 时更新 rcRunStatus、推送、清除标记
- [x] 3.4 实现 runStatus 为空或其他值时保留标记、记录跳过日志
- [x] 3.5 添加 @XxlJob 注解，Handler 名称为 `rcRunStatusCompensationHandler`

## 4. 任务自动注册

- [x] 4.1 在 `InitXxlJob.init()` 中添加 XxlJobVo 配置，自动注册补偿任务

## 5. 测试验证

- [ ] 5.1 本地启动测试，确认任务自动注册到 XXL-Job
- [ ] 5.2 手动触发任务，验证补偿逻辑正常执行
