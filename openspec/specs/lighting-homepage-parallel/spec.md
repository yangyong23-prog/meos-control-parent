## Requirements

### Requirement: 主线程 + 三路线程池并行查询
`execute()` 方法在阶段1完成后，SHALL 在主线程直接执行聚合接口查询，同时将另外 3 个查询提交线程池并行执行：
1. `queryHomePageCardGroupIotData` — 聚合接口查询（**主线程执行**）
2. `getExceptionEventCountByObjIds` — 异常事件数量查询（线程池）
3. `queryControlDeviationData` — 控制偏差数据查询（线程池，从原 `fillControlDeviationStatus` 拆出）
4. `getDemandTemplateType` — 需求模版类型查询（线程池）

线程池任务 SHALL 使用 `BusinessThreadPoolExecutor.ttlExecutor()` 执行。主线程执行完聚合接口后，通过 `CompletableFuture.allOf().join()` 等待其余三路完成，再进行本地组装。

#### Scenario: 正常并行执行
- **WHEN** 阶段1完成并产出 queryMap、objIds、nightLighting
- **THEN** 聚合接口在主线程执行，其余三个异步任务同时发起，总等待时间为 max(聚合接口, 异常事件, 偏差查询, 需求模版) 而非四者之和

#### Scenario: 聚合接口返回 null
- **WHEN** `queryHomePageCardGroupIotData` 返回 null
- **THEN** 其他三个 Future 的结果被丢弃，方法返回空的 CardDateVO，与改造前行为一致

### Requirement: fillControlDeviationStatus 拆分为查询和应用
原 `fillControlDeviationStatus` 方法 SHALL 拆分为两个方法：
- `queryControlDeviationData(queryMap)` — 只做远程查询，返回 `Map<String, Map<String, LightingRunningDeviationTypeEnum>>`
- `applyDeviationResults(childCardList, deviationMap)` — 清空 moreOpenTotal/lessOpenTotal 后用查询结果填充

公区和停车场路径保持原有 `mapCircuitIdAndTag` 调用不变。

夜景照明路径 SHALL 将 `mapCircuitIdAndInfoCodeValues` 调用提到 `forEach` 外部（只调 1 次替代 3 次），并去掉多余的 `LightingCircuitTagVO` 中间对象，直接使用局部变量。

#### Scenario: 查询与应用解耦
- **WHEN** `queryControlDeviationData` 在阶段1完成后立即发起
- **THEN** 远程查询与聚合接口并行执行，`applyDeviationResults` 在 childCardList 构建后使用查询结果完成填充

### Requirement: setDemandTemplateType 拆分为查询和应用
原 `setDemandTemplateType` 方法 SHALL 拆分为：
- 在 execute() 中直接调用 `getDemandTemplateType(templateTypeList)` 作为并行任务
- 保留 `setDemandTemplateType` 中的填充逻辑，接收预查询的 nameMap 参数

#### Scenario: 需求模版查询并行化
- **WHEN** `getDemandTemplateType` 作为并行任务在阶段1完成后发起
- **THEN** 需求模版查询与聚合接口并行执行，填充逻辑在所有查询完成后执行

### Requirement: 移除 queryCanJump 调用
`execute()` 方法 SHALL 注释掉 `queryCanJump(childCardList)` 调用，不再执行阶段5的 BDTP 关系查询。`queryCanJump` 方法本身 SHALL 保留代码以备恢复。小卡片的 `canJump` 字段 SHALL 在 `setDemandTemplateType` 中统一设为 `Boolean.TRUE`（前端固定依赖此字段，不设值会导致前端判断逻辑失效）。

#### Scenario: 跳过阶段5
- **WHEN** execute() 执行到原阶段5位置
- **THEN** 不执行 queryCanJump，小卡片的 canJump 字段在 setDemandTemplateType 中被设为 true

### Requirement: 照明 Handler 内联构造 CardTagVO
照明 Handler SHALL 不调用基类 `buildCardTagVO`，改为直接 `new CardTagVO()` 并仅设置 `errorEventTotal`（来自并行查询的异常事件数量）。`offlineTotal` 和 `orderFailAlarmTotal` 保持 int 默认值 0（前端已确认不展示）。基类 `AbstractHomePageCardDataHandler.buildCardTagVO` SHALL 不被修改。

#### Scenario: CardTagVO 只包含异常事件数
- **WHEN** 照明 Handler 构建 CardTagVO
- **THEN** 返回的 CardTagVO 仅 errorEventTotal 有值，offlineTotal 和 orderFailAlarmTotal 为默认值 0

#### Scenario: 其他 Handler 不受影响
- **WHEN** 通风/冷源/热源等 Handler 调用 buildCardTagVO
- **THEN** 基类方法行为不变，offlineTotal 和 orderFailAlarmTotal 仍正常赋值
