# 黑名单与值选

黑名单和值选是需求管理中的两个专项能力。黑名单影响需求实例是否参与配置查询，值选主要服务空调需求的配置选择和趋势展示。

## 黑名单入口

黑名单 Controller 位于 SaaS server：

```text
saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/demand/DemandBlacklistController.java
```

基础路径：`/demand/blacklist`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/addRules` | 保存黑名单规则。 |
| POST | `/listSpaceIdsByRule` | 按规则解析空间 ID。 |
| POST | `/query` | 查询黑名单规则配置。 |

通用服务和策略位于 `common`：

```text
common/src/main/java/com/persagy/apccontrol/basic/service/demand/blacklist
```

## 黑名单策略

黑名单规则通过 `DemandBlacklistTypeEnum` 映射到策略实现。现有策略包括：

| 策略 | 说明 |
| --- | --- |
| `DemandBlacklistAirTerminalStrategy` | 末端空调服务空间规则。 |
| `DemandBlacklistSystemServiceSpaceStrategy` | 系统服务空间规则。 |
| `DemandBlacklistRoomFuncTypeStrategy` | 空间功能类型规则。 |
| `DemandBlacklistInstantiateRuleStrategy` | 实例化规则。 |
| `DemandBlacklistInFloorStrategy` | 楼层范围规则。 |

公共逻辑抽在 `AbstractDemandBlacklistStrategy`，策略接口为 `DemandBlacklistStrategy`。

## 黑名单对配置查询的影响

配置查询上下文中，`DemandConfigAggregateInitByInstanceContext#fillInstanceIds()` 会移除无效实例。无效实例查询条件由 `DemandInstanceConfigValueObject` 表达：

- `inBlacklisted=true`：实例在黑名单中。
- `shielded=true`：当查询参数要求过滤屏蔽空间时，一并过滤屏蔽实例。

过滤发生在聚合根生成之前，因此被过滤的实例不会进入配置继承和 VO 输出。

## 屏蔽空间

屏蔽空间和黑名单都属于实例配置的一部分，主要通过 `DemandInstanceGateway`、`DemandInstanceRepository`、`DemandInstanceConfig` 和 `DemandInstanceConfigValueObject` 查询。

页面树接口和配置查询接口都可能提供过滤参数。新增实例过滤条件时，需要同步检查：

- 实例树查询。
- 配置查询 `fillInstanceIds()`。
- 自定义配置实例列表。
- 对外需求空间接口。

## 值选入口

值选 Controller 位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/controller/demand/ValueSelectionController.java
```

基础路径：`/valueSelection`

| 方法 | 说明 |
| --- | --- |
| `listValueSelectType` | 查询值选类型。 |
| `listConfigsByType` | 按类型查询值选配置。 |
| `currentTempAndCarbonRate` | 查询当前温度和碳排系数。 |
| `currentApplyConfig` | 查询当前应用配置。 |
| `saveConfig` | 保存值选配置。 |
| `listElectricityData` | 查询电量趋势。 |
| `listCarbonData` | 查询碳排趋势。 |

服务入口：

```text
common/src/main/java/com/persagy/apccontrol/basic/service/demand/airconditioning/ValueSelectionService.java
common/src/main/java/com/persagy/apccontrol/basic/service/demand/airconditioning/impl/ValueSelectionServiceImpl.java
```

## 空调专项接口

`DemandAirconditioningController` 提供：

| 方法 | 说明 |
| --- | --- |
| `getWorkConditionByShellConfigData` | 根据空调壳配置数据解析工况。 |
| `fixHistoryValueSelectConfig` | 修复历史值选配置兼容数据。 |

这些接口服务空调需求专项，不应放入通用 `DemandQueryService` 主流程中。

## 维护规则

- 黑名单规则新增类型时，要在枚举、策略实现和 DTO/VO 转换上保持一致。
- 黑名单和屏蔽空间影响实例过滤，应在聚合根生成前处理，避免后续继承链引用无效实例。
- 值选接口属于空调专项，新增逻辑优先落在 `ValueSelectionService` 或空调需求策略中。
- 对外接口依赖值选配置时，要确认项目、日期和模板类型上下文是否完整传递。
