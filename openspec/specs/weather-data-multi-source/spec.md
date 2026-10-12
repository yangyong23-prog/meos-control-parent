## ADDED Requirements

### Requirement: 天气数据多源获取
系统必须根据壳配置`ALG_WEATHER_DATA_SOURCE`的值动态选择天气数据源（天气服务或气象监测站），并返回对应的天气数据。

#### Scenario: 配置为天气服务时调用天气服务API
- **WHEN** `ALG_WEATHER_DATA_SOURCE`的值为`weather_service`
- **THEN** 调用公司天气服务API获取干球温度和相对湿度，湿球温度和焓值返回-0.9999

#### Scenario: 配置为气象站时查询孪生数据
- **WHEN** `ALG_WEATHER_DATA_SOURCE`的值为设备ID（如`WS001`）
- **THEN** 通过设备ID查询该气象站的ambientTemp、ambientRH、twb、h四个信息点的最新值

#### Scenario: 无配置时降级到天气服务
- **WHEN** `ALG_WEATHER_DATA_SOURCE`未配置或值为空
- **THEN** 自动降级到天气服务，调用天气服务API获取数据，记录WARN日志

#### Scenario: 气象站不存在时降级到天气服务
- **WHEN** `ALG_WEATHER_DATA_SOURCE`配置的设备ID在RWD中不存在
- **THEN** 自动降级到天气服务，调用天气服务API获取数据，记录WARN日志

#### Scenario: 气象站数据查询异常时降级到天气服务
- **WHEN** 查询气象站孪生数据时发生异常（网络异常、设备离线等）
- **THEN** 自动降级到天气服务，调用天气服务API获取数据，记录ERROR日志

### Requirement: 天气数据返回格式
无论数据源是天气服务还是气象站，返回的WeatherVO必须符合统一格式，缺失字段使用-0.9999标识。

#### Scenario: 天气服务返回数据格式
- **WHEN** 数据源为天气服务
- **THEN** 返回的WeatherVO必须包含：
  - `dryBulb`: 干球温度（从天气服务获取）
  - `relativeHumidity`: 相对湿度（从天气服务获取）
  - `wetBulb`: -0.9999（天气服务无此字段）
  - `alkane`: -0.9999（天气服务无此字段）

#### Scenario: 气象站返回数据格式
- **WHEN** 数据源为气象站
- **THEN** 返回的WeatherVO必须包含：
  - `dryBulb`: 干球温度（从ambientTemp获取）
  - `relativeHumidity`: 相对湿度（从ambientRH获取）
  - `wetBulb`: 湿球温度（从twb获取）
  - `alkane`: 焓值（从h获取）

#### Scenario: 气象站信息点值为空时返回-0.9999
- **WHEN** 气象站的某个信息点（如twb）查询结果为空
- **THEN** 对应的WeatherVO字段返回-0.9999

### Requirement: 天气数据源切换后的数据更新
数据源切换后，所有依赖天气数据的接口必须返回新数据源的数据（需手动刷新页面）。

#### Scenario: 切换数据源后查询天气数据
- **WHEN** 用户将数据源从天气服务切换为气象站`WS001`并保存
- **THEN** 下次调用`/weather/queryWeather`接口时，返回气象站`WS001`的数据

#### Scenario: 切换数据源后运行监控页面数据更新
- **WHEN** 用户切换数据源后手动刷新运行监控页面
- **THEN** 右侧边栏显示的室外天气数据必须来自新数据源

#### Scenario: 切换数据源后图表数据更新
- **WHEN** 用户切换数据源后查看冷机运行负载图表
- **THEN** 图表中的"室外干球温度"曲线必须使用新数据源的数据
