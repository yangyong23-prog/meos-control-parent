# Demand Config Result Precompute

> **Rule**: Demand config result precompute stores the same business result as real-time demand config calculation. Refresh logic must preserve customization, shielding, inheritance, and demand-scope boundaries.

## Current Support Boundary

- The current rollout supports `DemandTypeEnum.AIRCONDITIONING`, `DemandTypeEnum.VENTILATION`, and
  `DemandTypeEnum.SECURITY`.
- These supported demand types currently have no result-precompute scenes, so their result rows use
  `relSceneId == null`.
- Lighting is the only demand type that uses scenes, and lighting result precompute is not part of the current rollout.
- The refresh repository implementation owns the single supported-demand-type set and derives supported template types
  from it. Listeners, initialization jobs, debug entrypoints, and repository guards must call
  `listSupportedTemplateTypes()`, `supportsTemplateType(...)`, or `supportsTemplate(...)`; they must not hardcode
  `DemandTypeEnum.AIRCONDITIONING` independently.
- Initialization, debug, DTX, config, template, blacklist, and shielded-event entrypoints must ignore demand types
  outside the supported-demand-type collection.
- This is a runtime-entry boundary, not a permanent no-scene data model. Result tables, business keys, read filters, refresh signatures, snapshots, and write locks must retain non-null real `sceneId` support.
- Do not enable lighting listeners, scene refresh, or scene cleanup until lighting result precompute is designed and explicitly enabled.

---

## Scenario: Refresh Demand Config Results

### 1. Scope / Trigger

Read this spec when changing:

- `DemandConfigResultRefreshRepository` or its implementation.
- `DemandConfigResultRepository` / mapper / XML batch write behavior.
- `/demand/config/listConfigResultsByInstances` or result-table read paths.
- `DemandConfigInfoCodeAnalyzeStrategy` demand-config reads.
- DTX publish listeners that refresh demand config result data.
- Cloud XXL-JOB handlers that initialize demand config result data.

This is a cross-layer contract because events, gateway queries, repository writes, XML mappers, and DB snapshots must stay consistent.

### 2. Signatures

```java
public interface DemandConfigResultRefreshRepository {
    @NonNull List<DemandTemplateTypeEnum> listSupportedTemplateTypes();

    boolean supportsTemplateType(@NonNull DemandTemplateTypeEnum templateType);

    boolean supportsTemplate(@NonNull Long templateId);

    void refreshInstance(@NonNull Long templateId,
                         @Nullable Long sceneId,
                         @NonNull DemandInstanceValueObject instance);

    void refreshByTemplate(@NonNull Long templateId);

    void refreshByTemplateType(@NonNull DemandTemplateTypeEnum templateType);

    void refreshByDtxChangedInstances(@NonNull DemandTemplateTypeEnum templateType,
                                      @NonNull Collection<DemandInstanceValueObject> instances);

    void updateShielded(@NonNull DemandTemplateTypeEnum templateType,
                        @NonNull Set<String> instanceIds,
                        boolean shielded);

    void removeByTemplateTypeAndInstances(@NonNull DemandTemplateTypeEnum templateType,
                                          @NonNull Collection<DemandInstanceValueObject> instances);
}

public interface DemandConfigResultRepository {
    @NonNull List<DemandConfigResult> syncResults(
            @NonNull Collection<DemandConfigResult> deleteParams,
            @NonNull Collection<DemandConfigResult> upsertParams);

    @NonNull List<DemandConfigResult> syncRefreshScope(
            @NonNull DemandConfigResultRepositoryQueryParam scopeParam,
            @NonNull Collection<DemandConfigResult> upsertParams);
}

public interface DemandConfigGateway<T extends BaseDemandConfig> {
    @NonNull List<DemandConfigAggregate<T>> listStableConfigsByInstances(
            @NonNull @Valid DemandConfigByInstanceBatchQueryParam param);

    @NonNull List<DemandConfigAggregate<T>> listConfigsByInstances(
            @NonNull @Valid DemandConfigByInstanceBatchQueryParam param);

    @NonNull List<DemandConfigResultVO<T>> listConfigResultsByInstances(
            @NonNull @Valid DemandConfigResultBatchQueryParam param);
}

public class DemandConfigResultBatchQueryParam extends BaseDemandTemplateParam {
    private @Nullable Long sceneId;
    private @NonNull DemandInstanceTypeEnum instanceType;
    private @Nullable Set<@Nullable String> instanceIds;
    private boolean autoFillEmptyInstanceIds;
    private boolean filterShieldedSpaces = true;
    private boolean queryInstanceTag = false;
    private boolean fillInstanceName = false;
}

public class DemandConfigResultVO<T extends BaseDemandConfig> {
    private @NonNull String instanceId;
    private boolean shielded;
    private @Nullable DemandInstanceTagVO instanceTag;
    private @Nullable DemandConfigValueObject<T> finalConfig;
}

public interface DemandQueryService {
    @NonNull List<DemandConfigResultVO<BaseDemandConfig>> listConfigResultsByInstances(
            @NonNull @Valid DemandConfigResultBatchQueryParam param);
}
```

The supported-demand-type collection is the only enablement switch for result precompute. Adding a future demand type
must not require changing each listener/job/controller. This central switch does not replace demand-specific lifecycle
implementation: lighting must define and implement scene refresh/cleanup behavior before it is added to the set.

`refreshInstances(...)` is an implementation-internal helper. Do not expose it on
`DemandConfigResultRefreshRepository`; callers should enter through the scoped public methods above so transaction
boundaries and refresh intent stay explicit.

Controller API `/demand/config/listConfigResultsByInstances` must accept the dedicated
`DemandConfigResultBatchQueryParam`, enter through the query service, and must not expose or rebuild
`DemandConfigAggregate`. `DemandQueryServiceImpl` is an application-layer adapter only and must delegate the complete
result query to `DemandConfigGateway#listConfigResultsByInstances(...)`. Cross-business callers must use this Gateway
entry directly instead of depending on `DemandQueryService`.
Return a lightweight `DemandConfigResultVO<T>` with only:

- `instanceId`
- `shielded`
- `instanceTag` (only when `queryInstanceTag == true`)
- `finalConfig`

