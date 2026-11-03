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
| `switch.opt.runningLogQueue` | B1 | false | 热生效 | 运行日志上云改为本地有界队列（默认 1 万）+ 后台线程合批发送；失败写本地文件，恢复后按序重放；有积压时新消息排在积压之后，保证旧消息先到；单条发送失败而紧随其后的一条成功时，判定为毒消息移入 `<spool-file>.dead`（指标 `runningLog.dead`），不卡住重放；判定下游不可用后退避 30 秒，期间新消息只追加到本地文件。**⚠ 下游抖动时正常消息也可能被误判，`.dead` 文件需人工核对后补发** |
| `switch.opt.autoLogDedupe` | B1 | false | 热生效 | 自动日志建档按 (traceId, objectId, infoCode) 进程内去重 10 分钟，后续“未完成”帧不再做分类远程查询、加锁、查库 |
| `switch.opt.ctrlFrameOrderFix` | B1 | false | 热生效 | 修 16.9：同一控制指令“完成”帧已在缓存时，丢弃后到的帧（不计算、不写缓存、不推送；运行日志仍处理） |
| `switch.opt.mqttHealth` | B1 | false | 需重启 | 注册 MQTT 健康检查 `optMqtt`：超过 `opt.mqtt.health.max-silence-seconds`（默认 600）未收包为 DOWN。依赖 mqttIsolation 记录收包时间。**⚠ 必须与 mqttIsolation 同时打开**：只开 mqttHealth 时收包时间从不更新，10 分钟后健康检查恒为 DOWN；若存活探针用 `/actuator/health`，服务会被反复重启 |
| `switch.opt.realtimeCacheWrite` | B2 | false | 热生效 | MQTT 反馈值在任何换算之前写 `apc:iot_real_time_data:{objectId}:{infoCode}`（Twin 原始值，`"~"` 写离线标记，旧帧不覆盖）。契约见 contract-iot-real-time-data.md |
| `switch.opt.realtimeCacheRead` | B2 | off | 热生效 | `verify`：仍返回 Twin 结果，同时读缓存比对计数；`on`：Twin 当前值查询先读缓存，未命中按 ≤200 对象分片回源并回填，回源仍无效的点负缓存 60 秒 |
| `opt.realtime.stale-seconds` / `negative-seconds` | B2 | 120 / 60 | 需重启 | 缓存陈旧阈值（超过即回源）、负缓存时长 |
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
