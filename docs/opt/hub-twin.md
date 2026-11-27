# 孪生数据源切换：当前值与实时推送改读 meos-hub（switch.opt.twinSource）

背景：边缘盒子架构目标是 meos-hub 替代 dtp-twin。4.91 上 dtp-twin 从 2026-11-20 起不再写实时数据，meos-hub 孪生计算正常。可行性评估见项目文档《meos-control 改读 meos-hub 孪生数据 · 可行性评估》。

## 开关

`switch.opt.twinSource`，热生效，只在边缘模块（meos-control-common-edge / compute-edge）接入，云端不受影响。

| 取值 | 当前值查询（`/data/queryTwinDataOnMoment` 的 7 个调用方） | MQTT 实时推送 |
|---|---|---|
| `twin`（默认） | 与改造前一致，查 dtp-twin | 与改造前一致 |
| `verify` | 返回 dtp-twin 结果；另查 meos-hub 逐点比对，计数 `hub.verify.*`，不一致样例每分钟一条 info 日志 | meos-hub 主题只计数 `mqtt.recv.hub` |
| `hub` | 改查 meos-hub `POST {context}/edge2cloud/point/getPointTwin`，≤200 对象一片 | 配置了 meos-hub 主题时：meos-hub 报文写实时缓存并按反馈值处理，dtp-twin 反馈值主题（含 B3 专用主题）不再处理；未配置时仍用 dtp-twin 推送 |

只处理“当前值”查询；带 `twinTime` / `unit` 的请求仍走 dtp-twin。与 B2 `realtimeCacheRead` 可叠加：缓存未命中的回源换成上表的数据源。

## 不在本次范围（仍走 dtp-twin）

- 历史：`/data/queryTwinDataHistory`（meos-hub 历史接口要浏览器会话，已提需求）。
- 状态统计：`/run/watch/agg/*`、`/state/statis/manage/*`、`/home/watch/agg/coolHeatSys`（后续在 meos-control 内基于当前值实现）。
- 控制与配置：`/twc/*`、`/reset/*`、`/subscribe/rule/*`（TWC 控制层归 dtp-meos）。

## 字段映射

getPointTwin 行 → `DtpTwinIotData`：

| meos-hub | meos-control | 说明 |
|---|---|---|
| objectId / classCode / infoCode | 同名 | objectId 与 dtp-rwd 同一套，原样透传（4.91 已实测） |
| val | dataValue | |
| time（ISO，UTC） | dataTime | 转 `opt.hub-twin.zone` 下的 `yyyyMMddHHmmss` |
| val 为空 / `~` / transfiniteType 不是 normal | dataFlag=2 | 否则 1；TWC、规则点没有数据标记，按有效 |
| onlineStatus=offline | dataStatus=2 | 否则 1 |
| — | dataIndate | `opt.hub-twin.data-indate`，默认 0（不过期） |
| — | forced | 留空，meos-hub 没有强制状态 |

MQTT 原生载荷 `{projectId, objectId, classCode, reportTime, samples:[{infoCode, val, time(ms), validity, ...}]}` → 每个 sample 一条 `FeedbackInfoMsgMqttVO`；validity 不是 VALID 时 invalid=1、dataStatus=2，不进反馈值处理。

## 已知限制（需 meos-hub 补）

1. getPointTwin 不返回规则合成点（只有孪生规则、没有绑点的计算点）。hub 模式下这类点取不到。
2. 请求必须显式带信息点；`infoCodeSet` 为空时 meos-hub 只回直采绑点（计数 `hub.read.emptyInfoCodes`）。
3. 盒内业务 MQTT 出口 `hub.exit.mqtt-topic` 需在 meos-hub 的 nacos 配置后，meos-control 才能收到推送。

## 上线步骤（4.91）

1. nacos 配 `inner-url.meos-hub.service: http://meos-hub-server-edge:8080`，重启。
2. `switch.opt.twinSource: verify`，打开 `switch.opt.metrics`，看 `hub.verify.*`。dtp-twin 无数据时 verify 只能看到 `twinMiss`，可直接进下一步并人工核对运行监控页面。
3. `switch.opt.twinSource: hub`，冒烟运行监控、首页卡片、控制下发前的状态校验。
4. meos-hub 配好业务 MQTT 出口后：`mqtt.topic.meos-hub-twin.name` 填该主题并加入 `paas.mqtt.receive-topics`，打开 `switch.opt.realtimeCacheWrite`，重启。建议同时关闭 `switch.opt.pointSubscribe`（hub 模式下不再需要向 dtp-twin 订阅）。

回退：`switch.opt.twinSource: twin`，热生效。
