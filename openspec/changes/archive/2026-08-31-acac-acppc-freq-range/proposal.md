## Why

PRD-260901 要求在"空调冷源自动需求计算条件输入"的"控制组频率约束"分类下，新增"冷热共用水泵控制组（ACPPC）"的频率调节范围参数。当前系统仅支持冷冻水泵（一次/二次泵组）、冷却水泵、冷却塔三类控制组的频率约束配置，缺少 ACPPC 实例的配置能力。此参数需通过壳配置持久化，并经 BDTP 定时推送至控制侧。

PRD 4.1（云端持久化数据显示规则）已在代码中实现，无需额外变更。

## What Changes

- **新增壳配置枚举** `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE`：在 `ShellConfigItemEnum` 的 `group_alg_acac_freq` 分组下新增，默认值 40Hz-50Hz，一位小数，范围 0-99，valSource 关联 ACPPC 实例（CombineSubset/Sy2Eg）
- **注册 tab 绑定**：在 `ShellConfigItemExtendEnum` 中绑定三级分类名"冷热共用水泵控制组"，使前端正确渲染
- **扩展实例过滤逻辑**：在 `ShellConfigServiceImpl.getInstanceByValSource()` 的 else-if 分支中追加 ACPPC 枚举码，确保配置项对 ACPPC 实例可见
- **扩展 BDTP 推送集合**：在 `InnerSchdule.saveFreqRangeConf()` 的 `freqRangeCodes` 集合中追加新枚举码，使 5 分钟定时任务自动将值拆分为 vFDMinFreqCtrl / vFDMaxFreqCtrl 推送至 BDTP

## Capabilities

### New Capabilities

- `acac-acppc-freq-range`: ACAC 系统下新增冷热共用水泵控制组（ACPPC）频率调节范围参数的壳配置定义、实例过滤和 tab 绑定

### Modified Capabilities

- `freq-range-bdtp-sync`: BDTP 推送枚举集合从 8 个扩展为 9 个，新增 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 的推送覆盖

## Impact

- **ShellConfigItemEnum.java**: 新增 1 个枚举项（common 模块）
- **ShellConfigItemExtendEnum.java**: 新增 1 行 tab 绑定（common 模块）
- **ShellConfigServiceImpl.java**: 修改 `getInstanceByValSource()` 的 if-else 条件（common 模块）
- **InnerSchdule.java**: `freqRangeCodes` 集合从 8 个扩展为 9 个（local/meos-control-compute-edge 模块）
- 无 API 变更、无数据库迁移、无 Breaking Change
