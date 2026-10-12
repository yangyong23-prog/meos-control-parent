# 扩展与维护指南

信息点配置模块的扩展应优先沿用已有分层：接口参数由 Controller/Service 承接，聚合根由 `InfoCodeGateway` 创建，定义和值由 `InfoCodeAnalyzeStrategy` 处理，横切增强由 `InfoCodeAggregateLifeAware` 处理。

## 新增信息点类型

适用场景：需要支持新的 `infoCodeType`，并且该类型有独立的定义或值填充逻辑。

步骤：

1. 在 `InfoCodeTypeEnum` 中增加类型枚举。
2. 实现对应的 `InfoCodeAnalyzeStrategy`。
3. 在策略中返回新的 `infoCodeType()`。
4. 如需解析 `tackValueLogic`，在对应模型中增加类型化解析方法或复用已有 value object。
5. 确认 `fillInfoCodeDefineModel` 和 `fillInfoCodeDataModel` 都覆盖必要逻辑。
6. 增加或更新用例，至少覆盖定义填充、值填充和空数据路径。

检查点：

- 不要在 Service 中硬编码新类型的取值逻辑。
- 不要让策略逐条访问外部服务，优先批量查询或复用 `additionalDataValues`。
- 如果新类型属于 IOT、知识或风险事件分类，需要同步检查 `InfoCodeTypeEnum` 的类型判断方法。

## 新增生命周期处理器

适用场景：需要在聚合根创建前后批量修正模板、配置、分组、上下文或聚合根。

步骤：

1. 实现 `InfoCodeAggregateLifeAware`。
2. 注册为 Spring Bean。
3. 实现 `getOrder()`，明确执行优先级。
4. 只覆写需要的生命周期方法。
5. 保持处理器无状态；如确实需要跨生命周期共享请求级状态，再考虑 prototype。

检查点：

- 单例 Bean 不应使用实例字段保存请求数据。
- 生命周期处理器不应替代具体信息点类型策略。
- 修改配置集合时要考虑排序、去重和空集合行为。

## 新增接口参数

适用场景：接口需要增加过滤字段、时间字段、placeholder 或展示配置字段。

步骤：

1. 更新请求参数类。
2. 更新 Converter 映射，尤其是 `InfoCodeConverter`。
3. 更新 DAO 参数或网关参数。
4. 检查新版接口和旧版接口是否共用同一个参数类。
5. 检查 Service 私有方法是否需要透传字段，例如 `queryInfoCodeObjectGroupVo`。
6. 更新接口契约文档和回归用例。

检查点：

- `objectType` 与 `objectTypes` 同时存在时，应保持资源层交集行为。
- `placeholders` 需要从事件或接口参数透传到 `InfoCodeConfigByObjectQueryParam`。
- 空集合和 null 应保持原有查询行为，不应意外变成“查全部”或“查不到”。

## 新增模板或分组配置

适用场景：业务需要新增一种模板类型、分组或信息点配置。

检查点：

- 模板类型的 `runningMonitorType`、`runningMonitorSecondType`、`templateType`、`objectType`、`classCode` 是否完整。
- `infoCodeFilter` 是否需要 source、placeholder 或关系查询。
- 信息点配置的 `infoCodeType` 是否存在对应策略。
- 分组的 `groupCode`、父子关系、排序和展示样式是否满足前端展示。
- 对象附加信息、IOT 设定值、动态过滤等特殊分组是否需要加入已有分组集合。

## 修改 RWD source 或关系查询

涉及 source、`queryParam`、`takeValueExpression`、`sourceValueExpression`、`relationFrom`、`relationTo` 时，需要同步检查取值表达式和关系绑定规则。

关键规则：

- 直接 placeholder 匹配优先于 SpEL root 引用。
- `#root['key']`、`#root["key"]`、`#root.key` 都可能代表 source 依赖。
- 关系查询如果使用 `${objectId}` 作为 source key，关系绑定值应使用对象 `id`。
- `takeValueExpression` 与关系匹配 ID 是两个概念，不能混用。

## 修改缓存逻辑

涉及 `infoCodeTemplate`、`infoCodeTemplateObject`、`infoCodeTemplateObject_deprecated`、`infoCodeTemplateObjectIotRule` 时，需要确认：

- 请求参数是否完整参与缓存 key。
- 新旧接口上下文是否需要分开缓存。
- 对象过滤条件是否会改变结果。
- IOT 规则是否会改变模板对象可用性。
- 缓存失效是否覆盖模板、配置、分组和对象关系变化。

## 最小验证清单

- 编译检查通过，尤其是 MapStruct 映射。
- 新增参数在 Controller、Service、Converter、DAO 参数之间完整传递。
- 按模板查询和按对象查询都覆盖。
- 空模板、空对象、空配置、空 IOT 值路径返回稳定。
- 旧版 `/equipment/infoCodeConfig` 行为未被误改。
- 涉及前端展示时，确认 `InfoCodeObjectGroupVO` 输出结构没有破坏分组层级和排序。
