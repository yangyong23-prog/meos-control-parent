# 边缘调用优化（opt/edge-call 分支）开关与配置

本目录只属于 `opt/edge-call` 优化分支。依据《边缘控制我方服务改造方案》第三节 B 项。

规则：

- 每个改造项一个 Nacos 开关 `switch.opt.*`，**默认关闭**；关闭时行为与改造前完全一致。开关读 Spring `Environment`，Nacos 刷新后立即生效（除非下表注明“需重启”）。
- 跨服务共享的 Redis key 一律用 `RedisCacheUtil` 写 `apc:` 开头的裸 key，不用 `@Cacheable`。
- 新逻辑放在各模块的 `opt` 包里；热点文件只做最小接入改动。

## 开关清单

| 开关 / 配置 | 改造项 | 默认 | 是否热生效 | 打开后的行为 |
|---|---|---|---|---|
| `switch.opt.metrics` | B0 | false | 热生效 | 进程内计数，每分钟输出一行 `[opt-metrics] 1min {...}` 日志后清零 |
| `switch.opt.asyncDiscardOnFull` | B0 | false | 热生效 | `@Async` 池满时丢弃任务并计数 `async.drop`，不再 CallerRuns 压回调用线程（MQTT 消费线程） |
| `opt.scheduling.pool-size` | B0 | 1 | 需重启 | >1 时 `@Scheduled` 使用该大小的独立线程池；≤1 不做任何改动 |
| `opt.xxl.executor-timeout` | B0 | 0 | 新注册的任务生效 | XXL 任务执行超时（秒），0 为不限制 |
| `switch.opt.mqttIsolation` | B1 | false | 热生效 | Twin 报文只解析一次；`"~"` 日志降为 debug、warn 每分钟一条；`handlerMessage` 结束在 finally 清理 ContextUtil（16.18）；记录收包时间和 `mqtt.recv` |
| `switch.opt.runningLogQueue` | B1 | false | 热生效 | 运行日志上云改为本地有界队列（默认 1 万）+ 后台线程合批发送；失败写本地文件，恢复后按序重放；有积压时新消息排在积压之后，保证旧消息先到；单条发送失败而紧随其后的一条成功时，判定为毒消息移入 `<spool-file>.dead`（指标 `runningLog.dead`），不卡住重放 |
| `switch.opt.autoLogDedupe` | B1 | false | 热生效 | 自动日志建档按 (traceId, objectId, infoCode) 进程内去重 10 分钟，后续“未完成”帧不再做分类远程查询、加锁、查库 |
| `switch.opt.ctrlFrameOrderFix` | B1 | false | 热生效 | 修 16.9：同一控制指令“完成”帧已在缓存时，丢弃后到的帧（不计算、不写缓存、不推送；运行日志仍处理） |
| `switch.opt.mqttHealth` | B1 | false | 需重启 | 注册 MQTT 健康检查 `optMqtt`：超过 `opt.mqtt.health.max-silence-seconds`（默认 600）未收包为 DOWN。依赖 mqttIsolation 记录收包时间。**⚠ 必须与 mqttIsolation 同时打开**：只开 mqttHealth 时收包时间从不更新，10 分钟后健康检查恒为 DOWN；若存活探针用 `/actuator/health`，服务会被反复重启 |
| `switch.opt.realtimeCacheWrite` | B2 | false | 热生效 | MQTT 反馈值在任何换算之前写 `apc:iot_real_time_data:{objectId}:{infoCode}`（Twin 原始值，`"~"` 写离线标记，旧帧不覆盖）。契约见 contract-iot-real-time-data.md |
| `switch.opt.realtimeCacheRead` | B2 | off | 热生效 | `verify`：仍返回 Twin 结果，同时读缓存比对计数；`on`：Twin 当前值查询先读缓存，未命中按 ≤200 对象分片回源并回填，回源仍无效的点负缓存 60 秒 |
| `opt.realtime.stale-seconds` / `negative-seconds` | B2 | 120 / 60 | 需重启 | 缓存陈旧阈值（超过即回源）、负缓存时长 |
| `switch.opt.pointSubscribe` | B3 | false | 热生效 | 应用就绪后（本地缓存已加载）在 Twin 专用主题（topic=`opt.subscribe.topic`，默认 `meos` → `twin/compute_meos`）上按点订阅：每 `opt.subscribe.sync-interval-minutes`（默认 30）分钟查询现有规则求差量，先删后加；读路径发现未订阅点连续 `opt.subscribe.auto-threshold`（默认 3）次回源 Twin 且有效时自动补订阅（记入 Redis set `apc:opt:subscribe:auto`）。原按类订阅（topic=external）不动 |
| `mqtt.topic.meos-compute-point.name` | B3 | 空 | 需重启 | 专用主题名，填 `twin/compute_meos`。必须与 `receive-topics` 同时配置，否则该主题报文会被当作控制值处理 |
| `switch.opt.meosTopicFullPipeline` | B3 | false | 热生效 | 关：专用主题报文只写共享缓存（需同时打开 realtimeCacheWrite）；开：同时按反馈值走完整处理。只有在 receive-topics 去掉按类主题后才可打开，否则同一点会处理两次 |
| `opt.subscribe.extra-points` | B3 | 空 | 下次同步生效 | 手工补充的订阅点，格式 `classCode:infoCode,classCode:infoCode` |
| `switch.opt.sseKeepalive` | B9 | false | 热生效（超时对新连接生效） | 新建 SSE 连接带 `opt.sse.emitter-timeout-minutes`（默认 30）超时，浏览器自动重连；每 `opt.sse.heartbeat-seconds`（默认 30）秒发注释心跳，写失败即回收；调用过 `GET /server-event/sse/ping?eventKey=` 的前端超过 `opt.sse.lease-seconds`（默认 60）秒未再 ping 即回收（拔网线场景） |
| `switch.opt.sseSpaceStatusCache` | B9 | false | 热生效 | SSE 空间带状态在对象模型新缓存 READY 后读缓存（同 listSpaceEquipment 的 READY 路径）+ 补标签，不再每拍查 RWD；未 READY 仍走原实现 |
| `switch.opt.ssePreFilter` | B9 | false | 热生效 | IOT_DATA_CHANGE / INFO_EXTEND_SET / RELEASE_INTERVENTION 按订阅者范围先过滤，再提交 SSE 线程池 |
| `switch.opt.sseCoalesce` | B9 | false | 热生效 | `opt.sse.coalesce-events`（默认 LIGHT_LOOP、LIGHT_SPACE_STATUS、LIGHT_EQUIPMENT_STATUS、LIGHT_DEMAND_CHANGE）的全量重算触发，同一 (事件, 项目, 订阅类型) 在 `opt.sse.coalesce-window-ms`（默认 3000）内只执行一次，最大延迟一个窗口 |
| `switch.opt.ssePushDedupe` | B9 | false | 热生效 | 定时全量推送内容与上次相同且距上次推送不到 `opt.sse.full-push-minutes`（默认 15）分钟时不推；有新连接加入该 eventKey 时立即推 |
| `executor.pool.sse.queueCapacity` | B9 | 1000000（bootstrap.yml） | 需重启 | 建议试点在 Nacos 覆盖为 10000；满时由原拒绝策略丢弃最老任务 |
| `switch.opt.feignPageFirstLimit` | B10 | false | 热生效 | FeignPageExecutor 不分页查询的首发由 size=Integer.MAX_VALUE 改为按 `page.size.limit`（默认 1000）分页，再按 total 拉剩余页、按页序拼接；下游不返回 total 且首页已满时退回原逻辑 |
| `opt.realtime.twin-parallelism` | B10 | 4 | 需重启 | B2 读路径回源 Twin 时 ≤200 对象一片，多片用独立小池并行（满时调用线程执行） |
| `opt.runninglog.queue-capacity` / `spool-file` / `spool-max-bytes` | B1 | 10000 / `./opt-spool/running-log.jsonl` / 50MB | 需重启 | 运行日志队列容量、本地重放文件位置和上限 |

