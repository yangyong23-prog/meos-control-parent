# 运行监测策略模型

运行监测类型策略用于解决“不同业务类型的对象范围、Dashboard 查询和异常事件过滤不同”的问题。它是当前运行组维护运行监测能力的核心模型，应作为后续新增能力的主要扩展点。

## 类型枚举

`RunningMonitorTypeEnum` 表示运行监测一级类型，主要字段包括：

| 字段 | 说明 |
| --- | --- |
| `value` | 类型编码，如 `LIGHTING`、`AIR_TERMINAL`、`AIR_CONDITIONING_COLD_SOURCE`。 |
| `reasonPhrase` | 国际化文案 key。 |
| `strategyBeanName` | 对应 Spring 策略 Bean 名称；为空时使用静态策略。 |

`RunningMonitorTypeEnum#getRunningMonitorStrategy` 会按 `strategyBeanName` 从 Spring 容器取策略；没有配置策略 Bean 时创建 `StaticRunningMonitorStrategy`。这意味着新增类型不一定必须新增策略，但只要对象范围、Dashboard 或异常过滤有差异，就应该提供专用策略。

`RunningMonitorSecondTypeEnum` 表示二级类型，承担两个转换职责：

| 方法 | 说明 |
| --- | --- |
| `toRunningMonitorTypeEnum` | 把二级类型归并到一级运行监测类型。 |
| `toDemandTemplateTypeEnum` | 把与需求相关的二级类型转换为需求模板类型。 |

二级类型中包含运行分析相关值，例如 `RUN_ANALYSIS_TDB`、`RUN_ANALYSIS_CO2`。这些值会映射到 `RUN_ANALYSIS`，但运行监测策略只描述对象范围和策略分派，运行分析的算法和报表逻辑应单独维护。

## 策略接口

位置：`common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/RunningMonitorStrategy.java`

| 方法 | 职责 |
| --- | --- |
| `runningMonitorType` | 返回当前策略服务的一级运行监测类型。 |
| `demandInheritType` | 返回需求继承类型，用于和需求实例层级联动。 |
| `listSpaces` | 按运行监测空间查询参数返回空间集合。 |
| `listDashboardSpaces` | 返回 Dashboard 需要的空间集合。 |
| `listDashboardSystems` | 返回 Dashboard 需要的系统集合。 |
| `listEquipments` | 按运行监测设备查询参数返回设备集合。 |
| `exceptionFilterCondition` | 返回异常事件二次过滤函数。 |
| `exceptionFilterRiskMeasureAimLevel2` | 返回异常事件查询的二级风险目标集合。 |
| `setExceptionEventObjectQueryParam` | 给异常事件对象查询参数追加策略特定条件。 |

接口中保留了若干废弃方法，用于兼容旧的 `DemandInstanceValueObject` 参数。新逻辑必须优先使用 `RunningMonitorSpaceQueryParam`、`RunningMonitorEquipmentQueryParam` 和 `RunningMonitorSystemQueryParam`。

## 基础策略

`BaseRunningMonitorStrategy` 提供通用能力：

- 将新查询参数转换为旧的 `DemandInstanceValueObject`。
- 校验建筑、楼层、对象过滤类型等旧参数约束。
- 提供按实例化规则做并集、交集过滤的工具方法。
- 提供过滤无效需求空间的公共方法。

`StaticRunningMonitorStrategy` 用于没有专用策略的类型。它保存当前枚举类型，并提供相对静态或默认的对象查询行为。适合对象范围不需要复杂规则的类型。

## 业务策略

策略实现位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl
```

当前代码中包含冷热源、冷源、热源、照明、空调末端、通风、安防、电梯、客流、停车场、厨房油烟机、机房安全、消防、消防电、污水、弱电、空调冷源、空调热源、空调冷热源、空压机、卫生间、光伏、智能体等策略。

策略只围绕以下差异展开：

- 当前业务类型应该从 RWD 查询哪些空间、设备或系统。
- 是否需要和需求模板、实例继承、空间功能类型联动。
- Dashboard 需要的空间或系统是否和运行监测页面不同。
- 异常事件是否只看特定风险目标，是否需要额外过滤。
- 异常事件对象查询时是否需要追加对象筛选条件。

## 与订阅类型的关系

`RunningMonitorTypeEnum#of(SubscribeTypeEnum)` 和 `RunningMonitorSecondTypeEnum#of(SubscribeTypeEnum)` 把 SSE 或页面订阅类型映射到运行监测类型。新增订阅类型时需要同步检查这两个方法，否则前端订阅、运行监测查询和策略分派可能不一致。

订阅映射只用于类型识别和策略分派，不代表策略层负责推送链路本身。推送和控制类能力不属于当前运行组维护范围；运行日志属于运行组维护，但不属于策略层职责。

## 维护要点

- 新增一级类型时，需要检查枚举值、国际化 key、策略 Bean、订阅类型映射和二级类型映射。
- 新增二级类型时，需要检查 `toRunningMonitorTypeEnum`；如果二级类型关联需求配置，还要检查 `toDemandTemplateTypeEnum`。
- 策略中不要直接下发控制指令、查询运行日志或组装 2D 系统图运行态；策略只负责对象范围和过滤条件。
- 异常事件过滤相关方法应保持可组合，不要在通用异常事件服务中写死业务类型判断。
