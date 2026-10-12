# Research: result-db-query-review

- Query: 专项审查 D：需求配置结果表/快照表 Mapper、SQL、索引、历史查询、正式查询接口性能与正确性
- Scope: internal
- Date: 2026-07-09

## Findings

### Files Found

- `.trellis/workflow.md` - Trellis workflow and research persistence rules.
- `.trellis/spec/backend/index.md` - Backend spec index and mandatory backend rules.
- `.trellis/spec/backend/database-guidelines.md` - Mapper, batch query, and batch insert guidelines.
- `.trellis/spec/backend/quality-guidelines.md` - Null annotation and entity-field annotation rules.
- `.trellis/spec/backend/demand-config-result-precompute.md` - Result-table read/write/history contract for this feature.
- `.trellis/tasks/07-07-demand-config-result-precompute/prd.md` - Task requirements and acceptance criteria.
- `.trellis/tasks/07-07-demand-config-result-precompute/design.md` - Data model, read path, SCD2, and performance design.
- `.trellis/tasks/07-07-demand-config-result-precompute/implement.md` - Implementation checklist and validation focus.
- `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml` - Main result list SQL, snapshot history branch, batch insert, dynamic filters.
- `common/src/main/resources/mapper/demand/DemandConfigResultSnapshotMapper.xml` - Snapshot result map and batch insert.
- `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql` - Cloud-side result/snapshot DDL and indexes.
- `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql` - Edge-side result/snapshot DDL and indexes.
- `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResult.java` - Result entity.
- `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResultSnapshot.java` - Snapshot entity and open-ended valid time constant.
- `common/src/main/java/com/persagy/apccontrol/basic/model/vo/demand/DemandConfigResultVO.java` - Lightweight result API VO.
- `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java` - Controller-facing result query service.
- `common/src/main/java/com/persagy/apccontrol/basic/controller/demand/DemandController.java` - `/config/listConfigResultsByInstances` endpoint.
- `common/src/main/java/com/persagy/apccontrol/basic/model/param/demand/DemandConfigResultBatchQueryParam.java` - Dedicated result-query parameter and `sceneId == null` semantics.
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/domain/repository/BaseRepositoryQueryParam.java` - Lambda-based column selection conversion.
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRepositoryImpl.java` - Upsert, duplicate lookup, snapshot close, delete paths.
- `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultServiceImpl.java` - Mapper list and batch insert service.
- `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultSnapshotServiceImpl.java` - Snapshot insert and open snapshot close logic.

### Code Patterns

- Lightweight endpoint path: `DemandController#listConfigResultsByInstances` delegates directly to `DemandQueryServiceImpl#listConfigResultsByInstances` (`DemandController.java:218-222`, `DemandQueryServiceImpl.java:1157-1163`).
- Narrow read columns are selected by adding only template type, instance ID, and final JSON (`DemandQueryServiceImpl.java:1181-1183`), converted through lambda metadata (`BaseRepositoryQueryParam.java:73-89`), then injected into XML as `${withColumn}` (`DemandConfigResultMapper.xml:31-34`, `DemandConfigResultMapper.xml:53-56`).
- History routing is implemented in the mapper: past dates read `demand_config_result_snapshot`; null/current/future dates read `demand_config_result` (`DemandConfigResultMapper.xml:25-68`).
- Snapshot SCD2 filtering uses `date(update_time) <= #{param.date}` and `date(valid_end_time) > #{param.date}`, then keeps `row_number() = 1` per business key (`DemandConfigResultMapper.xml:40-48`).
- Business-key lookup and delete predicates use OR-expanded business keys with null-aware scene matching (`DemandConfigResultMapper.xml:189-209`).
- `relSceneIdIsNull` exists as a query flag but the public result endpoint does not set it (`DemandConfigResultRepositoryQueryParam.java:94-97`, `DemandQueryServiceImpl.java:1177-1179`).
- `sceneId == null` in the dedicated result API parameter is documented as no scene filtering.
- Scene mode is consistent within one demand template type: a type either has scenes for all result rows or has no scenes for all result rows.
- Result read API intentionally keeps the shared query-param contract: `sceneId == null` means no scene filtering; callers that need scene-specific results must pass `sceneId`.
- Result read API uses independent `instanceType` and `instanceIds`: omitting both queries all objects for the template/scene filter.
- Full object-scope result reads leave both repository instance filters unset, so all-object SQL does not generate redundant instance predicates.
- Snapshot close updates rows by `id in (...)` and `valid_end_time = OPEN_END_TIME` (`DemandConfigResultSnapshotServiceImpl.java:60-68`).
- Batch result upsert chunks business-key existence queries at 500 (`DemandConfigResultRepositoryImpl.java:45-52`, `DemandConfigResultRepositoryImpl.java:238-268`), and explicit result-read instance IDs are also chunked at 500.

