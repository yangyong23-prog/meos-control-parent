# 缓存模型 READY 路由基础能力

## Goal

由缓存模型状态服务统一提供 READY 判断，并明确模板缓存许可与状态判断的组合规则；
支持类型未 READY 时由消费者选择实时路径，其他系统继续使用历史持久化链路。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 14 已通过，直接依赖子任务 11、14。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 固定支持集合只包含 `RunningMonitorTypeEnum.AIR_TERMINAL`、`LIGHTING`，不接动态配置。
- `CacheObjectModelStatusService#isReady` 统一判断固定支持范围和对应 Redis 状态是否为 `READY`。
- 模板消费者选择新模型必须同时满足：模板 `allowCache() == true` 且状态服务返回 READY；普通消费者直接使用状态服务。
- 照明所有二级类型整体受 `LIGHTING` 状态控制，不按二级类型单独启用。
- Redis Key 缺失、值非法、反序列化失败或访问异常时不向业务请求抛出状态异常，
  统一返回未就绪；末端、照明消费者据此选择实时路径，而不是旧版持久化缓存。
- 不新增只做条件转发的路由策略类；状态判断属于状态服务，模板缓存许可固定维护在模板类型枚举中，模板模型实例方法仅负责委托，业务入口只负责显式组合两者。

## Out Of Scope

- 不修改对象、模型图、模板明细或规则读取实现。
- 不修改写入、初始化、核验或边缘调度。
- 不新增白名单、Nacos 开关或数据库动态开关。

## Acceptance Criteria

- [x] 支持类型、READY 和 Redis 异常降级测试完整，模板缓存许可由模板类型枚举固定维护，模板模型实例方法仅负责委托。
- [x] Redis 各类异常均安全返回未就绪且不传播异常，支持类型转入实时路径。
- [x] 通风、机房安全及其他系统始终选择旧模型。
- [x] 本任务 diff 只包含状态服务 READY 能力、模板许可规则和纯单元测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核路由条件与异常降级；审核通过前不得开始子任务 16。
