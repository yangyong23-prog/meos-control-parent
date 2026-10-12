## Why

《【PRD】穹顶通风控制逻辑（电动气窗+风机）》定义的穹顶通风控制逻辑依赖一批阈值参数（穹顶温度设定值、室外空气状态判断的温差设定值与超限持续时长、恶劣天气判断的相对湿度设定值等），需支持按项目差异化配置。PRD-260902 要求将这批参数纳入壳配置「控制逻辑条件输入」，并要求分类结构与 code 命名支持停车场通风、卫生间通风等场景的横向扩展：新增场景时仅新增三级分类与对应 code，不改动既有场景的结构、配置与数据。

现有壳配置 scene=control 仅有 2 个主Tab（末端控制逻辑条件输入、控制逻辑选机条件输入），无通风相关参数承载位置。

## What Changes

- `ShellConfigTabEnum` 新增主Tab `CONTROL_VENTILATION("control", "control_ventilation", "通风控制逻辑条件输入")`
- `ShellConfigGroupEnum` 新增穹顶通风场景分组 `GROUP_CONTROL_VENT_DOME("group_control_vent_dome", "穹顶通风", "", false)`（项目级，systemType 为空）
- `ShellConfigItemEnum` 新增 22 个参数枚举项（scene=control，code 前缀 `control_vent_dome_`），覆盖 5 个参数分组：穹顶排风控制（4）、电动气窗控制（1）、非营业时段室外空气状态判断（7）、营业时段室外空气状态判断（7）、恶劣天气判断（3）
- `ShellConfigItemExtendEnum` 注册上述 22 个参数的 `tabObjectName`（5 个分组名），复用 ALG 场景的第四层分组机制
- 单选枚举参数（判断室外空气状态的方式，2 个）采用 `step=-1`、value 存选项文本的既有模式，选项列表由前端维护

## Capabilities

### New Capabilities

- `shell-config-vent-control`: 通风控制逻辑条件输入的分类结构、22 个穹顶通风参数定义、项目级保存与查询、场景横向扩展约束

### Modified Capabilities

（无）

## Impact

- **改动文件**: 仅 4 个枚举文件纯增量（`ShellConfigTabEnum`、`ShellConfigGroupEnum`、`ShellConfigItemEnum`、`ShellConfigItemExtendEnum`），共约 46 行枚举项
- **零改动**: `ShellConfigServiceImpl`（22 个参数均无 valSource，走 `getInstanceByValSource` 空值直通分支）、`ShellConfigController`、RcBasicsConfig 存储结构、既有 control 主Tab 的枚举与存量数据
- **不下发控制组**: 本期无后端消费方，不做 BDTP 推送（`InnerSchdule` 不改）；控制组消费链路由后续需求另行处理
- **第三方**: 按需求方决策，本期不考虑第三方对接问题
- **前端契约**: 前端需以 `groups=["group_control_vent_dome"]` 调用 `/shellConfig/query` 过滤出新 Tab 数据；单选枚举（`step=-1`）的三个静态选项（干球温度判断/湿球温度判断/干球温度+湿球温度综合判断）由前端按 code 维护
- **校验**: 沿用壳配置现状，min/max 范围校验由前端执行，后端保存不校验（既有取舍，非本次引入）
