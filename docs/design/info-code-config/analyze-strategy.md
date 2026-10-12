# 解析策略与取值

信息点配置模块通过 `InfoCodeTypeEnum` 区分信息点类型，通过 `InfoCodeAnalyzeStrategy` 执行定义和值填充。这样可以让不同类型的信息点复用聚合根结构，同时保留独立取值逻辑。

## 策略接口

策略接口位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/strategy/InfoCodeAnalyzeStrategy.java
```

核心方法：

| 方法 | 说明 |
| --- | --- |
| `infoCodeType()` | 返回策略支持的信息点类型。 |
| `fillInfoCodeDefineModel(List<InfoCodeAggregate>, Map<String, Object>)` | 填充定义模型。 |
| `fillInfoCodeDataModel(List<InfoCodeAggregate>, Map<String, Object>)` | 填充值模型。 |

`BaseInfoCodeAnalyzeStrategy` 定义了策略间共享的附加数据 key：

| key | 说明 |
| --- | --- |
| `KNOWLEDGE_DEFINE` | 知识定义上下文。 |
| `IOT_DATA_CONTEXT` | IOT 数据上下文。 |
| `RISK_EVENT_DETAIL` | 风险事件详情上下文。 |
| `TIME_RANGE` | 查询时间范围。 |
| `PLACEHOLDERS` | 调用方传入或自动解析的占位符值。 |

## 类型枚举

`InfoCodeTypeEnum` 位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/enumeration/InfoCodeTypeEnum.java
```

它负责把配置中的 `infoCodeType` 映射到具体策略 Bean，并提供类型判断方法。

主要类型可以按业务来源分为：

| 分类 | 类型示例 |
| --- | --- |
| 基础/IOT | `BASIC`、`IOT_RUNNING`、`IOT_SETTING`、`IOT_STATISTIC`、`IOT_CLOUD_STATISTIC`、`IOT_INFO_CODE_HANDLER` |
| IOT 持久化 | `IOT_PERSISTENCE`、`IOT_PERSISTENCE_MULTI`、`IOT_PERSISTENCE_TREND` |
| RWD | `RWD_OBJECT`、`RWD_OBJECT_DETAIL`、`RWD_OBJECT_LIST`、`RWD_OBJECT_STAT`、`RWD_INFO_TAG` |
| 需求/日历 | `DEMAND_TARGET`、`DEMAND_TARGET_STATISTIC`、`DEMAND_TARGET_TYPE`、`DEMAND_CONFIG`、`SPACE_ACCESS_DEMAND_STATUS`、`CALENDAR_WORK_TIME` |
| 事件 | `RISK_EVENT`、`DANGER_EVENT`、`RISK_EVENT_DETAIL`、`RISK_EVENT_DETAIL_SPACE_WITH_EQUIPMENTS`、`ROUTING_INSPECTION` |
| 其他配置 | `QUALITY_INTERVAL`、`EXIST_INFO_CODE_CONFIG`、`INFO_CODE_SET_EXTEND_CONFIG`、`WEB_CONFIG`、`JSON_OBJECT` |

## 定义模型填充

定义模型用于描述信息点“是什么”，通常包括编码、名称、别名、类型、单位、展示样式、IOT 规则等。填充入口是 `InfoCodeGateway#fillInfoCodeDefineModel`。

设计规则：

- 定义填充按信息点类型分组执行。
- 同一类型的聚合根批量交给对应策略，避免逐条查询。
- 需要知识定义等附加数据时，通过 `mapAdditionalDefineValues` 预先加载，再传给策略。
- 策略应只填充本类型相关定义，不应修改不属于自己类型的聚合根。

## 值模型填充

值模型用于描述信息点“当前值或统计值是什么”。填充入口是 `InfoCodeGateway#fillInfoCodeDataModel`。

设计规则：

- 值填充按信息点类型分组执行。
- 不同类型策略可以并发执行，执行器为 `BusinessThreadPoolExecutor.ttlExecutor`。
- 请求时间条件、占位符、IOT 数据上下文、风险事件上下文等通过 `additionalDataValues` 传入。
- 策略内部应优先使用网关预查询的上下文，避免重复访问外部服务。

## 占位符与时间条件

按对象查询时，`InfoCodeConfigServiceImpl#queryInfoCodeObjectGroupVo` 会将请求参数中的时间条件和 `placeholders` 放入 `additionalDataValues`。策略读取 `BaseInfoCodeAnalyzeStrategy.TIME_RANGE` 和 `BaseInfoCodeAnalyzeStrategy.PLACEHOLDERS` 即可获取这些上下文。

占位符的设计规则：

- 调用方传入的 placeholder 优先级高于自动解析值。
- 空 map 等同于未传入。
- 新增 SSE 或接口字段时，需要确认字段是否继续向 `InfoCodeConfigByObjectQueryParam.placeholders` 透传。

## 策略实现边界

策略适合处理：

- 某一类信息点的定义转换。
- 某一类信息点的实时值、历史值、统计值或外部业务值填充。
- `tackValueLogic` 的类型化解析和使用。

策略不适合处理：

- Controller 请求参数校验。
- 模板类型与对象查询。
- 聚合根初始化。
- 分组结构转换为 VO。

这些能力应分别留在 Controller/Service、`InfoCodeGateway`、`InfoCodeAggregateInitContext` 和 VO 转换层。
