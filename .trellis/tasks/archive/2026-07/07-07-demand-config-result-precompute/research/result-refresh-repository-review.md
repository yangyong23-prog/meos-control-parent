# Research: result-refresh-repository-review

- Query: 专项审查 A - 需求配置结果刷新与仓储写入/删除正确性
- Scope: internal
- Date: 2026-07-09

## Findings

### Files Found

- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java` - 刷新入口、实时结果转换、miss 删除、屏蔽更新、模板类型全量刷新。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRepositoryImpl.java` - 结果 upsert、remove、shielded update、快照关闭/新增编排。
- `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultServiceImpl.java` - 当前结果表查询、XML 批量插入、按 ID 删除。
- `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultSnapshotServiceImpl.java` - 快照批量插入、开放快照关闭。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/param/DemandConfigResultBusinessKey.java` - 结果表业务键定义，包含 nullable `relSceneId`。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/param/DemandConfigResultRepositoryQueryParam.java` - 结果表查询条件，包含业务键、实例范围、空场景查询标记。
- `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml` - 当前/历史查询 SQL、业务键定位 SQL、批量 insert SQL。
- `common/src/main/resources/mapper/demand/DemandConfigResultSnapshotMapper.xml` - 快照批量 insert SQL。
- `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResult.java` - 当前结果实体字段。
- `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResultSnapshot.java` - 快照实体和 `DemandConfigResultSnapshot.of(...)` 映射。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java` - 模板/场景删除、黑名单、屏蔽事件入口。
- `common/src/main/java/com/persagy/apccontrol/basic/model/context/demand/DemandConfigAggregateInitByInstanceContext.java` - `DemandConfigByInstanceBatchQueryParam` 空实例集合的实际解释逻辑。
- `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql` - SaaS 表结构和索引。
- `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql` - Edge 表结构和索引。

### Related Specs

- `.trellis/spec/backend/demand-config-result-precompute.md:3` - 预计算结果必须与实时需求配置业务结果一致，并保留定制、屏蔽、继承、需求范围边界。
- `.trellis/spec/backend/demand-config-result-precompute.md:64` - 大批量结果写入应使用 mapper 级分块 SQL。
- `.trellis/spec/backend/demand-config-result-precompute.md:88` - 结果行业务键和字段契约，`relSceneId` nullable。
- `.trellis/spec/backend/demand-config-result-precompute.md:104` - 空集合、无 aggregate、final config null、无模板、无需求空间、屏蔽变化的行为矩阵。
- `.trellis/spec/backend/database-guidelines.md:39` - 禁止循环查询造成 N+1。
- `.trellis/spec/backend/database-guidelines.md:57` - 大批量 insert 要分块，并手动补齐 XML insert 默认字段。
- `.trellis/spec/backend/quality-guidelines.md:82` - service/repository 应从 `ContextUtil.getProjectId()` 读取当前项目。

### External References

- None. 本次只做内部代码与项目 spec 审查。

### Must Fix

1. `DemandConfigResultRefreshRepositoryImpl.java:456-483` - 全量/模板/场景刷新路径只 upsert 实时返回且有 finalConfig 的 aggregate，没有删除实时 miss 或 finalConfig 为 null 的旧结果。
   状态：已解决。`refreshByQueryParam(...)` 现在先按当前项目 + 模板 + 场景边界查询已有结果业务键，再用本次有效实时结果业务键做差集删除；实时结果为空时也会删除该刷新边界内的旧结果并关闭快照。
   风险：`refreshByTemplate(...)`、`refreshByScene(...)`、`refreshByTemplateType(...)` 走 `refreshByQueryParam(...)`；当物理世界减少对象、模板场景下部分对象失效、实时链路仅返回部分 aggregate 或 aggregate finalConfig 为 null 时，结果表会保留旧 row，后续预计算查询读到过期配置。该行为与 spec `Gateway returns no aggregate... -> Delete existing result rows`、`Aggregate has null final config -> delete stale matching result` 不一致。
   触发条件：全量初始化后对象被移出需求范围；场景/模板刷新时某些对象实时计算 miss；继承链删除导致某实例最终无配置；`refreshByQueryParam(...)` 返回空 aggregate 时目前直接 `return`。
   建议：让 `refreshByQueryParam(...)` 像 `refreshInstances(...)` 一样基于本次请求范围计算 delete keys；至少对明确传入的 `instanceTypeAndInstanceIds` 做 missing/finalConfig-null 删除。若请求范围包含空 set 表示全类型，需要先解析为明确实例集合或按模板/场景/类型查询现有结果后做差集删除。

2. `DemandConfigResultRepositoryImpl.java:188-214` + `saas/.../V7.260701.0.0__demand_config_result.sql:20-25` + `local/.../V7.260701.0.0__demand_config_result.sql:20-25` - upsert 是先查再插/改，但结果表没有业务唯一约束，也没有对输入集合按业务键去重。
   风险：并发刷新同一业务键时两个事务都可能查不到旧 row，然后各自 insert，形成多个 current row；同一次 `results` 中若出现重复业务键，也会批量插入多条。后续 `list(...)` 会返回重复结果，`mapExistsResultsByBusinessKeyPartition(...)` 只在 Java map 合并时丢一条，无法修复表内脏数据，快照也会分裂。
   触发条件：DTX 全量刷新、初始化任务、黑名单/配置变更刷新同时命中同一 template/scene/instance；网关/入参重复返回同一 business key；重复初始化与异步事件交叠。
   建议：为 current 表增加与业务键一致的唯一约束，需注意 MySQL/Kingbase 对 nullable `rel_scene_id` 的唯一语义；可用规范化场景键、函数索引或拆分无场景/有场景约束。代码侧在 `upsert(...)` 前按 `DemandConfigResultBusinessKey` 去重并定义冲突策略。并发场景下应捕获唯一冲突后重查更新或使用数据库原子 upsert + 显式快照处理。

3. `DemandConfigResultRefreshRepositoryImpl.java:624-666` + `DemandConfigAggregateInitByInstanceContext.java:250-309` + `DemandConfigAggregateInitByInstanceContext.java:332-338` - `toDemandScopeInstanceTypeAndInstanceIds(...)` 固定放入 SPACE/FLOOR/BUILDING/PROJECT，其中 FLOOR/BUILDING 可能是空 set；下游空 set 在网关/仓储语义中不是“查 0 个”，而是“该类型全量/从已有配置补齐”。
   风险：需求空间只有 SPACE、没有 floorId/buildingId 时，模板类型刷新仍携带空 FLOOR/BUILDING set。`fillInstanceIds()` 的 all-empty 判断只看是否所有 set 都空，PROJECT 非空会使其继续；`fillConfigs()` 遇空 set 会从已查配置中取该类型全部配置。结果可能把不属于需求空间范围的楼层/楼栋级旧配置重新聚合并 upsert，扩大刷新范围，掩盖应该删除的旧 row。
   触发条件：RWD demand space 返回空间缺少 floorId 或 buildingId；某类型在当前需求范围为空但历史/配置表存在该类型配置；全量初始化或 DTX 发布按模板类型刷新。
   建议：`toDemandScopeInstanceTypeAndInstanceIds(...)` 不要保留空 set 类型，或在 `refreshByTemplateScene(...)` 前移除空 set；若业务必须表达“该类型 0 个对象”，需要扩展参数语义，避免与现有“查全部”混用。

4. `DemandConfigResultRefreshRepositoryImpl.java:278-318` + `DemandConfigResultRefreshRepositoryImpl.java:504-528` - `refreshByTemplateType(...)` 标了事务，但模板刷新在 `CompletableFuture.runAsync(...)` 子线程执行，外层事务不覆盖子线程写库，且子任务通过 private 方法调用。
   风险：某个模板分片失败时，已经完成的子线程 upsert/delete/快照操作不会随外层 `refreshByTemplateType(...)` 回滚，最终形成同一 templateType 的部分刷新状态。调用方只能看到失败异常，数据已部分提交。
   触发条件：模板类型下多个模板并发刷新，后一个模板实时查询/DB 写入失败，前一个模板已提交；初始化任务或 DTX 发布刷新期间发生网关/数据库异常。
   建议：明确事务粒度。如果需要模板类型整体原子性，取消子线程写库或引入可控事务编排；如果接受“单模板/单场景原子”，则移除外层整体事务假象，并在任务/日志/重试策略中声明 partial commit，保证失败后可幂等重跑且无重复 current row。

### Should Fix

1. `DemandConfigResultRefreshRepositoryImpl.java:826-839` - `toUpsertParam(...)` 每个 SPACE aggregate 都调用 `isShielded(...)`，该方法按单实例查询 `demandInstanceRepository.listConfig(...)`。
   风险：一次刷新 1 万个空间时会产生 1 万次屏蔽查询，是典型 N+1；全量初始化、DTX 发布或模板类型刷新会显著放大数据库压力。
   触发条件：`refreshByTemplateType(...)` / `refreshByTemplate(...)` 计算大量 SPACE aggregate；模板多个场景会按场景重复查询。
   建议：按 templateType + SPACE ids 批量预取屏蔽集合，在转换 upsert 参数时用内存 set 判断；或让实时聚合/上下文一次性携带屏蔽值。

2. `DemandConfigResultRepositoryImpl.java:120-145` - `updateShielded(...)` 先查所有待变更完整结果，再一次性 `closeOpenSnapshots(...)`、`updateByIdValidate(...)`、`insertSnapshots(...)`，没有按 `BUSINESS_KEY_QUERY_BATCH_SIZE` 或 `RESULT_DELETE_BATCH_SIZE` 分片。
   风险：`businessKeys` 很大时，`list(queryParam)` 会生成巨大 OR SQL，并读取 JSON 大字段；`closeOpenSnapshots(...)` 使用单条大 IN，`updateByIdValidate(...)` 虽内部 batchSize=1000，但前置 `countByIdValidate(ids)` 也可能是大 IN。屏蔽大批量变更时容易出现 SQL 过长、解析慢、锁持有时间长。
   触发条件：一次屏蔽/取消屏蔽大量空间，或事件把某 templateType 下多模板/多场景 business keys 全部传入。
   建议：仿照 `mapExistsResultsByBusinessKeys(...)` 和 `removeResults(...)` 对 business keys/result ids 分片；查询可先窄列取 id，再按 id 分片拉完整行用于快照。

3. `DemandConfigResultRefreshRepositoryImpl.java:337-378` + `DemandConfigResultRefreshRepositoryImpl.java:1015-1029` - `refreshByDtxChangedInstances(...)` 对每个模板调用一次 `listSceneIds(...)`，再按每个场景调用 `refreshInstances(...)`。
   风险：模板数和场景数增多时，场景查询是模板级循环查询，刷新写库是模板 x 场景批次；虽然不是按实例逐条写，但仍可能成为 DTX 黑名单变更后的放大器。
   触发条件：同一 templateType 下模板较多、每模板最多 20 个场景、黑名单一次变更多个空间。
   建议：可以接受当前串行稳定性取舍，但建议批量查询 templateId -> sceneIds，或在日志/限流中明确最大模板/场景规模；若后续性能压测不过，优先优化场景 ID 批量查询。

4. `DemandConfigResultRefreshRepositoryImpl.java:301-308` 与 `.trellis/spec/backend/demand-config-result-precompute.md` - 代码在“template type 无需求空间”时删除该 templateType 当前项目全部历史结果，早期 spec 曾写“Return early; do not query all instances”。
   状态：已解决。spec 已同步为“删除该模板类型旧结果，且不得回退为全实例查询”。
   风险：从数据正确性看删除旧结果是合理的；如果 spec 文本仍保留早退语义，后续 reviewer/agent 可能按旧 spec 把这段回退成早返回，导致旧结果残留；也可能有人误解为不应清理。
   触发条件：物理世界变更导致需求空间为空；按模板类型刷新。
   建议：若产品语义确认“无需求空间应清理旧结果”，更新 spec 的行为矩阵；若不是，则需回退实现。当前审查倾向保留清理语义，但要求契约同步。

5. `DemandConfigResultMapper.xml:27` - 历史查询使用 `@java.time.LocalDate@now()` 判断当前/历史日期，未使用项目 `DateUtils`。
   风险：与项目 date utility 规范不一致；在时区/测试可控时间场景下可能和业务日期来源不一致。
   触发条件：历史查询传入接近自然日边界日期，或项目统一时间工具有特殊时区/模拟时间。
   建议：如 mapper 静态调用支持，改为项目统一日期工具；至少在 spec 中确认 XML 可接受 `LocalDate.now()`。

### Looks OK

1. `DemandConfigResultRefreshRepositoryImpl.java:98-100`、`DemandConfigResultRepositoryImpl.java:85-87`、`DemandConfigResultServiceImpl.java:45-47`、`DemandConfigResultSnapshotServiceImpl.java:44-46`、`DemandConfigResultSnapshotServiceImpl.java:61-63` - 主要 public 写入/删除/快照方法对空集合均早返回，基本安全。

2. `DemandConfigResultRefreshRepositoryImpl.java:119-130`、`DemandConfigResultRefreshRepositoryImpl.java:141-149` - 指定实例刷新 `refreshInstances(...)` 对“实时结果全空”和“部分 miss/finalConfig null”都有删除旧结果路径，语义正确。

3. `DemandConfigResultMapper.xml:189-209` + `DemandConfigResultBusinessKey.java:48-64` - 业务键查询正确处理 `relSceneId == null`，SQL 使用 `rel_scene_id is null`，避免 `= null` 查不到无场景 row。

4. `DemandConfigResultRefreshRepositoryImpl.java:697-715` - upsert 参数保留 `ContextUtil.getProjectId()`、`templateType`、nullable `relSceneId`、`isCustomizedConfig`、`isShielded` 和继承链 JSON；没有用默认值覆盖 `isCustomizedConfig/isShielded`。

5. `DemandConfigResultRepositoryImpl.java:154-160` + `DemandConfigResultSnapshotServiceImpl.java:64-68` + `DemandConfigResultSnapshot.java:107-125` + DDL `V7.260701.0.0__demand_config_result.sql:45-46` - 删除结果前按 result id 关闭开放快照，快照表主键为 `(id, valid_end_time)`，关闭条件 `id in (...) and valid_end_time = OPEN_END_TIME` 命中的是当前结果 id 的开放版本，字段未关错。

6. `DemandConfigResultServiceImpl.java:48-57` + `BaseServiceImpl.java:38-56` + `DemandConfigResultSnapshotServiceImpl.java:47-55` - 当前结果和快照 insert 使用 XML 分片批量写入，当前结果 insert 前补齐 id/create/update 审计字段，符合批量 insert spec。

7. `DemandConfigResultRepositoryImpl.java:238-274` - upsert 前按业务键查询旧结果时已经按 500 分片，且只选择 id、创建审计字段和业务键，避免一次性读取 JSON 大字段。

8. `DemandConfigResultRefreshEventListener.java:114-117`、`DemandConfigResultRefreshEventListener.java:146-149`、`DemandConfigResultRefreshEventListener.java:221-242` - 模板删除、场景删除不依赖实时链路，直接按旧 templateId/sceneIds 删除结果，方向正确；查询由 repository/service 注入当前 project context。

## Code Patterns

- 结果表读取统一从 `DemandConfigResultServiceImpl.list(...)` 进入，SQL 始终传入 `ContextUtil.getProjectId()`：`DemandConfigResultServiceImpl.java:37-40`。
- 当前/历史表切换在 XML 中完成：历史日期查 snapshot 并用 `row_number()` 取业务键最新有效行，当前/未来日期查 current 表：`DemandConfigResultMapper.xml:25-68`。
- 快照采用应用层 SCD2：新增/更新插入 open snapshot，更新/删除前关闭旧 open snapshot：`DemandConfigResultRepositoryImpl.java:211-220`、`DemandConfigResultRepositoryImpl.java:154-160`。
- 指定实例刷新具备“实时 miss -> 删除旧结果”的差异同步模式：`DemandConfigResultRefreshRepositoryImpl.java:133-149`。
- 模板类型全量刷新预取需求空间一次并按模板并发窗口执行：`DemandConfigResultRefreshRepositoryImpl.java:288-318`、`DemandConfigResultRefreshRepositoryImpl.java:504-528`。

## Caveats / Not Found

- 未运行数据库测试、浏览器测试或任何写库验证；本文件只基于静态代码、SQL、DDL 和 Trellis spec 审查。
- 未发现 current 表业务唯一约束；DDL 中只有普通索引。如果线上通过其他平台脚本补了唯一约束，本结论需以实际库结构复核。
- 未审查所有 `DemandStrategy` 具体实现，只从网关上下文和当前刷新参数语义推断空 set 风险。
- 未审查分布式锁覆盖所有非 DTX 入口；并发 upsert 风险按当前 repository/DDL 自身幂等性判断。

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终处置，优先于前面的历史审查描述。

| 审查项 | 最终处置 |
| --- | --- |
| 批量刷新漏删旧结果 | 已修复：`syncRefreshScope(...)` 在模板/场景写锁和事务内查询旧结果并做差集删除，空实时结果也会清理当前边界。 |
| 无 UNIQUE 约束导致重复当前行 | 已修复：批内按业务键去重，并通过模板级/场景级分布式写锁串行化查询和写入；因云边同步约束不增加 UNIQUE。 |
| 需求范围保留空 FLOOR / BUILDING 集合 | 已修复：只写入非空实例类型集合，PROJECT 单独加入，避免空集合被解释为查全部。 |
| 外层事务无法覆盖并发模板 | 已确认设计：模板类型编排无外层写事务，每个模板通过 `syncRefreshScope(...)` 独立原子提交；失败后按项目/模板类型幂等重跑。 |
| 屏蔽空间逐实例查询 | 已修复：模板类型边界批量预取一次屏蔽空间，并在并发模板中复用不可变集合。 |
| 大 business key / ID SQL | 已修复：业务键查询、结果更新、快照关闭和删除按 SQL 形态分片；模板类型清理分页获取 ID。 |
| 模板循环查询场景 | 接受现状：本期只启用无场景空调需求；后续启用照明前需补模板到场景的批量查询。 |
| 无需求空间删除旧结果 | 已确认：删除当前项目该模板类型旧结果，且禁止回退成全实例查询。 |
| 历史查询使用 `date(column)` | 无需修改：沿用需求配置历史日期语义，部署后通过 EXPLAIN 和实际数据量观察性能。 |