Because this VO does not return instance name, this read path must not query RWD for instance names.
The Gateway must resolve the template by calling the inherited
`BaseDemandTemplateParam#queryAndGetTemplate()`, preserving `templateId > templateTag > date` priority and the
`templateType` requirement for tag/date lookup. If result-API lookup returns `null`, log one project-scoped `WARN`
with `templateType`, `templateId`, `templateTag`, and `date`, then return an empty list without querying result/config/
RWD paths. Request validation failures and repository/system exceptions must still propagate normally. All result-table
and repository queries must use the resolved `DemandTemplate#getId()`, not an unvalidated request value.

The dedicated result parameter retains `sceneId`, `instanceType`, `instanceIds`, `autoFillEmptyInstanceIds`,
`filterShieldedSpaces`, `queryInstanceTag`, and `fillInstanceName`. The Gateway maps `instanceType` and `instanceIds` directly to
the repository's independent demand-instance-type and demand-instance-ID filters. Their result-path semantics are:

- `sceneId == null` adds no scene predicate; non-null filters the real scene ID.
- `instanceType` is required and uses JSpecify `@NonNull` plus Bean Validation `@NotNull`; standard `@NotEmpty` must
  not be used on an enum because it has no compatible validator.
- a non-null `instanceType` with null/empty `instanceIds` means all precomputed results of that type.
- keep `instanceIds` and its request collection elements `@Nullable` because JSON may omit the collection or contain
  null elements before `checkAndSet()` normalizes it.
- `checkAndSet()` must copy `instanceIds` into a new set while filtering runtime null elements, then apply scope
  validation to the cleaned collection. A missing `instanceType` is invalid regardless of whether the cleaned ID
  collection is empty; a cleaned empty collection keeps all-of-type semantics.
- non-empty `instanceIds` retain result-service batching at 500 IDs per mapper query.
- `autoFillEmptyInstanceIds` must not trigger RWD lookup on this path; result-table empty-ID semantics are sufficient.
- `filterShieldedSpaces` defaults to `true`; omitted input filters rows where `is_shielded` is true.
- `filterShieldedSpaces == false` includes shielded rows, and every returned VO exposes the persisted state through
  `shielded`. The same narrow result query must select `is_shielded`; do not issue a second query for this flag.
- `queryInstanceTag` defaults to `false`; when `true`, the query additionally selects the result instance type and
  customization flag, reads the frontend object scope from instance-config storage, and batches one existing
  demand-strategy custom-tag lookup for that request scope. Result storage does not contain blacklisted objects;
  a blacklisted object in the requested scope must therefore be appended to the response with its `instanceId`,
  `instanceTag.inBlacklisted=true`, and `finalConfig=null`. The default shielded-object filter also applies to these
  appended rows. It must not perform one external tag lookup per result row.
- `instanceTag.customConfig` comes only from the persisted result row's `is_customized_config`, which represents
  whether the original `demand_config` contributes self configuration. Project `valueSelect` / `workCondition`
  generated at query time from the independent project-custom table must not change this tag or the persisted flag.
- `fillInstanceName` defaults to `false` and remains non-operative because the lightweight VO has no instance-name
  field and the path must not query RWD for names. The opt-in `queryInstanceTag` branch may call the existing demand
  strategy custom-tag lookup.

`DemandConfigResultRepository#list(...)` must follow the existing demand mapper history-date contract:

- `param.date == null` or current/future date -> query `demand_config_result`.
- `param.date` before today -> query `demand_config_result_snapshot`, filter `date(update_time) <= param.date`
  and `date(valid_end_time) > param.date`, then use `row_number()` to keep the latest valid historical row per
  business key.
- Keep the `date(column)` predicates for this historical query. The result snapshot contract is date-level SCD2; using
  raw datetime half-open ranges can make multiple changes inside the same second conflict with the business-date view.
  Deterministic history depends on maintaining correct main/snapshot data, not on adding mapper-level tie-breakers for
  corrupted duplicate rows.
- Historical guarantees begin with the first initialization/write that creates an open snapshot. A date before that
  boundary, or a date with no valid snapshot window, is a result miss. Do not fall back to realtime inheritance and do
  not return the current main-table row as historical data. Full initialization cannot backfill pre-initialization
  dates or repair old closed snapshots.

The read path must still keep selected columns narrow for lightweight APIs. `/demand/config/listConfigResultsByInstances`
selects only `demand_template_type`, `demand_instance_id`, `final_config_json`, and `is_shielded`; it must not rebuild
`DemandConfigAggregate` or query RWD instance names. Air-conditioning query-time enrichment may read the current
DTX/RWD work-condition model only for a `PROJECT` result; `SPACE`, `FLOOR`, and `BUILDING` result queries must not call
the project custom-work-condition service.

### Stable Result JSON and Query-Time Enrichment

`final_config_json` stores the stable result of template/scene resolution, instance scope, inheritance, and
space-function-type merging. It must not persist values whose correctness depends on the request date, current DTX/RWD
state, or request locale. For air-conditioning, the following values are query-time data:

- filtering `environmentQuality.tdbQuality.seasonQuality` by the business date's calendar air-season data;
- project-level `valueSelect` and `workCondition`, because their service combines dated custom configuration with the
  current DTX binding rules, RWD systems/pumps, and localized names.

`DemandConfigAirconditoningAware` remains the registered, unchanged lifecycle implementation for the legacy realtime
aggregate path. Ordinary `listConfigsByInstances(...)` and `listOriginConfigs(...)` must continue invoking its
`afterAggregateInit(...)` hook through the existing `demandConfigLifeAware` list. Do not delete the component, delegate
it to a new class, or otherwise change those existing entrypoints as part of result precompute.

`DemandConfigAirconditioningQueryEnricher` is result-read-only. It exposes `enrichResults(...)` after
`final_config_json` deserialization and must not expose or receive `DemandConfigAggregate`. It is injected into
`DemandConfigGatewayImpl` exclusively for `listConfigResultsByInstances(...)`; the legacy aggregate query methods must
not invoke it. Keeping the two Gateway paths separate preserves existing realtime callers. Do not duplicate result
enrichment in `DemandQueryServiceImpl`, InfoCode callers, or refresh repositories.

Result refresh must call `DemandConfigGateway#listStableConfigsByInstances(...)`. That entry still executes template,
scene, configuration, inheritance, and every stable demand-type lifecycle hook, but excludes only
`DemandConfigAirconditoningAware#afterAggregateInit(...)`. Calling ordinary `listConfigsByInstances(...)` from refresh
would write calendar-filtered, DTX/RWD-derived, or localized data into the result and snapshot tables. The stable entry
must force inheritance on and instance-name filling off, rather than relying only on each caller to set those flags.

