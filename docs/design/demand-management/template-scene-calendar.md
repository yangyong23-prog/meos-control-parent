# 模板、场景与日历

模板、场景和日历共同决定“某一天、某类需求、某个场景下应该使用哪套配置”。配置查询前必须先定位模板，再决定是否带场景查询。

## 模板类型

`DemandTypeEnum` 表示需求大类，`DemandTemplateTypeEnum` 表示具体模板类型。

`DemandTypeEnum#toDemandTemplateTypes()` 用于从大类展开模板类型列表。`DemandTemplateTypeEnum#toDemandTypeEnum()` 用于从模板类型回到大类，并进一步取得 `DemandStrategy`。

模板类型还负责：

- 映射运行监控二级类型：`toRunningMonitorSecondTypeEnum()`。
- 判断是否夜景照明：`isNightLighting()`。
- 获取配置解析策略：`demandConfigAnalyzeStrategy()`。

## 模板标签和日期标签

`DemandTemplateTagEnum` 将中台日历日期标签转换成需求模板标签：

| 日期来源 | 需求模板标签 |
| --- | --- |
| `WORKDAY` | `WORKDAY` |
| `DAYOFF` | `DAYOFF` |
| `MINOR_VACATION`、`NATIONAL_DAY`、`NEW_YEAR` | `HOLIDAY` |
| `ANNIVERSARY` | `ANNIVERSARY` |
| 其他或未知 | `CUSTOM` 或保留原逻辑 |

模板标签有优先级和排序。日期匹配、模板列表排序和默认模板选择都应复用现有枚举语义。

## 模板查询

模板查询入口：

- `DemandCalendarController#listTemplateType`
- `DemandCalendarController#listTemplate`
- `DemandCalendarController#listDateSetting`
- `DemandCalendarQueryServiceImpl`
- `DemandTemplateRepository`

`DemandTemplateRepository` 提供：

| 方法 | 说明 |
| --- | --- |
| `getByTemplateId` | 按模板 ID 查询。 |
| `getByTemplateIdAndDate` | 按模板 ID 和日期查询可用模板。 |
| `getByTemplateByTemplateTypeTag` | 按模板类型和标签查询。 |
| `listByTemplateIds` | 批量查询模板。 |
| `listTemplateIdsByTemplateType` | 按模板类型查询模板 ID。 |

## 日期设置

日期设置由 `DemandTemplateCalendar` 和 `DemandTemplateCalendarService` 支撑。查询服务会根据模板、日期范围和日期标签输出 `DemandTemplateDateSettingVO`。

`DemandCommonOpeningCalendarController#listInstantiationDateTag` 用于查询实例化日期标签，`listAirDateTagByRule` 用于空调相关日历规则。

## 场景模型

场景领域模型：

- `DemandSceneEntity`
- `DemandSceneIdentifier`
- `DemandConfigGroupEntity`
- `DemandConfigGroupIdentifier`

场景网关：

- `DemandSceneGateway`
- `DemandSceneGatewayImpl`
- `DemandSceneRepository`

主要能力：

| 方法 | 说明 |
| --- | --- |
| `getScene` | 按场景 ID 查询。 |
| `getValidSceneByNow` | 查询当前有效场景。 |
| `getValidSceneByTime` | 按时间查询有效场景。 |
| `listScenes` | 查询场景列表。 |
| `listScenesByTemplateId` | 按模板查询场景。 |
| `listValidScenesByDate` | 按日期查询有效场景。 |
| `saveOrUpdate` | 保存或更新场景。 |
| `addScene` / `editScene` / `delScene` | 新增、编辑、删除场景。 |
| `delSceneByTemplateId` | 删除模板下场景。 |

## 场景应用服务

`DemandSceneApplicationServiceImpl` 是场景接口的应用编排层：

| 方法 | 说明 |
| --- | --- |
| `listSceneConfig` | 查询模板下场景配置。 |
| `listAllScene` | 批量查询所有场景。 |
| `editSceneConfigs` | 保存场景和相关配置。 |
| `copySceneConfig` | 从一个场景复制到另一个场景。 |
| `copySceneConfigByTemplate` | 从一个模板复制场景配置到另一个模板。 |
| `listExistsSceneConfigTemplate` | 查询存在场景配置的模板。 |
| `resultComputeDemandLightingResult` | 场景变更后触发照明结果计算。 |
| `removeSceneAndConfig` | 删除场景及相关配置。 |

## 事件联动

需求模块存在以下事件：

| 事件 | 说明 |
| --- | --- |
| `DemandTemplateChangeEvent` | 模板变更。 |
| `DemandSceneChangeEvent` | 场景变更。 |
| `DemandConfigChangeEvent` | 需求配置变更。 |
| `DemandInstanceConfigChangeEvent` | 实例配置变更。 |
| `DemandInstanceGroupChangeEvent` | 实例组变更。 |

`DemandConfigGatewayImpl` 和 `DemandSceneGatewayImpl` 中都有事件监听方法。修改模板、场景或配置删除逻辑时，需要检查事件处理是否会同步清理相关场景、配置、分组和缓存结果。
