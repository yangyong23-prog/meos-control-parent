# 模板对象明细缓存持久化实施计划

## 1. Entity, Business Key and Converter

- [x] 新增 `CacheTemplateObjectDetail` 实体，`resultJson` 使用 JSON 字符串并保留 bigint 主键和审计字段。
- [x] 新增完整业务键与查询参数，明确空键集合不触发全项目查询。
- [x] 新增明细记录转换器，使用独立 JSON 树深拷贝并迭代删除所有 `relObjects` 层级的动态配置字段。
- [x] 使用固定 TypeReference 恢复有序 DTO 列表，保持 `[]` 与缺行的不同语义。

## 2. Mapper and Resource Service

- [x] 新增 Mapper/XML，提供完整业务键 OR 批量查询、单模板范围删除和批量插入。
- [x] 查询按 500 条分片、Mapper 多值插入按统一的 1000 条分片，查询条件顺序匹配 `idx_template_object_info`，不得使用分离 IN 条件或 N+1。
- [x] Resource Service 使用当前项目上下文，并通过 `super.fillInsertDefaultValue()` 填充 bigint ID 与审计字段。

## 3. Domain Repository

- [x] 新增 Repository 契约与实现，提供整批完整命中结果映射和单模板完整替换。
- [x] 完整命中按业务键集合判断；缺行或数据库重复行整批返回空，存在行的 `[]` 仍算命中。
- [x] 替换前完成全部范围和记录校验；同业务键相同 JSON 去重，不同 JSON 在删除前报错，随后才进入事务删除和插入。
- [x] 空目标集合只清理当前模板键，不访问其他缓存表。
- [x] 统一使用国际化 `BusinessException`；JSON 转换复用 `ObjectMapperUtils` fail-safe 方法且不自行捕获 Jackson 异常，缓存坏行和坏 JSON 在读取仓储记录业务键后整批未命中，Mapper 与写事务异常继续抛出。

## 4. Static Validation

- [x] 编译 `common` 模块，不运行测试。
- [x] 解析 Mapper XML，静态核对 JSON 类型、字段剥离、业务键、索引顺序、分片和事务范围。
- [x] 运行 `git diff --check`，确认没有生产事件、读取策略、其他缓存表或测试文件变更。

## Review and Stop Point

完成实现和静态检查后停止，等待用户审核 JSON 转换、完整命中和模板范围替换；审核通过前不启动子任务 07。