`DemandConfigGateway#listOriginConfigs(...)` is an existing compatibility boundary and continues executing the complete
lifecycle list, including `DemandConfigAirconditoningAware`. Result precompute must not use origin queries and must not
change their behavior while introducing the stable entry.
As a final persistence guard, aggregate-to-result conversion must remove `globalConfig.valueSelect` and
`globalConfig.workCondition` from the converted JSON tree before writing. Perform this on the `JsonNode`, not on the
aggregate object, so shared inheritance references and `isCustomizedConfig` calculation are not mutated.

For historical result reads, the enricher must preserve the legacy realtime semantics: use the requested date's
materialized calendar day tags and dated project custom configuration, while the custom-work-condition service may still
combine that record with the current DTX/RWD model exactly as the legacy path does. A null date means today; current and
future dates use the project's current global calendar rule.

The legacy realtime aggregate path must preserve the registered `DemandConfigAirconditoningAware` class and its edge behavior.
When the aggregate identifier has no `configDate`, pass `null` through to the project custom-config service; resolving
an effective today value is only for calendar selection and result-table reads. If any realtime air-conditioning
aggregate lacks final config, `environmentQuality`, or `tdbQuality`, stop the whole batch's season rewrite at that
point. Default season configuration stays lazy and must not be queried when the first incomplete aggregate aborts the
batch. Result-table enrichment may skip a placeholder row with null `finalConfig` so blacklisted placeholders do not
block valid result rows.

Because a calendar-filtered JSON value cannot reconstruct seasons that were previously removed, deployment of this
storage-boundary change requires a full rerun of `demandConfigResultInitTaskHandler` before result consumers are enabled.
After the stable rows are rebuilt, calendar air-season changes and DTX rule/localization changes are visible on the next
query and do not require result-table recomputation. DTX changes that alter the demand object scope, hierarchy, or stable
inherited configuration still follow the existing refresh events.

`DemandConfigInfoCodeAnalyzeStrategy` is the first production caller switched to this result read path. It must:

- resolve the current air-conditioning template and season as before;
- call `DemandConfigGateway#listConfigResultsByInstances(...)` instead of depending on `DemandQueryService`;
- query by `templateId`, business date, `SPACE`, and the requested space IDs;
- set `fillInstanceName` to `false`;
- map only `DemandConfigResultVO.instanceId` and `finalConfig` into temperature, humidity, carbon-dioxide, and
  pressure-difference InfoCode values;
- treat a result miss as no demand configuration and never fall back to `DemandConfigGateway` realtime inheritance
  calculation.

The result read API requires callers to choose one `instanceType`. Omitting `instanceIds` queries every result of that
type for the template/scene filter; the public API does not expose a cross-type all-object query.
When callers explicitly pass a large `instanceIds` collection, result Gateway/repository code must split the independent
instance-ID filter into 500-ID batches instead of letting the XML mapper build one large `IN` predicate.

Refresh code must use `DemandConfigGateway#listStableConfigsByInstances(...)` to build `finalConfigJson` and must disable
instance-name filling for that call. Result rows do not store or return instance names, so refreshing result data must
set `DemandConfigByInstanceBatchQueryParam#fillInstanceName` to `false`; existing realtime callers keep the default
name-filling and query-enrichment behavior unless they explicitly opt out.

Repository batch writes should use mapper-level chunked SQL, not MyBatis-Plus `saveBatch`, for potentially large result sets.
Repository update paths that close snapshots and update existing result rows must also be chunked; do not pass thousands
of IDs to `closeOpenSnapshots(...)` or `updateByIdValidate(...)` in one call.
When tuning `DemandConfigResultRepositoryImpl` batch sizes, distinguish SQL shape before increasing values:
business-key queries expand to one `OR` branch per result business key and should stay smaller than ID-based mutations;
result-ID delete/update and shielded-flag update paths use primary-key `IN` predicates and may use a moderately larger
chunk to reduce round trips, but still must avoid very large packets and long row-lock windows.

`DemandConfigResultRepository#upsert(...)` must maintain business-key uniqueness in application code because the
current result table intentionally has no `UNIQUE` constraint for cloud-edge sync compatibility. Before querying old
rows, deduplicate the input collection by `DemandConfigResultBusinessKey`; if the same key appears more than once in
one batch, the later item in iteration order wins. This keeps a repeated aggregate from inserting multiple current
rows in the same transaction.

Application-level locking is also required for concurrent refreshes. A single database transaction keeps one
execution's current row and snapshot rows consistent, but it does not prevent two concurrent transactions from both
querying "no existing row" and then inserting the same business key. Serialize result writes by both a template-level
lock (`projectId + relTemplateId + ALL_SCENE`) and a scene-level lock (`projectId + relTemplateId + relSceneId`),
acquire multiple locks in sorted key order, and keep the locks until the active Spring transaction completes. The
template-level lock is required for template deletion or other template-wide cleanup that has no concrete scene ID.
Releasing a distributed lock in a `@Transactional` method body before the proxy commits is not safe because another
transaction can query before the first commit is visible.
Write-scope lock acquisition must use a finite wait bound; indefinite blocking is forbidden. The current implementation
uses one 120-second total wait budget for all sorted locks in one write operation, not 120 seconds per lock. This budget
is sized for the maximum current air-conditioning scope of about ten thousand objects in one project/template and for
slow database batches that update both current rows and snapshots. If a lock cannot be acquired before the bound, log
the project, template, scene, and lock key, perform no query or write for that scope, and fail the caller so the
operation can be retried. Keep watchdog-managed or equivalent automatic lease renewal after acquisition instead of
using a fixed lease that may expire before the transaction completes.
Direct shielded-flag updates must use the same write-scope lock because they also close snapshots, update current rows,
and insert new snapshots.
Template/scene-scope deletes that are not expressed as individual business keys must still use the same write-scope
locks before closing snapshots and deleting current rows. When deleting all rows for a template type, prefetch scene
IDs once, iterate concrete template/scene boundaries, and page result IDs in fixed chunks; do not delete by a single
broad template-type query that loads all IDs at once or cannot take the same write-scope lock.
Template/scene batch reconciliation must calculate its delete difference inside the same write-scope lock and
transaction as the delete/upsert/snapshot writes. Realtime gateway calculation should remain outside the lock because
it may call RWD and other external services, but the repository must acquire the concrete template/scene lock before
querying current result rows. It must then derive `existingBusinessKeys - upsertBusinessKeys`, remove stale rows, and
reuse the same locked query result to distinguish inserts from updates. Do not query existing rows before acquiring
the lock and pass a precomputed delete list into the transaction; a concurrent refresh may insert a now-stale key
between that query and the lock acquisition. An empty upsert collection must still acquire the scope lock and delete
all current rows in that exact template/scene boundary.

