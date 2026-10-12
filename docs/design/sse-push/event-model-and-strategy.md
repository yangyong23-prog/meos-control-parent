# 事件模型与策略分派

SSE 事件体系由事件枚举、订阅类型、订阅参数、推送对象和策略实现组成。事件枚举决定“推什么”，订阅类型决定“谁订阅”，策略实现决定“怎么组装数据”。

## 事件枚举

`SseEmitterEventEnum` 是事件类型总表。常见事件包括：

| 类别 | 事件 |
| --- | --- |
| IOT | `IOT_DATA_CHANGE`、`CLOUD_IOT_DATA_CHANGE` |
| 信息点配置 | `INFO_CODE_CONFIG_EVENT`、`INFO_CODE_CONFIG_BATCH`、`INFO_CODE_CONFIG_SINGLE` |
| 照明 | `LIGHT_DEMAND_CHANGE`、`LIGHT_LOOP`、`LIGHT_EQUIPMENT_STATUS`、`LIGHT_SPACE_STATUS`、`ILLUM_INFO`、`LIGHTING_DASHBOARD` |
| 空调末端 | `SPACE_WITH_STATUS`、`SPACE_QUALITY_STATISTICS`、`EQUIPMENT_STATUS_STATISTICS`、`BDTP_EQUIPMENT_STATUS_STATISTICS`、`OUTDOOR_WEATHER` |
| 安防 | `SECURITY_ARMING_SPACE_STATISTICS`、`SECURITY_ACCESS_SPACE_STATISTICS`、`SECURITY_EQUIPMENT_STATISTICS`、`SECURITY_DASH_BOARD` |
| 客流/停车/电梯/通风 | `PASSENGER_FLOW_*`、`PARKING_*`、`ELEVATOR_*`、`VENTILATION_EQUIP_STATISTICS` |
| 事件与日志 | `ABNORMAL_EVENT`、`ABNORMAL_STATE`、`DANGER_EVENT`、`DANGER_EVENT_MONITORING_SITUATION`、`PAGE_BROADCAST` |
| 业务通知 | `INFO_EXTEND_SET`、`RELEASE_INTERVENTION`、`OBJECT_TAG`、`AI_NOTIFY_CALC_STATUS` |

新增事件时，枚举值、策略 Bean 名和前端 event name 必须一致。

## 订阅类型

`SubscribeTypeEnum` 描述前端页面或业务域：

| 类型 | 示例 |
| --- | --- |
| 运行监测域 | `LIGHT_MONITORING`、`AIR_TERMINAL`、`VENTILATION`、`SECURITY`、`ELEVATOR`、`PASSENGER_FLOW` |
| 系统域 | `ACCC`、`ACCI`、`ACCH`、`ACAH`、`ACAI`、`SE`、`WATER_SUPPLY` |
| 专项域 | `PARKING`、`KITCHEN_RANGE_HOOD`、`COMPUTER_ROOM_SECURITY`、`FIRE_FIGHTING`、`TOILET`、`PHOTOVOLTAIC`、`AI` |

定时任务按订阅类型选择事件清单；事件触发时也常按订阅类型过滤订阅者。

## 推送对象

`SseEmitterObject` 是实际发送给前端的数据包装：

| 字段 | 说明 |
| --- | --- |
| `groupCode` | 集团编码。 |
| `projectId` | 项目 ID。 |
| `eventId` | 事件 ID，通常是 eventKey。 |
| `eventType` | SSE 事件类型。 |
| `eventName` | 事件名称。 |
| `comment` | 事件描述。 |
| `data` | 事件数据。 |

发送时，SSE event name 使用 `eventType.value()`，data 是完整 `SseEmitterObject`。

## 策略上下文

`SseHandlerContext` 在启动时注入所有 `AbstractSseEventStrategyTemplate` Bean：

```java
public void strategyInterface(Map<String, AbstractSseEventStrategyTemplate> map) {
    this.HANDLER_MAP.putAll(map);
}
```

获取策略：

```java
handlerContext.getStrategy(SseEmitterEventEnum.IOT_DATA_CHANGE)
```

要求策略 Bean 名等于 `SseEmitterEventEnum.value()`。例如：

```java
@Component(value = "IOT_DATA_CHANGE")
public class IotDataChange extends AbstractSseEventStrategyTemplate
```

## 策略接口

`SseEventStrategy` 提供四种触发方式：

| 方法 | 使用场景 |
| --- | --- |
| `doOperation(eventKey, param, emitters)` | 定时任务按已有 eventKey 刷新。 |
| `doOperation(projectId, subscribeType)` | 按项目和订阅类型触发。 |
| `doOperation(projectId, subscribeType, condition)` | 按项目、订阅类型和条件触发。 |
| `doOperation(projectId, condition)` | IOT、日志、事件等按项目广播到相关订阅，再由策略过滤。 |

## 策略模板

`AbstractSseEventStrategyTemplate` 统一处理：

- 订阅事件类型过滤。
- 根据项目和订阅类型查找 eventKey。
- 异步提交到 `asyncSSEExecutor`。
- 可选 Redisson 分布式锁。
- 设置 `ContextUtil` 中的 `groupCode`、`tenant`、`projectId`。
- 调用 `getSseEmitterObject` 生成数据。
- 调用 `SseEmitterServer#publishEvent` 发送。
- 事件耗时记录。

策略实现只需要重写：

| 方法 | 说明 |
| --- | --- |
| `getEventType()` | 返回当前事件枚举。 |
| `getSseEmitterObject(param)` | 定时或按订阅刷新时组装数据。 |
| `getSseEmitterObject(param, condition)` | 条件触发时组装数据。 |
| `getRedisLockKey(...)` | 需要防重复执行时提供锁 key。 |

## 过滤规则

模板和策略共同完成过滤：

| 过滤点 | 规则 |
| --- | --- |
| 事件类型 | `param.eventType` 非空且不包含当前事件时不推送。 |
| 项目 | 多数策略要求 `param.projectId == condition.projectId`。 |
| 订阅类型 | 定时任务和 `doOperation(projectId, subscribeType)` 按订阅类型筛选。 |
| 对象范围 | `checkClaasCode2objectIds` 按 classCode/objectId 过滤。 |
| 事件参数 | `eventParamMap` 用于页面播报分类、云端 IOT 查询条件等。 |

`checkClaasCode2objectIds` 返回 true 表示不匹配，需要过滤掉。

## 线程池

SSE 策略异步执行使用 `asyncSSEExecutor`：

| 配置 | 默认值 |
| --- | --- |
| `executor.pool.sse.corePoolSize` | `5` |
| `executor.pool.sse.maxPoolSize` | `20` |
| `executor.pool.sse.queueCapacity` | `1024` |
| `executor.pool.sse.keepAliveSeconds` | `300` |

边缘端 `bootstrap.yml` 当前配置为：

| 配置 | 值 |
| --- | --- |
| `corePoolSize` | `20` |
| `maxPoolSize` | `30` |
| `queueCapacity` | `1000000` |
| `keepAliveSeconds` | `300` |

拒绝策略使用自定义处理后执行 `DiscardOldestPolicy`，并计数 `sse推送丢弃总数`。

## 维护要点

- 策略 Bean 名必须等于事件枚举 value，否则 `SseHandlerContext#getStrategy` 返回空。
- 定时刷新策略实现 `getSseEmitterObject(param)`；事件触发策略实现 `getSseEmitterObject(param, condition)`。
- 需要防重复计算时重写 `getRedisLockKey`。
- 事件参数要放在 `eventParamMap`，不要把所有字段塞进顶层订阅参数。
