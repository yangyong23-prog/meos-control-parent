# IOT 规则去重持久化

## Goal

交付 `cache_rule` 与 `cache_rule_key_rel` 的去重仓储，独立审核稳定规则 ID、共享关联和孤儿清理。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 06 已通过。

## Requirements

- 继承父任务统一异常边界：业务校验和范围冲突使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils` fail-safe，Converter 不自行捕获 Jackson 异常；读取方对 `null` 整批降级，写入方在删除前拒绝 `null`，Mapper 和事务异常不得吞掉。
- 新增两张表的实体、Mapper、XML 批量 SQL 和 Repository/Service 契约。
- 规则业务键为 `(projectId, objectId, infoCode, type)`；批量查询已有业务键并复用稳定 ID，只为新增规则生成 ID。
- 必须先完成规则主表 UPSERT，再用稳定 `rule_id` 原子替换缓存键关联。
- 键关联只使用缓存键表达一级系统、二级类型和业务场景，不重复保存运行监测类型字段；删除一个键不得删除其他键仍引用的规则。
- 支持按缓存键批量取规则，并按类型和可选对象 ID 批量过滤；`objectIds == null` 与空集合语义保持区分。
- 仅清理已无任何键关联的孤儿规则，不新增外键或业务唯一索引。

## Out Of Scope

- 不修改 `CacheIotRuleEventListener` 或生产规则写入。
- 不切换 `AbstractCacheIotRuleRepository` 读取。
- 不实现 READY 路由。

## Acceptance Criteria

- [x] 多个缓存键共享同一业务规则时主表只有一条稳定记录。
- [x] 删除任一键不影响其他键查询，最后一个关联删除后可清理孤儿规则。
- [x] 规则 UPSERT 必须先于键关联写入，异常时不产生悬空关联。
- [x] 本任务 diff 只包含规则持久化及聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核规则去重与关联语义；审核通过前不得开始子任务 08。
