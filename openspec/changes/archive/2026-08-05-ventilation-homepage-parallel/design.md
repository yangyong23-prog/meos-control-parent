## Context

通风值班首页 `VentilationHomePageCardDataHandler#execute` 当前三个外部调用全串行执行：

1. `queryHomePageCardGroupIotData(queryMap)` — 聚合接口 Feign 调用（~1-2s）
2. `buildCardTagVO(dutyHomePageGroup, objIds)` — 内部调用 `getExceptionEventCountByObjIds` Feign（~1-8s），被锁在父类方法内部，必须等 ① 完成
3. `queryCanJump(childCardList)` — `queryDemandTemplateType` 查询（耗时不确定）

照明 handler (`LightHomePageCardDataHandler`) 已在同一分支 `feat_v7.260801.0_通风&照明值班首页性能优化` 完成同类优化（L211-254），采用 `CompletableFuture.supplyAsync` + `BusinessThreadPoolExecutor.ttlExecutor()` + `allOf().join()` 并行模式，并砍除 `queryCanJump` 和大卡片 `offline/alarm`。本变更将通风 handler 对齐照明 handler 的优化模式。

前置条件：`ventilation-cache-object-model`（28/28 完成）已将 `queryEquipGroupId` 优化为本地缓存读取（READY 时 0 Feign），本变更不再涉及该阶段。

## Goals / Non-Goals

**Goals:**
- 将异常事件查询与聚合接口从串行改为并行，wall-clock 从 sum(②, ③) 降为 max(②, ③)
- 砍除 `queryCanJump` 调用（`queryDemandTemplateType`），改为 `setCanJump(true)` 写死
- 砍除大卡片 `offlineTotal`/`alarmTotal`，`CardTagVO` 只设 `errorEventTotal`
- 增加短路保护：聚合接口返回 null 时直接 return

**Non-Goals:**
- 不修改任何被调用方法的函数体（`getExceptionEventCountByObjIds`、`queryHomePageCardGroupIotData`、`buildChildCardDateVO`、`buildCardTagVO` 等均原封不动）
- 不添加 `StopWatch` 性能埋点（独立事项，不纳入本变更范围）
- 不涉及 `queryEquipGroupId` 阶段的优化（已由 `ventilation-cache-object-model` 完成）
- 不涉及照明 handler 的改动（照明已完成优化）

## Decisions

### 决策 1：并行模式复用照明验证过的 CompletableFuture + ttlExecutor

**选择**：`CompletableFuture.supplyAsync(() -> getExceptionEventCountByObjIds(objIds), BusinessThreadPoolExecutor.ttlExecutor())` + 主线程跑聚合接口 + `allOf().join()`

**理由**：照明 handler 在同一分支已验证此模式，TTL 线程池保证 `ContextUtil` 上下文（projectId/groupCode）正确传递到异步任务的 Feign 调用。无需引入新模式或新线程池。

**替代方案**：不并行，仅砍除 `queryCanJump` 和 `offline/alarm`。放弃，因为并行化的收益（串行→并行）是确定性的，且照明已验证可行性。

### 决策 2：绕过父类 buildCardTagVO，内联组装 CardTagVO

**选择**：在 `execute()` 中直接 `new CardTagVO()` + `tag.setErrorEventTotal(errorEventFuture.join())`，不调用父类 `buildCardTagVO`。

**理由**：`buildCardTagVO` 内部同时调用 `getExceptionEventCountByObjIds`（需并行化）和读取 `dutyHomePageGroup` 的 `offLineTotal`/`alarmTotal`（需砍除）。如果继续调用 `buildCardTagVO`，则两个目标都无法实现。绕过它不修改其函数体，其他 handler 仍可使用。

**替代方案**：修改 `buildCardTagVO` 签名接受预计算的 errorEventCount 参数。放弃，因为这会修改父类方法，违反"不改被调用方法函数体"的约束，且可能影响其他 handler。

### 决策 3：砍除 queryCanJump，写死 setCanJump(true)

**选择**：移除 `queryCanJump` 方法及其对 `queryDemandTemplateType` 的调用，改为 `childCardList.forEach(card -> card.setCanJump(true))`。

**理由**：产品已确认通风与照明一致处理（照明 L256-259 注释："产品已确认移除"）。`queryDemandTemplateType` 在照明实测中耗时 19s+，砍除后直接消除该调用。照明 L402-403 注释说明："默认都能跳转，主要是用于前端固定使用，不然前端就得去掉这个判断逻辑"。

### 决策 4：聚合接口 null 短路保护

**选择**：`dutyHomePageGroup == null` 时直接 `return resultVo`，丢弃未完成的 `errorEventFuture`。

**理由**：照明 L228-232 已验证此模式。`CompletableFuture` 被丢弃不影响正确性（JVM GC 会回收），且聚合接口失败时后续 `dutyHomePageGroup.getSubStat()` 会 NPE，必须短路。

## Risks / Trade-offs

- **[TTL 上下文丢失]** → 异步任务中的 Feign 调用依赖 `ContextUtil.getProjectId()` 等上下文。照明已验证 `BusinessThreadPoolExecutor.ttlExecutor()` 能正确传递，风险极低。
- **[异步任务异常未捕获]** → `getExceptionEventCountByObjIds` 内部 Feign 调用如果抛异常，`CompletableFuture` 会包装为 `CompletionException`，在 `join()` 时抛出。需确保异常被正确处理或传播。照明未做额外异常处理（直接 `join()`），通风保持一致。
- **[大卡片 offline/alarm 前端展示缺失]** → 前端可能依赖 `offlineTotal`/`orderFailAlarmTotal` 字段。照明已砍除且产品确认，通风同样处理。如前端有差异化需求，需单独评估。
