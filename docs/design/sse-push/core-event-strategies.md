# 核心事件策略

SSE 策略分布在 `common` 和 `local/meos-control-common-edge`。公共策略服务云端和边缘通用场景，边缘策略承接本地 IOT、照明、干预、页面播报和业务配置等能力。

## IOT_DATA_CHANGE

边缘策略：

```text
local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/sse/strategy/iot/IotDataChange.java
```

触发条件是 `FeedbackInfoMsgMqttVO`。处理流程：

1. 校验 condition 类型。
2. 校验项目 ID 是否匹配订阅参数。
3. 如果订阅参数没有 `claasCode2objectIds`，按订阅类型计算对象范围。
4. 通过 `checkClaasCode2objectIds` 过滤对象。
5. 将 `FeedbackInfoMsgMqttVO` 转为 `ObjectIotData`。
6. 从 `LocalRedisCacheHandler` 获取信息点配置，补齐单位和值名称。
7. 构造 `eventType=IOT_DATA_CHANGE` 的 `SseEmitterObject`。

对象范围来源：

| `SubscribeTypeEnum` | 来源服务 |
| --- | --- |
| `AIR_TERMINAL` | `AirTerminalService#mapClaasCode2objectIds` |
| `VENTILATION` | `VentilationService#mapClaasCode2objectIds` |
| `SECURITY` | `SecurityService#mapClaasCode2objectIds` |
| `LIGHT_MONITORING` | `LightingService#mapClaasCode2objectIds` |
| `ELEVATOR` | `ElevatorService#mapClaasCode2objectIds` |

## CLOUD_IOT_DATA_CHANGE

公共策略：

```text
common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/CloudIotDataEvent.java
```

该策略用于云端或周期性 IOT 批量刷新。它从 `eventParamMap` 中读取事件参数：

| 参数 | 说明 |
| --- | --- |
| `templateType` / `templateTypes` | 信息点模板类型。 |
| `claasCodeAndObjectIds` | classCode 到对象 ID 集合。 |
| `objectIdAndInfoCodes` | 指定对象和信息点，存在时优先按点查询。 |
| `groupCodes` | 信息点分组编码。 |

主要流程：

1. 从订阅类型解析运行监测类型。
2. 校验事件参数。
3. 如果指定 `objectIdAndInfoCodes`，调用 `BizDtpRwdService#queryIpDataOnMoment` 查询实时点位。
4. 否则通过 `InfoCodeGateway` 组装信息点聚合。
5. 填充 IOT 数据。
6. 批量读取 `RcInfoCodeCfg` 补齐单位和值名称。
7. 返回 `ObjectIotData` 列表。

## PAGE_BROADCAST

边缘策略：

```text
local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/sse/strategy/iot/PageBroadcast.java
```

支持两种 condition：

| condition | 过滤规则 |
| --- | --- |
| `FeedbackInfoMsgMqttVO` | 项目匹配，并且订阅类型等于 `modelType`。 |
| `RunningLogVO` | 读取 `eventParamMap[PAGE_BROADCAST]`，按一级分类和可选二级分类过滤。 |

`RunningLogVO` 的事件参数为：

| 字段 | 说明 |
| --- | --- |
| `firstLevelCategory` | 日志一级分类，必须匹配。 |
| `secondLevelCategory` | 日志二级分类，可为空。 |

## INFO_EXTEND_SET

边缘策略：

```text
local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/sse/strategy/iot/InfoExtendSet.java
```

condition 为 `FeedbackInfoMsgMqttVO`：

1. 校验项目匹配。
2. 按 classCode/objectId 过滤。
3. 返回原始 `FeedbackInfoMsgMqttVO`。

该事件常用于对象扩展设定、控制台需求变化和页面局部刷新。

## RELEASE_INTERVENTION

边缘策略：

```text
local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/sse/strategy/intervention/ReleaseIntervention.java
```

condition 为 `ReleaseInterventionDTO`：

1. 要求 `objectId` 和 `infoCode` 非空。
2. 按 classCode/objectId 过滤。
3. 返回干预解除 DTO。
4. 使用 `eventKey + ": RELEASE_INTERVENTION"` 作为锁 key。

## 异常与风险事件

公共策略目录中维护异常和风险相关策略：

| 事件 | 策略示例 |
| --- | --- |
| `ABNORMAL_EVENT` | `AnomalousEvent` |
| `ABNORMAL_STATE` | `AbnormalState` |
| `ABNORMAL_DATA_LIST_DIVIDE_SYSTEM` | `AbnormalDataListDivideSystem` |
| `DANGER_EVENT` | `DangerEventPush` |
| `DANGER_EVENT_MONITORING_SITUATION` | `DangerEventMonitoringSituationsPush` |

触发源可以是 MQTT、Kafka 或定时任务。策略负责按订阅参数返回前端需要的数据。

## 领域统计策略

领域统计策略大多继承 `AbstractSseEventStrategyTemplate`，按订阅参数查询对应模块数据：

| 领域 | 策略目录 |
| --- | --- |
| 空调末端 | `common/.../sse/strategy/airterminal` |
| 照明 | `common/.../sse/strategy/light` 与 `local/.../sse/strategy/light` |
| 安防 | `common/.../sse/strategy/security` |
| 停车 | `common/.../sse/strategy/parking` |
| 客流 | `common/.../sse/strategy/passengerflow` |
| 通风 | `common/.../sse/strategy/ventilation` |
| 厨房 | `common/.../sse/strategy/kitchen` |

## 维护要点

- 新策略要同时考虑定时刷新和条件触发两种入口是否需要支持。
- 策略返回 `data=null` 或空集合时不会发送。
- 对象范围过滤要复用 `checkClaasCode2objectIds`。
- 需要订阅参数时，优先通过 `eventParamMap` 解析并校验。
