# PRD-26.0801 照度反馈条件按回路维度配置

## Goal

将照度反馈调节条件从空间维度统一配置改为按照明回路维度逐条配置。空间级保留反馈方式（室内/室外照度调节），回路级新增是否参与反馈调节、自动开启条件、自动关闭条件和回路开关设定。

## Background

当前照度反馈调节按空间维度配置自动开启条件、自动关闭条件，同一空间下所有普通照明回路共用相同的反馈条件。实际项目中，同一空间下不同普通照明回路的自动开启阈值、自动关闭阈值和持续时间可能不同，因此需要在空间反馈方式不变的前提下，将反馈条件调整为按回路配置。

## Requirements

### R1: 普通照明回路配置（配置模型变更）

**R1.1** 扩展回路配置值对象 `DemandConfigLightingCircuitValueObject`，新增 `feedbackAdjustSetting` 对象字段（类型 `DemandLightingCircuitFeedbackAdjustSettingDTO`），当 `settingMode = FEEDBACK_ADJUSTMENT` 时携带：
- `underOutdoorIlluminance`（BigDecimal）— 自动开启条件照度阈值，单位 lx，取值 0-10000 正整数
- `underDuration`（Integer）— 自动开启条件持续时间，单位 min，取值 0-300 整数，0 表示达到阈值即生效
- `overtopOutdoorIlluminance`（BigDecimal）— 自动关闭条件照度阈值，单位 lx，取值 0-10000 正整数
- `overtopDuration`（Integer）— 自动关闭条件持续时间，单位 min，取值 0-300 整数，0 表示达到阈值即生效

**R1.2** 回路配置校验规则：
- 当回路 `settingMode` = `FEEDBACK_ADJUSTMENT` 时：自动开启条件和自动关闭条件最少配置一项
- 当自动开启条件和自动关闭条件都配置时，自动开启条件的照度阈值必须小于自动关闭条件的照度阈值
- 当回路 `settingMode` = `OPEN` 或 `CLOSE` 时：`feedbackAdjustSetting` 为 `null`

**R1.3** 空间级反馈调节配置调整：
- `spaceFeedbackAdjustType`（反馈方式：室内/室外照度调节）保留在功能类型配置 `DemandConfigLightingFuncTypeValueObject` 上，不变
- `DemandConfigLightingFeedbackAdjustDetailValueObject` 中的 `allFeedback` 字段废弃，反馈条件不再在空间级统一存储
- 空间级仅保留反馈方式选择，反馈条件下沉到回路级

### R2: 多空间服务回路一致性校验

**R2.1** 同一模板且相同场景时段下，同一照明回路服务多个空间时：
- 仅当该回路在多个空间中均配置为"参与反馈调节"（`settingMode` = `FEEDBACK_ADJUSTMENT`）时，校验反馈条件一致性
- 若某空间配置为"不参与反馈调节"，则该空间不纳入一致性校验

**R2.2** 条件一致的定义：
- 条件类型一致（自动开启条件、自动关闭条件是否都配置了）
- 自动开启条件的阈值和持续时间一致
- 自动关闭条件的阈值和持续时间一致

**R2.3** 校验时机：点击页面"保存"时校验，不做即时校验

**R2.4** 不一致时提示：`【{回路名称}】在【{空间名称}】中的配置为：自动开启条件（照度低于XX lx持续XXmin）、自动关闭条件（照度高于XXlx持续XXmin）`，按每个空间的配置逐条展示

**R2.5** 并发保存冲突处理：保存时后端需基于最新配置再次校验同模板、同场景时段、同回路的反馈条件一致性；若保存前已有其他用户提交了不一致配置，则当前保存失败，并返回冲突回路及冲突空间配置

### R3: 配置预览

**R3.1** 回路为反馈调节（`settingMode` = `FEEDBACK_ADJUSTMENT`）时，配置预览展示该回路的自动开启条件和自动关闭条件
**R3.2** 服务空间下任一回路最终开关需求为反馈调节时，该空间渲染为反馈调节（此为已有逻辑，作为测试范围）

### R4: 回路开关需求计算逻辑

**R4.1** 空间需求继承优先级保持不变：空间本身配置 > 空间类型配置 > 楼层配置 > 建筑配置 > 全局配置

**R4.2** 功能类型为普通照明时，按空间级反馈方式和回路控制方式计算：
- 回路参与反馈调节时：配置预览需求为反馈调节，执行阶段读取该回路"普通照明反馈调节判断结果"信息点
- 回路不参与反馈调节时：需求为配置的开启或关闭
- 功能类型不为普通照明时，沿用原全开、全关、部分开启逻辑

