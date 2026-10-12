# 边缘 MQTT 程序设计文档

边缘 MQTT 模块只存在于 `local` 边缘端，核心接入入口位于 `local/meos-control-compute-edge`。模块负责边缘端 MQTT 配置接入、订阅初始化、消息接收分流、Twin 点位数据消费、业务事件消息承接、模拟发数和运行观测。

SaaS 不配置、不订阅、不消费 MQTT。`local/meos-control-common-edge` 中的接口、模型、缓存、日志和 SSE 能力是边缘侧公共依赖，不代表 SaaS 或 common 层具备 MQTT 接入职责。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 模块职责、边界、核心代码入口和总体链路。 |
| [配置与订阅初始化](./configuration-and-subscription.md) | `starter-mqtt`、`paas.mqtt` 配置、订阅 topic 和 Twin 订阅初始化流程。 |
| [接收与分流](./receive-and-routing.md) | `MqttReceiveServiceImpl` 的单条/批量接收、上下文设置、topic 分流和入参校验。 |
| [Twin 点位消费](./twin-iot-consumption.md) | `FeedbackInfoMsgMqttVO`、反馈值/控制值识别、消费入口和 IOT 事件监听。 |
| [IOT 缓存与推送](./iot-cache-and-push.md) | 信息点二次计算、缓存写入、SSE 推送、历史 WebSocket 兼容边界、运行状态跟踪和运行日志衔接。 |
| [业务事件消息](./business-event-messages.md) | 异常事件、风险事件、DTP-MEOS 业务消息和运行日志业务消息处理。 |
| [模拟与观测](./simulation-and-observability.md) | `/mqtt` 模拟发数接口、Excel 批量发数、计数器和线程池队列观测。 |
| [扩展与维护指南](./extension-guide.md) | 新增 topic、业务类型、订阅类、信息点计算和推送能力时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| MQTT 依赖 | `local/meos-control-compute-edge/pom.xml` |
| MQTT 默认配置 | `local/meos-control-common-edge/src/main/resources/application-paas.yml` |
| 订阅初始化 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/init/MqttSubscribeInitServiceImpl.java` |
| MQTT 接收服务 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/mq/MqttReceiveServiceImpl.java` |
| MQTT 消费接口 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/service/MqttService.java` |
| MQTT 消费实现 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/service/impl/MqttServiceImpl.java` |
| IOT 消费监听 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/event/PushIOTEventListener.java` |
| IOT 计算与推送 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/handler/ComputeIotDataHandler.java` |
| 历史 WebSocket 兼容服务 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/service/impl/WsMessageServiceImpl.java` |
| 业务消息消费 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/service/DtpMeosServerEdgeService.java` |
| 模拟发数接口 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/controller/MqttController.java` |

## 维护原则

- MQTT 接入点只放在边缘端，新增能力时必须检查 `local/meos-control-compute-edge`。
- SaaS 侧不新增 MQTT 配置、订阅、接收或消费实现。
- `MqttService` 是边缘消费链路的公共接口，具体实现仍由边缘计算服务提供。
- 运行日志可以消费 MQTT 后形成的数据，但日志查询、分类、导出和展示仍归 [运行日志设计文档](../running-log/README.md) 维护。
- 前端实时推送以后续维护 SSE 为主，WebSocket 已废弃；历史 WebSocket 类名和转调逻辑只保留兼容说明。
- 控制服务通过 `dtp-meos` topic 上报的业务消息只在本文档描述边缘接收和分流，不把控制动作执行逻辑并入 MQTT 模块。
- 新增 topic 或业务类型时，需要同步配置、接收分流、消息模型、计数观测和异常处理。
