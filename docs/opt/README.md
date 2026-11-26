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
| `switch.opt.feignPageFirstLimit` | B10 | false | 热生效 | FeignPageExecutor 不分页查询的首发由 size=Integer.MAX_VALUE 改为按 `page.size.limit`（默认 1000）分页，再按 total 拉剩余页、按页序拼接；下游不返回 total 且首页已满时退回原逻辑。**⚠ 最后打开**：分页依赖下游排序稳定、两页之间数据不变，否则会重复或漏行。先取 `verify`（仍返回原逻辑结果，另按分页查一次比对条数，指标 `feign.page.verifyMatch` / `verifyMismatch` / `verifyError`；下游查询量约翻倍），观察 `verifyMismatch` 持续为 0 后再取 `true`。行内有 id 字段时同时比对 id 集合（重复或漏行即使条数相同也算不一致）。verify 期间每个请求多查一遍下游，只在试点站点短期打开 |
| `opt.realtime.twin-parallelism` | B10 | 4 | 需重启 | B2 读路径回源 Twin 时 ≤200 对象一片，多片用独立小池并行（满时调用线程执行） |
| `switch.opt.twinWriteDiff` | B8 | false | 热生效 | 仅 InnerSchdule 壳配置定时同步内：Twin twc/conf 写入（saveList、saveClassConfs、deleteByClassConf、saveExclusions、deleteByConf、saveStartIntervals、reviseObjectInSys）按项记账 `apc:twinWrite:{group}:{projectId}:{identity}`，全部项不变且距上次成功写入不到 `opt.twin-write.full-rewrite-hours`（默认 24）小时不写；写入结果校验 isSuccess，失败抛异常、不记账、下一轮重写；设备频率快照改存合并后的全量、删除时移除。**⚠ 试点建议 `opt.twin-write.full-rewrite-hours=1`**：原逻辑每 5 分钟全量重写，Twin 侧配置被改坏或丢失时能自愈；默认 24 小时会让自愈最长推迟一天 |
| `switch.opt.lightingSendDiff` | B8 | false | 热生效 | 照明反馈调节（空间级、回路级）指令与上次相同不重发，距上次下发满 `opt.lighting.resend-rounds`（默认 6 轮 = 30 分钟）强制重发；下发失败（调用异常、无响应或 Twin 返回非 00000 的 respCode）时本次指令下一轮重发，指标 `lighting.sendFail` |
| `switch.opt.scheduleSeasonMemo` | B10 | false | 热生效（对下一次下发生效） | 时间表下发（distributionSchedule）一次调用内，冷热站出水温度点的季节查询 `querySeasonByTime` 按日期只调一次，结果复用；指标 `memo.scheduleSeason.hit` |
| `switch.opt.coldSourceGroupParallel` | B10 | false | 热生效 | 冷源首页卡片「大小冷机运行台数」：各系统的 `queryAcccccGroupBySystemId`（下游无批量接口）用 ttlExecutor 并行查询，结果按系统合并，与逐个查询一致；任一失败按原样抛出 |
| `switch.opt.systemGraphJumpBatch` | B10 | false | 热生效 | 系统图交互配置（buildInteractiveConfig）：候选跳转对象原来逐个 `getSystemGraph`（每个一次 SQL）直到可跳转，改为按 classCode 一次 IN 查询后按原顺序挑选；同一张图内按 classCode 查询的对象列表（`listObject`）只查一次。指标 `systemGraph.jumpBatch.candidates`、`memo.systemGraphObjects.hit` |
| `switch.opt.weakPowerRelationBatch` | B10 | false | 热生效 | 弱电运行监测按关系下钻（queryChildrenByDeviceType）：原来每个设备×每种关系查一次 RWD；改为对同一层将要下钻的兄弟设备按关系类型合批（objFrom 多个，200 一批），按 relObjIds 拆回各设备后递归照常进行。无法归属或预取失败时退回逐个查询。指标 `weakPower.relationBatch.saved`、`.fallback`、`.error` |
| `switch.opt.clientSystemRelationBatch` | B10 | false | 热生效 | ClientServerHandler 系统关联对象：`querySystemRelationBySystemClassCode` 各系统的空间、`querySystemRelationObjectsByAccc` 各系统的管网/竖井/空间，原来每个系统各查一次 RWD，改为每类一次（200 个一批）后按 relObjIds 拆回各系统；无法归属或失败时退回逐个系统查询。指标 `clientRelation.batch.saved`、`.fallback`、`.error` |
| `switch.opt.monitorSpaceById` | B10 | false | 热生效 | 运行监测按系统查设备（AbstractRunningMonitorServer.convertEquipmentInfo）：原来每次请求先查全项目所有空间再按设备所在空间取用，改为先查设备→空间关系，再按这些空间 id 查（条件同为 classCode=space、valid=1，500 个一批） |
| `switch.opt.forceInfoBatch` | B10 | false | 热生效 | 运行监测强制信息点取值（CE RunningMonitorServiceImpl.getClassObjectForceInfoCodeValue）：同一 (classCode, 图, 关系, 方向) 的关系查询只查一次；每个 (对象, 信息点) 的 Twin 当前值先登记，循环结束后按对象合批查询（≤200 对象一片）再回填。指标 `pointBatch.saved`、`memo.forceInfoRelation.hit` |
| `switch.opt.shellConfigRelationMemo` | B10 | false | 热生效 | 壳配置按场景取值（ShellConfigServiceImpl.getConfigValueByScene → getInstanceByValSource）：同一请求内相同的 existsObjects / countObject / 关系 queryObjectList 只查一次；列表结果每次给独立副本（调用方会 removeIf）。指标 `memo.shellConfigRelation.hit` |
| `switch.opt.projectFromRwd` | B15 | false | 热生效 | `MidPermissionClientService#getProject`（城市编码、项目名，约 9 处调用，含天气 queryCitiCode）改读 dtp-rwd 项目对象（classCode=project，id=projectId）：项目名取 localName，为空取 name；城市编码取 infos.city。读不到（对象不存在、city 或名称为空、调用失败）退回调一次原 getProject，结果同样缓存。进程内存缓存 `opt.project.refresh-minutes`（默认 360）分钟，重读失败继续用旧值；应用就绪后后台预读 project.id。从 dtp-rwd 得到的结果没有省/市/区名称，运行分析报告下载这三格为空。指标 `project.load.rwd`、`project.load.midPermission`、`project.cache.hit` |
| `switch.opt.operatorNameLocal` | B15 | false | 热生效 | 边缘前端在请求头 `opt-user-name` 里带主框架登录姓名（URL 编码）；收到后按请求头 userId 记到进程内存和 Redis hash `apc:opt:operator_name`（姓名不变不重写）。`getUserNameByUserIds`（运行日志回执 RunningLogHandle、RunningLogEdgeServiceImpl、RcUserServiceImpl）和 `queryPersonNameOrUserNameByUserIds` 只从这里取，不再调 mid-permission `getUserInfoListByUserIds`；取不到时照旧写 userId。需配合前端 PR（meos-control-web opt/edge-call）。姓名只用于显示，不参与权限判断。指标 `operatorName.write`、`.hit`、`.miss` |
| `opt.runninglog.queue-capacity` / `spool-file` / `spool-max-bytes` | B1 | 10000 / `./opt-spool/running-log.jsonl` / 50MB | 需重启 | 运行日志队列容量、本地重放文件位置和上限 |

