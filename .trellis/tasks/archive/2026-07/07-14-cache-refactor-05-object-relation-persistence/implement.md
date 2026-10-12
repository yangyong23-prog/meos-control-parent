# 对象关系缓存持久化实施计划

## 1. Entity and Query Contract

- [x] 新增 `CacheObjectRel` 实体，明确主体、图谱、关系和目标字段方向。
- [x] 新增关系查询参数，支持正向/反向 ID、图谱、关系编码和按需列。
- [x] 明确显式空 ID 集合短路，反向查询不交换落库字段。

## 2. Mapper and Resource Service

- [x] 新增关系 Mapper/XML，提供正向查询、反向查询、范围删除和批量插入。
- [x] 新增资源 Service，按正向或反向 ID 分片，并填充稳定 bigint ID 与审计字段。
- [x] 确认正反向 SQL 条件顺序匹配各自索引且没有 N+1。

## 3. Domain Repository

- [x] 新增关系 Repository 契约及实现。
- [x] 实现单个 `(objectId, graphCode, relCode)` 范围事务替换和目标 ID 去重。
- [x] 空目标集合只清理当前范围，不访问或删除对象主表。

## 4. Static Validation

- [x] 编译 `common` 模块，不运行测试。
- [x] 静态核对关系方向、索引条件、批量分片和删除范围。
- [x] 运行 `git diff --check`，确认没有生产事件、其他缓存表或测试文件变更。

## Review and Stop Point

完成实现和静态检查后停止，等待用户审核关系方向、正反向查询和范围替换；审核通过前不启动子任务 06。