DTX-triggered demand-result refresh uses realtime RWD queries. Demand-space lookup goes through
`DemandStrategy#listDemandSpace`, `DemandSpacesQueryParam`, and the ordinary
`DtpRwdClientService#listSpace(DtpRwdObjectQueryParam)` overload; building/floor/space hierarchy expansion likewise
uses the ordinary `listBuilding(param)`, `listFloor(param)`, and `listSpace(param)` overloads. These overloads do not
enable the dynamic `@Cacheable` query path and do not read the running-monitor `CacheRwdObject` table. Therefore:

- `DemandConfigResultRefreshEventListener` does not depend on `CacheDtxPublishChangedListener` or
  `CacheRwdBuildTriggerEvent` completing first.
- Do not serialize demand-result refresh behind running-monitor cache rebuilding unless a future demand strategy
  explicitly changes its data source to `CacheRwdObject` or a `queryCache = true` RWD overload.
- A realtime RWD query failure may still fall back to an empty collection when `isThrows = false`. Destructive cleanup
  based on an empty demand scope must distinguish a confirmed empty scope from an RPC failure; waiting for
  `CacheRwdObject` rebuilding is not a valid mitigation.

DTX event context is carried by `DtxPublishMessageEvent.Message`, not by an assumption that the publisher thread's
`ContextUtil` will remain available after `@Async` dispatch. `DtxPublishMessageEvent` must keep overriding
`getGroupCode()` and `getProjectId()` from its source message. The demand-result listener must build its lock key from
`event.getProjectId()`, then call `BaseEvent#executeWithLock(...)`; that method restores group, tenant, and project
context from the event before executing any repository query or write. With this order, demand-result refresh does not
depend on project context propagation by the async executor. Do not replace the explicit event-derived lock key with a
`ContextUtil.getProjectId()` lookup before `executeWithLock`. Other `BaseEvent` subclasses or listeners that build
keys from `ContextUtil` before event-based initialization require separate review and are not covered by this contract.

Do not expose a public "delete by result IDs" repository method for normal callers; result IDs do not carry enough
scope information to acquire the template/scene write locks. ID deletion should remain an internal step after a locked,
paged query has selected the current IDs to remove.

### Cloud Initialization Job

The first online rollout should initialize current stable `demand_config_result` rows from the cloud side and begin
snapshot history at the initialization write time.

Use a `TecXxJobHandler`-based XXL-JOB flow that is automatically registered through `InitXxlJob` but remains
manual-trigger-only; do not configure long-running scheduled execution:

- Expose `demandConfigResultInitTaskHandler` through a thin `JobTask` `@XxlJob("demandConfigResultInitTaskHandler")` method.
- `JobTask` must only delegate to `DemandConfigResultInitTaskHandler`; it must not contain group/project traversal, parameter parsing, sharding, database checks, or refresh business logic.
- `DemandConfigResultInitTaskHandler` should extend `BaseXXJob` and implement `TecXxJobHandler`, with initialization business logic kept inside the handler class.
- Reuse the existing `TecXxJobHandler` group/project traversal and XXL-JOB sharding path, but use the no-cron-check entry because initialization parameters are filters, not cron expressions, and empty parameters mean full initialization.
- Add this initialization job to `InitXxlJob` / `XxlJobVo` automatic registration so it is created in XXL-JOB during deployment.
- Mark the initialization job as manual-trigger-only: application startup may create or update the XXL-JOB definition, but must not start scheduled execution or trigger the handler. Operations should manually trigger it in XXL-JOB during the first rollout.
- Allow an empty XXL-JOB parameter for full initialization. Optional filter parameters support controlled gray runs or manual re-runs.
- "Full initialization" means every valid group/project/current supported template scope, not replaying every
  historical business date. It rebuilds current rows and creates or replaces their open snapshots from the execution
  day onward; it cannot backfill dates before initialization or repair previously closed snapshots.
- Reuse the existing group/project lookup and XXL-JOB sharding semantics so online runs can be split across executors or filtered to a specific group/project.
- Supported optional parameters are `groupCodes` and `projectIds`; absent fields mean no filtering.
- XXL-JOB parameter arrays are operator-filled and collection elements are treated as non-null; absent or empty collections mean no filtering.
- Inside each project, iterate `DemandConfigResultRefreshRepository#listSupportedTemplateTypes()` and call
  `refreshByTemplateType(...)`; the job must not hardcode air-conditioning or maintain a second support list.
- Keep template types serial in the job layer. The repository already has controlled per-template parallelism; adding project, template-type, and template parallelism together can overload RWD and database resources.
- Use the project-standard shared business thread pool for repository template parallelism; do not introduce a dedicated result-refresh executor.
- The job must set group, tenant, and project context before calling refresh logic, and clear context after each project.
- Keep existing `BaseXXJob#processHandle(...)` failure behavior unchanged for historical jobs. Only
  `processHandleWithoutTimeCheck(...)`, which is used by this one-time initialization, aggregates project failures:
  a failed project is logged, later projects continue, and the final XXL-JOB result is failure.
- Group/project context cleanup must stay in `finally`. A failed refresh or reused executor thread must not leak
  group, tenant, or project context into another project.
- Blank job parameters mean full initialization. Non-blank invalid JSON or a deserialized `null` parameter must fail
  fast; never convert a malformed operator input into an unfiltered full initialization.
- The debug initialization endpoint uses the current request group/project context and iterates
  `listSupportedTemplateTypes()`; it must not maintain a second hardcoded support list.
- Debug initialization and XXL-JOB initialization are both manually triggered. Operations must not run them concurrently; this operational contract is accepted and no additional global initialization lock is required in this task.
- XXL-JOB routing and task-internal sharding follow the existing framework contract. This task does not change the historical routing strategy.

### Logging and Performance Tracking

Demand config result refresh and initialization can cover many groups, projects, air-conditioning templates, and
instances. Normal-path logs must therefore stay quiet:

- Do not print per-project, per-template-type, per-template, per-instance, or per-batch success logs from the
  result refresh repository, listener, debug endpoint, or XXL-JOB handler.
