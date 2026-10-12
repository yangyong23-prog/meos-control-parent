# 对象主表与缓存键关联持久化技术设计

## 1. Scope and Boundary

本任务只实现 `cache_object` 与 `cache_object_key_rel` 的公共持久化能力：

- 新增两张表对应的实体、Mapper、XML、资源 Service 和领域 Repository。
- 新增对象实体与 RWD 字段之间的转换组件，复用子任务 02 的投影和重建逻辑。
- 不接入 `CacheRwdObjectEventListener`，不修改任何生产读写入口。
- 不实现模型视图、对象关系、模板明细、规则或 READY 路由。

## 2. Layering

- 实体放在既有 `model/entity/cache` 包：`CacheObject`、`CacheObjectKeyRel`。
- Mapper 放在既有 `mapper/cache` 包，XML 放在 `resources/mapper/cache`。
- 查询参数放在既有 `resource/cache/param` 包，使用类型安全字段投影。
- 资源 Service 负责 Mapper 分片、审计字段填充和当前项目隔离。
- `structure/cacherwd/repository` 中的领域 Repository 负责对象去重、缓存键范围替换和查询结果组织。
- 对象与 RWD DTO 的转换继续放在既有 `structure/cacherwd/projection` 包，避免字段规则散落到仓储或 SQL。

## 3. Object Persistence

`CacheObject` 继承 `BaseInputIdDO`，对象 ID 原样作为主键。`object_data` 保存子任务 02 投影器输出的 JSON 字符串；无扩展字段时固定写入 `{}`。

`monitoring_surveillance_status` 在实体中表示为可空的 `List<String>`，通过 Jackson TypeHandler 映射 MySQL JSON：

- RWD 未返回该字段或明确返回 `null` 时保存 SQL `NULL`；
- RWD 明确返回空集合时保存 `[]`；
- 非空值只提取 code，按首次出现顺序去重，不保存 name；
- 支持 RWD 常见的 code/name 对象集合，也兼容单个 code 值。

批量写入使用 `INSERT ... ON DUPLICATE KEY UPDATE`。同一批次内相同对象 ID 先在内存中去重，后出现的数据覆盖先出现的数据；XML 按固定批次执行，不使用 MyBatis-Plus `saveBatch`。更新已有对象时保留原创建审计字段，只更新业务列和更新审计字段。

## 4. Object Conversion

转换组件接收能够表达字段是否存在的 `Map<String, ?>`：

1. 独立提取 ID、名称、classCode、objType、建筑、楼层和监测监控状态。
2. 调用 `CacheObjectExtensionProjector#projectObject` 生成 `object_data`。
3. 重建 DTO 时先从扩展 JSON 恢复允许字段，再由独立列覆盖公共字段。
4. SQL `NULL` 的监测监控状态不写入 DTO 动态字段，`[]` 则恢复为空列表，保留两种语义。

## 5. Query Contract

对象查询参数支持：

- 对象 ID 集合；
- 多个监测监控状态 code，SQL 使用参数化的 OR 语义 `JSON_CONTAINS`；
- Lambda 字段集合转换得到的按需列投影。

对象 ID 数量超过固定阈值时由 Service 分片查询。状态 code 为空时不追加状态条件，SQL `NULL` 和 `[]` 均不会误命中非空 code。

缓存键查询按当前项目和缓存键集合批量读取关联记录，并在 Service 中对缓存键集合分片；Repository 组织为 `cacheKey -> objectIds`，不循环访问数据库。

## 6. Atomic Key Replacement

单个缓存键以 `(projectId, cacheKey)` 为完整替换范围：

1. 使用 `LinkedHashSet` 对目标对象 ID 去重并保留调用顺序；
2. 在同一事务内删除当前范围旧关联；
3. 为目标关联生成稳定 bigint ID 和审计字段；
4. 按固定批次插入新关联。

空目标集合表示清空该缓存键。替换过程中不删除 `cache_object`，避免影响其他缓存键仍引用的共享对象。

## 7. Validation Timing

按用户要求，本子任务不新增测试代码。完成实现后只执行编译、Mapper/XML 静态核对和 `git diff --check`；数据库测试、批量行为测试和完整回归统一在最终集成子任务执行。
