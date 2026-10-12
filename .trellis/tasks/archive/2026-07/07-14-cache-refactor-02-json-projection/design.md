# 对象与视图扩展字段投影技术设计

## 1. Scope and Package

本任务在 `common` 模块的缓存 RWD 结构下新增纯 Java 字段配置与投影组件，并同步调整既有云端 Flyway 的公共状态列定义；不新增数据库实体、Mapper、Repository、RWD 客户端或事件监听器，也不实现生产持久化接线。

交付两个协作对象：

- 字段配置：深度不可变地维护对象 Map、模型视图 Map 和公共字段集合。
- 投影组件：根据实际响应字段生成 JSON，并用公共字段与扩展 JSON 重建目标 DTO。

## 2. Field Configuration

字段配置类直接保存两份完全独立的 `private static final classCode -> fields` Map，并单独保存一份 `objType -> 模型视图基础字段` Map。使用项目已有的 Guava 不可变集合直接声明，不通过实例字段、Spring 注入、`build...()` 或 `addFields()` 方法组装配置。

### 2.1 Object Extension Fields

- 空间 `GeneralZone`、`ACZone`：`roomFuncType`。
- `AirTerminalService.ALL_EQUIPMENT_CLASS_CODES` 对应的当前末端设备类中，仅末端风机 `ACVTSF` 保存 `funcType`；其他末端类当前使用空 Set 显式登记。
- 照明回路 `LTLTCC`：`serviceAreaType`、`funcType`、`lampFuncType`。
- 照明控制模块 `LTLTCU` 当前使用空 Set 显式登记。

### 2.2 Model View Extension Fields

- `objType=equipment` 的基础字段固定为 `bimId`、`bimLocation`。
- `objType=space` 的基础字段固定为 `bimId`、`bimLocation`、`outline`。
- `ACZone` 设备类编码作为基础字段兼容键时，使用与 `objType=space` 相同的 `bimId`、`bimLocation`、`outline`。
- `MODEL_VIEW_EXTENSION_FIELDS` 只保存 classCode 增强项：空间 `GeneralZone`、`ACZone` 增强 `roomFuncType`；当前末端设备类、`LTLTCC`、`LTLTCU` 使用空 Set 显式登记。
- 对象扩展 Map 和模型视图增强 Map 首版均显式登记 22 个 classCode，其中两个空间 classCode 有 `roomFuncType`，其余 20 个模型视图 classCode 使用空增强集合。
- 最终模型视图字段为 `baseFields(objType) ∪ extensionFields(classCode)`。未知 objType 没有基础字段但仍应用已配置增强；未知 classCode 仍保留 objType 基础字段。

### 2.3 Forbidden Common Fields

扩展 Map 不得包含 `id`、`projectId`、`localName`、`classCode`、`objType`、`objectType`、`buildingId`、`floorId`、`monitoringSurveillanceStatus`。模型视图基础字段 `bimId`、`bimLocation`、`outline` 也不得重复进入 `MODEL_VIEW_EXTENSION_FIELDS`。`name`、`localId`、`valid`、`relObjIds`、`undefinedProperties` 和 `dynamicName` 不是设备类扩展数据。

Guava Map 和 Set 在声明完成后即深度不可修改。聚焦测试扫描两份静态 Map，确保公共字段不进入扩展配置；字段配置只表达对象类与字段的关系，不依赖数据库表名或列名。

### 2.4 Monitoring Surveillance Status

`cache_object.monitoring_surveillance_status` 使用可空 JSON 独立列，值固定为字符串 code 数组。单个对象允许同时保存多个状态，例如 `["2","3"]`；不保存 `{code,name}`，名称由现有信息点枚举或字典翻译。RWD 未返回字段时保存 SQL `NULL`，明确返回空集合时保存 `[]`，不得改成逗号字符串。

数据库筛选接收多个 code，并为每个 code 生成 `JSON_CONTAINS` 条件后以 OR 连接，语义为命中任意状态。当前投影组件只保证该信息点属于独立字段、包含在 RWD 查询并能作为公共属性重建；Mapper SQL 在对象持久化子任务交付。

## 3. Projection Contract

对象投影入口接收 `classCode`，模型视图投影入口接收 `objType` 和 `classCode`；两者都接收能表达字段存在性的 `Map<String, ?> actualFields`。

算法按配置字段的稳定顺序遍历，仅在 `actualFields.containsKey(field)` 时写入结果：

```text
allowed(classCode) ∩ keys(actualFields) -> extension JSON
```

因此：

- 未返回字段不会补键；
- 显式返回 `null` 会保留该键和值；
- 映射外字段被剥离；
- 未配置 `classCode` 或空交集固定序列化为 `{}`。

对象和模型视图使用同一投影算法。对象使用对象扩展 Map；模型视图先合并 objType 基础字段和 classCode 增强字段，再投影 `view_data`，不能把对象扩展字段混入视图结果。

## 4. Query Field Contract

组件按 `objType + classCode` 提供所需 RWD 查询字段集合，结果为以下字段的并集：

- 独立列来源字段：`id`、`localName`、`classCode`、`objType`、`buildingId`、`floorId`、`monitoringSurveillanceStatus`；
- 对象扩展字段；
- 模型视图 objType 基础字段；
- 模型视图 classCode 增强字段。

查询字段集合仅用于后续云端写入任务组装一次 `withColumns` 查询；投影时仍分别裁剪两种 JSON。

## 5. Rebuild Contract

对象重建入口接收 classCode，模型视图重建入口接收 objType 与 classCode；两者都接收公共属性 Map、扩展 JSON 和目标类型：

1. `NULL`、空字符串或 JSON 空对象按空扩展处理；
2. 只接受对应 `classCode` Map 允许的扩展字段；
3. 先放扩展字段，再放公共属性，保证公共属性拥有最终值；
4. 通过项目 `ObjectMapperUtils.convertValue` 重建目标 DTO。

公共属性 Map 使用 Java 属性名，后续实体转换负责把数据库列名转换为 `objType` 等 DTO 属性名。脏 JSON 中即使包含公共字段，也不能覆盖独立列。

## 6. Compatibility and Boundaries

- 不修改现有 `cache_rwd_object` 序列化与读取。
- 不修改 `DtpRwdObjectDTO` 的 Jackson 行为。
- 不查询 RWD，不写七张新表，不接入现有缓存事件。
- 新增系统或字段必须修改代码 Map、重新初始化并重新核验；不提供运行时配置入口。

## 7. Validation

- 验证两份静态 Map 深度不可变、实例独立且无公共字段冲突。
- 验证模型视图设备/空间默认字段、classCode 增强字段及未知 objType/classCode 的合并边界。
- 验证 `monitoringSurveillanceStatus` 只属于独立字段查询，不进入两份扩展 Map，并能以多 code 列表恢复到 DTO 动态属性。
- 验证缺失字段、显式 `null`、映射外字段、未配置 `classCode` 和空交集。
- 验证对象与视图投影不会因查询字段并集而相互串字段。
- 验证公共字段覆盖脏扩展 JSON，并可重建空间、设备或基础 RWD DTO。
- 运行聚焦单元测试和 `common` 编译。
