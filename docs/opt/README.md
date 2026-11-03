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
