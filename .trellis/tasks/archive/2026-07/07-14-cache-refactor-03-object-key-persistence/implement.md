# 对象主表与缓存键关联持久化实施计划

## 1. Object Model and Conversion

- [x] 新增 `CacheObject`、`CacheObjectKeyRel` 实体并完整映射表字段。
- [x] 新增对象记录转换组件，复用对象字段投影和公共列优先重建契约。
- [x] 将监测监控状态规范化为可空字符串 code 列表，保留 SQL `NULL` 与 `[]` 差异。

## 2. Mapper and Resource Service

- [x] 新增两份 Mapper 和 XML，提供对象批量 UPSERT、对象批量查询、关联批量查询、删除和插入 SQL。
- [x] 新增对象查询参数，支持 ID 分片、状态任一 code 过滤和按需列投影。
- [x] 新增两个资源 Service，集中处理当前项目、审计字段和固定批次。

## 3. Domain Repository

- [x] 新增对象仓储契约及实现，按对象 ID 去重后批量 UPSERT。
- [x] 实现缓存键到对象 ID 的批量查询与结果分组。
- [x] 实现单缓存键事务范围替换，空集合可清空，且不删除共享对象。

## 4. Static Validation

- [x] 编译 `common` 模块，不运行测试。
- [x] 静态核对 JSON TypeHandler、参数化 `JSON_CONTAINS`、UPSERT 更新列和事务边界。
- [x] 运行 `git diff --check` 并确认没有生产事件、其他缓存表或测试文件变更。

## Review and Stop Point

完成实现和静态检查后停止，等待用户审核对象去重、状态过滤和缓存键范围替换；审核通过前不启动子任务 04。
