## Context

壳配置采用"枚举定义元数据 + RcBasicsConfig 表存实例值"的架构：`ShellConfigSceneEnum`（场景）→ `ShellConfigTabEnum`（主Tab）→ `ShellConfigGroupEnum`（分组）→ `ShellConfigItemEnum`（参数），实例值按 `project_id + BasicsConfigTypeEnum.SHELL_CONFIG_CONTROL` 存为 JSON。

PRD-260902 要求的展示结构为四层：主Tab「通风控制逻辑条件输入」→ 场景子Tab「穹顶通风」(dome) → 参数分组（5 个）→ 参数（22 个）。基础模型只有三层。ALG 场景已有第四层先例：`ShellConfigItemExtendEnum` 通过 `item code → tabObjectName` 把同一 group 内的参数再切分为子分组，`ShellConfigItemEnum.getListByScene()` 中 `copyProperties` 注入该字段。

PRD 关键约束：
- 配置粒度项目级（无 objectId），scene=`control`，修改后即时生效
- 新增场景（停车场通风、卫生间通风等）时仅新增三级分类与对应 code，不动既有场景
- PRD 参数表中「后端code」「group」两列留白，命名由后端定案
- 参数不下发控制组、不考虑第三方（需求方决策）

## Goals / Non-Goals

**Goals:**

- scene=control 下新增第 3 个主Tab「通风控制逻辑条件输入」，穹顶通风场景 5 分组 22 参数可配、可存、可查、默认值正确
- 分类结构与 code 命名为场景横向扩展预留：新增场景 = 纯枚举增量
- 与既有壳配置机制完全同构，Service/Controller/存储零改动

**Non-Goals:**

- 不做 BDTP 推送 / 控制组下发（本期无后端消费方）
- 不处理第三方对接
- 不新增后端 min/max 校验（沿用前端校验现状）
- 不为单选枚举参数做后端选项下发（静态选项由前端按 code 维护）
- 不做国际化 key 扩展（枚举 name 为中文文本，`LocaleMessageUtil.getMessage` 找不到 key 时原样返回，与现有 control 参数行为一致）

## Decisions

### D1: 四层结构映射——复用 ALG 场景的 tabObjectName 机制

**选择**: 主Tab=`ShellConfigTabEnum` 新枚举；场景子Tab（穹顶通风）=`ShellConfigGroupEnum` 新分组（项目级，systemType 为空）；参数分组（5 个）=`ShellConfigItemExtendEnum.tabObjectName` 按值聚合；参数=`ShellConfigItemEnum` 新枚举项。

**原因**: 与 ALG 场景「同一 group 下按 tabObjectName 切子分组」的既有范式一致，前端已有渲染先例；`getConfigValueByScene()` 中 `systemType` 为空的分组走"项目级配置直接回填 value"分支，天然满足项目级粒度。

**备选方案**:
- group 编码内嵌场景段（`group_control_vent_dome_exhaust` 每分组一个 group）→ 参数分组变 group 后前端需按 code 拆解渲染，且 group 数量随场景数膨胀，放弃
- `ShellConfigItem` 新增 sceneSubTab 字段 → 改动数据模型与前端协议，与 PRD「沿用现有的一二三级结构」约定不符，放弃

### D2: code 命名规范 `control_vent_dome_<分组前缀>_<参数语义>`

**选择**: 四段式：业务域段 `control_` + 通风域段 `vent_` + 场景段 `dome_` + 分组前缀与参数语义。

**原因**: 与既有 `control_vrv_*`、`control_air_*` 命名模式一致；场景段保证未来 `vent_parking_*`、`vent_toilet_*` 平行共存不冲突；分组前缀（exhaust/window/off/on/weather）区分同名参数（营业/非营业时段的 7 对参数完全同名，靠 `off_`/`on_` 前缀区分 code）。

**注意**: `off`/`on` 前缀对应"非营业时段/营业时段"；`wet_low`/`wet_high` 直接跟随 PRD 参数名（"判断室外湿球温度低的超限持续时长"），不做语义二次加工，code 与参数名一一对应便于比对。

### D3: 单选枚举参数采用 step=-1 + 值存选项文本的既有模式

