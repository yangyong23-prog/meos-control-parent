# 边缘 READY 状态收敛

## Goal

在边缘端把已同步到本地数据库的 READY 标识收敛到边缘 Redis，仅实现状态传播，不修改业务读取逻辑。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：缓存写入、状态基础和初始化能力已具备，直接依赖子任务 11、12；原子任务 13 的数量核验契约已取消。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 边缘服务启动时立即执行一次状态收敛，此后每 30 秒执行。
- 每轮先检查 `AIR_TERMINAL`、`LIGHTING` 的边缘 Redis Key，只批量查询 Key 不存在的一级类型。
- 数据库缺失或不是 `READY` 时不写 Redis并等待下一轮；读取到 `READY` 后写入边缘 Redis。
- Redis 中只要已有显式值就不得查询数据库或覆盖；测试手工设置或历史遗留的 `FAILED` 仅作为非 READY 兼容值，生产构建流程不再写入，删除 Key 后重新进入收敛。
- 边缘数据库出现 READY 即视为同步完成，不检查七张表数量、版本或内容；实际不完整归云边同步项目组。

## Out Of Scope

- 不实现云边同步、不校验边缘缓存、不自动回退 READY。
- 不修改对象、视图、明细或规则消费者。
- 不新增手动状态接口。

## Acceptance Criteria

- [x] 启动执行、30 秒调度、缺失 Key 批量查询和 READY 写入测试通过。
- [x] 已有 Redis 值不被覆盖，删除 Key 后可重新收敛。
- [x] 任务不访问七张缓存表或 RWD。
- [x] 本任务 diff 只包含边缘状态收敛和聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核边缘状态收敛；审核通过前不得开始子任务 15。
