## ADDED Requirements

### Requirement: 通风值班首页异常事件查询与聚合接口并行执行

通风值班首页 `execute` 方法 SHALL 将异常事件数量查询（`getExceptionEventCountByObjIds`）通过 `CompletableFuture.supplyAsync` 提交到 `BusinessThreadPoolExecutor.ttlExecutor()` 线程池异步执行，同时主线程执行聚合接口（`queryHomePageCardGroupIotData`）。异常事件查询只需 `objIds`（来自 `queryEquipGroupId`），不依赖聚合接口结果，因此可以并行。wall-clock 从串行之和降为两者之最大值。

#### Scenario: 正常并行执行
- **WHEN** `queryEquipGroupId` 返回非空设备分组
- **THEN** 异常事件查询通过 `CompletableFuture.supplyAsync` 异步提交，主线程同步执行聚合接口，两者完成后 `allOf().join()` 汇合结果

#### Scenario: 聚合接口返回 null 时短路
- **WHEN** 聚合接口 `queryHomePageCardGroupIotData` 返回 null
- **THEN** execute 直接返回带 `runningMonitorType` 的空 `CardDateVO`，不等待异步异常事件 future，不调用 `buildChildCardDateVO`

### Requirement: 通风值班首页大卡片只展示异常事件数

通风值班首页大卡片 `CardTagVO` SHALL 只设置 `errorEventTotal`（来自并行查询结果），`offlineTotal` 和 `orderFailAlarmTotal` SHALL 保持默认值 0。`execute` 方法不再调用父类 `buildCardTagVO`，改为内联构造 `CardTagVO`。

#### Scenario: 大卡片字段值
- **WHEN** execute 正常完成
- **THEN** 返回的 `CardTagVO` 中 `errorEventTotal` 为异常事件数量，`offlineTotal` 为 0，`orderFailAlarmTotal` 为 0

### Requirement: 通风值班首页小卡片默认可跳转

通风值班首页所有小卡片 SHALL 设置 `canJump = true`，不再通过 `queryDemandTemplateType` 查询需求模板类型来判断跳转。`execute` 方法不再调用 `queryCanJump`。

#### Scenario: 小卡片跳转标识
- **WHEN** `buildChildCardDateVO` 构建小卡片列表完成
- **THEN** 每个小卡片的 `canJump` 字段为 `true`

## REMOVED Requirements

### Requirement: 通风值班首页 queryCanJump 需求模板查询
**Reason**: 产品已确认通风与照明一致处理，照明已移除 `queryCanJump`（L256-259 注释："产品已确认移除"），改为默认可跳转。该查询在照明实测中耗时 19s+，砍除后直接消除该调用。
**Migration**: 小卡片 `canJump` 统一设为 `true`，前端无需根据该字段判断是否展示跳转入口。