**选择**: 「判断室外空气状态的方式」（营业/非营业各 1 个，共 2 个）设 `step=-1`、`min/max=null`、defaultValues="干球温度判断"，value 直接存中文选项文本；选项列表（干球温度判断/湿球温度判断/干球温度+湿球温度综合判断）由前端按 code 维护。

**原因**: 参照 `ALG_COOLING_MACHINE_MIX_ENABLED`（step=-1，defaultValues="是"）的既有静态单选先例；无后端消费方时存中文无解析成本。若未来控制逻辑程序化消费，再扩展英文编码值并处理存量数据兼容，属后续需求范围。

### D4: 22 个参数均无 valSource，Service 层零改动

**选择**: 全部参数为纯数值输入或静态单选，不配置 `valSource`，`getInstanceByValSource()` 走 `StrUtil.isBlank(valSource)` 直通分支原样返回；`ShellConfigServiceImpl` 不新增任何 code 分支。

**原因**: 与既有「三文件协同规范」中 valSource 参数需在 `getInstanceByValSource` 注册的场景不同——本变更无设备实例选择需求，触发不了该分支。这是改动面收敛到 4 个枚举文件的前提。

### D5: 完整参数清单（22 项，作为实现与验收的唯一对照表）

**选择**: 下表为 `ShellConfigItemEnum` 新增枚举项的权威定义（枚举参数顺序：scene, group, tab, code, name, unit, defaultValues, step, min, max, hint, desc；tab 统一为"通风控制逻辑条件输入"，group 统一为 `group_control_vent_dome`）：

