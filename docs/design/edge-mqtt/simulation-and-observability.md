# 模拟与观测

边缘 MQTT 模块提供 `/mqtt` 接口用于现场模拟发数、批量发数和消费观测。该 Controller 受 `@ConditionalOnMqttEnabled` 控制，只在 MQTT 启用时生效。

## 接口入口

`MqttController` 位于：

```text
local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/controller/MqttController.java
```

基础路径：

```text
/mqtt
```

接口标签为“MQTT模拟发数接口”。

## 模拟发数

| 接口 | 方法 | 入参 | 说明 |
| --- | --- | --- | --- |
| `/mqtt/sendData` | `POST` | `FeedbackInfoMsgMqttVO` | 直接调用 `MqttService#expense`，模拟单条点位消费。 |
| `/mqtt/sendBatchIotData` | `POST` | Excel 文件 | 解析 Excel 后逐条调用 `MqttService#expense`。 |

模拟发数绕过 broker 和 `MqttReceiveServiceImpl#twinMqtt`，直接进入 IOT 消费链路。因此排查问题时需要区分：

| 现象 | 优先排查 |
| --- | --- |
| broker 收不到或 topic 不分流 | `paas.mqtt`、starter-mqtt、`MqttReceiveServiceImpl`。 |
| 模拟发数正常但真实 MQTT 异常 | topic、payload、`mqtt_receivedTopic`、反馈值/控制值识别。 |
| 模拟和真实都异常 | `MqttServiceImpl`、`PushIOTEventListener`、`ComputeIotDataHandler`、缓存或推送链路。 |

## Excel 批量发数

`sendBatchIotData` 使用 EasyExcel 读取 `FeedbackInfoMsgMqttExcel`，监听器为：

```text
local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/model/excel/FeedbackInfoMsgMqttExcelListener.java
```

处理流程：

1. 每行 Excel 转换为 `FeedbackInfoMsgMqttVO`。
2. 暂存到 `dataList`。
3. `doAfterAllAnalysed` 中逐条调用 `MqttService#expense`。
4. 清空 `dataList`。

Excel 模拟发数同样不经过 topic 分流，使用时需要在 Excel 中准备完整的点位字段。

## 统计接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/mqtt/sse/statistics` | `GET` | 返回 `DailyStringCounter.getCount()`。 |
| `/mqtt/sse/statisticsSwitch` | `GET` | 设置 `DailyStringCounter.SWITCH`。 |
| `/mqtt/sse/executorQueueSize` | `GET` | 返回 MQTT 消费线程池队列大小。 |

`statisticsSwitch(Boolean sw)` 中 `sw` 为空时不处理。

## 计数器来源

当前可通过统计接口观察到的 MQTT 相关计数包括：

| 计数器 | 来源 |
| --- | --- |
| `mqtt接收总数` | `MqttReceiveServiceImpl#handlerMessage` |
| `mqtt反馈值` | `MqttReceiveServiceImpl#twinMqtt` 反馈值分支 |
| `mqtt控制值总数` | `MqttReceiveServiceImpl#twinMqtt` 控制值分支 |
| `mqtt_异常事件接收总数` | `MqttReceiveServiceImpl#eventChange` |
| `mqtt_dtp_meos接收总数` | `MqttReceiveServiceImpl#dtpMeosServerEdge` |
| `mqtt衍生计算总数` | `ComputeIotDataHandler#computeInfoCodeData` |
| `websocket发送总数{channelId}` | 历史 WebSocket 兼容链路 `WsMessageServiceImpl#pushMessage`。 |
| `websocket发送失败总数{channelId}` | 历史 WebSocket 兼容链路 `WsMessageServiceImpl#pushMessage`。 |

计数器是排查工具，不能替代数据库、Redis 或前端订阅状态的准确性校验。

## 队列观测

`MqttCounter#getExecutorQueueSize` 用于查看 MQTT 消费线程池队列情况。该组件同样使用 `@ConditionalOnMqttEnabled`，只在 MQTT 启用时注册。

排查消费堆积时，建议同时查看：

- `paas.mqtt.comsumer.consumer-buffer-queue-size`
- `paas.mqtt.comsumer.consumer-thread.*`
- `/mqtt/sse/executorQueueSize`
- `DailyStringCounter` 中接收数、反馈值数、控制值数、SSE 发送数和历史 WebSocket 失败数
- 业务日志中的解析错误、无效值丢弃、推送失败

## 维护要点

- 模拟发数接口不要用于验证 broker 连接和 topic 分流，它只验证消费链路。
- 批量 Excel 模拟发数会直接冲击消费链路，现场使用时需要控制数据量。
- 新增计数器时要保证命名能区分接收、解析、分流、计算、推送和失败。
- 观测接口属于边缘调试能力，不要迁移到 SaaS。
