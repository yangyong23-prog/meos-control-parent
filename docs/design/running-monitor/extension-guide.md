# 扩展与维护指南

当前运行监测策略扩展应围绕 `RunningMonitorStrategy` 策略体系展开。新增能力前先确认它是否属于策略范围：如果需求是控制、手自动、强控、干预、累计运行时间清除或 2D 系统图旧运行态，应转到对应边界处理，不应扩展运行监测策略。运行日志见独立的 [运行日志程序设计文档](../running-log/README.md)。

## 新增运行监测类型

检查清单：

- 在 `RunningMonitorTypeEnum` 增加一级类型、国际化 key 和策略 Bean 名称。
- 在 `RunningMonitorSecondTypeEnum` 增加二级类型，并维护 `toRunningMonitorTypeEnum`。
- 如二级类型关联需求配置，维护 `toDemandTemplateTypeEnum`。
- 如该类型有 SSE 或订阅入口，维护两个枚举的 `of(SubscribeTypeEnum)` 映射。
- 新增或复用 `RunningMonitorStrategy` 实现。
- 检查 Dashboard 空间、系统和设备查询是否需要专用逻辑。
- 检查异常事件风险目标和过滤条件是否需要策略实现。
- 检查策略查询参数中的二级类型和缓存 key 是否能区分新类型。

## 新增策略

新增策略时优先继承 `BaseRunningMonitorStrategy`，并实现：

- `runningMonitorType`
- `demandInheritType`
- `listSpaces`
- `listDashboardSpaces`
- `listDashboardSystems`
- `listEquipments`

如果异常事件展示有差异，再实现：

- `exceptionFilterCondition`
- `exceptionFilterRiskMeasureAimLevel2`
- `setExceptionEventObjectQueryParam`

策略只负责对象范围和过滤条件，不应写 IOT 查询、运行日志查询或控制下发逻辑。

## 新增查询参数能力

检查清单：

- 新字段是否属于空间、设备、系统三类参数的共同能力，若是应优先放在公共基类。
- 过滤条件能否下推到 RWD 查询，不能下推时是否已经补齐内存过滤所需字段。
- 是否影响 `mysqlCacheKeyType` 或 `canCache`。
- 是否需要在 `fillRequiredWithColumns` 中补齐必须查询字段。
- 是否需要在返回前通过 `withColumns` 裁剪。
- 是否会引入实时值依赖，如果会，默认不应复用缓存。

## 新增异常事件叠加

检查清单：

- 是否需要策略声明风险目标或二次过滤函数。
- 是否需要 `setExceptionEventObjectQueryParam` 给异常事件对象查询追加条件。
- 过滤条件是否只依赖策略可识别的对象范围，不要耦合旧 2D 系统图运行态。
- 不要在异常事件服务中写死运行监测类型判断。

## 非运行组能力

以下能力不在当前运行监测策略扩展范围内：

- 批量信息点值和信息点分组树展示。
- 2D 系统图运行态对象集合和系统运行参数。
- 参数设定、手自动、强控、干预解除和累计运行时间清除。
- 旧 `/running-monitor` Controller 和边缘 Controller 的新增接口。


## 排查建议

- 对象缺失：先查二级类型映射，再查策略实现和 RWD 查询条件。
- Dashboard 数据不一致：先确认调用的是 `listDashboardSpaces`、`listDashboardSystems` 还是普通 `listSpaces`/`listEquipments`。
- 缓存结果不一致：检查二级类型是否进入 `mysqlCacheKeyType`，以及查询是否包含实时值过滤。
- 异常事件过滤不生效：检查策略的风险目标、过滤函数和对象查询参数扩展点。
