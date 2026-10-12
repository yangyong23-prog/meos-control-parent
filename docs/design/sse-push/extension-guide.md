# 扩展与维护指南

SSE 扩展要先确定事件、订阅范围、触发源和数据来源。不要从 WebSocket 旧链路复制实现。

## 新增 SSE 事件

检查清单：

- 在 `SseEmitterEventEnum` 新增事件。
- 新建策略类继承 `AbstractSseEventStrategyTemplate`。
- 策略 `@Component(value = "...")` 必须等于事件枚举 value。
- 实现 `getEventType()`。
- 根据场景实现 `getSseEmitterObject(param)` 或 `getSseEmitterObject(param, condition)`。
- 如果需要定时刷新，同步云端或边缘 `getScheduleEventTypes`。
- 前端订阅时在 `eventType` 中增加该事件。
- 如果需要参数，把参数放入 `eventParamMap`。

## 新增订阅类型

检查清单：

- 在 `SubscribeTypeEnum` 新增类型。
- 如果订阅类型能映射运行监测类型，同步 `RunningMonitorTypeEnum.of` 或 `RunningMonitorSecondTypeEnum.of`。
- 如果需要 IOT 变化按对象过滤，提供 `mapClaasCode2objectIds` 来源。
- 同步云端和边缘定时任务事件清单。
- 前端订阅参数补充 `subscribeType`、`instanceId`、`instanceType` 和必要事件参数。

## 新增触发源

优先使用：

```java
sseHandlerContext.getStrategy(event).doOperation(projectId, condition)
```

如果需要按订阅类型触发：

```java
sseHandlerContext.getStrategy(event).doOperation(projectId, subscribeType)
```

检查清单：

- 触发源是否已补齐 `projectId`、`groupCode` 和 `tenant`。
- condition 是否包含策略过滤所需字段。
- 数据组装是否放在策略中，而不是触发源中。
- 是否需要计数或日志。
- 是否需要异常保护，避免触发源影响主业务流程。

## 新增对象过滤

优先使用 `SseSubscribeParam#claasCode2objectIds`。如果前端不直接传对象范围，可以在策略中按订阅类型延迟计算。

检查清单：

- classCode 是否与 IOT 数据或 DTO 中的 classCode 一致。
- objectId 是否是 RWD 对象实例 ID。
- `checkClaasCode2objectIds` 返回 true 表示需要过滤。
- 对象范围计算是否需要加锁，避免同一个订阅并发重复计算。

## 新增事件参数

使用 `eventParamMap`：

```json
{
  "PAGE_BROADCAST": {
    "firstLevelCategory": "...",
    "secondLevelCategory": "..."
  }
}
```

策略中通过 `analyzeEventParam` 或 `ObjectMapper` 转换。维护时要：

- 定义内部 `EventParam`。
- 做参数校验。
- 缺失必填参数时返回空或抛出明确异常。
- 保持事件参数只服务当前事件，不污染顶层订阅参数。

## 新增定时刷新

检查清单：

- 判断应该放云端、边缘端，还是两端都需要。
- 判断周期是 1 分钟、5 分钟还是 15 分钟。
- 在对应 `getScheduleEventTypes` 中增加事件。
- 确认策略实现 `getSseEmitterObject(param)`。
- 确认是否需要分布式锁。
- 确认 `statistics.push.switch=false` 时业务是否仍可接受。

## 排查路径

| 问题 | 排查顺序 |
| --- | --- |
| 前端连接失败 | `/server-event/sse/subscribe`、响应 `text/event-stream`、参数校验、网关超时。 |
| 没有收到事件 | `eventType` 是否订阅、eventKey 是否存在、策略 Bean 名是否匹配。 |
| 只有部分页面收到 | `projectId`、`subscribeType`、`claasCode2objectIds`、`eventParamMap`。 |
| 数据为空 | 策略 `getSseEmitterObject` 返回值、`data` 是否为空集合。 |
| 推送延迟 | `asyncSSEExecutor` 队列、Redisson 锁、策略查询耗时。 |
| 定时不执行 | `statistics.push.switch`、cron、订阅是否存在、事件清单是否包含该事件。 |
| 旧 WebSocket 仍在发送 | 检查是否还有业务调用 `IWSMessageService` 或 `WebSocketTypeEnum`。 |

## 观测指标

常见计数器：

| 计数器 | 说明 |
| --- | --- |
| `{eventType}事件sse发送请求总数` | 某事件进入发送请求的次数。 |
| `sse实际发送总数{eventKey}` | 某 eventKey 实际发送次数。 |
| `sse实际发送总数` | 全局实际发送次数。 |
| `sse发送异常重试总数{eventKey}` | 某 eventKey 发送失败次数。 |
| `sse推送丢弃总数` | SSE 线程池拒绝并丢弃旧任务次数。 |

边缘端还可以通过 MQTT 观测接口查看 `asyncSSEExecutor` 队列大小。

## 禁止事项

- 不新增 WebSocket 推送能力。
- 不在 Controller 中直接遍历 `SseEmitters` 做业务推送。
- 不让策略 Bean 名和事件枚举 value 不一致。
- 不在多个策略中重复同一套复杂数据查询；应抽服务复用。
- 不把运行日志分类、控制动作执行或 MQTT 接入逻辑写进 SSE 策略。
