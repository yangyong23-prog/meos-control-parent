# 模板对象明细缓存持久化技术设计

## 1. Scope and Data Flow

本任务只交付 `cache_template_object_detail` 的数据模型、JSON 转换器、精确批量查询和单模板范围替换；同时按父任务统一约束回扫子任务 01～05 新增代码的异常类型，但不改变其功能边界：

```text
有序 DTO 列表 -> 独立 JSON 树深拷贝 -> 删除动态配置 -> JSON 字符串 -> 范围替换
完整业务键集合 -> 分片批量查询 -> 完整性判断 -> DTO 列表反序列化
```

不接入模板事件、信息点消费者、动态配置实时挂回、READY 路由或 RWD 回退。

## 2. Persistence Model

新增 `CacheTemplateObjectDetail`，继承 `BaseAssignIdDO`：

- `projectId`：由 Resource Service 使用当前项目上下文覆盖；
- `templateKey`：模板范围；
- `objectId`：当前信息点聚合根对象 ID；
- `infoCode`：信息点编码；
- `resultJson`：完整有序 `List<InfoCodeRwdObjectDetailDTO>` JSON。

业务行键固定为 `(projectId, templateKey, objectId, infoCode)`，与索引 `idx_template_object_info` 顺序一致。`templateKey` 后续必须来自 `InfoCodeTemplateTypeModel#getTemplateTypeKey()`，`infoCode` 来自当前聚合的配置模型。单模板完整替换范围为 `(projectId, templateKey)`，范围内使用 `(objectId, infoCode)` 去重，不建立数据库唯一约束。

`resultJson` 在实体和 Mapper 边界使用 `String`，XML 不配置 Jackson TypeHandler。当前 MyBatis-Plus TypeHandler 只能按原始 `Class<?>` 反序列化泛型 List，直接映射会得到 `List<LinkedHashMap>`；具体 DTO 类型统一由转换器使用 `TypeReference<List<InfoCodeRwdObjectDetailDTO>>` 恢复。

## 3. JSON Conversion Contract

新增 `CacheTemplateObjectDetailRecordConverter`：

1. 将调用方有序 DTO 集合先通过 `writeValueAsStringFailSafe` 序列化，再通过 `readValueFailSafe` 恢复为独立 `ArrayNode`，该树与源 DTO 不共享可变引用；
2. 使用显式栈遍历根节点和每层 `relObjects`，从每个 DTO 对应的 `ObjectNode` 中删除 `relInfoCodeGroupConfigs`；
3. 只沿 `relObjects` 进入下一层，保留 `inSpaces`、`inFloor`、`inBuilding`、动态 RWD 字段和结果顺序；
4. 使用 `writeValueAsStringFailSafe` 将清理后的树序列化为非空 JSON 字符串；空业务结果保存为 `[]`；
5. 读取时使用 `readValueFailSafe` 和固定 TypeReference 恢复有序 DTO 列表，非法 JSON 返回 `null`。

不能只把复制 DTO 的动态配置字段设置为 `null`，因为全局 Jackson 配置不保证忽略 null 字段，可能落盘为 `"relInfoCodeGroupConfigs": null`。结构化删除保证字段真正不存在。Converter 不自行捕获 Jackson 异常：读取 fail-safe 返回 `null` 后由仓储按下一节的完整命中契约降级；写入 fail-safe 返回 `null` 后由使用方在删除前作为无效业务数据阻止替换。

## 4. Exact Batch Query and Complete Hit

新增 `CacheTemplateObjectDetailBusinessKey` 和查询参数。Mapper 按当前项目及多个完整业务键生成 OR 分组：

```sql
project_id = ? and (
    (template_key = ? and object_id = ? and info_code = ?)
    or ...
)
```

不能分别使用 `template_key in (...)`、`object_id in (...)`、`info_code in (...)`，否则会返回调用方没有请求的组合。Resource Service 对完整业务键每 500 条分片，不执行逐键 SQL；空业务键集合直接返回空列表。

Repository 先校验并按首次出现顺序去重请求键，再把查询结果映射为业务键。完整命中要求：

- 每个去重后的请求键都存在；
- 同一数据库业务键只返回一行；
- 每行 JSON 均可反序列化。

任一键缺失或数据库存在重复业务行时整批返回空 Map，不返回部分结果。缓存记录的业务键字段损坏或 `readValueFailSafe` 返回 `null` 时，仓储记录模板键、对象 ID、信息点编码、请求数和返回数，并整批返回空 Map。请求键本身重复不影响完整命中；`resultJson = []` 的存在行仍是有效命中，不能按结果列表是否为空判断。

查询 Mapper 调用不包含在降级 catch 中：SQL、连接、超时和其他基础设施异常继续向上抛出，不能伪装成普通缓存未命中。调用方提供的空业务键也属于契约错误，使用国际化 `BusinessException`，不作为数据库脏数据降级。

## 5. Atomic Template Replacement

Repository 持有完整事务：

1. 校验模板键及全部目标记录；
2. 确认每条记录属于当前模板范围，且对象 ID、信息点编码和结果 JSON 非空；
3. 在内存中按 `(objectId, infoCode)` 去重：相同 JSON 保留一条，不同 JSON 视为业务键冲突并在删除前抛出国际化 `BusinessException`；
4. 删除当前 `(projectId, templateKey)` 全部旧记录；
5. Resource Service 使用 `super.fillInsertDefaultValue()` 填充稳定 bigint ID 和审计字段，并每 500 条批量插入。

全部校验和 JSON 转换必须发生在删除前。插入失败由外层事务回滚并保留上一版；空目标集合只清空当前模板范围，不访问或删除对象、关系、规则等其他缓存表。

## 6. Compatibility and Validation Timing

本任务不接生产写入或读取消费者，因此新增仓储为空时不影响旧链路。表业务键不包含信息点分组、配置 ID 或请求态 `additionalValues`：后续子任务 09/18 只能对 `(templateKey, objectId, infoCode)` 足以唯一确定结果的模板接入该缓存，不能在本仓储中静默覆盖不同取值逻辑。

按用户确认的节奏，本阶段不新增或运行测试，只执行 `common` 编译、Mapper XML 解析、业务键/索引/事务静态核对和 `git diff --check`。完整 JSON 往返、数据库替换及读取命中测试统一放在最终集成子任务。
