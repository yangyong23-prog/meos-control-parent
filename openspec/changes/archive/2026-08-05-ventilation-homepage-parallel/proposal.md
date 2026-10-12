## Why

通风值班首页 `VentilationHomePageCardDataHandler#execute` 的三个外部调用（聚合接口 Feign、异常事件 Feign、queryCanJump）全串行执行，wall-clock 为三者之和。照明 handler 已在同一分支完成同类优化（并行化 + 砍除冗余查询），通风作为同分支的对应模块，需对齐优化。

## What Changes

- 异常事件查询并行化：`getExceptionEventCountByObjIds` 从父类 `buildCardTagVO` 内部提取到 `CompletableFuture.supplyAsync(…, BusinessThreadPoolExecutor.ttlExecutor())`，与聚合接口 `queryHomePageCardGroupIotData` 并行执行
- 砍掉 `queryCanJump`：移除 `queryDemandTemplateType` 调用，改为 `childCard.setCanJump(true)` 写死（产品已确认，照明已做同样处理）
- 砍掉大卡片 `offlineTotal`/`alarmTotal`：`CardTagVO` 内联构造（绕过父类 `buildCardTagVO`），只设 `errorEventTotal`，`offlineTotal` 和 `orderFailAlarmTotal` 默认 0（产品已确认，照明已做同样处理）
- 增加短路保护：`dutyHomePageGroup == null` 时直接 return，丢弃异步 future

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `duty-homepage`: 通风值班首页 execute 方法从串行调用改为并行编排，大卡片砍除 offline/alarm 字段、小卡片砍除 queryCanJump 跳转查询

## Impact

- **`VentilationHomePageCardDataHandler`**: 唯一代码改动点，仅修改 `execute()` 方法内部编排和 `queryCanJump` 方法移除
- **父类 `AbstractHomePageCardDataHandler`**: 不改动（`buildCardTagVO`、`getExceptionEventCountByObjIds`、`queryDemandTemplateType` 方法体均不变，只是通风 handler 不再调用 `buildCardTagVO` 和 `queryDemandTemplateType`）
- **聚合适配器 `HomePageCardIotDataAdapter`**: 不改动
- **参考实现**: `LightHomePageCardDataHandler` L211-254（同分支已验证的并行模式）
