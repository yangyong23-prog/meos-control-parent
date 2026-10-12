# 穹顶通风配置新增自然通风需求时间（PRD-260902 5.4）

## Goal

为穹顶通风需求配置新增「自然通风需求时间」字段（PRD-260902 第 5.4 章《通风需求配置》），打通后端配置全链路：保存 → 查询回显 → 字段级向上引用 → 预计算结果 → 老数据兼容，作为普通窗/排烟窗自然通风需求时段的配置载体。范围已与用户确认：仅 PRD 5.4 章节，5.2.3 窗类关系纳管由独立任务承接。

## Background（代码调研已确认的事实）

- 配置保存：`/demand/addDemandConfig` 经 `DemandConfigAnalyzeStrategy#analyzeConfig` 将 `configData` JSON 反序列化为强类型 VO；未落到 VO 的未知字段会被静默丢弃，因此新字段必须落在穹顶配置 VO，否则保存/继承/预计算链路全部丢失。
- 穹顶配置 VO：`common/.../structure/demand/model/valueobject/ventilation/DemandConfigVentilationDomeValueObject.java`（继承 `BaseDemandConfigVentilationValueObject`）；基类已有排风/送风需求时间字段（`List<DemandTimeIntervalValueObject>` + Map 形态 `@JsonSetter` 反序列化模式）。
- 字段级向上引用与状态：`BaseDemandConfig#anyFieldNotEmpty()`、`DemandConfigAggregate#isCustomizedConfig()` 均基于反射枚举全部字段，新增字段自动参与「引用」「已配置/特殊配置」判断。
- 清除/复制：一键清除按实例整体清配置、复制按配置 JSON 整体复制，新增字段自动覆盖（满足 PRD 5.3.5 复制要求）。
- 项目默认值：`DemandVentilationDefaultSettingProperties.dome` 仅填充反馈调节与温度目标；自然通风需求时间默认「无」，无需加入默认值填充。
- 跨零点拆分：`DemandTimeIntervalValueObject#analyzeTimeInterval` 已实现跨天拆分为两段 Range 的语义，复用同一类型即可保持一致。
- 预计算：通风已接入 `demand_config_result` 预计算（`DemandTypeEnum.VENTILATION`），字段落在 VO 即自动进入 `final_config_json`。
- 本仓库 Java 代码无排风/送风需求时间的执行端消费点（执行对象生成与下游控制不在本仓库）；「为空不生成通风需求」依赖空值语义表达，本任务无需新增计算分支。

## Requirements

- R1 新增字段：`DemandConfigVentilationDomeValueObject` 增加「自然通风需求时间」（建议 JSON 名 `naturalVentilationTimeIntervals`，类型 `List<DemandTimeIntervalValueObject>`），沿用基类既有 `@JsonSetter` + `readValueFromMap` 反序列化模式，保证全链路 JSON 往返不丢字段。
- R2 默认值与必填：项目默认值为「无」（不参与 `fillDefaultConfig` 默认值填充）；建筑/楼层/空间无默认值且非必填；四个层级查询均回显该字段。
- R3 字段级向上引用：空间未配置时依层级向上取最近已配置值；仅配置该字段的对象应显示「已配置/特殊配置」状态。
- R4 空值语义：字段为空时不产生需求副作用；老数据（JSON 无该字段）升级后默认为「无」，不重算、不覆盖历史配置。
- R5 时间字段通用规则（PRD 5.4.2）：与排风/送风需求时间相互独立、无跨字段互斥校验；最多 20 段、不可重叠、开始早于结束、跨零点拆分等由前端时间段组件与既有 `DemandTimeIntervalValueObject` 语义承载，后端不新增服务端校验（与排风/送风字段现状对齐）；保存后按已保存顺序回显。

## Acceptance Criteria

- [ ] 保存含自然通风需求时间的穹顶配置（项目/建筑/楼层/空间层级），查询接口按原顺序回显，其余字段顺序与结构不受影响。
- [ ] 空间未配置该字段时，按字段级向上引用取楼层/建筑/项目的最近已配置值；全链路均未配置时为「无」。
- [ ] 对象仅配置自然通风需求时间时，「已配置/特殊配置」状态为真。
- [ ] `demand_config_result` 中穹顶通风模板的 `final_config_json` 携带该字段，配置变更后预计算正常刷新。
- [ ] 老数据（JSON 无该字段）在保存、查询、预计算、复制、清除链路均不报错，字段表现为「无需求」。
- [ ] 单元测试覆盖：JSON 反序列化（含 Map 形态）、字段级继承合并、状态判断、空值（null/空数组）语义。

## Out of Scope

- 5.2.3 普通窗（CFCSWN）/排烟窗（CFCSEW）「设备所在空间」关系纳管扩容：已确认由独立任务承接；5.2.3 落地前，本任务验收以现有（风机纳管）穹顶空间为准。
- 5.5~5.8 前端展示层改造（字段布局顺序、日历图例、模型视图样式）；后端若因反射/整体 JSON 机制自动生效，仅做回归验证。
- 普通窗/排烟窗执行对象生成、消防排烟/人工控制优先级等下游控制逻辑（不在本仓库）。
- 前端时间段组件交互（段数上限提示、重叠校验、5 分钟间隔、提示文案「最多可配置20个时段」）。

## Technical Notes

- 已配置判断的空集合语义：`BaseDemandConfig#readValueFromMap` 对 null/空数组返回非 null 空 List，显式传 `null`/`[]` 会使 `anyFieldNotEmpty()` 视为「已配置」；此为排风/送风字段既有行为，本任务沿用不做变更，前端未配置字段应省略不传。
- 字段 JSON 命名 `naturalVentilationTimeIntervals` 为按现有命名惯例（`exhaustTimeIntervals`/`deliverTimeIntervals`）的建议值，与前端联调时需最终确认。