| # | 枚举常量 | code | 分组(tabObjectName) | 参数名 | 单位 | 默认 | step | min | max |
|---|---|---|---|---|---|---|---|---|---|
| 1 | CONTROL_VENT_DOME_EXHAUST_OPEN_TEMP | control_vent_dome_exhaust_open_temp | 穹顶排风控制 | 开启排风机的穹顶温度设定值Ts | ℃ | 45 | 0 | 0 | 99 |
| 2 | CONTROL_VENT_DOME_EXHAUST_OPEN_DURATION | control_vent_dome_exhaust_open_duration | 穹顶排风控制 | 穹顶温度高于设定值的持续时长 | min | 15 | 0 | 0 | 1440 |
| 3 | CONTROL_VENT_DOME_EXHAUST_CLOSE_TEMP | control_vent_dome_exhaust_close_temp | 穹顶排风控制 | 关闭排风机的穹顶温度设定值Tx | ℃ | 35 | 0 | 0 | 99 |
| 4 | CONTROL_VENT_DOME_EXHAUST_CLOSE_DURATION | control_vent_dome_exhaust_close_duration | 穹顶排风控制 | 穹顶温度低于设定值的持续时长 | min | 15 | 0 | 0 | 1440 |
| 5 | CONTROL_VENT_DOME_WINDOW_HOLD_DURATION | control_vent_dome_window_hold_duration | 电动气窗控制 | 电动气窗关闭后的保持时间 | min | 15 | 0 | 0 | 1440 |
| 6 | CONTROL_VENT_DOME_OFF_JUDGE_MODE | control_vent_dome_off_judge_mode | 非营业时段室外空气状态判断 | 判断室外空气状态的方式 |  | 干球温度判断 | -1 | null | null |
| 7 | CONTROL_VENT_DOME_OFF_WET_TEMP_DIFF | control_vent_dome_off_wet_temp_diff | 非营业时段室外空气状态判断 | 判断可通风的室内外湿球温差设定值 | ℃ | 6 | 0 | 0 | 99 |
| 8 | CONTROL_VENT_DOME_OFF_WET_LOW_DURATION | control_vent_dome_off_wet_low_duration | 非营业时段室外空气状态判断 | 判断室外湿球温度低的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 9 | CONTROL_VENT_DOME_OFF_WET_HIGH_DURATION | control_vent_dome_off_wet_high_duration | 非营业时段室外空气状态判断 | 判断室外湿球温度高的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 10 | CONTROL_VENT_DOME_OFF_DRY_TEMP_DIFF | control_vent_dome_off_dry_temp_diff | 非营业时段室外空气状态判断 | 判断可通风的室内外干球温差设定值 | ℃ | 6 | 0 | 0 | 99 |
| 11 | CONTROL_VENT_DOME_OFF_DRY_LOW_DURATION | control_vent_dome_off_dry_low_duration | 非营业时段室外空气状态判断 | 判断室外干球温度低的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 12 | CONTROL_VENT_DOME_OFF_DRY_HIGH_DURATION | control_vent_dome_off_dry_high_duration | 非营业时段室外空气状态判断 | 判断室外干球温度高的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 13 | CONTROL_VENT_DOME_ON_JUDGE_MODE | control_vent_dome_on_judge_mode | 营业时段室外空气状态判断 | 判断室外空气状态的方式 |  | 干球温度判断 | -1 | null | null |
| 14 | CONTROL_VENT_DOME_ON_WET_TEMP_DIFF | control_vent_dome_on_wet_temp_diff | 营业时段室外空气状态判断 | 判断可通风的室内外湿球温差设定值 | ℃ | 6 | 0 | 0 | 99 |
| 15 | CONTROL_VENT_DOME_ON_WET_LOW_DURATION | control_vent_dome_on_wet_low_duration | 营业时段室外空气状态判断 | 判断室外湿球温度低的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 16 | CONTROL_VENT_DOME_ON_WET_HIGH_DURATION | control_vent_dome_on_wet_high_duration | 营业时段室外空气状态判断 | 判断室外湿球温度高的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 17 | CONTROL_VENT_DOME_ON_DRY_TEMP_DIFF | control_vent_dome_on_dry_temp_diff | 营业时段室外空气状态判断 | 判断可通风的室内外干球温差设定值 | ℃ | 6 | 0 | 0 | 99 |
| 18 | CONTROL_VENT_DOME_ON_DRY_LOW_DURATION | control_vent_dome_on_dry_low_duration | 营业时段室外空气状态判断 | 判断室外干球温度低的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 19 | CONTROL_VENT_DOME_ON_DRY_HIGH_DURATION | control_vent_dome_on_dry_high_duration | 营业时段室外空气状态判断 | 判断室外干球温度高的超限持续时长 | min | 60 | 0 | 0 | 1440 |
| 20 | CONTROL_VENT_DOME_WEATHER_RH_THRESHOLD | control_vent_dome_weather_rh_threshold | 恶劣天气判断 | 判断室外恶劣天气的室外相对湿度设定值 | % | 95 | 0 | 0 | 100 |
| 21 | CONTROL_VENT_DOME_WEATHER_RH_OVER_DURATION | control_vent_dome_weather_rh_over_duration | 恶劣天气判断 | 判断相对湿度超限的持续时长 | min | 60 | 0 | 0 | 1440 |
| 22 | CONTROL_VENT_DOME_WEATHER_CLEAR_DURATION | control_vent_dome_weather_clear_duration | 恶劣天气判断 | 判断室外非恶劣天气的持续时长 | min | 60 | 0 | 0 | 1440 |

**hint** 取值：温度/温差类为"范围 （0-99）"，湿度类为"范围 （0-100）"，时长类为"范围 （0-1440）"，单选类为空串。**desc** 逐条取 PRD「参数填写的tips说明」对应文本，原样保留。

## Risks / Trade-offs

- **[code 一经定案即与存量数据耦合]** → code 写入 RcBasicsConfig JSON 后不可随意更名；D5 表作为唯一权威定义，实现时逐项核对，避免 code 拼写偏差成为长期负债
- **[前端需按 groups 过滤]** → `/shellConfig/query` 不传 groups 时 `getListByScene("control")` 返回全部 control 参数（含新 Tab 22 项），前端必须传 `groups=["group_control_vent_dome"]` 渲染新 Tab，属既有机制但需与前端明确约定
- **[后端不校验 min/max]** → 超范围值可入库，沿用壳配置全局现状；若未来控制逻辑消费需强校验，另行扩展
- **[单选值存中文文本]** → 后端无解析需求时无成本；未来程序化消费时需做文本→编码映射及存量兼容，已记录于 D3
