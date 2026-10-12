## MODIFIED Requirements

### Requirement: 运行标准查看接口返回压差标准

运行标准查看接口 SHALL 在返回的环境品质目标数据中包含压差标准，但仅在压差开关开启时返回。此外，信息点配置查询接口（`listInfoCodeConfigsByObjectId`）SHALL 在 `objectAdditionalInfo` 分组下返回压差标准信息点。

- 项目级全局配置预览接口（`previewProjectGlobalConfig`）SHALL 在响应中新增 `pressureDiffQuality` 字段
- 特殊配置详情接口（`previewSpecialDemandConfig`）SHALL 在响应中新增 `isPressureDiffQualityEnabled` 和 `pressureDiffQuality` 字段
- 当压差开关为 false 或未配置时，`pressureDiffQuality` SHALL 为 null
- 信息点配置查询接口（`listInfoCodeConfigsByObjectId`）SHALL 在 `objectAdditionalInfo` 分组下返回 `relativePressureDiffToOtherSpace` 信息点，数据为 `pressureDiffQuality` 对象
- 信息点配置查询接口中，当压差开关为 false 时，SHALL 完全不返回压差信息点（包括定义也不返回）

#### Scenario: 查询启用了压差配置的运行标准

- **WHEN** 用户查询某日的空调运行标准，且当日使用的模板启用了压差配置
- **THEN** 响应中包含 `pressureDiffQuality` 对象，包含 `lowerLimitValue` 和 `upperLimitValue`

#### Scenario: 查询未启用压差配置的运行标准

- **WHEN** 用户查询某日的空调运行标准，且当日使用的模板未启用压差配置
- **THEN** 响应中 `pressureDiffQuality` 为 null，不展示压差参数

#### Scenario: 信息点配置查询接口返回压差标准（开关开启）

- **WHEN** 用户调用 `listInfoCodeConfigsByObjectId` 查询空间信息点配置，且压差开关开启并有配置值
- **THEN** 响应的 `objectAdditionalInfo` 分组下包含 `relativePressureDiffToOtherSpace` 信息点，含上下限值数据

#### Scenario: 信息点配置查询接口不返回压差标准（开关关闭）

- **WHEN** 用户调用 `listInfoCodeConfigsByObjectId` 查询空间信息点配置，且压差开关关闭
- **THEN** 响应中不包含 `relativePressureDiffToOtherSpace` 信息点的定义和数据