- Keep failure logs at the public refresh boundary so production issues include enough scope to rerun or investigate
  (`templateType`, `templateId`, `sceneId`, `instanceType`, `instanceCount`, and `shielded` when relevant).
- Use `PerformanceTracker#recordStep(...)` / scoped nodes for timing diagnostics instead of normal log lines. Repository,
  listener, and XXL-JOB code must not actively print tracker summaries such as `prettyPrint(...)`; controller requests
  can rely on the existing request performance aspect.
- Do not add `PerformanceTracker` nodes to every helper method. Keep scoped tracker nodes at public refresh entrypoints,
  expensive realtime calculation boundaries, batch database write boundaries, and explicit parallel branches. Small
  conversion/query-param helper methods should be represented by the caller's `recordStep(...)` only when the step is
  useful for diagnosing real production latency.
- Do not log final config JSON, inheritance JSON, full instance ID collections, or full result objects. If a temporary
  diagnostic log is ever needed, log only aggregated counts and remove it before merging unless it is an error path.

### 3. Contracts

`DemandConfigResult` rows represent one stable calculated config for a business key:

- `projectId`: current project from `ContextUtil.getProjectId()`.
- `demandTemplateType`: template type from the resolved template/identifier.
- `demandInstanceType` + `demandInstanceId`: target instance key.
- `relTemplateId`: resolved template ID.
- `relSceneId`: nullable scene ID. Null means no scene.
- `finalConfigJson`: stable inherited config JSON before query-time calendar / DTX / RWD / locale enrichment.
- `inheritInstancesJson`: inheritance chain and modes used to produce the final config.
- `isCustomizedConfig`: real value from `DemandConfigAggregate#isCustomizedConfig()`.
- `isShielded`: real shielding value for the target space and template type.

Do not set `isCustomizedConfig` or `isShielded` through generic default-value helpers. These fields are business values, not persistence defaults.

The current result-precompute scope is air-conditioning only. Air-conditioning has no scenes, so every currently
generated result uses `relSceneId == null`; refresh code must not query or iterate `demand_scene`. The shared
result-read parameter contract remains unchanged: `sceneId == null` means no scene filtering, while a non-null value
filters by the real scene ID. Result business keys, snapshot queries, and write-scope locks must keep this non-null
scene dimension even though current air-conditioning entrypoints do not use it. Lighting scene support is a future
extension and must define template/scene creation, deletion, reconciliation, and migration rules before enabling any
lighting event listener or result calculation.

`DemandInstanceConfigChangeEvent` carries both blacklist and shielded-space changes through `Param#changeType`.
Listeners must branch on this field before doing side effects:

- `BLACKLIST`: spaces in `inInstanceIds` enter the blacklist and must delete original demand configs and result rows directly; spaces in `outInstanceIds` leave the blacklist and must recompute result rows.
- `SHIELDED`: must not delete original demand configs and must not recompute final config JSON; only update result rows' `is_shielded` value.

Template/template-type batch refresh is a reconciliation flow, not an append-only upsert flow. Before writing
new stable results, calculate stable aggregates outside the database transaction, then enter
`DemandConfigResultRepository#syncRefreshScope(...)`. That method must acquire the current project + `relTemplateId` +
`relSceneId` write-scope lock before querying existing result business keys with narrow columns. Upsert only realtime
aggregates with non-null `finalConfig`; delete existing business keys that are no longer present in those valid
realtime aggregates and close their open snapshots. This also applies when realtime calculation returns an empty
aggregate list for that refresh boundary.
Template-type refresh should load the air-conditioning template ID list once and must not query the scene table.
It should also prefetch the demand-scope object IDs once for the template type. If that scope is empty, delete old
results for that template type by concrete no-scene template boundaries; do not let an empty scope fall through to an
all-object realtime query. When building the prefilled scope map, omit instance types whose ID set is empty because
an empty set has "all objects of this type" meaning in the shared realtime query parameter.
After the scope is known, query shielded space IDs once for the template type and scope, freeze the result as a
non-null immutable set, and reuse it across all parallel template tasks. An empty set means that the scope has no
shielded spaces; `null` must not be used as a "not prefetched" sentinel. Single-instance, configuration-incremental,
DTX, and standalone template refreshes should continue querying shielding for their actual affected aggregate range.
`refreshByTemplateType(...)` is an orchestration boundary and must not open one outer Spring transaction around
template lookup, RWD calls, shielded-space prefetch, and parallel-task waiting. Each template task enters
`DemandConfigResultRepository#syncRefreshScope(...)` on the repository bean so its query, snapshot close, result
delete/update/insert, and snapshot insert remain atomic under the concrete template/scene lock. A child-task failure
must fail the caller, but already committed sibling templates are reconciled by an idempotent rerun rather than a
fictional cross-thread rollback.
Template-type refresh may process templates with bounded parallelism, but the persistence transaction boundary is the
single template refresh. Keep the repository-level template-ID concurrency small and fixed at 3 by default: this
only applies inside one project and one template type, while job-layer template types remain serial. Production scale can
reach about 89 groups, 149 projects, 4-10 air-conditioning templates per project, and 10k demand objects, so increasing
this window further can multiply RWD calls, database writes, and shared business-thread-pool
pressure across XXL-JOB shards. This keeps database/RWD usage controlled and makes failed runs idempotently rerunnable
without pretending the whole template type is one cross-thread transaction.

Only air-conditioning template metadata events participate in result precompute. Template `INSERT` may initialize that
template's result rows and template `REMOVE` must delete rows by old template ID. Template `UPDATE` / `APPLIED` do not
change the final config JSON or result business key and should not trigger recomputation. Scene events are lighting-only
and must not be handled by the current result listener.

Configuration change events may contain multiple instances. Listeners should group them by `DemandInstanceTypeEnum`
before calling refresh, so child-instance expansion, realtime inheritance calculation, and result writes are not
repeated once per instance.

Project-level air-conditioning humidity/pressure-difference cleanup may parse one stored-configuration read and reuse
the parsed objects when both switches are disabled. It must still write humidity first and pressure difference second,
including configurations whose target fields are already empty when an environment-quality node exists, so audit
fields and `demand_config_snapshot` triggers retain both transitions. These internal cleanup batches do not publish
events; the project configuration write that follows them publishes the final event, and project result refresh expands
to all descendants from the fully persisted state.

