# 对象主表与缓存键关联持久化

## Goal

交付 `cache_object` 与 `cache_object_key_rel` 的批量持久化和查询能力，仅形成可测试仓储，不接入生产事件。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01、02 已通过。

## Requirements

- 新增两张表的实体、Mapper、XML 批量 SQL 和 Repository/Service 契约。
- `cache_object.id` 使用 RWD 对象 ID，按主键批量 UPSERT；公共列独立保存，`object_data` 使用子任务 02 的对象投影。
- 提供 RWD 实际字段到对象实体、对象实体到 RWD DTO 的转换能力；独立列是公共字段唯一可信来源，扩展 JSON 不得覆盖公共字段。
- `monitoring_surveillance_status` 使用 JSON 字符串 code 数组映射；支持按多个 code 生成 OR 语义的 `JSON_CONTAINS` 批量过滤，空筛选条件不追加状态条件。
- 缓存键关联按 `(projectId, cacheKey)` 作为完整替换范围，在事务内删除旧关联并分片插入去重后的目标集合。
- 支持按项目和缓存键批量取得对象 ID，再按 ID 分片查询对象；支持直接 ID 批量查询和按需列投影。
- 大批量 SQL 使用 Mapper XML 分片，不使用 MyBatis-Plus `saveBatch`，不得出现 N+1。
- 本子任务不新增测试代码；仓储测试和完整验证统一在最终集成子任务执行，本阶段只做编译与静态检查。

## Out Of Scope

- 不实现模型视图、对象关系、模板明细或 IOT 规则仓储。
- 不修改 `CacheRwdObjectEventListener`，不双写生产数据。
- 不实现 READY 路由或消费者读取切换。

## Acceptance Criteria

- [x] 同一对象被多个缓存键引用时对象主表只有一条，关联集合各自完整。
- [x] 单键范围替换后不存在旧关联，且不删除其他键仍引用的对象。
- [x] 对象 UPSERT、空扩展 `{}`、公共列重建和批量查询契约已实现。
- [x] 状态数组支持 SQL `NULL`、`[]`、多 code UPSERT 和任一 code 过滤。
- [x] 本任务 diff 只包含对象/键关联持久化，不新增生产接线或测试代码。

## Review Gate

完成实现和质量检查后暂停，等待用户审核对象去重与键关联仓储；审核通过前不得开始子任务 04。
