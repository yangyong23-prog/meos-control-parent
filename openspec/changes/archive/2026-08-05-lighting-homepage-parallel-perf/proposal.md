## Why

照明值班首页 `LightHomePageCardDataHandler.execute()` 端到端耗时约 **14,872ms**，其中阶段2~5为串行执行。
经分析，阶段2（聚合接口）、阶段3（异常事件查询）、阶段4中的偏差查询和需求模版查询均只依赖阶段1的产出（queryMap/objIds），可以四路并行；阶段5（queryCanJump）产品已确认可移除。

## What Changes

- 将 `execute()` 方法中阶段2/3/4的远程查询改为并行执行：聚合接口在主线程运行（天然持有上下文 + null 检查不变），其余三路提交线程池，主线程利用等待时间做有用功，减少一个线程池占用
- 拆分 `fillControlDeviationStatus` 为「远程查询」和「本地应用」两步，查询部分提前并行
- `fillControlDeviationStatus` 夜景照明路径：将 `mapCircuitIdAndInfoCodeValues` 调用提到 `forEach` 外部（避免 3 次冗余 Feign 调用），去掉多余的 `LightingCircuitTagVO` 中间对象
- 拆分 `setDemandTemplateType` 为「getDemandTemplateType 查询」和「填充结果」两步，查询部分提前并行
- 照明 Handler 不调用基类 `buildCardTagVO`，直接内联构造 `CardTagVO` 只设 `errorEventTotal`（前端已确认不使用 `offlineTotal` 和 `orderFailAlarmTotal`），使异常事件查询可提前并行，不依赖聚合接口结果
- 注释掉阶段5 `queryCanJump` 调用（产品已确认移除）

## Capabilities

### New Capabilities

- `lighting-homepage-parallel`: 照明值班首页并行查询优化，覆盖 execute 方法的执行流重构

### Modified Capabilities

无（纯性能优化，不改变前端可见业务行为；`offlineTotal`/`orderFailAlarmTotal` 返回默认值 0 但前端不展示）

## Impact

- **代码**: `LightHomePageCardDataHandler.java`（唯一改动文件，~56 行）
- **API**: 无接口变更，返回结构不变；`offlineTotal`/`orderFailAlarmTotal` 默认为 0（前端不展示）
- **性能**: 预估从 ~14,872ms 降至 ~6,150ms（减少 58.6%）
