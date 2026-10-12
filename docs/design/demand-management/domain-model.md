# 领域模型

需求管理的领域模型围绕模板、日期、场景、实例、配置、配置分组和聚合根展开。配置数据以 JSON 存储，但在业务处理中会被模板类型策略解析为强类型配置对象。

## 核心实体

| 实体 | 表/含义 | 说明 |
| --- | --- | --- |
| `DemandTemplate` | `demand_template` | 需求模板，包含需求类型、模板类型、模板标签、模板名称、启用状态等。 |
| `DemandTemplateCalendar` | 模板日期设置 | 维护模板和日期标签/日期范围的关系。 |
| `DemandScene` | 需求场景 | 维护模板下的场景配置。 |
| `DemandConfig` | `demand_config` | 需求配置，按模板、场景、实例类型、实例 ID 存储 JSON 配置。 |
| `DemandConfigGroup` | 配置分组 | 自定义配置实例和空间功能类型配置的分组信息。 |
| `DemandInstanceConfig` | 实例配置 | 存储黑名单、屏蔽等实例级配置。 |
| `DemandBlacklistRule` | 黑名单规则 | 存储黑名单策略规则。 |
| `DemandConfigLighting` / `DemandConfigLightingScene` / `DemandConfigLightingResult` | 照明专项历史/兼容配置 | 支撑照明需求配置、场景和计算结果。 |
| `DemandConfigAirconditioningCustom` | 空调专项配置 | 支撑空调自定义和值选相关配置。 |

## 需求配置聚合根

`DemandConfigAggregate<T extends BaseDemandConfig>` 是需求配置查询的核心输出模型。

主要字段：

| 字段 | 含义 |
| --- | --- |
| `identifier` | 聚合标识，包含模板类型、模板标签、模板 ID、日期、场景、配置分组和实例。 |
| `config` | 当前实例自身配置。 |
| `parentConfig` | 当前实例继承到的父级配置。 |
| `finalConfig` | 最终生效配置，由自身配置和父级配置合并得到。 |
| `inheritInstanceAndModes` | 当前配置继承自哪些实例，以及继承模式。 |

`setConfig` 和 `setParentConfig` 都会触发 `handleFinalConfig()`，因此最终配置由聚合根自身维护。

## 聚合标识

`DemandConfigIdentifier` 由以下维度组成：

| 字段 | 说明 |
| --- | --- |
| `templateType` | 需求模板类型。 |
| `inheritConfigType` | 继承配置类型，来自模板类型策略。 |
| `templateTag` | 模板标签。 |
| `templateId` | 模板 ID。 |
| `configDate` | 配置日期，未传时默认当前日期。 |
| `scene` | 需求场景，可为空。 |
| `configGroup` | 配置分组，可为空。 |
| `roomFuncTypeRelGroupIdAndConfigGroup` | 空间功能类型配置关联分组。 |
| `instance` | 需求实例。 |

## 配置值对象

`DemandConfigValueObject<T>` 是配置 JSON 的强类型承载对象。

| 字段 | 说明 |
| --- | --- |
| `globalConfig` | 全局配置。 |
| `spaceFuncTypeConfigs` | 空间功能类型配置列表。 |

它会校验 `globalConfig` 和每个 `DemandSpaceFuncTypeConfig<T>`，并提供 `filterSpaceFuncTypeRelGroupId` 等辅助能力。

`BaseDemandConfig` 是所有具体配置对象的基类，提供：

- `fillDefaultConfig(DemandInstanceTypeEnum)`
- `allFieldEmpty()`
- `anyFieldNotEmpty()`
- `allFieldNotEmpty()`
- 基于 `PropertyDescriptorParser` 的字段缓存和反射读写能力。

## 实例值对象

`DemandInstanceValueObject` 表示一个需求实例：

| 字段 | 说明 |
| --- | --- |
| `instanceType` | 实例类型。 |
| `instanceId` | 实例 ID。 |
| `instanceName` | 扩展字段，查询后填充本地名称。 |

`DemandInstanceTypeEnum` 当前支持：

| 类型 | 层级 | RWD 类型 |
| --- | --- | --- |
| `PROJECT` | 0 | `project` |
| `BUILDING` | 1 | `building` |
| `FLOOR` | 2 | `floor` |
| `SPACE` | 3 | `space` |

层级数字越大代表越靠近末端。空间可向楼层、建筑、项目查找父级配置。

## 需求类型和模板类型

`DemandTypeEnum` 是需求大类：

| 类型 | 说明 | 策略 |
| --- | --- | --- |
| `AIRCONDITIONING` | 空调需求 | `DemandAirconditioningStrategyImpl` |
| `LIGHTING` | 照明需求 | `DemandLightingStrategyImpl` |
| `SECURITY` | 安防需求 | `DemandSecurityStrategyImpl` |
| `VENTILATION` | 通风需求 | `DemandVentilationStrategyImpl` |

`DemandTemplateTypeEnum` 是具体模板类型，并映射到配置解析策略：

| 大类 | 模板类型 |
| --- | --- |
| 空调 | `AIRCONDITIONING` |
| 照明 | `PUBLIC_AREA_LIGHTING`、`PARKING_LOT_LIGHTING`、`BUILDING_LIGHTING`、`LANDSCAPE_LIGHTING`、`SIGN_LIGHTING` |
| 安防 | `ARMING`、`PASSING` |
| 通风 | `LOGISTICS_VENTILATION`、`PARKING_VENTILATION`、`TOILET_VENTILATION`、`DOME_VENTILATION` |

## 模板标签

`DemandTemplateTagEnum` 用于把日历日期标签映射到需求模板标签：

| 标签 | 说明 |
| --- | --- |
| `WORKDAY` | 工作日。 |
| `DAYOFF` | 休息日。 |
| `HOLIDAY` | 节假日。 |
| `ANNIVERSARY` | 店庆日。 |
| `CUSTOM` | 自定义。 |

模板标签有优先级和排序，日期匹配时需要保持现有标签映射规则，不应把 `MINOR_VACATION`、`NATIONAL_DAY`、`NEW_YEAR` 等节假日拆成独立需求模板标签。