### External References

- No external references were used. This review is based on project specs, task artifacts, and source inspection only.

### Related Specs

- `.trellis/spec/backend/demand-config-result-precompute.md` - Result repository must route current/future to main table, historical to snapshot, and lightweight API must select only `demand_template_type`, `demand_instance_id`, `final_config_json`, `is_shielded`.
- `.trellis/spec/backend/demand-config-result-precompute.md:176` - The new endpoint must support historical dates with the same SCD2 windowing style as `DemandConfigMapper` / `DemandSceneMapper`.
- `.trellis/spec/backend/database-guidelines.md:27-48` - Batch queries are required; loop queries are forbidden.
- `.trellis/spec/backend/database-guidelines.md:49-79` - Large inserts should use chunked mapper-level batch insert and explicitly fill insert defaults before XML insert.
- `.trellis/spec/backend/quality-guidelines.md:118-126` - Database entity fields should not carry nullness annotations.

### Must Fix

- None for result read all-object scope after the query normalization.

### Confirmed Contracts

1. `sceneId == null` in `/demand/config/listConfigResultsByInstances`
   - Decision: No code change. The endpoint keeps the shared query-param contract: null scene means no scene filtering.
   - Caller contract: Callers that need scene-specific results must pass `sceneId`.
   - Related invariant: A demand template type is either fully scene-based or fully no-scene; mixed scene/no-scene rows in the same demand type are invalid.
2. Empty object scope in `/demand/config/listConfigResultsByInstances`
   - Decision: Keep the existing config-query contract: query all objects.
   - Implementation: The service leaves both independent repository instance filters unset before querying `demand_config_result`, avoiding redundant predicates.
   - Caller contract: Callers that need a subset must pass explicit instance filters; callers may intentionally omit filters for all-object reads.
3. Historical result query keeps `date(column)` predicates.
   - Decision: No code change. The result snapshot query follows existing demand SCD2 date-level semantics.
   - Reason: Using raw datetime ranges can conflict when multiple effective changes happen within the same second; the business contract is historical date, not timestamp.
4. Historical window tie-breakers are not added to hide duplicate snapshots.
   - Decision: No code change. Application write logic must keep current/snapshot rows correct and unique by business key; duplicate same-key/same-valid-time snapshots are invalid data.
5. XML `${withColumn}` remains internally controlled.
   - Decision: No code change. Current callers provide columns through Repository query params and lambda-to-column conversion, not external strings.

### Resolved Performance Finding

1. Explicit large ID lists in `/demand/config/listConfigResultsByInstances`
   - Resolution: The query service sets independent instance-type and instance-ID repository filters, and the result service splits non-empty instance IDs into batches of at most 500.
   - Effect: Each mapper query keeps one bounded `IN (...)` predicate while preserving all-object and all-of-type semantics for absent or empty IDs.

### Should Fix

1. [Confirmed] `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:44-45`, `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:48-51`, `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:48-51`
   - Risk: `date(update_time)` and `date(valid_end_time)` wrap indexed datetime columns, so historical queries cannot fully use datetime range portions of the snapshot indexes. This matches existing demand mapper style and spec wording, but it is still a scaling risk when snapshot history grows.
   - Trigger: Historical date query over large snapshot history for a template/type with many objects and scenes.
   - Decision: Keep `date(column)` for the current contract; monitor latency if snapshot history grows.

2. [Confirmed] `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:40`, `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:48`
   - Risk: Historical windowing orders only by `valid_end_time desc`. If duplicate snapshot rows exist for the same business key and same `valid_end_time` because no business unique constraint exists, the selected row is nondeterministic.
   - Trigger: Concurrent refresh, sync duplication, or retry inserts create duplicate business-key snapshots with identical close/open valid end times.
   - Decision: Do not add mapper-level tie-breakers for invalid duplicate data; keep correctness in application write logic.

3. `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:20-25`, `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:20-25`, `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRepositoryImpl.java:188-225`
   - Risk: There is no database unique constraint for the business key. This is consistent with the PRD's cloud-edge-sync constraint, but correctness now depends entirely on read-before-write application logic. Current-table duplicates would leak through the public query because the current branch does no dedupe/windowing.
   - Trigger: Parallel refresh for the same `(project_id, demand_template_type, rel_template_id, rel_scene_id, demand_instance_type, demand_instance_id)` before either transaction observes the other's insert, manual sync duplication, or retry after partial failure.
   - Suggestion: Do not add a UNIQUE constraint unless cloud-edge sync ownership changes. Instead add application-level serialization/idempotency, duplicate detection metrics, and an offline cleanup query. If the database can support it safely, a non-enforced diagnostic unique check or preflight duplicate scan would reduce risk.

