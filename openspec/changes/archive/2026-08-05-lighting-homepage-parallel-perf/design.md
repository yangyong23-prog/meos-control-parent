## Context

`LightHomePageCardDataHandler.execute()` 是照明值班首页的核心查询方法，当前采用串行执行模式：

```
阶段1(1,901ms) → 阶段2(1,358ms) → 阶段3(1,645ms) → 阶段4(5,408ms) → 阶段5(4,559ms)
总计: 14,872ms
```

经分析发现：
- 阶段2（聚合接口）、阶段3（异常事件查询）、阶段4中的偏差查询和需求模版查询**均只依赖阶段1的产出**
- `buildCardTagVO` 中的 `offlineTotal` 和 `orderFailAlarmTotal` 前端不展示，照明 Handler 不设这两个字段后阶段3也不再依赖阶段2
- 阶段5（`queryCanJump`）产品已确认可以去掉
- 项目已有 `BusinessThreadPoolExecutor.ttlExecutor()` 支持 CompletableFuture 并行

## Goals / Non-Goals

**Goals:**
- 将 execute() 中四个独立查询改为主线程 + 三路线程池并行，总耗时从 ~14.9s 降至 ~6.2s
- 注释掉阶段5 `queryCanJump` 调用
- 保持最小改动量，只修改 `LightHomePageCardDataHandler.java` 一个文件（不动基类 `AbstractHomePageCardDataHandler`）

**Non-Goals:**
- 不优化 `fillControlDeviationStatus` 内部的三路并行逻辑（已经并行化了）
- 不优化 `mapCircuitIdAndTag` / `listInfoCodeValueInOneHour` 等底层 Feign 接口性能
- 不优化阶段1的 `cacheObjectRepository.mapByCacheKeys` 缓存查询性能
- 不修改基类 `AbstractHomePageCardDataHandler` 的公共方法签名
- 不改变 API 返回结构

## Decisions

### 决策1: 主线程 + 三路线程池并行

**选择**: 在阶段1完成后，聚合接口在主线程直接执行，其余 3 个查询提交线程池：
- `[A]` queryHomePageCardGroupIotData（聚合接口）— **主线程执行**
- `[B]` getExceptionEventCountByObjIds（异常事件查询）— 线程池
- `[C]` queryControlDeviationData（偏差查询，从 fillControlDeviationStatus 拆出）— 线程池
- `[D]` getDemandTemplateType（需求模版查询）— 线程池

主线程执行完 A 后，`CompletableFuture.allOf(B, C, D).join()` 等待其余三路完成，然后本地组装结果。

**替代方案1**: 四路全部提交线程池，主线程空等。
**不选原因**: 主线程白白浪费，多占一个线程池线程。

**替代方案2**: 只并行 A+C+D，保留阶段3串行在阶段2之后。
**不选原因**: 移除 offlineTotal/orderFailAlarmTotal 后 B 也不再依赖 A，可以完全并行，额外节省 1,645ms 被隐藏在 4,246ms 窗口内。

### 决策2: 拆分 fillControlDeviationStatus 为查询+应用

**选择**: 提取新方法 `queryControlDeviationData(queryMap)` 只做远程查询返回中间 Map，原方法保留清空和写入逻辑改名为 `applyDeviationResults`。

**替代方案**: 保持原方法不变，整体作为 Future 在 childCardList 就绪后执行。
**不选原因**: 这样偏差查询必须等聚合接口完成，浪费 1,358ms 的并行窗口。

### 决策3: 注释掉而非删除 queryCanJump

**选择**: 注释掉 `queryCanJump` 调用，保留方法代码。

**替代方案**: 直接删除。
**不选原因**: 产品可能需要恢复，注释更安全，后续版本再决定是否彻底移除。

### 决策4: 使用 BusinessThreadPoolExecutor.ttlExecutor()

**选择**: 复用项目已有的 TTL 线程池，与 `fillControlDeviationStatus` 内部并行保持一致。

**替代方案**: 新建专用线程池。
**不选原因**: 不必要，现有线程池已支持 ContextUtil 上下文传递。

### 决策6: 不修改基类 buildCardTagVO，照明 Handler 内联构造 CardTagVO

**选择**: 不修改 `AbstractHomePageCardDataHandler.buildCardTagVO()`，照明 Handler 在 execute() 中直接 `new CardTagVO()` 只设 `errorEventTotal`，`offlineTotal` 和 `orderFailAlarmTotal` 保持 int 默认值 0。

**前提**: 前端已确认照明值班首页不展示 `offlineTotal`（离线设备数）和 `orderFailAlarmTotal`（控反不一致数），返回 0 不影响前端展示。

**替代方案**: 修改基类 `buildCardTagVO` 移除两行赋值。
**不选原因**: `buildCardTagVO` 被 10 个 Handler 共用，其中通风、空气终端、厨房、冷源、热源 5 个 Handler 传入真实 `dutyHomePageGroup`，修改基类会波及这些模块的 `CardTagVO` 返回值，影响面不可控。照明 Handler 内联构造完全隔离了改动范围。

### 决策5: 夜景照明路径消除冗余 Feign 调用

**选择**: 将 `fillControlDeviationStatus` 夜景照明路径中的 `mapCircuitIdAndInfoCodeValues` 调用从 `forEach` 内部提到外部，同时去掉多余的 `LightingCircuitTagVO` 中间对象。

**分析**: 当前 `forEach(templateType)` 循环内有 3 个 `isNightLighting` 枚举值（BUILDING / LANDSCAPE / SIGN），每次循环都调用 `mapCircuitIdAndInfoCodeValues(circuitIds, CONTROL_DEVIATION_STATUS)`，入参完全相同，导致 3 次冗余 Feign 调用。提到外部后只调用 1 次。

**业务逻辑不变**: 入参相同所以返回数据相同；循环内部只读取不修改，多次读取与一次读取结果一致；`LightingCircuitTagVO` 只是中间包装，去掉后计算链不变。

**替代方案**: 保持现状。
**不选原因**: 当前夜景照明为 0 回路所以不耗时，但这是正确写法，避免将来有回路时产生 3 倍冗余调用。

## Risks / Trade-offs

- **[风险] 聚合接口返回 null 时的提前退出逻辑** → 聚合接口在主线程执行，null 检查后直接 return，其余三路 Future 的结果被丢弃，与改造前行为一致。
- **[风险] 线程池饱和** → 三路提交线程池 + fillControlDeviationStatus 内部三路并行 = 最多 6 个并发任务（主线程不占线程池）。ttlExecutor 核心线程 16，远未达到上限。
- **[风险] fillControlDeviationStatus 内部的清空逻辑** → 原方法开头会 `setMoreOpenTotal(0) / setLessOpenTotal(0)`，拆分后 applyDeviationResults 仍需保留这个清空操作，确保放在 `buildChildCardDateVO` 之后执行。
- **[已确认] offlineTotal/orderFailAlarmTotal 返回 0** → 前端已确认照明值班首页不展示这两个字段，返回 int 默认值 0 不影响展示。基类 `buildCardTagVO` 不修改，其他 Handler 不受影响。
