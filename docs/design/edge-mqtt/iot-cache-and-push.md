# IOT 缓存与推送

Twin 点位数据进入 `PushIOTEventListener` 后，由 `ComputeIotDataHandler` 完成信息点值加工、缓存写入、SSE 推送、历史 WebSocket 兼容推送和运行状态跟踪。该层已经脱离 MQTT broker 接入，但仍是边缘 MQTT 消息的主要消费结果。

## 信息点配置与对象信息补齐

`PushIOTEventListener` 在进入计算前先补齐两类上下文：

| 数据 | 获取方式 | 用途 |
| --- | --- | --- |
| 信息点配置 | `LocalRedisCacheHandler#getInfoCodeConfig(classCode, infoCode)` | 单位、枚举值名称、缩放、小数位、扩展公式等。 |
| 对象业务字段 | `LocalRedisCacheHandler#getObjectBusinessFeildAndSystemInfo(groupCode, projectId, objectId, null)` | `localId`、`localName`、`objType`、`modelType` 和日志分类依赖字段。 |

如果信息点配置不存在，仍可能走普通缓存和推送，但二次计算、单位和值名称会受影响。

## 二次计算判断

`ComputeIotDataHandler#checkComputeInfoCodeDataCondition` 负责判断是否进入二次计算：

| 场景 | 处理 |
| --- | --- |
| 手自动设定信息点切到自动 | 返回需要计算，用于后续清空强控状态。 |
| 信息点配置为空、值为空、状态为空、字符串值信息点 | 不做二次计算。 |
| 有 `scale` | 先对 `dataValue` 乘缩放系数。 |
| 有 `iotDecimal` | 按小数位四舍五入。 |
| 反馈值且状态不是正常 | 不做二次计算。 |
| 空气源热泵运行状态 | 返回需要计算，用于重取机组运行状态。 |
| 控制值且存在扩展公式 | 判断控制值顺序和状态后进入计算。 |

该方法会直接改写 `source.dataValue` 的缩放和小数位结果，下游使用的是加工后的值。

## 二次计算执行

`ComputeIotDataHandler#computeInfoCodeData` 使用 `objectId + infoCode` 维度的 `ReentrantLock` 保证同一点位计算串行：

1. 计数器 `mqtt衍生计算总数` 加一。
2. 手自动设定切到自动时，调用冷源运行监测策略清空强控状态。
3. 空气源热泵运行状态场景调用 `updateACAHCInfoValue` 重新获取状态。
4. 调用 `computeInfoCodeDataMap` 计算扩展信息点。
5. 如果扩展信息点和原信息点相同，只返回扩展结果。
6. 如果扩展信息点不同，返回原消息和扩展消息。
7. 计算异常时记录日志并返回原消息。

扩展信息点值由 `TriggerFormulaInfo` 驱动：

- `InfoCodeLinkControlHandler#checkTriggerFormulaInfo` 校验触发公式配置。
- 当前点位使用本次上报值，其他变量从 IOT 缓存读取。
- 使用 Aviator 执行 `triggerFormula`。
- 计算成功后复制原消息，替换 `infoCode`、`dataValue`、`dataStatus`、`invalid` 和 `type`。

## 缓存写入

`ComputeIotDataHandler#pushMessage` 对每条结果数据执行：

1. 根据信息点配置补齐 `unit`。
2. 当前信息点等于配置的信息点时，补齐 `dataValueName`。
3. 调用 `IotDataCacheService#insert(msgMqttVO, infoCodeCfg)` 写入边缘 IOT 缓存。

缓存写入是后续运行监测、Dashboard、SSE 策略和其他边缘查询能力读取实时点位的基础。其他模块读取 `FeedbackInfoMsgMqttVO` 不代表它们接入 MQTT，只代表它们消费边缘 IOT 缓存。

## 历史 WebSocket 兼容推送

