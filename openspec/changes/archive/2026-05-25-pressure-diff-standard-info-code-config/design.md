## Context

当前 `listInfoCodeConfigsByObjectId` 接口通过 `DemandConfigInfoCodeAnalyzeStrategy` 将需求配置（tdb/co2/rh）的标准值映射到信息点数据模型中，在 `objectAdditionalInfo` 分组下展示。压差品质配置（`pressureDiffQuality`）已在需求配置模型和运行标准查看接口中完整实现，但尚未映射到信息点配置查询接口。

关键约束：
- 数据库中 `relativePressureDiffToOtherSpace` 信息点的 `DEMAND_CONFIG` 类型配置已存在，无需新增
- `InfoCodeEnum.RELATIVE_PRESSURE_DIFF` 枚举已存在，code 为 `relativePressureDiffToOtherSpace`
- `DemandConfigAirconditioningPressureDiffQualityValueObject` 已包含 `lowerLimitValue` 和 `upperLimitValue` 字段
- `DemandConfigAirconditioningEnvironmentQualityObject` 已包含 `isPressureDiffQualityEnabled` 开关和 `pressureDiffQuality` 对象
- `InfoCodeAggregate.invalid` 机制 + `InfoCodeAggregateLifeFilterAware.afterFillInfoCodeDataModel()` 已提供 aggregate 移除能力

## Goals / Non-Goals

**Goals:**
- 在 `listInfoCodeConfigsByObjectId` 接口响应的 `objectAdditionalInfo` 分组下展示压差标准的上下限值
- 遵循 CO2 标准的现有映射模式
- 开关关闭时完全移除压差 aggregate（连定义都不返回）

**Non-Goals:**
- 不新增数据库记录或 Flyway 脚本
- 不新增枚举值或信息点编码
- 不修改前端逻辑
- 不修改 `InfoCodeAggregateLifeFilterAware` 的过滤逻辑

## Decisions

### D1: 在 `mapSpaceIdAndDemandConfig()` 中增加压差映射

**选择**：在 `mapSpaceIdAndDemandConfig()` 方法中新增 `spaceIdAndPressureDiffDemandConfigs` map，遍历需求配置时提取 `pressureDiffQuality`，使用 `InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode()` 作为 map key。

**理由**：与 tdb/co2/rh 映射模式完全一致，代码一致性高。使用枚举常量而非硬编码字符串，避免拼写错误。

**替代方案**：新建独立的策略类 → 过度设计，压差与 CO2 同属 `DEMAND_CONFIG` 类型，共享策略更合理。

### D2: 开关关闭时不放入 result map

**选择**：当 `isPressureDiffQualityEnabled` 为 false 或 `pressureDiffQuality` 为 null 时，不往 `spaceIdAndPressureDiffDemandConfigs` 中放数据，也不往 `result` 中放 `relativePressureDiffToOtherSpace` key。

**理由**：这样在 `fillInfoCodeDataModel()` 中 `configMap.get(infoCode)` 返回 null，可以明确判断"该信息点无配置"，与其他信息点的处理逻辑区分开。

### D3: 开关关闭时通过 `setInvalid(true)` 完全移除 aggregate

**选择**：在 `fillInfoCodeDataModel()` 中，当压差信息点的 `configByInfoCode` 为 null 时，调用 `aggregate.setInvalid(true)`，由 `InfoCodeAggregateLifeFilterAware.afterFillInfoCodeDataModel()` 在生命周期过滤阶段移除。

**理由**：这是系统已有的 aggregate 移除机制（`InfoCodeSetExtendConfigAnalyzeStrategy` 和 `RwdTagInfoCodeAnalyzeStrategy` 均使用此方式），完全遵循既有模式。开关关闭时连定义都不返回，满足业务需求。

**替代方案**：在 `afterAggregateInit()` 阶段提前移除 → 需要额外查询需求配置，增加复杂度且不符合职责划分。

## Risks / Trade-offs

- **[fillInfoCodeDefineModel 仍会填充压差定义]** → 开关关闭时，定义模型仍会被填充一次，但随后 `setInvalid(true)` + `afterFillInfoCodeDataModel` 会移除 aggregate，开销可忽略
- **[前端兼容性]** → 开关开启后，接口响应会多出压差信息点，前端需确认是否需要适配展示 → 如前端已做通用渲染则无需改动
