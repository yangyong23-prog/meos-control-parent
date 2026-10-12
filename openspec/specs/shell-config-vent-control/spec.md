## ADDED Requirements

### Requirement: 通风控制逻辑条件输入主Tab

壳配置 scene=control 下 SHALL 新增第 3 个主Tab「通风控制逻辑条件输入」（tab code=`control_ventilation`，顺序在既有两个主Tab之后），其下首个场景子Tab为「穹顶通风」（group code=`group_control_vent_dome`，项目级配置，无 objectId）。

#### Scenario: 查询返回穹顶通风分组

- **WHEN** 调用 `/shellConfig/query`，参数 scene=control、groups=["group_control_vent_dome"]
- **THEN** 返回的 groups SHALL 包含 group=`group_control_vent_dome` 的分组
- **AND** 该分组下 items SHALL 恰好包含 22 个参数，全部参数的 tab SHALL 为"通风控制逻辑条件输入"，group SHALL 为 `group_control_vent_dome`

#### Scenario: 既有主Tab数据不受影响

- **WHEN** 调用 `/shellConfig/query`，参数 scene=control、groups 为既有分组（group_control_vrv/group_control_fan/group_control_air/group_control_cooling 等）
- **THEN** 返回结构、参数集合与存量已保存值 SHALL 与变更前完全一致
- **AND** scene=control 下既有参数的枚举定义 SHALL 无任何修改

### Requirement: 穹顶通风22参数元数据定义

`ShellConfigItemEnum` SHALL 新增 22 个 scene=control 的参数枚举项，code 前缀为 `control_vent_dome_`，元数据（code、name、unit、defaultValues、step、min、max、hint、desc）SHALL 与 design.md D5 权威参数表逐项一致（「判断室外空气状态的方式」的编码与默认值以本条约定为准）；其中「判断室外空气状态的方式」（营业/非营业时段各 1 个）SHALL 为单选枚举（step=-1，min/max=null，defaultValues="1"；编码约定 1=干球温度判断、2=湿球温度判断、3=干球温度+湿球温度综合判断）。

#### Scenario: 数值参数元数据完整

- **WHEN** 遍历 `ShellConfigItemEnum.getListByScene("control")` 中 code 前缀为 `control_vent_dome_` 的数值类参数（20 个）
- **THEN** 每个参数 SHALL 携带非空的 name、unit、defaultValues、min、max、hint、desc
- **AND** 温度/温差类参数（exhaust_open_temp、exhaust_close_temp、wet_temp_diff、dry_temp_diff 系列）min/max SHALL 为 0/99，默认值 SHALL 为 45/35/6/6
- **AND** 时长类参数（duration 系列）min/max SHALL 为 0/1440，默认值 SHALL 为 15/15/60
- **AND** 湿度阈值参数（weather_rh_threshold）min/max SHALL 为 0/100，默认值 SHALL 为 95

#### Scenario: 单选枚举参数定义

- **WHEN** 查询 code 为 `control_vent_dome_off_judge_mode` 或 `control_vent_dome_on_judge_mode` 的参数
- **THEN** step SHALL 为 -1，min/max SHALL 为 null，defaultValues SHALL 为"1"
- **AND** 参数 SHALL 不携带 valSource；静态选项 SHALL 由后端基于 `VentJudgeModeEnum` 下发（mulInstance 元素形状 {code, name, select}，code 取值为 "1"/"2"/"3"，name 为对应中文选项文本）
- **AND** select SHALL 按 value（value 为空时取 defaultValues）与 code 精确匹配；旧编码（DRY/WET/COMPREHENSIVE 及更早的中文文本）SHALL NOT 做兼容映射

#### Scenario: 营业/非营业同名参数 code 区分

- **WHEN** 对比 `control_vent_dome_off_*` 与 `control_vent_dome_on_*` 前缀的 7 对参数
- **THEN** 每对参数的参数名（name）SHALL 相同、code SHALL 不同（仅 off/on 前缀段差异）
- **AND** 两者的 defaultValues、min、max SHALL 一致

### Requirement: 参数分组展示

22 个参数 SHALL 在 `ShellConfigItemExtendEnum` 中注册 tabObjectName，按值聚合为 5 个参数分组：穹顶排风控制（4 个）、电动气窗控制（1 个）、非营业时段室外空气状态判断（7 个）、营业时段室外空气状态判断（7 个）、恶劣天气判断（3 个）。

#### Scenario: 查询返回 tabObjectName 分组

- **WHEN** 调用 `/shellConfig/query`（scene=control、groups=["group_control_vent_dome"]）且项目未保存过任何穹顶通风配置
- **THEN** 返回的 22 个 item 的 tabObjectName SHALL 非空，且 5 个分组名下的参数数量 SHALL 分别为 4/1/7/7/3
- **AND** 参数顺序 SHALL 按枚举定义顺序（PRD 参数表顺序）返回

### Requirement: 项目级保存与回显

穹顶通风参数 SHALL 按壳配置既有项目级机制保存（RcBasicsConfig，type=SHELL_CONFIG_CONTROL，result JSON 含 group_control_vent_dome 分组），保存后再次查询 SHALL 回显已保存值，未保存过的参数 SHALL 回显 defaultValues。

#### Scenario: 未配置时返回默认值

- **WHEN** 项目从未保存过穹顶通风配置，调用 `/shellConfig/query`（scene=control、groups=["group_control_vent_dome"]）
- **THEN** 全部 22 个参数的 value SHALL 为空或等于 defaultValues（前端按 defaultValues 渲染），`getShellConfigValue()` SHALL 返回 defaultValues

#### Scenario: 保存后回显配置值

- **WHEN** 调用 `/shellConfig/save`（scene=control）保存含 group_control_vent_dome 分组及部分参数值（如 control_vent_dome_exhaust_open_temp=50）的配置
- **THEN** 保存 SHALL 成功且不影响同 type 下其他分组的存量数据（save 为全量 JSON 覆盖，前端须携带完整 groups）
- **AND** 再次查询时 control_vent_dome_exhaust_open_temp 的 value SHALL 为 50，未修改参数 SHALL 回显其保存值或默认值

#### Scenario: 修改后即时生效

- **WHEN** 保存穹顶通风参数后立即调用 `/shellConfig/query`
- **THEN** 查询 SHALL 返回最新保存值（RcBasicsConfig 直查无缓存中间层）

### Requirement: 场景横向扩展约束

分类结构与 code 命名 SHALL 支持通风场景横向扩展：新增场景（如停车场通风 parking、卫生间通风 toilet）时 SHALL 仅新增 `ShellConfigGroupEnum` 场景分组枚举与对应 `ShellConfigItemEnum` 参数枚举（code 场景段替换为新场景标识），SHALL NOT 修改既有场景的枚举定义、存储数据或 Service 逻辑。

#### Scenario: 新增场景零侵入

- **WHEN** 后续需求新增"停车场通风"场景
- **THEN** 实现方式 SHALL 为新增 group（如 group_control_vent_parking）与新参数枚举（code 如 control_vent_parking_*），既有 group_control_vent_dome 的枚举定义与已保存数据 SHALL 无任何改动
- **AND** `ShellConfigServiceImpl` SHALL 无需为新场景新增任何代码分支（参数无 valSource 时）

#### Scenario: code 命名空间不冲突

- **WHEN** 检查全部新增 code 与既有 `ShellConfigItemEnum` 的 code 集合
- **THEN** 22 个 `control_vent_dome_*` code SHALL 全局唯一，不与任何既有 code 冲突
