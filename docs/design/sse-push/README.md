# SSE 推送程序设计文档

SSE 推送模块负责前端长连接订阅、事件参数保存、事件策略分发、云端/边缘端定时刷新、IOT/事件/日志/业务消息触发推送，以及现场观测。当前实时推送能力以后续维护 SSE 为主，WebSocket 已废弃，不再作为新增能力入口；历史 WebSocket 代码只保留兼容边界说明。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 当前维护范围、WebSocket 废弃边界、核心代码入口和总体链路。 |
| [订阅与连接管理](./subscription-and-connection.md) | `/server-event/sse/subscribe`、`SseEmitterServer`、`SseEmitters`、eventKey 和连接生命周期。 |
| [事件模型与策略分派](./event-model-and-strategy.md) | `SseEmitterEventEnum`、`SubscribeTypeEnum`、`SseSubscribeParam`、`SseHandlerContext` 和策略模板。 |
| [推送触发入口](./trigger-entrypoints.md) | 定时任务、IOT/MQTT、Kafka、运行日志、业务消息、边缘 Controller 和手动发布入口。 |
| [核心事件策略](./core-event-strategies.md) | IOT 数据变化、云端 IOT 刷新、页面播报、信息点扩展、干预解除、异常/风险事件等策略。 |
| [云边差异与调度](./cloud-edge-scheduling.md) | 云端和边缘端 SSE 定时任务差异、调度周期、推送开关和事件清单。 |
| [WebSocket 废弃边界](./websocket-deprecated-boundary.md) | 历史 WebSocket 类、配置、间接 SSE 转调和后续维护约束。 |
| [扩展与维护指南](./extension-guide.md) | 新增事件、订阅类型、策略、触发源、过滤规则和观测时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| 通用订阅 Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/SseEmitterController.java` |
| 边缘扩展 Controller | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/controller/SseEmitterEdgeController.java` |
| 云端手动触发 Controller | `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/controller/SseEmitterSaasController.java` |
| 连接服务 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SseEmitterServer.java` |
| 连接缓存 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SseEmitters.java` |
| 自定义 Emitter | `common/src/main/java/com/persagy/apccontrol/basic/sse/CustomSseEmitter.java` |
| 订阅参数 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SseSubscribeParam.java` |
| 推送对象 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SseEmitterObject.java` |
| 事件枚举 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SseEmitterEventEnum.java` |
| 订阅类型枚举 | `common/src/main/java/com/persagy/apccontrol/basic/sse/SubscribeTypeEnum.java` |
| 策略上下文 | `common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/SseHandlerContext.java` |
| 策略模板 | `common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/AbstractSseEventStrategyTemplate.java` |
| 云端定时任务 | `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/sse` |
| 边缘定时任务 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/task/scheduled/sse` |
| 边缘 SSE 策略 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/sse/strategy` |
| 公共 SSE 策略 | `common/src/main/java/com/persagy/apccontrol/basic/sse/strategy` |
| SSE 线程池 | `common/src/main/java/com/persagy/apccontrol/basic/config/ExecutorConfig.java` |

## 维护原则

- 新增实时推送能力必须走 SSE，不再扩展 WebSocket。
- 前端订阅通过 `SseSubscribeParam` 描述项目、订阅类型、实例、事件类型、对象范围和事件参数。
- 后台推送通过 `SseHandlerContext#getStrategy(event)` 获取策略，不直接操作 `SseEmitters`。
- 策略 Bean 名必须与 `SseEmitterEventEnum.value()` 保持一致。
- 定时推送和事件触发推送共用策略，不重复实现数据组装。
- WebSocket 相关类、配置、表和接口只作为历史兼容，不作为后续功能设计入口。
