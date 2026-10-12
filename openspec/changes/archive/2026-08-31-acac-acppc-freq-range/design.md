## Context

当前"空调冷源自动需求计算条件输入"的"控制组频率约束"分类下已有 4 类控制组的频率调节范围参数（一次泵组 ACCPC、二次泵组 ACCPC、冷却水泵 ACOPC、冷却塔 ACOTC），每类参数通过 4 个文件协同工作：

1. `ShellConfigItemEnum` — 枚举定义（scene/group/tab/code/name/valSource 等元数据）
2. `ShellConfigItemExtendEnum` — tab 绑定（item code → 前端三级分类显示名）
3. `ShellConfigServiceImpl.getInstanceByValSource()` — 实例过滤（classCode 匹配 + netUseType 判断）
4. `InnerSchdule.saveFreqRangeConf()` — BDTP 推送（5 分钟定时，值拆分为 vFDMinFreqCtrl / vFDMaxFreqCtrl）

PRD 4.1（云端持久化数据显示规则）已实现，无需变更。

## Goals / Non-Goals

**Goals:**

- 在 ACAC 侧"控制组频率约束"下新增"冷热共用水泵控制组（ACPPC）"频率调节范围参数
- 参数值通过 BDTP 定时推送至控制侧
- 复用现有 4 文件协同模式，不引入新架构

**Non-Goals:**

- 不在 ACCC（冷站）侧新增 ACPPC 配置（PRD 仅要求 ACAC 侧）
- 不修改 PRD 4.1 持久化数据显示规则（已实现）
- 不改变现有 4 类控制组的行为

## Decisions

### Decision 1: 枚举命名 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE`

**选择**: 沿用现有 `ALG_AIR_COOLING_PUMP_*` 前缀模式，以 ACPPC 标识冷热共用水泵控制组。

**理由**: 与 `ALG_AIR_COOLING_PUMP_1/2/ACOPC/ACOTC` 保持一致的命名规范。code 值 `alg_air_cooling_pump_acppc_freq_range` 遵循相同的小写下划线模式。

### Decision 2: 排序位置 — 插入在二次泵组之后、冷却水泵之前

**选择**: 枚举插入 `ALG_AIR_COOLING_PUMP_2_FREQ_RANGE`（L533）之后、`ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE`（L535）之前。

**理由**: PRD 明确要求排序在"冷冻水泵控制组（二次泵组）"后。`ShellConfigItemExtendEnum` 中的 tab 绑定排序与枚举一致。

### Decision 3: ACPPC 不需要 netUseType 判断

**选择**: 在 `ShellConfigServiceImpl.getInstanceByValSource()` 中，ACPPC 加入与 ACOPC/ACOTC 相同的 else-if 分支，直接 `add` 而不检查 netUseType。

**理由**: netUseType 仅用于区分 ACCPC 的一次泵/二次泵场景（1,2=一次泵组；3,4=二次泵组）。ACPPC 是独立的"冷热共用水泵控制组"，不按管网类型拆分，与 ACOPC/ACOTC 行为一致。

### Decision 4: paramLabel 使用 `item.getName()`

**选择**: BDTP 推送的 paramLabel 值沿用 `item.getName()`，即"频率调节范围"。

**理由**: 现有 8 个 FREQ_RANGE 枚举均使用此模式，保持一致。PRD 示例中的"冷源参数"仅为示意。

### Decision 5: 仅 ACAC 侧变更，ACCC 侧不变

**选择**: 不新增 `ALG_COOLING_PUMP_ACPPC_FREQ_RANGE`（ACCC 侧）。

**理由**: PRD 仅涉及 ACAC 系统，明确按"中央空调供冷全系统（ACAC）→ 冷热共用水泵控制组（ACPPC）"关系查询。用户已确认不需要 ACCC 侧变更。

## Risks / Trade-offs

- **[Risk] getInstanceByValSource 硬编码分支遗漏** → 如果不在 else-if 中加入 ACPPC 枚举码，新枚举虽能通过 RWD 图遍历发现实例，但因不匹配任何分支被 `continue` 跳过，前端不显示配置项。**Mitigation**: 本次变更明确包含此修改点。

- **[Risk] 新枚举码未加入 freqRangeCodes 集合** → 壳配置值不会被 BDTP 推送。**Mitigation**: 本次变更明确追加到集合中。

- **[Risk] 前端 tab 渲染异常** → 如果 `ShellConfigItemExtendEnum` 未注册，tabObjectName 为空。**Mitigation**: 本次变更包含 tab 绑定注册。
