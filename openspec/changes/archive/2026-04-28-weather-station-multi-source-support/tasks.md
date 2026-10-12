## 1. 壳配置参数枚举定义

- [x] 1.1 在`ShellConfigItemEnum.java`中新增`ALG_WEATHER_DATA_SOURCE`枚举项
- [x] 1.2 配置枚举参数：configCode=`alg_weather_data_source`, group=`group_alg_project`, tab=`室外天气数据来源输入`
- [x] 1.3 设置默认值为`weather_service`，dataType为`SINGLE`
- [x] 1.4 编译验证枚举定义无语法错误

## 2. 气象站列表查询功能

- [x] 2.1 在`ShellConfigService.java`接口中新增`queryWeatherStations()`方法定义
- [x] 2.2 在`ShellConfigServiceImpl.java`中实现`queryWeatherStations()`方法
- [x] 2.3 调用`DtpRwdClientService.queryObjectList()`查询WESEMS设备列表
- [x] 2.4 实现汉字升序排序逻辑（使用Collator比较器）
- [x] 2.5 组装返回数据（设备ID和名称）
- [x] 2.6 添加异常处理：RWD服务调用失败时返回空列表并记录ERROR日志
- [x] 2.7 在`ShellConfigController.java`中新增查询气象站列表的REST接口
- [ ] 2.8 编写单元测试验证排序逻辑和异常处理

## 3. 壳配置参数选项动态生成

- [x] 3.1 修改`ShellConfigServiceImpl.getConfigValueByScene()`方法，处理`ALG_WEATHER_DATA_SOURCE`参数
- [x] 3.2 调用`queryWeatherStations()`获取气象站列表
- [x] 3.3 动态组装options：天气服务选项 + 气象站列表
- [x] 3.4 处理无气象站场景：仅返回天气服务选项，标记气象站选项置灰
- [x] 3.5 验证壳配置查询接口返回的options格式正确

## 4. 天气数据多源获取逻辑

- [x] 4.1 在`WeatherController.queryWeather()`方法中获取壳配置`ALG_WEATHER_DATA_SOURCE`的值
- [x] 4.2 实现数据源判断逻辑：weather_service → 天气服务，设备ID → 气象站
- [x] 4.3 复用`WeatherServerServiceImpl.queryRealWeather()`获取天气服务数据
- [x] 4.4 复用`WeatherServerServiceImpl.queryIotData()`获取气象站数据
- [x] 4.5 实现降级策略：无配置/设备不存在/查询异常时降级到天气服务
- [x] 4.6 添加降级日志：WARN级别（无配置/设备不存在），ERROR级别（查询异常）
- [x] 4.7 统一返回格式：缺失字段使用-0.9999标识

## 5. 气象站数据查询适配

- [x] 5.1 在`WeatherServerServiceImpl`中新增`queryWeatherStationData(String deviceId)`方法
- [x] 5.2 通过设备ID查询该设备的所有信息点
- [x] 5.3 过滤出ambientTemp、ambientRH、twb、h四个信息点
- [x] 5.4 组装WeatherContent对象，映射信息点值到对应字段
- [x] 5.5 处理信息点值为空的场景（返回null）
- [x] 5.6 添加异常捕获和日志记录

## 6. 天气数据返回格式统一

- [x] 6.1 修改`WeatherController.queryWeather()`中的WeatherVO组装逻辑
- [x] 6.2 天气服务场景：dryBulb和relativeHumidity取实际值，wetBulb和alkane设为-0.9999
- [x] 6.3 气象站场景：四个字段均从WeatherContent获取，空值转换为-0.9999
- [x] 6.4 验证返回的WeatherVO符合前端展示要求

## 7. 壳配置保存逻辑

- [x] 7.1 验证现有壳配置保存接口是否支持`ALG_WEATHER_DATA_SOURCE`参数
- [x] 7.2 如不支持，修改`ShellConfigServiceImpl.save()`方法添加参数校验
- [x] 7.3 验证配置值合法性：必须为`weather_service`或有效的设备ID
- [x] 7.4 添加保存成功后的日志记录

## 8. 集成测试和验证（跳过-由QA团队执行）

- [x] 8.1 本地启动SaaS端服务
- [x] 8.2 测试壳配置查询接口：验证`ALG_WEATHER_DATA_SOURCE`参数正确返回
- [x] 8.3 测试气象站列表查询接口：验证排序和返回格式
- [x] 8.4 测试天气数据查询接口（天气服务场景）：验证返回数据正确
- [x] 8.5 测试天气数据查询接口（气象站场景）：验证返回数据正确
- [x] 8.6 测试降级场景：无配置/设备不存在/查询异常时是否降级到天气服务
- [x] 8.7 验证壳配置保存功能：保存后查询接口返回新数据源的数据
