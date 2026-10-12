# 订阅与连接管理

SSE 订阅入口由 `SseEmitterController` 提供，基础路径为 `/server-event`。连接创建后，`SseEmitterServer` 生成 eventKey，`SseEmitters` 保存 eventKey 与连接、订阅参数的映射。

## 订阅接口

| 接口 | 方法 | 入参 | 说明 |
| --- | --- | --- | --- |
| `/server-event/sse/subscribe` | `GET` | `SseSubscribeParam` query/model 参数 | 创建单个订阅连接。 |
| `/server-event/sse/subscribe` | `POST` | `List<SseSubscribeParam>` | 一个 `SseEmitter` 绑定多个订阅参数。 |
| `/server-event/sse/send` | `POST` | `SseEmitterObject` | 手动按 `eventId` 发布事件。 |
| `/server-event/sse/getMultiEmitters` | `GET` | 无 | 查询 eventKey 与连接集合。 |
| `/server-event/sse/getSseParamMap` | `GET` | 无 | 查询 eventKey 与订阅参数映射。 |

`GET /sse/subscribe` 返回 `MediaType.TEXT_EVENT_STREAM_VALUE`。`CustomSseEmitter` 也会把响应头设置为 `text/event-stream;charset=UTF-8`。

## 订阅参数

`SseSubscribeParam` 描述前端想订阅什么：

| 字段 | 说明 |
| --- | --- |
| `groupCode` | 集团编码，单订阅入口从 `ContextUtil` 补齐。 |
| `subscribeType` | 订阅类型，不能为空。 |
| `demandTemplateType` | 需求模板类型，照明等策略可能使用。 |
| `projectId` | 项目 ID，不能为空。 |
| `instanceId` | 实例 ID，不能为空。 |
| `instanceType` | 实例类型，不能为空。 |
| `classCode` | 实例类型编码。 |
| `eventType` | 订阅的事件类型列表；为空表示不按事件类型过滤。 |
| `claasCode2objectIds` | 对象类到对象 ID 集合的映射，用于 IOT/标签/干预等范围过滤。 |
| `claasCode2objectIdsByString` | 前端传入 JSON 字符串时解析为 `claasCode2objectIds`。 |
| `eventParamMap` | 事件类型到事件参数的映射，用于按需轮询或分类筛选。 |

`claasCode2objectIdsByString` 会解析 JSON 对象，值必须是数组，解析失败只记录日志。

## eventKey 生成

`SseEmitterServer#genEventKey` 使用：

```java
Objects.hash(param.toString()) + ""
```

这意味着 eventKey 由订阅参数整体决定。同一组参数会生成同一个 eventKey，`addMulti` 会将同一个 `SseEmitter` 绑定到多个 eventKey。

连接创建成功后，服务端会先发送一个特殊事件：

```text
name: EVENT_ID
comment: eventKey
```

前端可从该事件拿到 eventKey，用于后续追加轮询参数或排查连接。

## 连接缓存

`SseEmitters` 维护两个核心 Map：

| Map | key | value |
| --- | --- | --- |
| `multiEmitters` | eventKey | `CopyOnWriteArrayList<SseEmitter>` |
| `sseParamMap` | eventKey | `SseSubscribeParam` |

连接添加时：

1. 保存 eventKey 与订阅参数。
2. 将 `SseEmitter` 放入 `multiEmitters`。
3. 注册 `onCompletion`、`onError`、`onTimeout` 回调。
4. 回调触发时移除连接。
5. 某 eventKey 下连接为空时，同时移除 `sseParamMap` 和 `multiEmitters`。

## 发送流程

`SseEmitterServer#publishEvent` 先检查数据是否为空：

- `SseEmitterObject` 为空，不发送。
- `data` 为空，不发送。
- `data` 是空集合，不发送。

随后调用 `SseEmitters#send(eventKey, object)`：

1. 构造 `SseEmitter.event()`。
2. `id` 使用 `IdWorker.getIdStr()`。
3. `name` 使用 `message.getEventType().value()`。
4. `data` 为完整 `SseEmitterObject`。
5. 对 eventKey 下所有连接逐个发送。
6. 发送失败时 `completeWithError` 并从连接集合移除。

## 连接查询

策略和调度通过以下方法查找订阅者：

| 方法 | 说明 |
| --- | --- |
| `getEventKeys(projectId, subscribeType)` | 按项目和订阅类型筛选。 |
| `getEventKeys(subscribeType)` | 按订阅类型筛选。 |
| `getEventKeysByProjectId(projectId)` | 按项目筛选。 |
| `getSseEmittersByEventKey(eventKey)` | 查询某 eventKey 下的连接集合。 |

查询时会顺带清理已经没有连接的 eventKey。

## 轮询参数追加

`SsePollingParam` 用于给已有 eventKey 追加事件参数：

| 字段 | 说明 |
| --- | --- |
| `eventId` | eventKey。 |
| `eventType` | SSE 事件类型。 |
| `param` | 对应事件的参数对象。 |

边缘端 `/server-event/addPollingParam` 调用 `SseEmitterServer#addPollingParam`，最终写入 `SseSubscribeParam#eventParamMap`。

## 维护要点

- 前端订阅参数变化会导致 eventKey 变化，排查连接复用时要看完整参数。
- `eventType` 列表非空时，策略模板会过滤未订阅的事件。
- 不要直接操作 `multiEmitters` 发送业务消息，统一走 `SseHandlerContext` 或 `SseEmitterServer#publishEvent`。
- `SseEmitter` timeout 当前使用 `0L`，连接生命周期主要依赖客户端断开、异常和服务端清理。
