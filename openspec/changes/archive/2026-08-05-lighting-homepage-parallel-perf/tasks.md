## 1. 拆分 fillControlDeviationStatus

- [x] 1.1 从 `fillControlDeviationStatus` 中提取远程查询逻辑为新方法 `queryControlDeviationData(queryMap)`，返回 `Map<String, Map<String, LightingRunningDeviationTypeEnum>>`（只包含 CompletableFuture 异步查询部分，不含清空和写入 childCardList 的逻辑）
- [x] 1.2 夜景照明路径：将 `mapCircuitIdAndInfoCodeValues` 调用提到 `forEach` 外部（只调 1 次替代 3 次），去掉多余的 `LightingCircuitTagVO` 中间对象，直接用局部变量
- [x] 1.3 将 `fillControlDeviationStatus` 剩余的清空+写入逻辑重命名为 `applyDeviationResults(childCardList, deviationMap)`，接收预查询的 deviationMap 参数替代 queryMap

## 2. 拆分 setDemandTemplateType

- [x] 2.1 将 `setDemandTemplateType` 中的 `getDemandTemplateType(templateTypeList)` 调用移出方法，改为接收预查询的 nameMap 参数（方法签名改为 `setDemandTemplateType(vo, nightLighting, nameMap)`）

## 3. 重构 execute() 为主线程 + 三路线程池并行

- [x] 3.1 在阶段1完成后，将 getExceptionEventCountByObjIds、queryControlDeviationData、getDemandTemplateType 用 `CompletableFuture.supplyAsync` + `BusinessThreadPoolExecutor.ttlExecutor()` 提交线程池；聚合接口 queryHomePageCardGroupIotData 在主线程直接执行
- [x] 3.2 主线程执行完聚合接口后，检查 dutyHomePageGroup，若为 null 则 return 空 VO
- [x] 3.3 `CompletableFuture.allOf(B, C, D).join()` 等待其余三路完成，然后执行本地组装：内联构造 CardTagVO（`new CardTagVO()` 只设 `errorEventTotal` 为异常事件 Future 结果，不调用基类 `buildCardTagVO`）、buildChildCardDateVO、applyDeviationResults、setDemandTemplateType、sort

## 4. 注释掉阶段5

- [x] 4.1 注释掉 `queryCanJump(childCardList)` 调用及其 StopWatch 计时，保留方法定义以备恢复

## 5. 验证

- [x] 5.1 编译通过，确认无语法错误