### R5: 照度反馈条件下发任务（LightingFeedbackAdjustTask 重构）

**R5.1** 空间级 `illumFbCtrType` 写入逻辑（保持写入空间对象）：
- 写入对象范围：查询当前使用的场景时段，场景时段中有配置反馈调节条件的空间
- 写入频率：每 5min 写入一次
- 写入逻辑：对比当前和上一个 5min 的场景时段配置，以单个空间对比判断有变化的进行下发
  - 上个 5min 有配置、当前有配置：按当前反馈方式写入（室内="1"，室外="2"）
  - 上个 5min 有配置、当前无配置：写入"3"（无反馈调节）
  - 上个 5min 无配置、当前有配置：按当前反馈方式写入

**R5.2** 回路级条件信息点写入逻辑（改为写入回路对象 LTLTCC）：
- 写入对象范围：查询当前使用的场景时段，场景时段中需求为反馈调节的回路
- 写入频率：每 5min 写入一次
- 写入逻辑：对比当前和上一个 5min 的场景时段配置，以单个回路对比判断有变化的进行下发
  - 上个 5min 有配置、当前有配置：按当前照度条件写入
  - 上个 5min 有配置、当前无配置：清空回路的照度条件
  - 上个 5min 无配置、当前有配置：按当前照度条件写入
- 写入的信息点编码（4 个，业务点非字典信息点，已有定义）：
  - `outdoorIlluminationValueToTurnOnGeneralLighting` — 自动开启条件阈值
  - `feedbackofLightingTurnonLastTime` — 自动开启条件持续时间
  - `outdoorIlluminationValueToTurnOffGeneralLighting` — 自动关闭条件阈值
  - `feedbackofLightingTurnoffLastTime` — 自动关闭条件持续时间

**R5.3** Redis 缓存记录维度从空间 ID 改为回路 ID

### R6: 新增"普通照明反馈调节判断结果"信息点

**R6.1** 在 `InfoCodeEnum` 中新增"普通照明反馈调节判断结果"信息点定义
**R6.2** 该信息点写入对象为照明回路（LTLTCC），由边缘系统写入，本项目仅读取
**R6.3** 执行阶段通过该信息点判断反馈调节回路的开关需求

### R7: 二开接口

**R7.1** 临时需求：增、删、改、查（定时执行）
**R7.2** 查询空间及空间下的回路、查询编组的接口

## Constraints

- **C1**: 不兼容历史数据（历史空间级反馈调节配置不做自动迁移）
- **C2**: `DemandCommandTask.feedbackAdjustmentCompute()`（common-edge 模块）不在本次修改范围
- **C3**: 空间级反馈方式（室内/室外照度调节）仍按空间级配置，不变
- **C4**: 空间级显示条件（5.1.1）和空间级反馈方式（5.1.2）无变更
- **C5**: 信息点编码已有定义（i18n 已包含），无需新增 i18n key（R6 除外）

## Acceptance Criteria

- [ ] AC1: 回路配置值对象支持按回路配置自动开启条件、自动关闭条件，校验规则正确
- [ ] AC2: 空间级反馈方式保留，`allFeedback` 字段废弃，反馈条件下沉到回路级
- [ ] AC3: 同一回路服务多空间时，参与反馈调节的反馈条件一致性校验正确
- [ ] AC4: 并发保存冲突时，后端基于最新配置再次校验并返回冲突信息
- [ ] AC5: 配置预览正确展示回路级反馈调节条件
- [ ] AC6: `LightingFeedbackAdjustTask` 按回路维度遍历，`illumFbCtrType` 写入空间，4 个条件信息点写入回路（LTLTCC）
- [ ] AC7: `LightingFeedbackAdjustTask` 的 5min 增量对比逻辑（上个 5min vs 当前）按回路维度正确执行
- [ ] AC8: Redis 缓存记录维度从空间 ID 改为回路 ID
- [ ] AC9: 新增"普通照明反馈调节判断结果"信息点定义，由边缘系统写入
- [ ] AC10: 二开接口（临时需求增删改查、空间回路查询）可用

## Out of Scope

- 历史空间级反馈调节配置的自动迁移（PRD 5.4 不在本次范围）
- `DemandCommandTask.feedbackAdjustmentCompute()` 的修改（common-edge 模块）
- 前端 UI 实现
