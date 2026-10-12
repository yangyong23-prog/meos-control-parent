## ADDED Requirements

### Requirement: 信息点配置接口展示压差标准上下限值

`listInfoCodeConfigsByObjectId` 接口 SHALL 在 `objectAdditionalInfo` 分组下展示压差标准信息点（`relativePressureDiffToOtherSpace`），数据来源为需求配置中的 `pressureDiffQuality` 对象，包含 `lowerLimitValue` 和 `upperLimitValue`。

- 压差标准信息点 SHALL 通过 `DemandConfigInfoCodeAnalyzeStrategy` 映射，与 CO2 标准映射模式一致
- 压差标准信息点的 map key SHALL 使用 `InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode()`（即 `relativePressureDiffToOtherSpace`）
- 当 `isPressureDiffQualityEnabled` 为 true 且 `pressureDiffQuality` 不为 null 时，SHALL 将品质值对象映射到对应空间的信息点数据中

#### Scenario: 开关开启且有压差配置时查询信息点

- **WHEN** 用户调用 `listInfoCodeConfigsByObjectId` 查询某空间的信息点配置，且该空间对应的需求配置启用了压差品质开关（`isPressureDiffQualityEnabled = true`）并配置了 `pressureDiffQuality`
- **THEN** 响应的 `objectAdditionalInfo` 分组下包含 `relativePressureDiffToOtherSpace` 信息点，数据值为 `pressureDiffQuality` 对象（含 `lowerLimitValue` 和 `upperLimitValue`）

#### Scenario: 开关开启但压差配置为空时查询信息点

- **WHEN** 用户调用 `listInfoCodeConfigsByObjectId` 查询某空间的信息点配置，且该空间对应的需求配置启用了压差品质开关（`isPressureDiffQualityEnabled = true`）但 `pressureDiffQuality` 为 null
- **THEN** 响应中不包含 `relativePressureDiffToOtherSpace` 信息点（aggregate 被标记为 invalid 并移除）

### Requirement: 压差开关关闭时完全移除信息点

当 `isPressureDiffQualityEnabled` 为 false 时，`listInfoCodeConfigsByObjectId` 接口 SHALL 完全不返回压差标准信息点，包括信息点定义也不返回。

- 开关关闭时，`DemandConfigInfoCodeAnalyzeStrategy` SHALL 不在结果 map 中放入 `relativePressureDiffToOtherSpace` 键
- 开关关闭时，SHALL 对压差信息点的 aggregate 调用 `setInvalid(true)`
- `InfoCodeAggregateLifeFilterAware.afterFillInfoCodeDataModel()` SHALL 移除所有被标记为 invalid 的 aggregate
- 开关关闭后，响应中 SHALL 不存在任何与压差相关的信息点数据或定义

#### Scenario: 开关关闭时查询信息点

- **WHEN** 用户调用 `listInfoCodeConfigsByObjectId` 查询某空间的信息点配置，且该空间对应的需求配置关闭了压差品质开关（`isPressureDiffQualityEnabled = false`）
- **THEN** 响应的 `objectAdditionalInfo` 分组下不包含 `relativePressureDiffToOtherSpace` 信息点
- **AND** 响应中不存在任何压差相关的信息点定义或数据
