# 对象与视图扩展字段投影

## Goal

独立交付并审核 `cache_object.object_data` 与 `cache_object_model_view.view_data` 的字段 Map、投影和重建规则；同时调整既有云端 Flyway 的公共状态列结构。本任务不实现对象持久化 Mapper、Repository 或生产写入接线。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 已通过。

## Requirements

- `monitoringSurveillanceStatus` 是跨场景高频使用的信息点，必须从对象扩展 JSON 移到 `cache_object` 独立 JSON 列。
- 同一个对象可以同时拥有多个状态码；独立列统一保存字符串 code 数组，例如 `["2","3"]`，不冗余保存展示名称。RWD 未返回该字段时保存 SQL `NULL`，明确返回空集合时保存 `[]`。
- 状态筛选条件允许传入多个 code，并按“命中任意一个”处理；当前任务固定字段和重建契约，仓储过滤 SQL 在对象持久化子任务实现。
- 在专门配置类中直接声明两份 `private static final` 不可变 `classCode -> 扩展字段集合` Map，对象 JSON 与模型视图 JSON 不得共用 Map。
- 模型视图字段由“基础字段 + `classCode -> 增强字段`”组成：设备基础字段固定为 `bimId`、`bimLocation`，空间基础字段固定为 `bimId`、`bimLocation`、`outline`；`ACZone` 设备类键使用空间基础字段，`MODEL_VIEW_EXTENSION_FIELDS` 只管理默认基础上的增强项。
- 每个 `classCode` 的字段必须在静态 Map 声明中显式可见，不使用 `build...()`、`addFields()` 或实例构造器组装配置。
- 两份字段 Map 只能由专门的字段配置类维护；配置类和投影组件不得硬编码数据库表名或列名。
- 公共独立列字段（包括 `monitoringSurveillanceStatus`）不得进入扩展 Map；测试必须扫描两份静态 Map 并阻止字段重叠。
- 投影结果只取 Map 允许字段与 RWD 实际返回字段的交集，不补写未返回字段，不合成默认值，RWD 返回值原样保留。
- Map 未配置或没有实际扩展字段时生成 `{}`；SQL `NULL` 仅按异常/历史数据容错为空扩展对象。
- 提供“独立列公共属性 + 扩展 JSON”重建 DTO 的纯转换能力，扩展 JSON 不得覆盖公共属性。
- Map 固定在代码中，不接数据库、Nacos 或其他动态配置。
- 新增字段和公开转换契约必须提供完整 Javadoc；字段构造、保真投影、深度不可变校验和公共字段覆盖等关键逻辑必须说明维护原因与不变量。
- 当前类实例字段和方法使用 `this.`，明确依赖父类实现时使用 `super.`，静态字段和方法使用声明类限定；生产代码和测试保持一致。

## Initial Field Scope

- 对象扩展 Map：
  - `GeneralZone`、`ACZone`：`roomFuncType`。
  - 当前空调末端设备类中仅 `ACVTSF` 保存 `funcType`，其余类当前没有对象扩展字段。
  - `LTLTCC`：`serviceAreaType`、`funcType`、`lampFuncType`。
  - `LTLTCU`：当前没有对象扩展字段。
- 模型视图扩展 Map：
  - `GeneralZone`、`ACZone`：仅增强 `roomFuncType`。
  - 当前空调末端设备类、`LTLTCC`、`LTLTCU`：当前没有 classCode 增强字段，使用空 Set 显式登记。
- `id`、`projectId`、`localName`、`classCode`、`objType`/`objectType`、`buildingId`、`floorId` 及关系字段属于独立列或关系表来源，不得出现在扩展 Map。
- 投影入口必须接收能表达字段存在性的 Map；使用 `containsKey` 区分“未返回”和“显式返回 null”。

## Out Of Scope

- 不新增七张表的 Mapper 或 Repository。
- 不查询 RWD、不修改缓存事件、不切换任何消费者。
- 不在本任务实现数据库 JSON 过滤 SQL或业务读取入口筛选。
- 不在本任务确定后续新增运行监测系统的字段 Map。

## Acceptance Criteria

- [x] 两份静态 Map 的独立性和深度不可变性有单元测试。
- [x] 覆盖未配置 classCode、字段缺失、显式返回值、空交集、映射外字段和公共字段冲突。
- [x] 两种 JSON 均能由公共列和扩展内容正确重建，且空扩展固定为 `{}`。
- [x] 首版末端、照明字段范围与现有生产消费者需要的字段一致。
- [x] 模型视图按 `objType` 或 `ACZone` 兼容设备类键取得固定基础字段，并与 classCode 增强 Map 求并集；增强 Map 不重复保存 `bimId`、`bimLocation`、`outline`。
- [x] `monitoringSurveillanceStatus` 不进入扩展 JSON，RWD 查询字段包含它，独立列数组可在 DTO 重建时恢复到同名动态属性。
- [x] 云端迁移为 `cache_object` 增加可空 JSON 独立列，且没有把展示名称或逗号字符串作为存储格式。
- [x] Javadoc、异常契约和关键复杂逻辑注释满足项目质量规范。
- [x] 本任务 diff 只包含字段映射、投影/重建组件、纯单元测试及既有云端 Flyway 的状态列调整，不包含 Mapper、Repository 或生产写入接线。

## Review Gate

完成实现和质量检查后暂停，等待用户审核字段映射与 JSON 契约；审核通过前不得开始子任务 03。