Air-conditioning shell-configuration backwrite remains synchronous: `saveOrUpdateConfigData(...)` must complete the
backwrite before returning. Performance work inside that path may remove redundant reads or RWD calls, but must not
move execution after commit, change failure handling, or alter caller-visible timing and consistency.
`getConfigData(...)` rebuilds work-condition systems from the current RWD response before shell backwrite, so the
resulting system `classCode` is authoritative for distinguishing `ACAC_SYSTEM` / `ACAH_SYSTEM`; shell mapping must reuse
that value instead of querying the same system IDs again.

### 4. Validation & Error Matrix

| Condition | Behavior |
|-----------|----------|
| Input instance collection is empty | Return early |
| Gateway returns no aggregate for requested instances | Delete existing result rows for those business keys |
| Aggregate has null final config | Do not upsert it; delete stale matching result where needed |
| Template batch refresh has old rows missing from valid realtime aggregates | Delete stale rows in that no-scene template refresh boundary |
| Template batch refresh returns no aggregate | Delete existing rows in that no-scene template refresh boundary |
| Upsert input contains duplicate business keys | Keep one row per business key; the later input item wins |
| Concurrent refreshes target the same project/template/scene | Serialize query + delete/update/insert + snapshot writes with a distributed lock held until transaction completion |
| Write-scope lock is not acquired within the finite wait bound | Log project/template/scene/lock key, perform no scope query or write, and fail the caller for retry |
| Write-scope lock is acquired | Keep watchdog-managed or equivalent automatic lease renewal until transaction completion |
| Template `UPDATE` / `APPLIED` | Do not recompute result rows |
| Air-conditioning template `REMOVE` | Delete result rows by the old template ID |
| Non-air-conditioning config/template/scene/instance event | Return without result calculation or writes |
| Template lookup returns null for a required template ID | Throw `ApplicationException` with invalid-input semantics |
| Result API template lookup by ID, tag, or date returns null | Log one project-scoped `WARN`, return an empty list, and do not query result/config/RWD paths |
| Result API receives `templateId`, `templateTag`, and `date` | Resolve by `templateId` first |
| Result API receives `templateTag` and `date` without `templateId` | Resolve by `templateTag` first |
| Result API receives `templateType + date` only | Resolve the calendar template, then query by its resolved template ID |
| Result API omits `instanceType` | Reject the request before querying result/config/RWD paths |
| Result API receives `instanceType` with null/empty `instanceIds` | Query all precomputed results of that type |
| Result API receives null elements inside `instanceIds` | Filter them into a new set before validation; a cleaned empty collection queries all results of the required type |
| Result API omits `filterShieldedSpaces` | Query only `is_shielded = false` rows |
| Result API sets `filterShieldedSpaces=false` | Include shielded rows and return their persisted `shielded` state |
| Result API omits `queryInstanceTag` | Do not query custom tags and omit `instanceTag` from the response |
| Result API sets `queryInstanceTag=true` | Read instance-config flags in the frontend object scope, append requested blacklisted objects missing from result storage, apply shield filtering to appended rows, then batch query custom tags once without per-row external calls |
| Result API returns air-conditioning rows | Apply the shared query enricher after JSON deserialization |
| Air-conditioning result API queries `PROJECT` | Fill dated project custom configuration and current DTX/RWD work condition once |
| Air-conditioning result API queries another instance type | Filter calendar seasons without calling the project custom-work-condition service |
| Result refresh calculates air-conditioning rows | Use the stable Gateway entry and do not apply query-time enrichment |
| Result API receives `fillInstanceName=true` | Keep lightweight-VO behavior; do not invoke RWD name filling |
| Historical result date predates the first valid snapshot window | Return a miss; do not use realtime calculation or the current main row |
| Template type has no templates | Return early |
| Template type has no demand-scope spaces | Delete existing rows for that template type; do not query all instances |
| Shielded flag changes | Update result rows' shielded flag directly |
| `DemandInstanceConfigChangeEvent#changeType == SHIELDED` | Do not delete original demand configs |
| `DemandInstanceConfigChangeEvent#changeType == BLACKLIST` and `inInstanceIds` is not empty | Delete original demand configs and result rows for entered blacklist spaces |
| `DemandInstanceConfigChangeEvent#changeType == BLACKLIST` and `outInstanceIds` is not empty | Refresh result rows for spaces leaving the blacklist |
| Cloud initialization thin `JobTask` entry delegates to `DemandConfigResultInitTaskHandler` | Wait for operations to manually trigger it from XXL-JOB |
| Cloud initialization execution fails or any project fails | Return XXL-JOB failure so operations can inspect logs and rerun |

### 5. Good/Base/Bad Cases

Good:

```java
DemandTemplate template = param.queryAndGetTemplate();
if (Objects.isNull(template)) {
    this.logProjectInstance().warn("未查询到需求模板，返回空的需求配置结果", Arrays.asList(
            Pair.of("templateType", param.getTemplateType()),
            Pair.of("templateId", param.getTemplateId()),
            Pair.of("templateTag", param.getTemplateTag()),
            Pair.of("date", param.getDate())
    ));
    return Collections.emptyList();
}
queryParam.setRelTemplateId(template.getId());
```

Good:

```java
Map<DemandInstanceTypeEnum, Set<String>> scopeIds = this.toDemandScopeInstanceTypeAndInstanceIds(templateType);
if (MapUtils.isEmpty(scopeIds)) {
    this.deleteResultsByTemplateType(templateType);
    return;
}
for (Long templateId : templateIds) {
    DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
    queryParam.setTemplateId(templateId);
    queryParam.setInstanceTypeAndInstanceIds(this.copyInstanceTypeAndInstanceIds(scopeIds));
    queryParam.setAutoFillEmptyInstanceIds(false);
    this.refreshByQueryParam(queryParam);
}
```

Base:

```java
DemandConfigResult result = new DemandConfigResult();
result.setProjectId(ContextUtil.getProjectId());
result.setIsCustomizedConfig(aggregate.isCustomizedConfig());
result.setIsShielded(this.isShielded(identifier.getTemplateType(), instance));
```

Base:

```java
List<DemandConfigResult> deduplicatedDeleteParams = this.deduplicateDeleteParams(deleteParams, upsertParams);
List<DemandConfigResult> deduplicatedUpsertParams = this.deduplicateByBusinessKey(upsertParams);
List<String> acquiredLockKeys = new ArrayList<>();
try {
    this.lockWriteScopes(deduplicatedDeleteParams, deduplicatedUpsertParams, acquiredLockKeys);
    this.removeResultsByBusinessKeys(deduplicatedDeleteParams);
    this.upsertSameProject(deduplicatedUpsertParams);
} finally {
    this.releaseLocksAfterTransactionCompletion(acquiredLockKeys);
}
```

