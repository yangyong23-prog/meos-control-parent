# 模型视图缓存持久化实施计划

## 1. Entity and Conversion

- [x] 新增 `CacheObjectModelView` 实体，完整映射公共列和 `view_data`。
- [x] 新增模型视图记录转换器，复用基础字段、classCode 增强字段和公共列优先重建契约。
- [x] 确认无增强字段时固定写入 `{}`，不写入对象表字段。

## 2. Mapper and Resource Service

- [x] 新增模型视图 Mapper/XML，提供批量 UPSERT 和按项目、对象、建筑、楼层查询。
- [x] 新增查询参数，支持 ID 分片和按需列投影。
- [x] 新增资源 Service，处理当前项目、审计字段和固定批次。

## 3. Domain Repository

- [x] 新增模型视图 Repository 契约及实现，按对象 ID 去重后批量 UPSERT。
- [x] 实现只查询模型视图表的批量查询，不 JOIN `cache_object`。
- [x] 保留后续对象与视图同事务组合写入所需的批量边界。

## 4. Static Validation

- [x] 编译 `common` 模块，不运行测试。
- [x] 静态核对 JSON 投影、公共列覆盖、建筑/楼层过滤、按需列和无 JOIN SQL。
- [x] 运行 `git diff --check`，确认没有生产消费者、对象表或测试文件变更。

## Review and Stop Point

完成实现和静态检查后停止，等待用户审核模型视图公共列、基础字段投影和无 JOIN 查询；审核通过前不启动子任务 05。
