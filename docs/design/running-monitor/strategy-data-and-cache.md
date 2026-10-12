# 数据源与缓存

当前运行监测策略层主要依赖 RWD 对象、需求实例配置和 IOT 规则/值过滤能力。这里描述的是策略查询会使用的数据源，不包含历史 2D 系统图运行态、控制下发和运行日志链路。运行日志由运行组单独维护，见 [运行日志程序设计文档](../running-log/README.md)。

## RWD 对象数据

策略最终返回的空间、设备、系统对象来自 RWD。公共参数基类持有 `DtpRwdClientService` 和 `DtpRwdConvert`，用于：

- 构造真实 RWD 查询参数。
- 复制和补齐查询字段。
- 通过 `relationFrom` 或 `relationTo` 查询关系对象。
- 查询后按对象 ID、类别、建筑、楼层等条件做二次过滤。
- 将 DTO 转换为 Dashboard 需要的 VO。

策略实现可以按业务需要查询空间、设备或系统，但应尽量复用公共参数的过滤能力。

## 需求配置数据

`BaseRunningMonitorStrategy` 中仍保留旧实例参数兼容逻辑，并提供 `filterInvalidDemandSpace`：

- 旧入参通过建筑、楼层、空间或项目转换为 `DemandInstanceValueObject`。
- 同一旧查询中建筑、楼层、对象过滤只能存在一种，且最多一个对象。
- `filterInvalidDemandSpace` 通过 `DemandInstanceGateway` 查询黑名单和屏蔽空间，并过滤无效需求空间。

新逻辑不应继续扩展旧实例入参；如果需要需求模板能力，应通过二级类型转换到 `DemandTemplateTypeEnum` 后复用现有需求配置查询。

## IOT 规则和值过滤

策略层只把 IOT 作为对象过滤来源：

| 能力 | 方法 | 用途 |
| --- | --- | --- |
| 信息点规则过滤 | `mapObjectIdAndExistsRuleInfoCodes` | 保留存在指定规则的信息点对象。 |
| 信息点值过滤 | `listInfoCodeValueInOneHour` | 按近一小时信息点值保留或排除对象。 |

这不是运行态点位展示能力。策略层不维护批量点位值接口、信息点配置树、`rcRunStatus` 中间态或控制状态。

## MySQL 缓存 key

`BaseObjectQueryParam#mysqlCacheKey` 由 `mysqlCacheKeyType` 和参数 `hashCode` 拼接得到。运行监测三个查询参数会把二级类型加入 key type：

```text
<secondType1>_<secondType2>_<baseKeyType>_<hashCode>
```

这样同一个空间/设备/系统查询参数在不同运行监测二级类型下不会误用缓存。

## 字段裁剪

`withColumns` 控制 RWD 查询和返回对象字段。公共参数会根据过滤条件自动补齐必须字段，最后再调用 `clearUselessFieldValueByWithColumns` 清理不需要返回的字段。

维护策略时要区分两类字段：

- 查询必须字段：为了过滤或关系查询必须临时获取。
- 响应字段：前端或调用方实际需要返回。

不要为了过滤方便无条件返回大量字段。

## 维护要点

- 策略数据源以 RWD 为主，需求和 IOT 只用于对象范围过滤。
- 需要控制、运行日志、2D 系统图运行态或信息点详情时，不应放入运行监测策略层。
- 新增 IOT 值过滤时，要确认查询不可缓存或缓存 key 足够区分。
- 新增二级类型时，要确认缓存 key、Dashboard 查询和需求模板映射同步更新。
