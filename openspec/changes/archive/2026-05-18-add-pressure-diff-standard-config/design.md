## Context

空调运行标准的环境品质目标当前支持温度、CO₂ 和湿度三项参数。湿度参数在 7.251201.0 版本中通过 `isRhQualityEnabled` 开关实现了"全局可开关、关闭自动清空、查看时控制显隐"的模式。本次迭代需要在同一体系下新增压差（Pa）标准配置，并在黑名单 IOT 监测中扩展压差维度。

压差监测与温度/CO₂/湿度不同：部分项目因硬件条件无法监测空间压差数据，因此必须通过开关控制是否启用；同时压差不涉及调度时长等算法参数，仅需简单上下限。

## Goals / Non-Goals

**Goals:**
- 在空调需求全局/特殊配置的环境品质目标中新增压差标准配置能力
- 压差配置通过开关控制，关闭时自动清空已配置值并在查看状态隐藏
- 压差不按空调季设置，采用全局统一的上下限值模式
- 运行标准查看接口在返回数据中补充压差标准
- 黑名单 IOT 监测规则支持按"未交付压差数据的空间"筛选
- 所有新增文案纳入国际化翻译体系

**Non-Goals:**
- 不涉及压差调度算法（压差不配置调度标准，不影响"是否参与调度"判断）
- 不修改温度和湿度的季节配置模式
- 黑名单多选交集逻辑已在现有后端实现中支持，本次不改动规则引擎
- 不修改 `energy-quality-server` 品质服务内部逻辑（本侧为透传接口）

## Decisions

### 1. 压差品质模型采用简化上下限模式（参考 CO2，不按空调季）

**决策：** 压差品质配置仅包含 `lowerLimitValue` 和 `upperLimitValue` 两个字段，不引入季节维度。

**理由：**
- PRD 明确"压差指标的录入不根据项目设置的空调季进行设置"
- 压差是建筑物理特性，不随季节变化（不像温度/湿度需要区分夏季/冬季标准）
- 与 CO₂ 品质配置模式保持一致，降低模型复杂度

### 2. 开关行为完全复用湿度开关模式

**决策：** 新增 `isPressureDiffQualityEnabled` 字段，开关关闭时执行与湿度开关相同的清理逻辑。

**理由：**
- 产品规则明确要求"规则同现在的湿度配置"
- 现有 `DemandConfigAirConditioningAnalyzeStrategy` 中已成熟的清理逻辑可直接扩展
- 保持用户心智模型一致

**实现方式：**
- 在 `DemandConfigAirConditioningAnalyzeStrategy` 中新增 `clearPressureDiffQualityConfig()` 方法
- 保存全局配置时，若开关为 false，则清空当前配置并级联清空所有已配置对象的压差品质数据

### 3. 数值校验放在保存分析策略层

**决策：** 压差上下限的数值范围校验（-1000~1000、一位小数、下限≤上限）放在 `DemandConfigAirConditioningAnalyzeStrategy` 中统一校验。

**理由：**
- 与现有需求配置的校验模式一致（同一策略中已处理湿度和温度的校验）
- 避免在前端/后端/数据库多层重复校验
- 便于统一错误提示和国际化

### 4. 黑名单 IOT 规则无需后端逻辑改动

**决策：** 黑名单后端 `DemandBlacklistInstantiateRuleStrategy` 不做任何代码修改。

**理由：**
- `nonInstantiationCodes` 列表天然支持多信息点编码传入
- `mapObjectIdAndExistsRuleInfoCodes` 的返回语义已是"有任意一个信息点规则即排除"，恰好等价于"所有选中信息点都没有规则才纳入"
- 仅需前端将 `relativePressureDiffToOtherSpace` 加入 `nonInstantiationCodes` 列表即可

### 5. InfoCodeEnum 新增压差编码常量

**决策：** 在 `InfoCodeEnum` 中新增 `RELATIVE_PRESSURE_DIFF("relativePressureDiffToOtherSpace", ...)`。

**理由：**
- 保持信息点编码的统一管理和可发现性
- 便于前端通过枚举引用而非硬编码字符串
- 与现有 `SPACE_TDB`、`SPACE_CO2`、`RH` 的模式保持一致

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 压差字段新增导致历史配置 JSON 反序列化兼容性问题 | 新增字段为可选（nullable），不设置默认值，Jackson 反序列化时缺失字段自动为 null，不影响历史数据 |
| 运行标准查看 VO 新增字段后前端未适配导致布局异常 | 压差字段在开关关闭时为 null，前端可按 null 隐藏处理；需在 tasks 中明确前端联调任务 |
| 品质服务侧 `queryStandard` 未同步处理压差展示 | 本侧仅做数据透传，需在任务清单中增加与品质服务团队的联调确认项 |
| 开关关闭时的级联清空操作在大数据量项目下可能耗时 | 复用现有湿度清理的批量更新模式，通过 `demandConfigRepository.addOrEditConfig(editParams)` 批量处理 |