## 运行标准过渡（2026-11-18，改动清单 B1–B4、B6）

背景：运行标准和配置改由 ids-mpc-operation-standards-server 管理，dtp-meos 读取；meos-control 只保留监测和手动控制，配置管理在新服务联调通过前保留。下表编号对应《运行标准服务对 meos-control 配置的覆盖评估》的改动清单，与上面第 2 波的 B 编号无关。全部只在边缘包（meos-control-server-edge）生效，云端不注册这些拦截器和接口。

| 开关 / 配置 | 改动 | 默认 | 是否热生效 | 打开后的行为 |
|---|---|---|---|---|
| `switch.opt.standardsMode` | B1 | false | 热生效 | `GET /standards/mode?projectId=` 返回 `{projectId, mode: STANDARD/LEGACY, readOnly, source, error}`。判定与 dtp-meos 一致：`demand_custom_config` 中 project_id = object_id = 项目、module_code = CALCULATE、config_type = STANDRAD_CONFIG 有记录即 STANDARD。关闭时恒为 LEGACY 且不查库 |
| `switch.opt.standards.mode-source` | B1 | db | 热生效 | `db` 读上表；`list` 只看 `switch.opt.standards.switched-projects`（逗号分隔），用于拆库后或联调时人工指定 |
| `switch.opt.standards.mode-cache-seconds` | B1 | 60 | 热生效 | 判定结果按项目缓存秒数；`POST /standards/mode/evict` 立即清空。查库失败按 LEGACY 处理，错误写在 `error` 字段，同样缓存 |
| `switch.opt.standardsReadonly` | B2 | false | 热生效 | 需同时开 standardsMode。项目为 STANDARD 时，命中只读清单的请求不进 Controller，返回 HTTP 200 + `{code: "A0403", message: "由云端运行标准管理"}`；未切项目照旧可写；取不到 projectId 的请求放行（计数 `standards.readonly.noProject`） |
| `switch.opt.standards.readonly-paths` | B2 | 见下 | 热生效 | Ant 风格、逗号分隔，覆盖默认清单 |
| `switch.opt.dtpMeosCallCounter` | B3 | false | 热生效 | dtp-meos 调我方的 5 个接口（`/demand/config/listConfigsByInstance`、`/demand/listInstanceConfig`、`/outside/intervention/taskConfigList`、`/external/demand/config/listConfigResultsByInstance`、`/valueSelection/currentApplyConfig`）按（接口, 项目, 调用方 browser/service）计数：Redis hash `apc:opt:dtpMeosCall:{yyyyMMdd}`（保留 40 天）+ `apc:opt:dtpMeosCall:last`（最近调用时间）；`GET /standards/dtpMeosCalls?days=7` 查看。接口本身不改 |
| `switch.opt.standardsDisplay` | B4 | false | 热生效 | `POST /standards/display/computeResult`（按日，入参 instanceIds、instanceGroup、instanceType、standardDate 或 beginDate/endDate、standardTarget = TDB/CO2/PRESS）和 `POST /standards/display/computeResultDetail`（按时间点，入参 standardTime 或 beginTime/endTime、standardType）转查 dtp-meos `/calculate/listStandardComputeResultService`、`/calculate/listStandardComputeResultDetailService`，返回其 data 原样。projectId 以请求头为准。关闭时返回 A0403“未开启”。不依赖 mpc-common-edge |
| `switch.opt.localRedis` | 去 Redis | false | **需重启** | 只允许 -edge 服务。meos-control 私有 Redis 数据改存进程内存，Redisson/`@RedisLock` 改进程内锁；与 dtp-meos 共享的键（手自动、干预、运行模式、强制、控制值、实时值）仍读写 Redis。`opt.local-redis.shared=false` 完全不连 Redis。详见 [local-redis.md](local-redis.md) |

