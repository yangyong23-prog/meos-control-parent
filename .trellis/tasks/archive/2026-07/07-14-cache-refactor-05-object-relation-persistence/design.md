# 对象关系缓存持久化技术设计

## 1. Scope and Boundary

本任务只实现 `cache_object_rel` 的实体、批量 SQL、查询和范围替换仓储：

- 支持正向、反向批量查询和单关系范围完整替换。
- 不查询或删除 `cache_object`，不判断对象是否仍被其他缓存键引用。
- 不接入 DTX、对象重建事件、模型视图、模板明细、规则或读取路由。

## 2. Relation Direction

关系落库方向固定：

- `object_id`：关系主体，正向查询入口；
- `graph_code`：图谱编码；
- `rel_code`：关系编码；
- `rel_object_id`：关系目标，反向查询入口。

反向查询只按 `rel_object_id` 查找指向当前对象的记录，返回的实体仍保持原始主体和目标方向，不交换字段。

## 3. Layering

- 实体放在既有 `model/entity/cache` 包：`CacheObjectRel`，继承 `BaseAssignIdDO`。
- Mapper 放在既有 `mapper/cache` 包，XML 放在 `resources/mapper/cache`。
- 查询参数放在既有 `resource/cache/param` 包，使用 Lambda 按需列投影。
- 资源 Service 负责当前项目隔离、正反向 ID 分片、审计字段和批量插入。
- `structure/cacherwd/repository` 中的领域 Repository 负责关系业务键去重和范围替换事务。

## 4. Query Contract

统一查询参数支持：

- 正向主体对象 ID 集合；
- 反向目标对象 ID 集合；
- 可选图谱编码集合和关系编码集合；
- 按需 Lambda 列投影。

正向查询只使用主体对象 ID，SQL 条件顺序对应 `idx_object(project_id, object_id, graph_code, rel_code)`；反向查询只使用目标对象 ID，条件顺序对应 `idx_rel_object(project_id, rel_object_id, graph_code, rel_code)`。

正向或反向 ID 集合为 `null` 或空集合时直接返回空结果，不表示取消该方向的对象范围限制。大 ID 集合按固定批次拆成多条批量 SQL；图谱和关系编码条件随每个分片保留。禁止逐对象循环查询。查询结果不承诺顺序，SQL 不增加与业务无关的排序开销。

## 5. Atomic Scope Replacement

完整替换范围固定为 `(projectId, objectId, graphCode, relCode)`：

1. Repository 校验范围字段，并使用 `LinkedHashSet` 对目标对象 ID 去重；
2. 在同一事务中删除当前范围全部旧关系；
3. 按去重后的目标 ID 生成关系实体；
4. Resource Service 使用 `super.fillInsertDefaultValue` 生成 bigint ID 和审计字段；
5. 按固定批次执行 XML 批量插入。

空目标集合表示清空当前关系范围。删除 SQL必须同时包含项目、主体对象、图谱和关系编码，不得影响其他范围。替换过程不访问或删除对象主表。

## 6. Compatibility and Validation Timing

当前任务不接生产事件，新增仓储为空时不影响旧缓存链路。按用户确认的节奏，本子任务不新增测试代码；完成后只执行 `common` 编译、XML/SQL 静态核对和 `git diff --check`，数据库测试和完整回归统一在最终集成子任务执行。