Bad:

```java
// ❌ Recomputes every template by repeatedly resolving the full demand-scope space set.
for (Long templateId : templateIds) {
    queryParam.setAutoFillEmptyInstanceIds(true);
    this.refreshByQueryParam(queryParam);
}
```

Bad:

```java
// ❌ Business values cannot be filled as defaults.
result.setIsCustomizedConfig(false);
result.setIsShielded(false);
```

### 6. Tests Required

When changing this flow, verify:

- `refreshInstances` upserts rows for aggregates with final config.
- `refreshInstances` deletes stale rows when no aggregate/final config exists for a requested instance.
- `refreshByTemplate` and `refreshByTemplateType` delete old rows that no longer appear in valid realtime aggregates for the current no-scene template boundary.
- Batch refresh with an empty realtime aggregate list deletes existing rows in the current no-scene template boundary instead of returning early.
- Batch refresh acquires the concrete template/no-scene write lock before querying current result rows and calculating the delete difference.
- A concurrent refresh that inserts a key between realtime calculation and lock acquisition cannot leave that key behind when it is absent from the locked refresh target.
- Scope refresh reuses the locked existing-result map for insert/update splitting instead of querying the same business keys again.
- `refreshByTemplateType` preloads demand-scope instance IDs once per template type; when the scope is empty it deletes old rows for that template type and must not fall back to all-instance querying.
- `refreshByTemplateType` queries shielded space IDs once per template type scope and reuses the immutable set across
  parallel template refreshes; single-instance and incremental refreshes still query their actual affected scope.
- `refreshByTemplateType` itself has no outer `@Transactional`; each parallel template write still enters the
  transactional `DemandConfigResultRepository#syncRefreshScope(...)` boundary.
- Initialization, debug, DTX, config, template, blacklist, and shielded-event paths ignore demand types outside the
  supported-demand-type collection.
- Disabling project humidity and pressure-difference quality together reads stored configurations once and performs
  two batch writes in humidity-then-pressure order, including configurations whose target fields are already empty
  when an environment-quality node exists. Internal cleanup events stay suppressed and the final project event covers
  project and descendant refresh.
- Air-conditioning custom-config saves complete shell-configuration backwrite synchronously before returning.
- Shell backwrite classifies air-conditioning source systems from the current RWD-built work-condition `classCode`
  without another ID-based RWD query.
- The current result listener does not subscribe to scene-change events.
- Repository business keys, current/history reads, snapshot writes, and lock keys isolate non-null real `sceneId` values without enabling lighting listeners or calculation.
- DTX publish refreshes demand config results, while DTX rule publish does not recompute them.
- `isCustomizedConfig` and `isShielded` preserve real aggregate/shielding values.
- `DemandConfigResultRepository#upsert(...)` deduplicates repeated business keys before insert/update splitting.
- Concurrent upserts for the same project/template/scene cannot release the application lock before the database
  transaction has completed.
- Lock acquisition times out within the configured finite wait, performs no scope write on timeout, and reports enough
  scope information for retry; acquired locks keep watchdog-managed or equivalent automatic renewal.
- Batch inserts call the shared insert-default helper before XML `insertBatch`.
- Cloud initialization exposes a thin `JobTask` `@XxlJob("demandConfigResultInitTaskHandler")` entry, delegates all business logic to `DemandConfigResultInitTaskHandler`, registers the job definition through `InitXxlJob`, and does not start scheduled execution or trigger it automatically.
- Cloud initialization with empty parameters initializes all valid groups/projects according to XXL-JOB sharding.
- Cloud initialization returns failure when any project fails so operations can inspect logs and rerun.
- `/demand/config/listConfigResultsByInstances` returns only `instanceId`, `shielded`, optional `instanceTag`, and `finalConfig`, and does not trigger instance-name filling.
- `/demand/config/listConfigResultsByInstances` resolves templates through `templateId > templateTag > date`, uses the
  resolved template ID for result queries, and logs then returns an empty list when no template is found.
- Result-query tests cover typed empty-ID scope, explicit IDs, required instance-type validation,
  null-ID filtering into a copied collection, null-only all-of-type semantics, scene/shield propagation,
  default shield filtering, explicit shield inclusion with `shielded` response propagation, default no-tag behavior,
  opt-in batched `instanceTag` propagation, blacklisted objects absent from result storage, shield filtering of
  appended blacklisted rows, `fillInstanceName=false` default and non-operation, no realtime/RWD fallback, and
  500-ID result-query batching.
- `/demand/config/listConfigResultsByInstances` supports historical dates by routing result-table reads to
  `demand_config_result_snapshot` with the same SCD2 windowing style as `DemandConfigMapper` / `DemandSceneMapper`.
- Historical result tests keep dates before the first initialization/write or outside every valid snapshot window as
  misses, without realtime fallback or current-row substitution. Initialization tests do not claim to repair closed
  snapshots or backfill pre-initialization history.
- Result refresh calls `DemandConfigGateway#listStableConfigsByInstances(...)`; it must not call the ordinary realtime
  query entry that applies air-conditioning query-time enrichment.
- The stable Gateway entry forces `queryInheritConfig=true` and `fillInstanceName=false`, skips only
  `DemandConfigAirconditoningAware#afterAggregateInit(...)`, and still invokes other lifecycle hooks.
- Ordinary and origin Gateway queries invoke the original `DemandConfigAirconditoningAware` exactly once and never
  invoke `DemandConfigAirconditioningQueryEnricher`; the enricher is used only by the result-read Gateway entry.
- `DemandQueryServiceImpl#listConfigResultsByInstances(...)` delegates to the Gateway without retaining result
  repository, tag conversion, or query-time enrichment logic.
- `DemandConfigInfoCodeAnalyzeStrategy` injects `DemandConfigGateway`, not `DemandQueryService`, and calls the result
  entry exactly once for one requested object batch.
- Aggregate-to-result conversion strips air-conditioning `valueSelect` / `workCondition` from the JSON node even when
  legacy raw configuration contains those fields, without changing the aggregate's business flags.
- Air-conditioning result reads apply `DemandConfigAirconditioningQueryEnricher` after JSON deserialization with the
  resolved template ID, requested instance type, and requested/effective date.