## B0 试点站点建议的 Nacos 配置

```yaml
switch:
  opt:
    metrics: true
    asyncDiscardOnFull: true
opt:
  scheduling:
    pool-size: 4          # 需重启
  xxl:
    executor-timeout: 240
spring:
  task:
    execution:
      pool:
        queue-capacity: 500   # 需重启；原值 50
redis:
  pool:
    timeout: 3s               # 需重启；原值 60s
management:
  health:
    redis:
      enabled: true           # 健康检查纳入 Redis
```

发布检查项：`project.request-statistics.enabled`、`project.feign.request-statistics.enabled` 保持关闭（它们按请求写 Redis，开销大）。

以下属于运维操作，不在代码里：XXL 日志保留 3 天；停用 job_group=3 的“获取历史蓄冷蓄热标签”任务。

## B3 外部前置条件与上线步骤

1. 运维：Twin 的 Nacos `mqtt.topic.twin.sub` 增加 `'compute.meos':'twin/compute_meos'`（Twin 服务配置，非本仓）。
2. 运维：meos-control 的 `paas.mqtt.receive-topics`（见 application-paas.yml）增加 `twin/compute_meos`，同时配置 `mqtt.topic.meos-compute-point.name: twin/compute_meos`，重启。
3. 打开 `switch.opt.realtimeCacheWrite`、`switch.opt.pointSubscribe`；观察 `sub.sync`、`sub.autoAdded`、`mqtt.recv.point`，以及 `realtimeCacheRead=verify` 下 `rt.verify.cacheMiss` 是否下降。
4. 确认专用主题覆盖完整后，才考虑去掉按类主题并打开 `meosTopicFullPipeline`（另行评审，本 PR 不做）。

