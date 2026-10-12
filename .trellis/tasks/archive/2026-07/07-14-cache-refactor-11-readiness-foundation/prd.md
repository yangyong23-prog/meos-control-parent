# 缓存模型状态基础能力

## Goal

交付 `CACHE_OBJECT_MODEL_STATUS` 的数据库持久化与共享 Redis 基础能力，不决定构建启用时序、边缘轮询或业务路由。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 10 已通过。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 新增内部 `BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS`，不得出现在普通基础配置列表和通用编辑入口。
- 复用 `rc_basics_config`：`type` 固定为该枚举，`child_type` 仅允许一级类型 `AIR_TERMINAL`、`LIGHTING`；生产构建流程只持久化 `READY`。`FAILED` 仅保留为历史数据或测试手工 Redis 非 READY 值的兼容解析，不再由生产流程写入数据库或 Redis。
- 业务键为 `project_id + type + child_type`；其 32 位 MD5 作为稳定字符串主键，使用固定主键 UPSERT 保证幂等。
- 定义包含集团、项目、固定 type 和一级类型的无过期 Redis Key；提供读取、写入、删除和状态解析能力。
- 数据库提交后才能更新 Redis；Key 缺失、未知值、反序列化失败或 Redis 异常统一返回未就绪，不向调用方抛出状态异常。

## Out Of Scope

- 不新增独立云端数量核验任务或初始化任务。
- 不实现边缘 30 秒收敛，不修改业务读路由。
- 不创建新的状态表或动态开关配置。

## Acceptance Criteria

- [x] 每项目、一级类型状态可幂等保存且不会产生重复记录。
- [x] 内部配置类型不出现在普通配置查询/编辑入口。
- [x] Redis 状态 CRUD、无过期语义和异常降级测试通过。
- [x] 本任务 diff 只包含状态基础能力及聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核状态持久化与 Redis Key；审核通过前不得开始子任务 12。