- Historical air-conditioning reads use calendar day tags to filter temperature seasons and keep the matching custom
  season value; current/future reads use the global calendar rule.
- Project result reads fill `valueSelect` / `workCondition` once through the project custom-config service, while
  space/floor/building reads do not call that service.
- Project query-time work-condition enrichment leaves `instanceTag.customConfig` equal to the persisted raw-config
  customization flag.
- Non-air-conditioning result reads skip the air-conditioning enricher without calendar, DTX, or RWD calls.
- Original `DemandConfigAirconditoningAware` compatibility tests pass a null config date and aggregate project ID through
  to the project custom-config service, stop the whole
  season-rewrite batch for null/incomplete temperature-quality aggregates, and do not eagerly load default seasons
  before that abort.
- `DemandConfigInfoCodeAnalyzeStrategy` reads through `DemandConfigGateway#listConfigResultsByInstances`, keeps seasonal
  air-conditioning mapping behavior, disables instance-name filling, and does not fall back to realtime calculation.

### 7. Wrong vs Correct

#### Wrong

```java
// DTX rule-derived work conditions are enriched at query time and must not rewrite stable result JSON.
public void onDtxRulePublishEvent(@NonNull DtxRulePublishMessageEvent event) {
    this.refreshDemandConfigResults(event);
}
```

#### Correct

```java
public void onDtxRulePublishEvent(@NonNull DtxRulePublishMessageEvent event) {
    this.publishCacheRwdBuildTriggerEvent(event, event.getData().getRunningMonitorType());
}

public class DemandConfigAirconditioningQueryEnricher {
    public <T extends BaseDemandConfig> void enrichResults(
            @NonNull List<DemandConfigResultVO<T>> results,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Long templateId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable LocalDate configDate);
}
```

#### Wrong

```java
// Applying the result-read enricher to aggregates changes every legacy Gateway caller.
this.demandConfigAirconditioningQueryEnricher.enrichAggregates(aggregates);
```

#### Correct

```java
// Ordinary and origin queries keep the original complete lifecycle invocation.
this.demandConfigLifeAware.forEach(item -> item.afterAggregateInit(aggregates));

// The new stable entry skips only the legacy query-time air-conditioning hook.
this.demandConfigLifeAware.stream()
        .filter(item -> !(item instanceof DemandConfigAirconditoningAware))
        .forEach(item -> item.afterAggregateInit(aggregates));

// Only the result-read entry enriches deserialized lightweight result VOs.
this.demandConfigAirconditioningQueryEnricher.enrichResults(
        resultVOs, templateType, templateId, instanceType, configDate);
```

#### Wrong

```java
// Persists calendar-filtered and current DTX/RWD-derived data.
List<DemandConfigAggregate<T>> aggregates =
        this.demandConfigGateway.listConfigsByInstances(queryParam);
```

#### Correct

```java
List<DemandConfigAggregate<T>> aggregates =
        this.demandConfigGateway.listStableConfigsByInstances(queryParam);
```

#### Wrong

```java
// Omitting the shield condition returns shielded objects by default and loses their response state.
queryParam.addColumns(DemandConfigResult::getDemandInstanceId,
        DemandConfigResult::getFinalConfigJson);
```

#### Correct

```java
queryParam.setIsShielded(param.isFilterShieldedSpaces() ? Boolean.FALSE : null);
queryParam.addColumns(DemandConfigResult::getDemandInstanceId,
        DemandConfigResult::getFinalConfigJson,
        DemandConfigResult::getIsShielded);
```

#### Wrong

```java
JsonNode finalConfigJson = this.objectMapper.convertValue(aggregate.getFinalConfig(), JsonNode.class);
```

#### Correct

```java
JsonNode finalConfigJson = ObjectMapperUtils.convertValue(aggregate.getFinalConfig(), JsonNode.class);
```

#### Wrong

```java
public void onDemandInstanceConfigChangeEvent(@NonNull DemandInstanceConfigChangeEvent event) {
    DemandInstanceConfigChangeEvent.Param param = (DemandInstanceConfigChangeEvent.Param) event.getSource();
    if (CollectionUtils.isEmpty(param.getInInstanceIds())) {
        return;
    }
    // ❌ SHIELDED events also have inInstanceIds. This deletes original configs for shielded spaces.
    this.demandConfigRepository.delConfigByInstance(removeParam);
}
```

#### Correct

```java
public void onDemandInstanceConfigChangeEvent(@NonNull DemandInstanceConfigChangeEvent event) {
    DemandInstanceConfigChangeEvent.Param param = (DemandInstanceConfigChangeEvent.Param) event.getSource();
    if (param.getChangeType() != DemandInstanceConfigChangeTypeEnum.BLACKLIST) {
        return;
    }
    if (CollectionUtils.isEmpty(param.getInInstanceIds())) {
        return;
    }
    this.demandConfigRepository.delConfigByInstance(removeParam);
}
```

#### Wrong

```java
public List<DemandConfigResultVO<T>> listConfigResultsByInstances(
        DemandConfigByInstanceBatchQueryParam param) {
    // Forces templateId and bypasses templateTag/date resolution.
    queryParam.setRelTemplateId(param.getTemplateId());
}
```

#### Correct

```java
public List<DemandConfigResultVO<T>> listConfigResultsByInstances(
        DemandConfigResultBatchQueryParam param) {
    DemandTemplate template = param.queryAndGetTemplate();
    if (Objects.isNull(template)) {
        this.logProjectInstance().warn("未查询到需求模板，返回空的需求配置结果", Arrays.asList(
                Pair.of("templateType", param.getTemplateType()),
                Pair.of("templateId", param.getTemplateId()),
                Pair.of("templateTag", param.getTemplateTag()),
                Pair.of("date", param.getDate())
        ));
        return Collections.emptyList();
    }
    queryParam.setRelTemplateId(template.getId());
}
```

---

## Demand Template Lookup

Business code should not call deprecated template repository methods such as `DemandTemplateRepository#getByTemplateId(...)` for current-template resolution.

```java
// ❌ FORBIDDEN
DemandTemplate template = this.demandTemplateRepository.getByTemplateId(true, templateId);
```

```java
// ✅ CORRECT
BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
templateParam.setTemplateId(templateId);
DemandTemplate template = templateParam.queryAndGetTemplate();
if (Objects.isNull(template)) {
    throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
            LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
}
```

Use `BaseDemandTemplateParam#queryAndGetTemplate()` so the lookup follows the current demand-template resolution rules.
