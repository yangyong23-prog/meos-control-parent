## Why

空调冷源（ACAC）和冷站（ACCC）的"频率调节范围"壳配置参数已在前端实现配置和保存，但尚未同步至BDTP孪生服务。BDTP控制侧需要这些参数（vFDMinFreqCtrl/vFDMaxFreqCtrl）来执行频率约束计算，缺少推送会导致控制逻辑无法获取最新的频率范围设定。此外，PRD要求将默认值从"35,40"调整为"40,50"。

## What Changes

- 新增BDTP同步任务方法 `saveFreqRangeConf()`，将8个频率调节范围壳配置参数同步至BDTP
- 壳配置中的范围值（如"40,50"）拆分为两个独立BDTP参数：`vFDMinFreqCtrl`（下限）和 `vFDMaxFreqCtrl`（上限）
- 推送以控制组ID为objectId，调用 `/twc/conf/saveList` 接口
- 空值时推送 `paramValue=""` 而非跳过或删除
- 全量推送，不做增量判断（与 `saveFlowDemandConf` 保持一致）
- 8个FREQ_RANGE枚举的默认值从 "35,40" 改为 "40,50"
- paramLabel使用 `item.getName()` 即"频率调节范围"

## Capabilities

### New Capabilities
- `freq-range-bdtp-sync`: 空调冷源（ACAC）和冷站（ACCC）频率调节范围参数的BDTP同步，覆盖8个FREQ_RANGE壳配置枚举，按控制组实例推送至BDTP的vFDMinFreqCtrl/vFDMaxFreqCtrl参数

### Modified Capabilities

## Impact

- `common` 模块：`ShellConfigItemEnum.java` 中8个FREQ_RANGE枚举默认值变更
- `compute-edge` 模块：`InnerSchdule.java` 新增同步方法和调度调用
- BDTP接口：复用已有 `/twc/conf/saveList` 端点，无需新增接口
