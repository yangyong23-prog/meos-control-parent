# 业务类型与策略

需求管理通过两层策略隔离业务差异：`DemandStrategy` 面向需求大类，`DemandConfigAnalyzeStrategy` 面向具体模板类型配置解析。

## 需求大类策略

`DemandStrategy` 的职责：

| 方法 | 说明 |
| --- | --- |
| `getTemplateAndCheckThrow` | 查询并校验启用模板。 |
| `listTemplateTypes` | 查询当前需求大类支持的模板类型。 |
| `listDemandSpace` | 查询模板类型下的需求空间。 |
| `mapDemandInstanceCustomizedTagNameAndValue` | 查询实例自定义标签。 |
| `demandType` | 返回需求大类。 |
| `externalListConfigResultsByInstance` | 对外输出需求配置结果。 |

当前大类策略：

| 需求大类 | 策略实现 |
| --- | --- |
| 空调 | `DemandAirconditioningStrategyImpl` |
| 照明 | `DemandLightingStrategyImpl` |
| 安防 | `DemandSecurityStrategyImpl` |
| 通风 | `DemandVentilationStrategyImpl` |
| 厨房排油烟 | `DemandKitchenRangeHoodStrategyImpl` |

## 模板配置解析策略

`DemandConfigAnalyzeStrategy<T extends BaseDemandConfig>` 的职责：

| 方法 | 说明 |
| --- | --- |
| `templateType` | 支持的模板类型。 |
| `inheritConfigType` | 配置继承类型。 |
| `analyzeConfig` | 将 JSON 配置解析为 `DemandConfigValueObject<T>`。 |
| `emptyConfig` | 创建空配置。 |
| `defaultConfig` | 创建指定实例类型的默认配置。 |
| `beforeAddConfigHandler` | 保存前预处理配置。 |

`DemandTemplateTypeEnum#demandConfigAnalyzeStrategy()` 会根据枚举中的 Bean 名称从 Spring 容器中取出策略。

## 空调需求

模板类型：`AIRCONDITIONING`

主要代码：

- `DemandAirconditioningStrategy`
- `DemandAirconditioningStrategyImpl`
- `DemandAirconditioningQueryService`
- `DemandAirconditioningQueryServiceImpl`
- `DemandConfigAirConditioningAnalyzeStrategy`
- `DemandConfigAirconditioningValueObject`
- `ValueSelectionService`

空调需求除了基础配置查询外，还包含值选配置、工况解析、电量/碳排趋势等专项能力。

## 照明需求

模板类型：

- `PUBLIC_AREA_LIGHTING`
- `PARKING_LOT_LIGHTING`
- `BUILDING_LIGHTING`
- `LANDSCAPE_LIGHTING`
- `SIGN_LIGHTING`

主要代码：

- `DemandLightingStrategy`
- `DemandLightingStrategyImpl`
- `DemandLightingService`
- `DemandLightingQueryService`
- `DemandConfigLightingHandler`
- `DemandConfigLightingComputeService`
- `DemandConfigLightingComputeNowService`
- `DemandConfigLighting*AnalyzeStrategy`
- `DemandConfigLightingValueObject`

照明需求有场景、回路、功能类型、灯具功能类型和反馈调节等专项模型。场景配置变更后可能触发照明结果计算。

## 安防需求

模板类型：

- `ARMING`
- `PASSING`

主要代码：

- `DemandSecurityStrategy`
- `DemandSecurityStrategyImpl`
- `DemandConfigSecurityArmingAnalyzeStrategy`
- `DemandConfigSecurityPassingAnalyzeStrategy`
- `DemandConfigSecurityArmingValueObject`
- `DemandConfigSecurityPassingValueObject`

安防需求通过模板类型区分布防和通行配置。

## 通风需求

模板类型：

- `LOGISTICS_VENTILATION`
- `PARKING_VENTILATION`
- `TOILET_VENTILATION`
- `DOME_VENTILATION`

主要代码：

- `DemandVentilationStrategy`
- `DemandVentilationStrategyImpl`
- `DemandConfigVentilation*AnalyzeStrategy`
- `BaseDemandConfigVentilationValueObject`
- 各通风场景的配置 value object

通风需求按机房后勤、停车场、卫生间、穹顶等场景拆分配置解析策略。

## 厨房排油烟需求

模板类型：`KITCHEN_RANGE_HOOD`

主要代码：

- `DemandKitchenRangeHoodStrategy`
- `DemandKitchenRangeHoodStrategyImpl`
- `DemandConfigKitchenRangeHoodAnalyzeStrategy`
- `DemandConfigKitchenRangeHoodValueObject`

空间范围为：设备类 `ACKLSF` 且风机定位（fanPos）=1 的排油烟风机所服务、且服务空间信息点"是否需要排油烟"（needExaustSmoke）=1 的空间，复用运行监控 `RunningMonitorKitChenRangeHoodStrategy` 的查询口径。配置仅"排油烟需求时间"（`exhaustTimeIntervals`），不含送风需求时间与反馈调节。本期未接入需求配置结果预计算。

## 策略设计规则

- 需求空间查询放在 `DemandStrategy`，不要散落在 Controller 或 Service 私有方法里。
- 配置 JSON 解析放在 `DemandConfigAnalyzeStrategy#analyzeConfig`，不要直接在查询服务中手写 `ObjectMapper` 转换。
- 默认值放在 `defaultConfig`，继承逻辑会读取项目级默认配置。
- 保存前需要清洗或补齐配置时，使用 `beforeAddConfigHandler`。
- 新模板类型必须同时补齐枚举、策略、配置 value object、默认配置和必要的查询输出。
