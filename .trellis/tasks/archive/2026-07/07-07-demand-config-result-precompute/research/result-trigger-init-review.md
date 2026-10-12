# Research: result-trigger-init-review

- Query: 专项审查 B：需求配置结果预计算触发入口、事件监听、XXL-JOB 初始化和调试接口安全边界。
- Scope: internal
- Date: 2026-07-09

## Findings

### Files Found

- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java` - 汇总 DTX、配置、模板、场景、黑名单/屏蔽事件并触发结果刷新/删除。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/listener/CacheDtxPublishChangedListener.java` - DTX / DTX rule / 实例配置变更后触发 RWD 缓存刷新。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java` - 结果刷新、按模板类型初始化、DTX 范围刷新、屏蔽标识更新和性能追踪。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRepositoryImpl.java` - 结果 upsert、删除、关闭快照、屏蔽更新。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java` - 配置保存/清空/取消引用、模板/场景/实例配置事件级联处理。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigRepositoryImpl.java` - 配置新增/编辑/删除事件发布。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/scene/DemandSceneRepositoryImpl.java` - 场景新增/编辑/删除事件发布。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/instance/DemandInstanceGatewayImpl.java` - 黑名单/屏蔽事件发布。
- `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java` - 模板新增/修改/删除/应用事件发布。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/DemandConfigResultInitTaskHandler.java` - XXL-JOB 初始化 handler、参数过滤、上下文设置、模板类型串行刷新。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/param/DemandConfigResultInitTaskParam.java` - 初始化参数 DTO。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/BaseXXJob.java` - XXL-JOB 集团/项目遍历、分片和失败汇总。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/JobTask.java` - `@XxlJob("demandConfigResultInitTaskHandler")` 薄入口。
- `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/config/InitXxlJob.java` - 初始化任务注册为 none-schedule job。
- `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/debug/cache/DebugCacheController.java` - HTTP debug 初始化入口。
- `common/src/main/java/com/persagy/apccontrol/basic/controller/demand/DemandController.java` - 预计算结果查询接口。

### Related Specs

- `.trellis/spec/backend/demand-config-result-precompute.md` - 结果刷新、DTX、初始化、黑名单/屏蔽、读接口合同。
- `.trellis/spec/backend/logging-guidelines.md` - business class 日志和 `LogComponent` 约定。
- `.trellis/spec/backend/error-handling.md` - 异常不得静默吞掉。
- `.trellis/spec/guides/cross-layer-thinking-guide.md` - 事件、仓储、任务、接口跨层数据流检查。

### External References

- None. 本审查仅基于仓库内代码和 Trellis 规格；未使用外部文档。

## Must Fix

1. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java:820`-`838`
   - Status: 已解决。`DemandConfigGatewayImpl#onDemandInstanceConfigChangeEvent` 已先判断 `changeType == BLACKLIST`，屏蔽事件不会再进入原始配置删除逻辑。
   - Risk: 屏蔽空间事件会误删原始需求配置。该监听器没有判断 `DemandInstanceConfigChangeTypeEnum.BLACKLIST`，只要 `inInstanceIds` 非空就按模板类型删除配置；而屏蔽事件由 `DemandInstanceGatewayImpl.java:230`-`236` 发布，进入屏蔽的空间同样会带 `inInstanceIds`。
   - Trigger: 空间被加入屏蔽，但不是加入黑名单。
   - Suggestion: 该级联删除逻辑只处理 `BLACKLIST`，`SHIELDED` 必须直接返回；屏蔽只应由 `DemandConfigResultRefreshEventListener.java:170`-`173` 更新 `is_shielded`。

2. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:456`-`483`
   - Status: 已解决。批量刷新现在以模板/场景为刷新边界查询已有结果业务键，再删除不在本次有效实时结果中的旧结果；空实时结果会清理该边界旧结果。
   - Risk: 按模板/场景/模板类型的批量刷新只 upsert 实时返回的聚合结果，不删除本次范围内实时链路未返回的旧结果；`aggregates` 为空时 `467`-`471` 直接返回，历史结果继续留在主表和开放快照。局部刷新 `refreshInstances` 在 `141`-`145` 有未命中删除保护，但批量刷新没有。
   - Trigger: DTX 物理世界变更导致部分需求空间不再属于需求范围、场景编辑后不再有效、初始化重跑时实时链路不再返回某些实例，或模板/场景全量刷新得到空结果。
   - Suggestion: `refreshByQueryParam` 需要像 `refreshInstances` 一样基于请求范围构造 missing delete；对 `autoFillEmptyInstanceIds=true` 的全量场景，要能按 templateId/sceneId 清理旧结果，避免“无实时结果但旧结果仍可查”。

3. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:60` and `common/src/main/java/com/persagy/apccontrol/basic/model/message/BaseEvent.java:110`
   - Risk: DTX 刷新锁 key 在 `executeWithLock` 初始化事件上下文之前生成，`CacheKey.Lock.DtxPublish.demandConfigResult()` 读取的是当前 async 线程里的 `ContextUtil.getProjectId()`；若 async 线程没有 TTL 上下文或残留旧上下文，锁 key 会变成空项目或错误项目，导致跨项目互相中断/串行，甚至同项目锁失效。
   - Trigger: DTX 发布事件进入 `@Async` listener；线程池未正确传播或复用上下文。
   - Suggestion: 先从 event message 初始化上下文，再构造 lock key；或让 lock key 显式接收 `event.getProjectId()`，不要在 listener 入口依赖线程上下文。

4. `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/DemandConfigResultInitTaskHandler.java:183`-`189` and `common/src/main/java/com/persagy/apccontrol/basic/utils/ObjectMapperUtils.java:102`-`110`
   - Risk: XXL-JOB 参数 JSON 解析失败时返回 null，`defaultParamIfNull` 把它转换为空参数；按规格空参数表示全量初始化，因此一个拼错的 JSON 或非法 enum 会误触发全量。
   - Trigger: 运维手动传入格式错误的 job param，例如 `templateTypes` 枚举拼错或 JSON 不合法。
   - Suggestion: 初始化 job 参数解析失败应 fail fast 并返回 XXL-JOB failure；只有真正 blank 参数才表示全量。

5. [Confirmed] `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/debug/cache/DebugCacheController.java:43`-`64` and `common/src/main/java/com/persagy/apccontrol/basic/task/BaseTask.java:335`-`348`
   - Decision: No code change. Debug 初始化接口允许空上下文触发全集团全项目初始化，这是调试/运维入口的预期能力。
   - Risk: debug 接口无方法级权限/环境保护，且没有集团/项目上下文时 `BaseTask` 会把 group/project 过滤解析为 null，从而执行全集团全项目初始化；请求体只能过滤 templateTypes，不能限制 group/project。
   - Trigger: 调用 `POST /debug/cache/demandConfigResult/init` 时缺少 group/project header，或网关/权限配置误暴露 debug 路径。
   - Suggestion: 保持现有能力，依赖 debug/运维入口权限控制。

## Should Fix

1. `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/debug/cache/DebugCacheController.java:47`-`64` and `common/src/main/java/com/persagy/apccontrol/basic/task/BaseTask.java:232`-`256`
   - Risk: debug 接口只返回 `!allFailed`，部分项目失败但至少一个项目成功时仍返回成功；调用方无法从接口结果判断是否需要重跑失败项目。
   - Trigger: 多项目 debug 初始化中单个项目刷新异常。
   - Suggestion: 返回项目级成功/失败摘要；至少在任一项目失败时返回失败状态或错误信息。

2. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:119`-`124` and `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:537`-`539`
   - Risk: 模板 UPDATE 被跳过，但规格要求“模板保存、启用、停用后”刷新模板下所有实例。当前 UPDATE 覆盖名称和启停状态，启用时还可能触发应用关系变化；跳过需要有明确业务证明，否则可能漏算或保留禁用模板结果。
   - Trigger: 修改模板启停状态或更新模板元数据后调用预计算结果查询。
   - Suggestion: 对启停状态变化刷新或清理结果；若确认为只改名称不影响结果，应在事件数据里区分字段级变化并写明合同。

3. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:122`-`124` and `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:689`-`699`
   - Risk: APPLIED 应用关系变更被跳过。结果表业务键包含 `relTemplateId`，若读路径按日期解析当前应用模板，旧应用模板切换到新模板时需要确保新模板结果已存在且旧模板不会被误读；当前只依赖模板创建/初始化时已有结果。
   - Trigger: 运营调整某日期的应用模板、清除未来日期应用关系。
   - Suggestion: 明确结果表读取是否按 resolved templateId 查询；若是，应在 APPLIED 后刷新新模板并按旧模板可见性规则清理/保留。

4. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:151`-`157` and `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/scene/DemandSceneRepositoryImpl.java:80`-`93`
   - Risk: 场景 EDIT 被跳过。若编辑场景时间段、顺序或有效性会影响“当前日期/时间命中哪个 sceneId”，只保留旧结果可能造成查询和结果表语义不一致。
   - Trigger: 修改场景时间段、顺序、跨天边界或使场景无效。
   - Suggestion: 对场景 EDIT 至少刷新该 sceneId；若编辑字段完全不影响配置结果，需要把不影响字段约束写进事件合同。

5. `common/src/main/java/com/persagy/apccontrol/basic/model/message/BaseEvent.java:143`-`147`
   - Risk: DTX async listener 中刷新异常会被 `executeWithLock` 捕获并只打日志，调用侧无法感知失败；虽然不会影响 Rabbit handler 返回，但补偿/重试入口不明确。
   - Trigger: DTX 刷新期间 RWD/DB 异常或结果 upsert 异常。
   - Suggestion: 至少增加失败计数/告警或重试任务入口；日志中保留 group/project/templateType 维度，便于人工补偿。

## Looks OK

1. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:78`-`96`
   - Risk Checked: 配置新增/修改/删除是否漏算、错删。
   - Trigger: `addOrUpdateConfig`、`clearConfig`、`cancelReference` 最终通过 `DemandConfigRepositoryImpl.java:157`-`160` 或 `228`-`235` 发布配置事件。
   - Result: listener 使用新/旧配置定位实例，并调用 `refreshByInstanceAndChildren`；仓储在 `DemandConfigResultRefreshRepositoryImpl.java:119`-`149` 对无聚合/无 finalConfig 的实例删除旧结果，能覆盖“删除自身配置后露出上级继承值”和“无有效结果要删除”两类边界。
   - Suggestion: 保持局部刷新路径的 missing-delete 行为，批量路径应向它对齐。

2. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:114`-`117` and `221`-`242`
   - Risk Checked: 模板/场景删除是否重复删、错删。
   - Trigger: 模板 REMOVE、场景 DELETE。
   - Result: 结果 listener 按旧 templateId/sceneId 直接删除结果；配置网关/场景网关级联删除原始配置时设置 `publishEvent=false` 或独立删除，避免配置删除再次触发结果刷新。
   - Caveat: 依赖 `removeResults` 幂等；即使场景删除和模板删除级联同时出现，也应是重复 no-op 删除，不应错删其他模板。

3. `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/listener/CacheDtxPublishChangedListener.java:51`-`55`
   - Risk Checked: DTX rule publish 是否误触发需求结果重算。
   - Trigger: `DtxRulePublishMessageEvent`。
   - Result: cache listener 只发布 `CacheRwdBuildTriggerEvent`，需求结果 listener 没有监听 DTX rule event，符合“DTX rule 不重算需求结果”的合同。

4. `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/JobTask.java:112`-`115` and `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/config/InitXxlJob.java:76`-`86`
   - Risk Checked: XXL-JOB 入口是否过厚、是否自动调度。
   - Trigger: 部署初始化 XXL-JOB 定义。
   - Result: `JobTask` 只委托 handler；`InitXxlJob` 使用 `addNoneScheduleJob` 注册无调度任务，符合手动触发初始化要求。

5. `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/BaseXXJob.java:71`-`90`, `187`-`231`, and `134`-`138`
   - Risk Checked: 多集团多项目分片和项目失败是否影响后续项目。
   - Trigger: XXL-JOB 手动初始化全量或过滤初始化。
   - Result: no-cron-check 入口复用集团/项目遍历和分片；单项目异常设置 `hasProjectFailed` 后继续后续项目，任务结束返回 FAIL，符合“失败不阻断后续项目但整体失败可见”。

6. `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/DemandConfigResultInitTaskHandler.java:112`-`135` and `145`-`157`
   - Risk Checked: 初始化上下文和模板类型并发。
   - Trigger: XXL-JOB 或 debug 调用 handler.process。
   - Result: 每个项目先设置 group/tenant/project context，finally 中 `ContextUtil.unloadContext()`；模板类型串行执行，null/空 templateTypes 过滤后表示全部类型，符合当前规格。

7. `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:929`-`1010`
   - Risk Checked: 日志和性能追踪是否足够且不会爆炸。
   - Trigger: 刷新、初始化、DTX。
   - Result: INFO 只输出 tracker、模板类型、数量和耗时；大范围实例/业务键只按类型计数汇总，详细调用树仅 debug 输出，不会默认打印大量实例 ID 或 JSON。

8. `common/src/main/java/com/persagy/apccontrol/basic/controller/demand/DemandController.java:218`-`222`
   - Risk Checked: 普通查询接口是否会误触发刷新。
   - Trigger: `POST /demand/config/listConfigResultsByInstances`。
   - Result: controller 只委托 query service 读取结果，不触发初始化或刷新。

## Caveats / Not Found

- 未运行数据库测试、浏览器测试或接口实测，符合本次“只分析”要求。
- 未审查全局网关/安全配置；debug 接口权限结论仅基于 controller 本身未见方法级权限/环境限制。
- 未完整追踪 `DemandQueryService.listConfigResultsByInstances` 的读路径；本文件只覆盖用户指定的触发入口、事件监听、初始化和 debug 边界。
- `BusinessThreadPoolExecutor.ttlExecutor()` 的 TTL 上下文传播实现未展开审查；并发模板刷新上下文安全结论基于命名和 `PerformanceTracker` 使用 TTL 的代码迹象，仍建议实测一次多项目初始化日志中的 projectId 是否串线。

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终处置，优先于前面的历史审查描述。

| 审查项 | 最终处置 |
| --- | --- |
| 屏蔽事件误删原始需求配置 | 已修复：原始配置级联删除只处理 `BLACKLIST`，`SHIELDED` 只更新结果表标识。 |
| 模板/场景批量刷新漏删旧结果 | 已修复：统一进入写锁事务内的范围差集同步。 |
| DTX 异步锁 Key 依赖线程上下文 | 已修复：锁 Key 显式使用 `event.getProjectId()`，`executeWithLock(...)` 再恢复事件上下文。 |
| 非法 XXL-JOB 参数误触发全量 | 已修复：空白参数才表示全量，非法 JSON 或反序列化为 null 直接失败。 |
| Debug 空上下文触发全集团全项目 | 已移除该路径：Debug 接口直接调用当前请求上下文下的刷新仓储，不复用 `BaseTask` 全集团遍历。 |
| 部分项目失败仍返回成功 | 已修复：初始化专用入口继续执行后续项目，并在任一项目失败时最终返回失败。 |
| 模板 UPDATE / APPLIED 重算 | 无需修改：最终配置不因模板名称、启停或应用日历变化而变化，只处理 INSERT 和 REMOVE。 |
| 场景 EDIT 重算 | 无需修改：本期未启用照明和场景监听；未来接入时只处理场景 INSERT / DELETE。 |
| DTX 异步失败补偿 | 接受现状：错误在事件公共边界记录，刷新可通过 XXL-JOB / Debug 按项目幂等重跑。 |
| 正常路径日志量 | 已复核：保留失败边界日志和性能追踪节点，不打印逐项目、逐模板、逐实例成功日志或配置 JSON。 |
