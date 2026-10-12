## Why

空调运行标准的环境品质目标已支持压差配置（上下限值、开关控制），但 `listInfoCodeConfigsByObjectId` 接口在"对象拓展信息点"分组下尚未展示压差标准的上下限值。CO2 标准值已通过 `DemandConfigInfoCodeAnalyzeStrategy` 在该分组下正常展示，压差标准需要遵循相同模式补充展示，且须受 `isPressureDiffQualityEnabled` 开关控制——开关关闭时完全不返回压差信息点。

## What Changes

- **在 `DemandConfigInfoCodeAnalyzeStrategy` 中新增压差品质映射逻辑**：`mapSpaceIdAndDemandConfig()` 方法新增 `relativePressureDiffToOtherSpace` 信息点的品质配置读取，仿照 CO2 标准逻辑从 `DemandConfigAirconditioningEnvironmentQualityObject.pressureDiffQuality` 取值
- **开关控制：关闭时完全移除 aggregate**：当 `isPressureDiffQualityEnabled` 为 false 时，不在结果 map 中放入压差数据，并在 `fillInfoCodeDataModel()` 中对压差 aggregate 标记 `setInvalid(true)`，由 `InfoCodeAggregateLifeFilterAware.afterFillInfoCodeDataModel()` 完全移除（连定义都不返回）
- **复用现有数据库配置和代码常量**：不新增 DB 记录、不修改 Flyway、不新增枚举项，数据库中 `relativePressureDiffToOtherSpace` 的 `DEMAND_CONFIG` 类型信息点配置已存在

## Capabilities

### New Capabilities

- `pressure-diff-standard-info-code-display`：信息点配置接口中压差标准的展示能力，包含开关控制下的数据填充与 aggregate 移除

### Modified Capabilities

- `airconditioning-pressure-diff-quality`：扩展压差品质配置的展示范围，从运行标准查看接口延伸到信息点配置查询接口

## Impact

- **common 模块**：`DemandConfigInfoCodeAnalyzeStrategy` 策略类新增压差映射逻辑和开关判断
- **前端**：`listInfoCodeConfigsByObjectId` 接口在 `objectAdditionalInfo` 分组下将出现压差标准信息点（开关开启时），前端需配合展示
