# 聚合模型

信息点配置模块的核心输出单位是 `InfoCodeAggregate`。一个聚合根表达“某个对象上的某个信息点配置”，并携带模板、配置、分组、定义和值等上下文。

## 聚合根结构

`InfoCodeAggregate` 位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeAggregate.java
```

主要字段：

| 字段 | 含义 |
| --- | --- |
| `identifier` | 聚合标识，通常由对象、信息点、模板等维度组合。 |
| `templateTypeModel` | 当前信息点所属模板类型。 |
| `configModel` | 当前信息点配置。 |
| `assembleModels` | 组合信息点配置，用于由多个信息点组装出的复杂展示和值。 |
| `groupModel` | 当前信息点所属分组。 |
| `infoCodeAndDefineModel` | 信息点定义模型，供前端了解字段、单位、展示配置等定义信息。 |
| `infoCodeAndDataModel` | 信息点值模型，供前端展示当前值或统计值。 |
| `invalid` | 聚合是否无效，生命周期或策略可以据此过滤。 |

## 相关模型

| 模型 | 说明 |
| --- | --- |
| `InfoCodeTemplateTypeModel` | 模板类型模型，描述运行监控类型、模板类型、对象类型、classCode、过滤规则和排序。 |
| `InfoCodeConfigModel` | 信息点配置模型，描述信息点编码、名称、类型、展示样式、取值逻辑和排序。 |
| `InfoCodeGroupModel` | 信息点分组模型，描述分组编码、名称、层级、展示样式和排序。 |
| `InfoCodeIdentifier` | 聚合标识模型，用于标识一个聚合根。 |

## 模板类型模型

`InfoCodeTemplateTypeModel` 是模板与对象之间的桥梁，核心字段包括：

- `templateTypeId`
- `runningMonitorType`
- `runningMonitorSecondType`
- `templateType`
- `objectType`
- `classCode`
- `infoCodeFilter`
- `sequence`

它提供以下关键能力：

- `match(DtpRwdObjectDTO)`：判断 RWD 对象是否匹配模板类型。
- `toDtpRwdObjectQueryParam()`：把模板条件转换为 RWD 对象查询参数。
- `mapInfoCodeQueryFilters()`：解析信息点过滤条件。
- `parseFilterValueObject()`：解析 `infoCodeFilter` 中的过滤配置。
- `queryTemplateObjects()`：查询模板类型关联对象。

## 信息点配置模型

`InfoCodeConfigModel` 描述单个信息点的配置骨架，核心字段包括：

- `configId`
- `infoCode`
- `infoCodeName`
- `alias`
- `infoCodeType`
- `existsIotRule`
- `tackValueLogic`
- `showStyle`
- `sequence`

其中 `tackValueLogic` 是不同信息点类型的重要扩展字段。模型中提供了解析方法，用于把 JSON 配置转换为静态信息点、需求目标、持久化信息点等类型需要的取值逻辑对象。

## 分组模型

`InfoCodeGroupModel` 负责前端展示分组和部分业务过滤。核心字段包括：

- `groupCode`
- `groupName`
- `sequence`
- `showStyle`
- `parent`

模块内存在一些有业务含义的分组编码集合：

| 集合 | 作用 |
| --- | --- |
| `OBJECT_STATUS_GROUP_NAMES` | 对象状态分组，如设备状态、空间状态。 |
| `IOT_SETTING_GROUP_NAMES` | IOT 设定值相关分组。 |
| `IOT_SETTING_CONDITION_GROUP_NAMES` | IOT 设定值条件分组。 |
| `DYNAMIC_REMOVE_RELATION_INFO_CODE_GROUP_NAME_WHITELIST` | 动态删除关联信息点时的分组白名单。 |
| `OBJECT_ADDITIONAL_INFO` | 对象附加信息分组。 |

## 聚合根生成结果

聚合根生成后通常有两类转换：

| 转换 | 使用场景 |
| --- | --- |
| `InfoCodeObjectGroupVO.of(aggregates)` | 按对象和分组输出前端展示结构。 |
| `InfoCodeObjectGroupVO.ofOrigin(aggregates)` | 输出原始配置分组结构。 |

因此，聚合根字段是否完整会直接影响前端看到的模板、分组、定义和值。