4. [Confirmed] `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:31-34`, `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:53-56`, `common/src/main/java/com/persagy/apccontrol/basic/model/base/domain/repository/BaseRepositoryQueryParam.java:73-89`
   - Risk: XML uses `${withColumn}` dynamic column injection. Current callers convert lambda property references to MyBatis-Plus column names, which makes this internally controlled, but the mapper method itself accepts raw strings.
   - Trigger: Future code calls `DemandConfigResultMapper#list(...)` directly with caller-supplied or manually composed `withColumns`.
   - Decision: Keep the current internally controlled path through `BaseRepositoryQueryParam#toSelectColumns()`.

5. [Resolved] `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRepositoryImpl.java:165-178`, `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:812-815`, `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:24-25`
   - Risk: Deleting by template type first queries all matching IDs into memory, then deletes in 500-ID batches. The index supports the scan prefix `(project_id, demand_template_type)`, but the Java side still materializes all IDs in one list and holds one long transaction.
   - Trigger: Demand-scope becomes empty and `deleteResultsByTemplateType(...)` removes all results for a template type. At the stated scale this can be hundreds of thousands to millions of IDs per project depending on scene/template spread.
   - Suggestion: Page ID discovery and delete/close snapshots in bounded chunks, or add a mapper-level delete/close-by-template-type flow with strict chunking. Avoid one huge ID list in memory.
   - Status: Fixed with a bounded `listIds(..., limit)` mapper/service path. The repository now repeatedly fetches at most 500 current-row IDs, closes their open snapshots, deletes those rows, and then fetches the next page.

6. `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultSnapshotServiceImpl.java:64-68`, `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:46-49`, `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:46-49`
   - Risk: Closing open snapshots filters by `id in (...)` plus `valid_end_time = OPEN_END_TIME`. The primary key starts with `id`, so small 500-ID close batches are OK. However the secondary snapshot business indexes put `valid_end_time` after business columns; they do not help this close path.
   - Trigger: Closing many snapshots during bulk update/delete.
   - Suggestion: Current 500-ID chunks are acceptable. If close latency shows up, add a dedicated `(id, valid_end_time)` secondary index only if the primary key access is not sufficient on the target DB; otherwise avoid redundant indexes.

7. `common/src/main/java/com/persagy/apccontrol/basic/model/vo/demand/DemandConfigResultVO.java:26-33`, `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java:1197-1216`
   - Risk: Result deserialization requires `demand_template_type` even though the VO returns only `instanceId/shielded/optional instanceTag/finalConfig`. This is correct for parsing, but any future attempt to "optimize" selected columns by dropping template type would break parsing.
   - Trigger: Maintainer removes `DemandConfigResult::getDemandTemplateType` from `toDemandConfigResultRepositoryQueryParam`.
   - Suggestion: Add a small unit test or comment asserting `demand_template_type` is a required internal parse column, not a response column.

### Looks OK

1. `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java:1181-1183`, `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:51-62`
   - Risk: Full-field return would be expensive because result rows include JSON fields and audit data.
   - Trigger: Normal `/config/listConfigResultsByInstances` current-date query.
   - Suggestion: Current implementation selects only `demand_template_type`, `demand_instance_id`, `final_config_json`, and `is_shielded`, which matches the spec and avoids `inherit_instances_json` / audit fields.

2. `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:25-68`
   - Risk: Historical dates could accidentally query the current table or current dates could query snapshots.
   - Trigger: `param.date` before today vs null/current/future date.
   - Suggestion: Current routing matches the result precompute spec: past dates use `demand_config_result_snapshot`; null/current/future use `demand_config_result`.

3. `common/src/main/resources/mapper/demand/DemandConfigResultMapper.xml:40-48`
   - Risk: Snapshot history can return multiple versions for the same business key.
   - Trigger: Multiple historical versions overlap the queried date at date granularity.
   - Suggestion: Current `row_number()` partition keeps one row per business key and follows the existing demand mapper SCD2 style. Add deterministic tie-breakers as noted under Should Fix.

