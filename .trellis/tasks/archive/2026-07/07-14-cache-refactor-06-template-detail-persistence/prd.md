# 模板对象明细缓存持久化

## Goal

交付 `cache_template_object_detail` 的自包含递归 JSON 仓储与转换能力，不接入信息点消费者。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 05 已通过。

## Requirements

- 新增实体、Mapper、XML 批量 SQL 和 Repository/Service 契约。
- 单行业务键为项目、模板键、根对象 ID、信息点编码；单模板键按完整目标集合原子替换。
- 每行业务结果是有序 `List<InfoCodeRwdObjectDetailDTO>`；列表内任意深度的 `relObjects` 作为自包含 JSON 保存，不拆成关系层级表，显式空列表 `[]` 是有效命中结果。
- `result_json` 在实体和 Mapper 边界使用 JSON 字符串，由独立转换器通过 `TypeReference<List<InfoCodeRwdObjectDetailDTO>>` 恢复具体类型，禁止依赖只能获得原始 `List` 类型的通用 Jackson TypeHandler。
- 写入前把结果转换为与源 DTO 完全独立的 JSON 树，并只沿 `relObjects` 逐层删除 `relInfoCodeGroupConfigs`；落盘 JSON 中不得出现该字段，也不得修改当前请求仍在使用的 DTO。
- 支持完整业务键的精确批量查询；请求键先去重，任一业务键缺失或数据库存在重复行时整批视为未完整命中，不得逐条读取或返回部分结果。
- 查询和插入均使用有界分片；精确查询不得拆成相互独立的对象 ID、信息点编码 `IN` 条件，避免产生不存在的笛卡尔业务键。
- 同一模板键、根对象 ID 和信息点编码只能对应一份业务结果；重复记录 JSON 相同时可去重，不同时必须在删除旧数据前失败。
- 所有写入数据必须先完成转换和校验，再在事务中删除并重建当前模板键范围；空目标集合只清理当前模板范围。
- 请求业务键、写入参数和范围冲突统一使用带中英文资源的 `BusinessException`；JSON 转换统一使用 `ObjectMapperUtils.writeValueAsStringFailSafe/readValueFailSafe`，不得在 Converter 中自行捕获和包装 Jackson 异常。
- fail-safe 转换返回 `null` 时由使用方处理：读取仓储记录完整业务键并整批返回未命中；写入方在删除旧范围前按无效业务数据抛出 `BusinessException`。SQL、连接及其他基础设施异常仍向上抛出。
- 标注 `@NonNull` 的参数由调用方保证，方法内部不得重复判空；只有 `@Nullable`、集合元素或 fail-safe 返回值允许在当前方法检查。

## Out Of Scope

- 不生成模板对象明细，不修改模板事件。
- 不实时挂回动态配置，不切换 `RwdObjectDetailInfoCodeAnalyzeStrategy`。
- 不实现对象、关系、规则或 READY 路由。
- 不解决依赖请求态 `additionalValues` 的结果可缓存性；后续写入和读取接线必须只对业务键足以唯一确定结果的模板启用新缓存。

## Acceptance Criteria

- [x] 有序 DTO 列表及任意深度 `relObjects` 可序列化、反序列化并保持业务内容，`[]` 与缺行语义分离。
- [x] 落盘 JSON 的所有对象层级均不含 `relInfoCodeGroupConfigs`，源 DTO 未被修改。
- [x] 单模板范围替换只影响当前模板键；精确批量查询可判断完整命中且无 N+1。
- [x] 除父任务统一异常规范及子任务 01～05 新增代码的异常类型回扫外，本任务功能 diff 只包含模板对象明细持久化与转换，不新增生产接线或测试代码。
- [x] 业务校验统一使用国际化 `BusinessException`；JSON 统一使用项目 fail-safe 工具，坏缓存内容在读取仓储整批降级，写入与基础设施异常保持失败传播。

## Validation Timing

- 按已确认的迭代节奏，本子任务不新增或运行测试；只执行编译、XML 解析和静态契约检查。
- DTO 深层往返、数据库范围替换和完整命中测试统一在最终集成子任务执行。

## Review Gate

完成实现和质量检查后暂停，等待用户审核明细 JSON 与仓储边界；审核通过前不得开始子任务 07。
