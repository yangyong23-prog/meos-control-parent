# 扩展与维护指南

边缘 MQTT 扩展要先判断变更属于接入层、点位消费层、业务消息层还是下游展示层。不要因为消息经由 MQTT 到达，就把所有下游业务规则都放进 MQTT 模块。

## 新增接收 topic

检查清单：

- 在边缘配置中新增 `paas.mqtt.receive-topics`。
- 在配置中心补充对应 `${mqtt.topic.*.name}`。
- 在 `MqttReceiveServiceImpl` 注入 topic 配置。
- 在 `handlerMessage` 中增加明确分支。
- 定义 payload 模型和空值保护。
- 增加必要的 `DailyStringCounter` 计数。
- 判断该 topic 是点位、事件还是业务消息。
- 确认是否需要模拟发数或观测接口。

注意：如果不加明确分支，当前默认会进入 `twinMqtt` 并按控制值处理。

## 新增 Twin 订阅对象类

检查清单：

- 确认对象类是否进入 Redis `RedisKey.CLASS_NAME`。
- 确认 `InfoCodeConfigService#getClassCodesForTwinSubscribe` 是否返回该 classCode。
- 如果 classCode 包含 `&`，确认只取前缀是否符合 Twin 订阅规则。
- 确认 `MqttSubscribe` 是否需要补充 `infoCode` 级订阅。
- 在边缘启动日志中检查 `iot边缘端创建mqtt订阅` 的对象类列表。
- Twin 注册失败时检查 `DtpTwinClient#addComputeSubscribe` 返回值和异常日志。

## 新增点位字段

检查清单：

- `FeedbackInfoMsgMqttVO` 增加字段和必要的 `@JsonAlias`。
- Excel 模拟发数字段是否需要同步。
- `MqttReceiveServiceImpl#twinMqtt` 是否需要补齐或校验该字段。
- `PushIOTEventListener` 是否需要从对象业务字段补齐。
- `IotDataCacheService#insert` 是否需要持久化或缓存该字段。
- SSE 和历史 WebSocket 兼容链路是否需要消费该字段。
- 运行日志是否需要该字段参与分类、展示或状态更新。

## 新增信息点二次计算

优先使用现有信息点配置和 `TriggerFormulaInfo`：

- 通过信息点配置维护触发信息点和目标信息点。
- 变量值从当前消息或 IOT 缓存读取。
- 公式由 Aviator 执行。
- 计算结果包装为新的 `FeedbackInfoMsgMqttVO`。

只有现有公式模型无法表达时，才在 `ComputeIotDataHandler` 增加专门逻辑。新增专门逻辑时要关注：

- 是否需要按 `objectId + infoCode` 加锁。
- 是否会改写原始 `dataValue`。
- 反馈值和控制值的状态条件是否不同。
- 推送和缓存是否包含原消息与扩展消息。
- 异常时是否应返回原消息。

## 新增业务消息类型

检查清单：

- 在 `BusinessTypeEnum` 新增枚举。
- 在 `DtpMeosServerEdgeService#dtpMeosServerEdge` 增加分支。
- 定义 `content` 的 DTO 或解析方式。
- 对 `content == null` 做保护。
- 明确下游是日志、SSE、页面刷新还是其他边缘业务服务。
- 增加必要错误日志。
- 如果消息来源是控制组，文档只描述接收和转交，不维护控制动作执行设计。
- 如果消息写入运行日志，同步运行日志设计文档。

## 新增 SSE 推送

检查清单：

- 判断是所有点位变化都要推，还是只针对特定 classCode/infoCode。
- SSE 优先通过 `SseEmitterEventEnum` 和对应策略扩展。
- 确认 `webType` 是否仍是 `PARAM_SET`，还是需要 `PAGE_BROADCAST` 或 `INFO_EXTEND_SET`。
- 增加计数或日志，便于现场排查。
- WebSocket 已废弃，不新增 `WSClient`、`IWSMessageService` 或 `websocket_config` 过滤规则。

## 新增运行日志衔接

运行日志由运行日志模块维护，MQTT 侧只负责提供消费后的点位数据。新增日志衔接时：

- 必须在 `RunningLogHandle` 或运行日志服务中处理。
- 不要在 `MqttReceiveServiceImpl` 中写日志业务规则。
- 区分新版 `RunningLog` 和旧版 `RcRunningLog`。
- 确认控制组上报和运行组上报的数据来源边界。
- 同步 [运行日志程序设计文档](../running-log/README.md)。

## 修改配置或线程池

检查清单：

- 搜索 `paas.mqtt`、`MqttCounter`、`starter-mqtt` 和 `ConditionalOnMqttEnabled`。
- 确认 starter-mqtt 是否绑定当前配置键名。
- 压测或现场观察消费队列，避免盲目调大线程导致下游缓存、Redis、SSE 或历史兼容推送压力放大。
- 修改 `handler=drop` 时要评估断联重连风险。
- 多实例部署时保留 `client-id-random` 或提供明确的 clientId 隔离方案。

## 排查路径

| 问题 | 排查顺序 |
| --- | --- |
| 没有收到消息 | broker 配置、`receive-topics`、`paas.mqtt.enabled`、starter-mqtt、`mqtt_receivedTopic`。 |
| 收到但未进入业务 | topic 分支、payload 解析、空值保护、`dataValue` 是否为 `~`。 |
| 点位未落缓存 | `MqttServiceImpl`、`PushIOTEventListener`、信息点配置、`IotDataCacheService#insert`。 |
| 前端未刷新 | `ComputeIotDataHandler#sseStrategy`、SSE 订阅参数、SSE 策略；历史兼容场景再检查 `WsMessageServiceImpl`。 |
| 日志未生成 | 是否为控制值、`dataStatus` 是否完成、`result` 是否可映射、信息点枚举值是否配置、运行日志服务。 |
| 业务消息无效果 | `BusinessExternalPushDTO.businessType`、`content` 模型、`DtpMeosServerEdgeService` 分支和异常日志。 |

## 禁止事项

- 不在 SaaS 侧新增 MQTT 接入实现。
- 不把控制动作执行逻辑写入 MQTT 接收服务。
- 不在 MQTT 接收层承载运行日志查询、分类和导出规则。
- 不绕过 `handlerMessage` 直接消费 MQTT 消息。
- 不让新 topic 依赖默认控制值分支，除非已经明确它就是控制值。