`ComputeIotDataHandler#pushMessage` 仍会调用历史 `IWSMessageService`，但 WebSocket 已废弃，不再作为新增实时推送能力入口。当前仅把这段逻辑作为旧链路兼容和少量 SSE 转调边界记录。

历史链路只对以下数据触发：

| 条件 | 说明 |
| --- | --- |
| `infoCode == rcRunStatus` | 重计算运行状态需要推送。 |
| `type == FEEDBACK` | 反馈值需要推送。 |

历史兼容推送由 `WsMessageServiceImpl#pushMessage` 完成：

1. 默认 `webType=PARAM_SET`。
2. 遍历当前项目的 `WSClientCache.getProjectClients(projectId)`。
3. 如果旧客户端带 `functionType`，使用 `LocalCacheConstant.WEBSOCKET_CONFIG` 按 `classCode + infoCode` 过滤。
4. 发送 JSON 字符串。
5. 记录 `websocket发送总数{channelId}` 或 `websocket发送失败总数{channelId}`。
6. 如果 `webType` 是 `PAGE_BROADCAST` 或 `INFO_EXTEND_SET`，额外触发对应 SSE。

新增前端实时刷新能力应直接进入 [SSE 推送设计文档](../sse-push/README.md)，不要继续扩展 `WSClient`、`websocket_config` 或 `IWSMessageService`。

## SSE 推送

`ComputeIotDataHandler#sseStrategy` 在缓存和历史兼容推送后执行：

| 条件 | SSE 事件 |
| --- | --- |
| 所有点位变化 | `IOT_DATA_CHANGE` |
| 照明回路 `runStatus` | `LIGHT_LOOP` |
| 照明回路 `orderFailAlarm`、`onlineStatus`、`runStatus` | `LIGHT_SPACE_STATUS` |
| 照明回路 `manualAutoStatus`、`onlineStatus`、`orderFailAlarm` | `LIGHT_EQUIPMENT_STATUS` |
| 项目对象 `outdoorIlluminance` | `ILLUM_INFO` |

SSE 策略由 `SseHandlerContext` 分派，订阅类型当前主要使用 `SubscribeTypeEnum.LIGHT_MONITORING`。

## 运行状态跟踪

对于重计算运行状态 `rcRunStatus`：

- 推送成功后，如果值为 `2` 或 `3`，调用 `putRcRunStatusTracking`。
- 其他值调用 `removeRcRunStatusTracking` 清理跟踪标记。
- Redis key 由 `RedisKey.buildRcRunStatusTrackingHashKey(projectId, classCode)` 构造。
- hash field 为 `objectId`，value 为过渡状态值。

该标记服务于运行状态补偿，不属于 MQTT 订阅状态。

## 运行日志衔接

`PushIOTEventListener` 最后调用 `RunningLogHandle#handleLog(source)`：

1. 缺少 `classCode`、`infoCode`、`dataValue` 或值为 `~` 时返回。
2. 只处理控制值 `EnumIotDataType.CTRL`。
3. 调用 `RunningLogEdgeNowService#updateByTwinMsg` 更新新版日志状态。
4. 对完成状态、结果可识别、枚举值可识别的控制消息，构造旧版运行日志。
5. 人工控制日志会根据 `user` 查询用户名称。
6. 调用 `RunningLogEdgeService#saveLog` 写入日志。

日志写入是 MQTT 消费后的边缘业务结果，不代表 MQTT 模块维护日志展示和查询。

## 维护要点

- 信息点扩展公式变更时，要同步检查缓存读取变量、数据类型和计算结果信息点。
- `scale` 和 `iotDecimal` 会改写原始值，排查数据问题时需要确认 broker 原值和缓存值的差异。
- 历史 WebSocket 兼容链路不再扩展，新增前端刷新优先设计 SSE 事件和订阅过滤。
- SSE 新增场景应优先扩展 `SseEmitterEventEnum` 和对应策略，再从 `sseStrategy` 触发。
- 运行日志逻辑不要放进 MQTT 接收层，保持在运行日志或边缘日志处理服务中承接。
