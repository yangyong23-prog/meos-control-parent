# 数据源与缓存

信息点配置模块的数据来源包含配置表、RWD 对象、IOT 数据、知识定义、风险事件和调用方传入的上下文。网关会尽量在策略执行前批量查询并缓存上下文，降低重复访问成本。

## 配置数据

配置资源层位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/resource/infocodeconfig
```

主要资源：

| 资源 | 说明 |
| --- | --- |
| `InfoCodeTemplateTypeResource` | 查询信息点模板类型。 |
| `InfoCodeConfigResource` | 查询信息点配置。 |
| `InfoCodeGroupResource` | 查询信息点分组配置。 |

对应 DAO 参数位于 `resource/infocodeconfig/param`，例如：

- `InfoCodeTemplateTypeQueryDaoParam`
- `InfoCodeGroupQueryDaoParam`
- `InfoCodeConfigQueryDaoParam`

## RWD 对象数据

RWD 对象数据主要通过 `DtpRwdClientService` 和 `InfoCodeGateway#listTemplateTypeObjects` 获取。模板类型模型中的对象类型、classCode、信息点过滤条件会参与对象筛选。

常见对象查询来源：

| 场景 | 查询方式 |
| --- | --- |
| 按对象 ID 查询 | `DtpRwdClientService#listObject` |
| 按模板类型查询对象 | `InfoCodeGateway#listTemplateTypeObjects` |
| 模板类型对象数量 | `InfoCodeGateway#mapTemplateTypeAndObjectCount` |
| 建筑/楼层/空间树 | `InstanceService#treeBuildingFloorSpace` |

## IOT 数据

IOT 数据用于动态过滤、运行值、设定值、统计值和持久化信息点。典型入口包括：

- `AbstractIotDataCacheServer#listInfoCodeValue`
- `AbstractIotDataCacheServer#listInfoCodeValueInOneHour`

动态信息点过滤会读取对象的信息点值，并只保留满足过滤条件的对象。值填充策略则通过 `IOT_DATA_CONTEXT` 获取网关预查询的 IOT 上下文。

## 附加业务数据

除配置表、RWD 和 IOT 外，部分策略还依赖额外上下文：

| 数据 | 使用方式 |
| --- | --- |
| 知识定义 | 通过 `KNOWLEDGE_DEFINE` 传给策略，用于知识类信息点定义和值。 |
| 风险事件 | 通过 `RISK_EVENT_DETAIL` 传给策略，用于风险事件详情类信息点。 |
| 时间范围 | 通过 `TIME_RANGE` 传给策略，用于统计、趋势、持久化查询。 |
| 占位符 | 通过 `PLACEHOLDERS` 传给策略，用于 source、表达式和调用方上下文覆盖。 |

## 缓存键

`InfoCodeGateway` 定义了模块级缓存键：

| 缓存键 | 说明 |
| --- | --- |
| `infoCodeTemplate` | 信息点模板缓存。 |
| `infoCodeTemplateObject` | 模板关联对象缓存。 |
| `infoCodeTemplateObject_deprecated` | 旧版上下文下的模板关联对象缓存。 |
| `infoCodeTemplateObjectIotRule` | 模板对象 IOT 规则缓存。 |

缓存使用时需要注意新版与旧版接口上下文差异，特别是 `LocalContextStorage.deprecatedVersion` 对旧接口行为的影响。

## infoCodeFilter 过滤规则

`InfoCodeTemplateTypeModel.infoCodeFilter` 支持 JSON 形式的过滤配置，并可解析为 `InfoCodeFilterValueObject`。过滤规则可引用 source、placeholder、SpEL root 引用和递归关系查询。

维护规则：

- 直接 placeholder 引用优先，例如 `${sourceKey}`。
- 当直接 placeholder 未命中时，可识别 `#root['key']`、`#root["key"]`、`#root.key` 等 SpEL root 引用。
- 关系查询中，如果 source key 是 `${objectId}`，绑定关系时应使用返回对象的 `id`。
- `takeValueExpression` 只负责写入 sourceMap 的值，不应被误用为关系匹配 ID。

## 模板对象缓存适用场景

`InfoCodeTemplateTypeModel#allowCache()` 会限定部分运行监控类型、模板展示类型可以使用模板对象缓存。当前已落地的典型组合包括：

- 末端空调：`AIR_TERMINAL` 的 `GRID`、`TABLE_LIST`
- 照明：`LIGHTING` 的 `DETAIL`、`TABLE_LIST`
- 通风：`VENTILATION` 的 `GRID`、`TABLE_LIST`
- 机房安全：`COMPUTER_ROOM_SECURITY` 的 `GRID`、`TABLE_LIST`

新增缓存场景时，需要确认对象过滤条件、运行监控上下文、旧版接口上下文和 IOT 规则是否都会影响缓存结果。