B2 默认只读清单（与前端 meos-control-web `src/apis/standards.js` 一致）：`/demand/addInstanceConfig`、`/demand/addDemandConfig`、`/demand/clearSettings`、`/demand/referenceSettings`、`/demand/copyConfigs`、`/demand/blacklist/addRules`、`/demand/instanceGroup/add`、`/demand/instanceGroup/edit`、`/demand/instanceGroup/copy`、`/demand/template/applyTemplateByDate`、`/demand/template/clearCalendar`、`/valueSelection/saveConfig`、`/shellConfig/mpcAgentCfg`，以及 2026-11-19 追加的壳配置 `/shellConfig/save`、`/shellConfig/changeAlgorithmConfig`、`/shellConfig/changeLtsmConfig` 和干预 `/interventionTaskConfig/saveOrUpdate`、`/interventionTaskConfig/remove`、`/intervention/config/saveOrUpdate`（Blake 11-19 定：安防/通风/排油烟需求、干预任务、壳配置都由运行标准服务和 dtp-meos 负责；前三类需求经 `/demand/addDemandConfig` 等通用接口写入，已在清单内）。其中需求类多数只在云端包存在，边缘包实际命中的是价值选择、MPC 智能体、壳配置和干预这几类。照明场景（`/demand/scene/*`）不在清单内。解除干预 `/intervention/release`、手自动切换等手动控制操作不拦（B5 改走 dtp-meos 接口，待 dtp-meos 提供）。

B6 表归属见 [table-ownership.md](table-ownership.md)。

建议的打开顺序：B3（先攒调用证据）→ B1（核对 `/standards/mode` 与 dtp-meos 实际行为一致）→ B4（前端 W3 联调）→ B2（新服务联调通过、项目正式切换时）。

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
| `twinWrite.skipped` / `twinWrite.written` / `twinWrite.fail` | B8 | 壳配置同步跳过、实际写入、写入失败的调用数（fail 始终计数）。验收：配置不变时每轮 written 接近 0 |
| `lighting.sendSkipped` | B8 | 照明反馈调节未重发的指令数 |
| `standards.mode.load` / `standards.mode.error` | 运行标准 B1 | 判定查库次数、查库失败次数（error 始终计数） |
| `standards.readonly.blocked` / `standards.readonly.noProject` | 运行标准 B2 | 被拦截的配置写入次数、取不到项目放行的次数（始终计数） |
| `dtpMeosCall.<接口>` / `dtpMeosCall.error` | 运行标准 B3 | 5 个接口每分钟调用次数、计数写 Redis 失败次数（始终计数） |
| `standards.display.result` / `standards.display.detail` | 运行标准 B4 | 转查 dtp-meos 解析结果的次数 |
