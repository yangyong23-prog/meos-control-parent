# WebSocket 废弃边界

WebSocket 已废弃，不再作为实时推送新增能力入口。当前代码中仍存在 WebSocket 相关类、配置和转调逻辑，主要用于历史兼容和少量旧链路承接。后续新增或调整实时推送时，应优先进入 SSE 模块。

## 历史代码位置

| 类型 | 位置 |
| --- | --- |
| WebSocket 服务端 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/NettyWebsocketServer.java` |
| WebSocket 客户端缓存 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/WSClientCache.java` |
| WebSocket 客户端模型 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/WSClient.java` |
| WebSocket 推送服务 | `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/service/impl/WsMessageServiceImpl.java` |
| 推送接口 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/service/IWSMessageService.java` |
| 推送配置表 | `common/src/main/java/com/persagy/apccontrol/basic/model/entity/WebsocketConfig.java` |
| 本地缓存 | `LocalCacheConstant.WEBSOCKET_CONFIG` |

这些代码不作为新能力入口。

## 残留转调

`WsMessageServiceImpl` 中仍有 SSE 转调：

| `webType` | 转调事件 |
| --- | --- |
| `WebSocketTypeEnum.PAGE_BROADCAST` | `SseEmitterEventEnum.PAGE_BROADCAST` |
| `WebSocketTypeEnum.INFO_EXTEND_SET` | `SseEmitterEventEnum.INFO_EXTEND_SET` |

这类逻辑是历史兼容。新增页面播报或对象扩展刷新，应直接调用：

```java
sseHandlerContext.getStrategy(SseEmitterEventEnum.PAGE_BROADCAST)
sseHandlerContext.getStrategy(SseEmitterEventEnum.INFO_EXTEND_SET)
```

## IOT 链路中的历史命名

`PushIOTEventListener` 和 `ComputeIotDataHandler` 仍位于 `websocket` 包下，并且 `ComputeIotDataHandler#pushMessage` 仍调用 `IWSMessageService`。这些是历史包名和旧推送链路残留。

当前文档维护口径：

- IOT 数据变化的前端实时刷新以后续 SSE 为主。
- `ComputeIotDataHandler#sseStrategy` 是边缘 IOT 到 SSE 的当前关键入口。
- WebSocket 推送配置和客户端缓存不再扩展。

## 配置与表边界

历史 WebSocket 相关能力包括：

- `paas.websocketPort`
- `edge.websocket`
- `websocket_config`
- `LocalCacheInit#loadWebsocketConfig`
- `WebsocketConfigService`
- `WebsocketConfigExcelListener`

这些配置和表只保留兼容。新增 SSE 推送过滤应使用 `SseSubscribeParam`、`eventType`、`claasCode2objectIds` 和 `eventParamMap`。

## 维护规则

- 不新增 WebSocket Controller。
- 不新增 `WSClient` 发送场景。
- 不新增 `websocket_config` 过滤规则。
- 不把新的前端实时刷新能力绑定到 `WebSocketTypeEnum`。
- 旧链路如果必须改动，只做兼容、迁移或删除，不在其中扩展新设计。
- 文档和需求评审中将“推送”默认理解为 SSE 推送。