不做预热：读路径未命中会回源 Twin 并回填，首个同步周期内命中率偏低属正常。

## 度量指标名

| 指标 | 来源 | 含义 |
|---|---|---|
| `feign.<服务>.<Client#方法>` | B0 | 每分钟各下游、各 Feign 方法的调用次数 |
| `async.drop` | B0 | async 池满被丢弃的任务数（不受 metrics 开关控制，始终计数） |
| `mqtt.recv` / `mqtt.invalidValue` / `mqtt.parseFallback` | B1 | MQTT 收包数、`"~"`/空值条数、Jackson 解析失败退回 fastjson 的条数 |
| `mqtt.ctrlFrameDiscard` | B1 | 16.9 丢弃的乱序控制帧数 |
| `runningLog.sent` / `fail` / `drop` / `spoolDrop` / `replayed` | B1 | 运行日志队列发送、失败、队列满丢弃、重放文件满丢弃、重放成功条数（drop/fail/spoolDrop 始终计数） |
| `runningLog.autoDedupeHit` | B1 | 建档去重命中次数 |
| `rt.write` / `rt.write.olderSkipped` / `rt.write.duplicateSkipped` / `rt.write.error` | B2 | 写缓存次数、旧帧跳过、重复帧跳过、写失败（error 始终计数） |
| `rt.read.hit` / `miss` / `negative` / `twinCall` / `twinFail` | B2 | 读缓存命中、未命中、负缓存命中的点数；回源 Twin 次数、回源失败次数 |
| `rt.verify.match` / `mismatch` / `cacheMiss` / `bothEmpty` | B2 | 双读校验结果（始终计数）。验收：mismatch / (match + mismatch) < 0.1% |
| `sub.sync` / `sub.addPoints` / `sub.autoPending` / `sub.autoAdded` / `sub.error` | B3 | 订阅全量同步次数、同步新增点数、待补订阅点数、自动补订阅点数、同步失败次数（始终计数） |
| `mqtt.recv.point` | B3 | 专用主题收包数 |
| `sse.heartbeat` / `sse.heartbeatFail` / `sse.leaseExpired` | B9 | 心跳发送数、心跳失败回收的连接数、租约过期回收的连接数（后两者始终计数） |
| `sse.spaceStatus.cache` / `sse.preFiltered` | B9 | 空间带状态走缓存的次数、提交前被过滤掉的订阅者任务数 |
| `sse.coalesced` / `sse.pushSkipped` | B9 | 被窗口合并掉的触发次数、内容无变化跳过的推送次数 |
| `feign.page.limitedFirst` / `feign.page.noTotalFallback` | B10 | 首发按 limit 分页的次数、因下游无 total 退回原逻辑的次数（后者始终计数） |
