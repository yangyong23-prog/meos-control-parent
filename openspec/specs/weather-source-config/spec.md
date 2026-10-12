## ADDED Requirements

### Requirement: 室外天气数据来源壳配置参数
系统必须在壳配置中提供"室外天气数据来源"配置项，允许用户选择天气服务或具体的气象监测站作为天气数据源。

#### Scenario: 查询壳配置时返回天气数据源配置项
- **WHEN** 前端调用壳配置查询接口且场景包含项目级配置
- **THEN** 返回的配置项列表中必须包含`ALG_WEATHER_DATA_SOURCE`参数，默认值为`weather_service`

#### Scenario: 用户选择天气服务作为数据源
- **WHEN** 用户在壳配置页面选择"天气服务"并保存
- **THEN** 系统将`ALG_WEATHER_DATA_SOURCE`的值保存为`weather_service`

#### Scenario: 用户选择气象监测站作为数据源
- **WHEN** 用户在壳配置页面选择某个气象监测站（设备ID: `WS001`）并保存
- **THEN** 系统将`ALG_WEATHER_DATA_SOURCE`的值保存为`WS001`

#### Scenario: 项目无气象站时自动降级
- **WHEN** 项目中没有任何WESEMS设备且用户首次打开壳配置页面
- **THEN** 系统自动将`ALG_WEATHER_DATA_SOURCE`设置为`weather_service`，气象监测站选项置灰不可选

#### Scenario: 升级后回显已配置的气象站
- **WHEN** 用户之前已选择了气象站（如`WS001`）且该设备仍存在
- **THEN** 壳配置页面回显时显示已选择的气象站名称

### Requirement: 壳配置参数元数据定义
`ALG_WEATHER_DATA_SOURCE`参数的元数据必须符合以下定义：
- configCode: `alg_weather_data_source`
- configName: `室外天气数据来源`
- group: `group_alg_project`
- tab: `室外天气数据来源输入`
- defaultValue: `weather_service`
- dataType: `SINGLE`（单选）
- options: 动态生成（天气服务 + 气象站列表）

#### Scenario: 壳配置参数选项动态生成
- **WHEN** 前端请求壳配置参数元数据
- **THEN** `ALG_WEATHER_DATA_SOURCE`的options字段必须包含：
  - `{ "name": "天气服务", "value": "weather_service" }`
  - 所有WESEMS设备列表，按名称汉字升序排序：`{ "name": "设备名称", "value": "设备ID" }`

#### Scenario: 壳配置参数必填校验
- **WHEN** 项目中存在WESEMS设备且用户未选择数据源
- **THEN** 前端展示必填校验提示"请选择室外天气数据来源"
