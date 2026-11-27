# 边缘盒子已移除服务的调用开关（opt/edge-scope）

依据：2026-11-17 Blake 决定标准边缘架构去掉 yiheng 风险事件链路、energy-ddp、ai-au；清单见项目文件 `edge-deploy/meos-control去除yiheng与energy-ddp调用清单.md`。

规则同 [README](README.md)：开关默认关闭，关闭时行为与改造前一致；只在边缘盒子 Nacos 打开。common 模块云边共用，所以不直接删代码。

| 开关 | 默认 | 是否热生效 | 打开后的行为 | 对应清单项 |
|---|---|---|---|---|
| `switch.opt.edgeNoYiheng` | false | 查询与 MQTT 热生效；Kafka 监听需重启 | `ExceptionEventClientService` 的风险事件、风险实例树查询直接返回空（`/dangerEvent/*`、SSE `DANGER_EVENT` 不再调 yiheng）；MQTT `event-change`、`danger-event` 报文丢弃；Kafka `yiheng-cloud-business-topic` 监听不启动 | Y1、Y2、Y5、Y6 |
| `switch.opt.edgeNoEnergyDdp` | false | 热生效 | `POST /dashboard/getSystemEfficiency` 不再查 energy-ddp 后评估配置，等级区间为空，效率值照常从孪生计算 | E1 |
| `switch.opt.edgeNoAiAu` | false | 热生效（xxl 任务注册在启动时） | 冷机策略 `fetchSupportAi` 直接返回 false，不调 ai-au；xxl 任务 `historyStorageHandler`（蓄冷蓄热标签，调 ai-au 算法服务）启动时不再注册、已有的被删除，即使被触发也直接跳过 | ai-au 残留调用 |

直接删除（无调用方）：`RcSaasClientStandby`（写死越秀云端地址，边缘模块）、`TaskApiClient`（Y7）。

异常事件 Y3/Y4 仍由原有 `switch.yihengServer` 控制；边缘盒子同时把它关掉。注意它也会挡住 `RcSaasClient.mapTaskInfo`。

边缘 Nacos 建议同时去掉 `mqtt.receive-topics` 里的 `event-change`、`danger-event` 主题，避免订阅。
