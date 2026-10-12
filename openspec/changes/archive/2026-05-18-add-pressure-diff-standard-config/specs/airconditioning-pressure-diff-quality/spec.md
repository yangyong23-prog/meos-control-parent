## ADDED Requirements

### Requirement: 全局配置支持压差品质开关

空调需求全局配置的环境品质目标中 SHALL 提供压差品质配置开关，控制压差参数是否可配置。

- 开关字段为 `isPressureDiffQualityEnabled`，类型为 Boolean，默认值为 false
- 开关仅在全局配置（项目级）中可控制，特殊配置继承全局开关状态，不可自行控制
- 开关关闭时，SHALL 自动清空当前配置对象及所有已配置对象的压差品质数据
- 开关关闭后，运行标准查看接口 SHALL 不返回压差品质数据

#### Scenario: 开启压差品质配置
- **WHEN** 用户在全局配置中将压差开关设为 true 并保存
- **THEN** 系统允许录入压差下限值和上限值
- **AND** 运行标准查看时展示压差标准

#### Scenario: 关闭压差品质配置
- **WHEN** 用户在全局配置中将压差开关设为 false 并保存
- **THEN** 系统清空当前配置的压差下限值和上限值
- **AND** 级联清空所有建筑、楼层、空间已配置的压差品质数据
- **AND** 运行标准查看时不展示压差标准

### Requirement: 压差品质配置支持上下限值录入

压差品质配置 SHALL 支持录入压差下限值和上限值，用于定义空间压差的允许范围。

- 配置字段包含 `lowerLimitValue`（下限值）和 `upperLimitValue`（上限值）
- 单位：Pa
- 可输入范围：[-1000, 1000]，包含边界值
- 精度：最多一位小数
- 校验规则：下限值 SHALL 小于或等于上限值
- 压差不按空调季设置，全局统一配置

#### Scenario: 录入有效的压差标准
- **WHEN** 用户录入下限值为 5.0 Pa，上限值为 25.0 Pa 并保存
- **THEN** 系统校验通过并持久化配置

#### Scenario: 录入无效的压差范围
- **WHEN** 用户录入下限值为 30.0 Pa，上限值为 20.0 Pa 并保存
- **THEN** 系统拒绝保存并提示"压差下限值不可大于上限值"

#### Scenario: 录入超出范围的压差值
- **WHEN** 用户录入下限值为 -1001.0 Pa 或上限值为 1001.0 Pa 并保存
- **THEN** 系统拒绝保存并提示超出允许范围

### Requirement: 运行标准查看接口返回压差标准

运行标准查看接口 SHALL 在返回的环境品质目标数据中包含压差标准，但仅在压差开关开启时返回。

- 项目级全局配置预览接口（`previewProjectGlobalConfig`）SHALL 在响应中新增 `pressureDiffQuality` 字段
- 特殊配置详情接口（`previewSpecialDemandConfig`）SHALL 在响应中新增 `isPressureDiffQualityEnabled` 和 `pressureDiffQuality` 字段
- 当压差开关为 false 或未配置时，`pressureDiffQuality` SHALL 为 null

#### Scenario: 查询启用了压差配置的运行标准
- **WHEN** 用户查询某日的空调运行标准，且当日使用的模板启用了压差配置
- **THEN** 响应中包含 `pressureDiffQuality` 对象，包含 `lowerLimitValue` 和 `upperLimitValue`

#### Scenario: 查询未启用压差配置的运行标准
- **WHEN** 用户查询某日的空调运行标准，且当日使用的模板未启用压差配置
- **THEN** 响应中 `pressureDiffQuality` 为 null，不展示压差参数

### Requirement: 黑名单 IOT 监测支持压差维度

黑名单配置的"按 IOT 监测"规则中 SHALL 新增"未交付压差数据的空间"选项。

- 新增选项对应的信息点编码为 `relativePressureDiffToOtherSpace`
- 该选项与温度、CO₂、湿度选项可多选
- 多选时 SHALL 同时满足所有勾选条件的空间才被纳入黑名单（交集逻辑）
- 后端规则引擎 SHALL 无需修改即可支持该选项

#### Scenario: 仅勾选未交付压差数据的空间
- **WHEN** 用户在黑名单 IOT 监测中仅勾选"未交付压差数据的空间"
- **THEN** 系统中没有压差监测实例化规则的空间被纳入黑名单

#### Scenario: 同时勾选未交付温度和未交付压差
- **WHEN** 用户同时勾选"未交付温度数据的空间"和"未交付压差数据的空间"
- **THEN** 仅当空间既没有温度监测也没有压差监测时才被纳入黑名单
- **AND** 空间只要有温度或压差任一监测则不被纳入黑名单

### Requirement: 新增压差相关国际化翻译

系统中 SHALL 为新增压差相关功能提供国际化翻译支持。

- 压差开关名称、压差下限/上限标签、解释说明文案 SHALL 纳入国际化体系
- 黑名单"未交付压差数据的空间"选项文案 SHALL 纳入国际化体系
- 黑名单提示语文案修改后的内容 SHALL 纳入国际化体系
