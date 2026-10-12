# 推送触发入口

SSE 推送触发源分为订阅侧手动发布、定时刷新、IOT 数据变化、MQTT/Kafka 事件、运行日志、业务接口和边缘控制台通知。不同触发源最终都应收敛到 `SseHandlerContext` 或 `SseEmitterServer`。

## 订阅侧手动发布

`SseEmitterController#publishEvent`：

```text
POST /server-event/sse/send
```

入参是 `SseEmitterObject`，直接调用 `SseEmitterServer#publishEvent(param)`。该接口适合调试或明确 eventId 的手动发布，不应作为业务推送主入口。

## 云端手动触发

`SseEmitterSaasController`：

```text
POST /server-event/publish
```

调用 `SseEventScheduledService#scheduled()`，用于手动触发云端定时刷新逻辑。

## 边缘手动触发

`SseEmitterEdgeController` 提供：

| 接口 | 说明 |
| --- | --- |
| `/server-event/equipmentBusiness` | 按订阅参数查询设备业务配置事件结果。 |
| `/server-event/releaseIntervention` | 批量触发干预解除推送。 |
| `/server-event/sendStrategyMsg` | 根据 `SendStrategyMsgDTO.event` 和 `conditionDto` 触发策略。 |
| `/server-event/addPollingParam` | 给已有 eventKey 追加轮询参数。 |
| `/server-event/mapClaasCode2objectIds` | 查询订阅类型对应的 classCode/objectIds 范围。 |
| `/server-event/publish` | 手动触发边缘端定时刷新逻辑。 |

`sendStrategyMsg` 使用：

```java
handlerContext.getStrategy(dto.getEvent()).doOperation(ContextUtil.getProjectId(), dto.getConditionDto());
```

## 定时刷新

云端：

- `SseEventScheduledService`：每 5 分钟执行一次。
- `SseEventScheduledServiceFor15Min`：每 15 分钟执行一次。

边缘端：

- `SseEventScheduledLocalService`：每 1 分钟执行一次。
- `SseEventScheduledLocalServiceFor15Min`：每 15 分钟执行一次。

定时刷新都受配置控制：

```text
statistics.push.switch
```

默认为 `true`。

## IOT 数据变化

边缘 MQTT 点位数据消费后，`ComputeIotDataHandler#sseStrategy` 触发：

| 条件 | 事件 |
| --- | --- |
| 所有 IOT 数据变化 | `IOT_DATA_CHANGE` |
| 照明回路运行状态变化 | `LIGHT_LOOP` |
| 照明回路运行/在线/控返异常变化 | `LIGHT_SPACE_STATUS` |
| 照明回路手自动/在线/控返异常变化 | `LIGHT_EQUIPMENT_STATUS` |
| 项目对象室外照度变化 | `ILLUM_INFO` |

这些触发来自边缘实时 IOT 消费链路，详见 [边缘 MQTT](../edge-mqtt/README.md)。

## MQTT 业务事件

`MqttReceiveServiceImpl` 从边缘 MQTT 接收到事件后触发：

| topic | 事件 |
| --- | --- |
| `event-change` | `ABNORMAL_EVENT` |
| `danger-event` | `DANGER_EVENT` |
| `dtp-meos` 中 `LIGHT_DEMAND_CHANGE` | `LIGHT_DEMAND_CHANGE`、`LIGHT_LOOP` |
| `dtp-meos` 中控制台需求变化 | `INFO_EXTEND_SET` |
| `dtp-meos` 中干预解除 | `RELEASE_INTERVENTION` |

SSE 只负责前端推送，不负责 MQTT 接入和业务消息解析。

## Kafka 事件

云端 Kafka 监听：

| 类 | 消息 | 事件 |
| --- | --- | --- |
| `KafkaConsumerListener#abnormalEventMessageHandle` | 异常事件 | `ABNORMAL_EVENT` |
| `KafkaConsumerListener#dangerEventMessageHandle` | 风险事件 | `DANGER_EVENT` |

公共 Kafka 监听：

| 类 | 消息 | 事件 |
| --- | --- | --- |
| `KafkaCommonConsumerListener#instanceTreeMonitoringSituationChange` | 风险事件树监控态变化 | `DANGER_EVENT_MONITORING_SITUATION` |

Kafka 监听负责解析消息和补齐项目上下文，SSE 策略负责订阅过滤和推送。

## 运行日志与页面播报

运行日志相关服务会触发 `PAGE_BROADCAST`：

- `RunningLogEdgeNowServiceImpl`
- `PageBroadcastMsgServiceImpl`
- 历史 WebSocket 转调中的 `WebSocketTypeEnum.PAGE_BROADCAST`

页面播报策略支持两类 condition：

- `FeedbackInfoMsgMqttVO`
- `RunningLogVO`

日志分类、查询和导出归运行日志模块维护。

## 信息点扩展与对象刷新

以下场景会触发 `INFO_EXTEND_SET`：

- 边缘冷源配置变更。
- 运行监测服务中对象扩展配置刷新。
- DTP-MEOS 控制台需求变化消息。
- 历史 WebSocket 转调中的 `WebSocketTypeEnum.INFO_EXTEND_SET`。

后续新增对象扩展刷新应直接调用 `SseHandlerContext#getStrategy(INFO_EXTEND_SET)`。

## AI 计算状态

边缘端 `AiServiceImpl` 使用 `SseHandlerContext` 获取 `AI_NOTIFY_CALC_STATUS` 策略，向 AI 订阅者推送计算状态。

## 维护要点

- 业务代码触发推送时优先调用 `SseHandlerContext#getStrategy(event)`。
- 不要新增 WebSocket Controller 或 WebSocket 客户端缓存来承接实时刷新。
- 定时任务用于补偿和周期刷新，实时变化优先使用事件触发。
- 触发源只做解析和分发，数据组装应放在对应 SSE 策略。
