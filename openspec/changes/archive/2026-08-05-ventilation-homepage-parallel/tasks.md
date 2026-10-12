## 1. 导入与依赖

- [x] 1.1 在 `VentilationHomePageCardDataHandler` 中新增 `import java.util.concurrent.CompletableFuture`
- [x] 1.2 新增 `import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor`

## 2. execute 方法并行化重写

- [x] 2.1 在 `queryEquipGroupId` 返回非空后，将 `getExceptionEventCountByObjIds(objIds)` 提取为 `CompletableFuture.supplyAsync(() -> getExceptionEventCountByObjIds(objIds), BusinessThreadPoolExecutor.ttlExecutor())` 异步任务
- [x] 2.2 主线程执行 `queryHomePageCardGroupIotData(queryMap)` 聚合接口调用（与异步异常事件查询并行）
- [x] 2.3 在聚合接口调用后添加 null 短路保护：`dutyHomePageGroup == null` 时直接 `return resultVo`
- [x] 2.4 调用 `CompletableFuture.allOf(errorEventFuture).join()` 等待异步任务完成
- [x] 2.5 内联构造 `CardTagVO`：`new CardTagVO()` + `tag.setErrorEventTotal(errorEventFuture.join())`，不调用父类 `buildCardTagVO`，不设置 `offlineTotal`/`orderFailAlarmTotal`（默认 0）
- [x] 2.6 保留 `buildChildCardDateVO(dutyHomePageGroup.getSubStat())` 小卡片构建逻辑不变

## 3. 砍除 queryCanJump

- [x] 3.1 移除 `execute` 方法中 `queryCanJump(childCardList)` 调用
- [x] 3.2 改为 `childCardList.forEach(card -> { if (card != null) card.setCanJump(true); })` 写死可跳转
- [x] 3.3 移除 `queryCanJump` 私有方法（L74-89）

## 4. 清理无用导入

- [x] 4.1 移除 `queryCanJump` 删除后不再使用的导入（`DemandTypeEnum`、`CollectionUtils` 如无其他引用则移除）

## 5. 验证

- [x] 5.1 编译通过，无语法错误
- [x] 5.2 验证 `execute` 返回的 `CardTagVO` 中 `errorEventTotal` 有值、`offlineTotal`/`orderFailAlarmTotal` 为 0
- [x] 5.3 验证所有小卡片 `canJump` 为 `true`
- [x] 5.4 验证聚合接口返回 null 时 execute 短路返回，不抛 NPE
- [x] 5.5 验证 `queryEquipGroupId` 返回空时 execute 仍提前返回（原有逻辑不变）
