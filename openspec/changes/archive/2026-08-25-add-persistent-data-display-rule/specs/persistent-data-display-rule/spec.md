## ADDED Requirements

### Requirement: 云端持久化数据显示规则壳配置场景
系统必须在壳配置中提供"云端持久化数据显示规则"场景，允许用户配置云端持久化数据的查询逻辑和时间间隔参数。

#### Scenario: 查询壳配置时返回持久化数据显示规则配置项
- **WHEN** 前端调用壳配置查询接口且 `scene` 为 `persistent_data_rule`
- **THEN** 返回的配置项列表中必须包含以下 2 个参数：
  - `persistent_data_query_logic`：持久化数据查询逻辑（单选），默认值为 `same_time`
  - `persistent_data_time_interval`：请设置距离当前的时间间隔（数字输入），默认值为 `1`，单位为 `h`，范围 1-168

#### Scenario: 首次查询时回显默认值
- **WHEN** 项目首次打开持久化数据显示规则壳配置页面且数据库中无保存记录
- **THEN** `persistent_data_query_logic` 的回显值必须为 `same_time`（查询同一个时刻点的数据）
- **AND** `persistent_data_time_interval` 的回显值必须为 `1`（1h）

#### Scenario: 保存配置后重新查询回显已保存值
- **WHEN** 用户在壳配置页面选择"查询距离当前最新的数据"并设置时间间隔为 3h，保存后重新查询
- **THEN** `persistent_data_query_logic` 的值必须为 `latest`
- **AND** `persistent_data_time_interval` 的值必须为 `3`

### Requirement: 持久化数据查询逻辑参数定义
`persistent_data_query_logic` 参数的元数据必须符合以下定义：
- scene: `persistent_data_rule`
- group: `group_persistent_data_rule`
- code: `persistent_data_query_logic`
- name: `持久化数据查询逻辑`
- unit: 空
- defaultValues: `same_time`
- valueType: `null`（单选，通过 valSource 渲染选项）
- valSource: JSON 数组，包含两个选项
- editAuth: `null`（默认可编辑，即必填）

#### Scenario: 查询逻辑选项列表
- **WHEN** 前端请求壳配置参数元数据
- **THEN** `persistent_data_query_logic` 的 `valSource` 必须包含以下两个选项：
  - `{ "name": "查询同一个时刻点的数据", "value": "same_time" }`
  - `{ "name": "查询距离当前最新的数据", "value": "latest" }`

#### Scenario: 查询逻辑参数描述
- **WHEN** 前端请求壳配置参数元数据
- **THEN** `persistent_data_query_logic` 的 `desc` 字段必须包含以下说明：
  - 查询同一个时刻点的数据：所有信息点都查询某一个时刻的固定分精度数据；若信息点的最小分精度数据大于设置的分精度则会获取不到数据
  - 查询距离当前最新的数据：不限制分精度，有最新的数据上数到云端即可查询，但不同信息点的最新时间点可能不一致

### Requirement: 距离当前的时间间隔参数定义
`persistent_data_time_interval` 参数的元数据必须符合以下定义：
- scene: `persistent_data_rule`
- group: `group_persistent_data_rule`
- code: `persistent_data_time_interval`
- name: `请设置距离当前的时间间隔`
- unit: `h`
- defaultValues: `1`
- min: `1`
- max: `168`
- step: `1`
- hint: `整数，范围（1-168）`
- valueType: `null`
- valSource: `null`
- editAuth: `null`（默认可编辑，即必填）

#### Scenario: 时间间隔输入范围约束
- **WHEN** 前端渲染 `persistent_data_time_interval` 参数
- **THEN** 输入框必须约束整数输入范围为 1-168
- **AND** 默认值为 1，步进为 1

#### Scenario: 时间间隔参数描述
- **WHEN** 前端请求壳配置参数元数据
- **THEN** `persistent_data_time_interval` 的 `desc` 字段必须说明：仅查询设置的时间范围内的最新数据，若没有最新数据，则页面会显示为--。比如设置3h，3h内没有新的数据上数则显示为--，若在持续上数则显示最新的一个数据

### Requirement: 条件显示逻辑
`persistent_data_time_interval` 参数仅在 `persistent_data_query_logic` 的值为 `latest`（查询距离当前最新的数据）时在前端显示。

#### Scenario: 查询逻辑为同一时刻点时隐藏时间间隔
- **WHEN** `persistent_data_query_logic` 的值为 `same_time`（查询同一个时刻点的数据）
- **THEN** 前端隐藏 `persistent_data_time_interval` 参数输入框
- **AND** 后端不校验此条件显示关系，无论参数 1 为何值均接受参数 2 的保存

#### Scenario: 查询逻辑为距离当前最新时显示时间间隔
- **WHEN** `persistent_data_query_logic` 的值为 `latest`（查询距离当前最新的数据）
- **THEN** 前端显示 `persistent_data_time_interval` 参数输入框，用户必须填写

### Requirement: 壳配置场景与数据库存储类型映射
系统必须将 `persistent_data_rule` 场景映射到 `shell_config_persistent_data_rule` 基础配置类型，用于数据库 `rc_basics_config` 表的 `type` 字段区分。

#### Scenario: 保存时正确映射存储类型
- **WHEN** 用户保存 `persistent_data_rule` 场景的壳配置
- **THEN** `ShellConfigSceneEnum.convertToBasicConfigTypeByScene("persistent_data_rule")` 必须返回 `BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE`
- **AND** 数据库 `rc_basics_config` 表中记录的 `type` 字段值必须为 `shell_config_persistent_data_rule`

#### Scenario: 基础配置类型列表包含新类型
- **WHEN** 前端调用 `BasicsConfigTypeEnum.getList()` 获取所有配置类型
- **THEN** 返回列表中必须包含 `shell_config_persistent_data_rule` 类型
- **AND** 该类型的名称通过 i18n 键 `key_shell_config_persistent_data_rule` 国际化展示

### Requirement: i18n 国际化支持
系统必须为 `shell_config_persistent_data_rule` 配置类型提供中英文国际化支持。

#### Scenario: 中文环境下显示中文名称
- **WHEN** 系统语言为中文
- **THEN** `key_shell_config_persistent_data_rule` 的值必须为 `云端持久化数据显示规则`

#### Scenario: 英文环境下显示英文名称
- **WHEN** 系统语言为英文
- **THEN** `key_shell_config_persistent_data_rule` 的值必须为 `Cloud Persistent Data Display Rule`
