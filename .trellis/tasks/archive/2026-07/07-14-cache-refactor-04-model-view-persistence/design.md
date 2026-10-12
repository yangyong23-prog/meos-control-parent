# 模型视图缓存持久化技术设计

## 1. Scope and Boundary

本任务只实现 `cache_object_model_view` 的独立持久化和查询能力：

- 新增模型视图实体、Mapper、XML、资源 Service、查询参数和领域 Repository。
- 新增模型视图记录转换组件，复用子任务 02 的模型视图投影与重建规则。
- 查询只访问 `cache_object_model_view`，不 JOIN `cache_object`，不读取 `object_data`。
- 不接入对象事件、模型图消费者、云端双写编排或 READY 路由。

## 2. Layering

- 实体放在既有 `model/entity/cache` 包：`CacheObjectModelView`，使用 `BaseInputIdDO` 保留 RWD 对象 ID。
- Mapper 放在既有 `mapper/cache` 包，XML 放在 `resources/mapper/cache`。
- 查询参数放在既有 `resource/cache/param` 包，使用 `BaseRepositoryQueryParam` 的 Lambda 列投影。
- 资源 Service 负责当前项目隔离、审计字段和批量 SQL 分片。
- `structure/cacherwd/repository` 中的领域 Repository 负责批量 UPSERT 和查询契约。
- 转换组件放在既有 `structure/cacherwd/projection` 包，字段规则仍由 `CacheObjectExtensionFieldConfig` 维护。

## 3. Storage Contract

`CacheObjectModelView` 的主键是 RWD 对象 ID，公共列保存：

- `projectId`、`localName`、`classCode`、`objectType`；
- 可空的 `buildingId`、`floorId`；
- `viewData` 为已序列化的模型视图 JSON。

批量写入使用自定义 XML `INSERT ... ON DUPLICATE KEY UPDATE`：

- 同一对象 ID 在调用 Repository 前只保留一条记录，后出现的数据覆盖先出现的数据；
- 重复主键更新业务列和更新审计字段，不更新原创建审计字段；
- 没有实际扩展字段时写入 `{}`，不把正常空扩展写成 SQL `NULL`；
- 固定批次执行，不能使用 MyBatis-Plus `saveBatch`。

## 4. Projection and Rebuild

转换器接收保留字段存在性的 RWD Map：

1. 提取对象 ID、名称、classCode、objType、建筑和楼层公共列；
2. 调用 `CacheObjectExtensionProjector#projectModelView(objType, classCode, actualFields)`，由基础字段 Map 与 classCode 增强 Map 求并集后取实际响应字段交集；
3. 重建 DTO 时先恢复 `view_data` 中允许的 JSON 字段，再写入独立公共列；
4. 公共列是唯一可信来源，JSON 中同名字段不能覆盖公共列；
5. `ACZone` 设备类使用空间基础字段，设备和空间默认字段不写入增强 Map。

## 5. Query Contract

查询参数支持对象 ID、建筑 ID、楼层 ID 集合和按需 Lambda 列投影。显式空对象 ID 集合直接返回空结果，未设置条件表示查询当前项目范围；建筑/楼层条件使用参数化 `IN`。

对象 ID 大于固定批次时由 Service 分片查询，其他条件在每个分片中保留。Mapper 的 `${withColumn}` 只接收 `BaseRepositoryQueryParam#toSelectColumns()` 产生的受信列名，不开放原始 SQL 列名。

所有查询 SQL 只引用 `cache_object_model_view`，结果映射不包含对象表字段或 `object_data`。

## 6. Transaction and Compatibility

资源 Service 的批量 UPSERT 可加入调用方事务；领域 Repository 提供清晰的批量边界，供后续对象与视图在同一事务中组合写入。当前任务不修改旧缓存表和生产消费者，出现问题时可停用新仓储而不影响旧链路。

## 7. Validation Timing

按用户确认的节奏，本子任务不新增测试代码。完成实现后只执行 `common` 编译、XML/SQL 静态核对和 `git diff --check`；数据库行为测试、投影回归和完整验证统一在最终集成子任务执行。