4. `common/src/main/java/com/persagy/apccontrol/basic/controller/demand/DemandController.java:218-222`, `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java:1157-1163`
   - Risk: New endpoint might silently fall back to real-time inheritance and hide result-table misses.
   - Trigger: Result table has no matching row.
   - Suggestion: Current service reads only `demandConfigResultRepository.list(...)` and maps returned rows; there is no fallback to `demandConfigGateway.listConfigsByInstances(...)`.

5. `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java:1157-1216`
   - Risk: Lightweight query might fill instance names via RWD.
   - Trigger: Normal endpoint request.
   - Suggestion: Current implementation maps directly to `DemandConfigResultVO` and does not call RWD/name-fill methods.

6. `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResult.java:23-87`, `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResultSnapshot.java:23-98`
   - Risk: Entity fields with JSpecify nullness annotations conflict with the backend quality spec.
   - Trigger: Entity model inspection.
   - Suggestion: Looks OK. The two database entities do not annotate persistence fields with JSpecify. The VO uses nullness annotations, which is outside the entity-field restriction.

7. `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:1-51`, `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:1-51`, `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResult.java:32-87`, `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand/DemandConfigResultSnapshot.java:37-98`
   - Risk: DDL and entity field names could drift, especially `is_shielded` / `is_customized_config`.
   - Trigger: Mapper/entity/DDL binding.
   - Suggestion: Looks consistent. DDL columns, XML result maps, and entity fields align for `is_customized_config`, `is_shielded`, JSON fields, template/scene IDs, audit fields, and `valid_end_time`.

8. `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultServiceImpl.java:48-57`, `common/src/main/java/com/persagy/apccontrol/basic/resource/demand/impl/DemandConfigResultSnapshotServiceImpl.java:47-55`
   - Risk: Large inserts through MyBatis-Plus `saveBatch` can be slow and miss custom fill behavior.
   - Trigger: Initialization or recompute writes many results/snapshots.
   - Suggestion: Looks OK. Both result and snapshot inserts use XML batch inserts with 500-row chunks; result inserts call `fillInsertDefaultValue(...)` before XML insert.

9. `saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:20-25`, `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.0__demand_config_result.sql:20-25`
   - Risk: Main result-table queries need indexes for by-template/by-scene/by-instance and by-template-type cleanup.
   - Trigger: Current query by `project_id + rel_template_id + rel_scene_id + instance`, no-scene query by `project_id + rel_template_id + instance`, and template-type cleanup by `project_id + demand_template_type`.
   - Suggestion: Looks broadly covered by the three main-table indexes. Remaining risk is query volume/chunking, not missing obvious leading index columns.

## Caveats / Not Found

- No database tests, EXPLAIN plans, migration execution, or browser tests were run, per the user request.
- I did not verify target DB optimizer behavior on the actual cloud/edge database versions.
- I did not find an explicit DB-level business-key unique constraint. This appears intentional from the PRD/design for cloud-edge sync compatibility, but it leaves duplicate prevention to application logic and operations checks.
- I did not find a public API guard that limits `/config/listConfigResultsByInstances` request size or forbids unbounded all-instance queries.

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终处置，优先于前面的历史审查描述。

| 审查项 | 最终处置 |
| --- | --- |
| 全对象查询 | 已确认合同：允许不传对象过滤查询全部结果，服务层移除无意义的空 ID OR 条件。 |
| 大量显式实例 ID | 已修复：按实例类型和 ID 分片查询，避免单条超大 `IN`。 |
| 历史查询 `date(column)` | 接受现状：必须保持业务日期级 SCD2 语义，部署环境通过 EXPLAIN 观察增长后的性能。 |
| 历史窗口缺少额外 tie-breaker | 无需修改：重复同业务键快照属于写入错误，不能由查询排序掩盖。 |
| 无数据库业务唯一约束 | 已修复应用侧并发控制：批内去重、分布式写锁、锁内查询与事务写入；不增加 UNIQUE。 |
| `${withColumn}` | 无需修改：只允许 Repository 参数通过 Lambda 元数据生成内部列名。 |
| 模板类型删除一次性加载全部 ID | 已修复：固定 500 条分页查询、关闭快照和删除。 |
| 开放快照关闭索引 | 当前可接受：500 条 ID 批次通过主键前缀访问；只有实际 EXPLAIN 显示不足时再增加冗余索引。 |
| 解析结果需要 `demand_template_type` | 已确认：该字段属于内部反序列化必需列，不属于 VO 响应字段。 |
| 当前表和快照表索引 | 已确认覆盖主要模板、场景、实例和模板类型清理前缀；上线前仍需在目标数据库执行 EXPLAIN。 |
