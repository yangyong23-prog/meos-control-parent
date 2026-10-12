# 扩展与维护指南

需求管理扩展应优先沿用现有的枚举、策略、网关、仓储和生命周期结构。不要把业务类型差异写进 Controller，也不要绕过 `DemandConfigGateway` 自行拼最终配置。

## 新增需求大类

适用场景：新增一个与空调、照明、安防、通风并列的需求大类。

步骤：

1. 在 `DemandTypeEnum` 增加枚举值、产品功能编码、策略 Bean 名称和排序。
2. 实现 `DemandStrategy`。
3. 为该大类定义一个或多个 `DemandTemplateTypeEnum`。
4. 为每个模板类型实现 `DemandConfigAnalyzeStrategy`。
5. 增加配置 value object，继承 `BaseDemandConfig`。
6. 补齐模板初始化、数据库迁移、Mapper/Resource/Repository 需要的字段。
7. 增加对外接口输出时，实现 `externalListConfigResultsByInstance`。

检查点：

- `DemandTypeEnum#toDemandTemplateTypes()` 是否覆盖新大类。
- `DemandTypeEnum#toRunningMonitorTypeEnum()` 是否需要映射运行监控类型。
- 页面入口是否需要新增 Controller，还是复用 `/demand/config/listConfigsByInstance`。

## 新增模板类型

适用场景：在现有大类下新增具体模板类型。

步骤：

1. 在 `DemandTemplateTypeEnum` 增加模板类型和配置策略 Bean 名称。
2. 在 `toDemandTypeEnum()` 中映射到需求大类。
3. 如有运行监控二级类型，补充 `toRunningMonitorSecondTypeEnum()`。
4. 实现对应 `DemandConfigAnalyzeStrategy`。
5. 新增或复用 `BaseDemandConfig` 子类。
6. 如需默认项目配置，实现 `defaultConfig(DemandInstanceTypeEnum.PROJECT)`。
7. 如需支持空间查询，确认对应 `DemandStrategy#listDemandSpace` 覆盖模板类型。

检查点：

- 配置继承类型是 `OBJECT` 还是 `FIELD`。
- 空间功能类型配置是否适用。
- 对外接口是否需要展示该模板类型。
- 日历模板标签是否需要初始化。

## 新增配置字段

适用场景：在某个需求配置 JSON 中增加字段。

步骤：

1. 在对应 `BaseDemandConfig` 子类中增加字段。
2. 增加校验和 `checkAndSet()` 逻辑。
3. 更新默认值逻辑。
4. 更新 `DemandConfigAnalyzeStrategy#analyzeConfig` 覆盖新字段。
5. 若是字段级继承，确认 getter/setter 能被属性描述符识别。
6. 更新 VO、前端字段和对外输出字段。

检查点：

- 空值是否表示继承父级字段。
- 默认值是否只在项目级生效。
- 保存空配置时是否会被 `addOrUpdateConfig` 忽略。
- `withColumns` 输出裁剪是否需要允许该字段。

## 新增生命周期处理器

适用场景：需要在聚合根初始化、继承处理、保存前后做横切处理。

步骤：

1. 实现 `DemandConfigLifeAware<T>` 或 `DemandInstanceConfigLifeAware`。
2. 注册为 Spring Bean。
3. 实现 `getOrder()` 明确优先级。
4. 只覆写需要的生命周期方法。

检查点：

- 生命周期处理器尽量无状态。
- 不要在处理器中替代业务策略解析。
- 修改聚合根列表时要考虑继承前后顺序。

## 新增黑名单规则

步骤：

1. 在 `DemandBlacklistTypeEnum` 增加规则类型。
2. 按现有黑名单策略包结构增加策略实现。
3. 补齐规则 DTO、VO 和参数校验。
4. 确认 `listSpaceIdsByRule` 和 `query` 都能识别新规则。
5. 检查配置查询中的实例过滤是否需要调整。

检查点：

- 黑名单规则应最终转换为空间或实例 ID 集合。
- 屏蔽空间和黑名单不要混淆，二者由不同字段表达。
- 保存规则时注意并发锁和重复规则覆盖行为。

## 修改配置保存流程

涉及 `DemandConfigGateway#addOrUpdateConfig` 时，需要检查：

- 模板和场景是否存在。
- 字段级继承下空配置是否应忽略保存。
- 配置分组不存在时是否需要创建。
- 已有分组是否需要重命名。
- 项目级空间功能类型分组是否需要新增、复用或删除。
- 是否需要发布配置变更事件。

## 修改清除或取消引用逻辑

涉及 `clearConfig`、`cancelReference` 时，需要检查：

- 清除范围是当前、下级还是当前加下级。
- 是否需要删除配置分组。
- 取消引用时是保留父级最终配置，还是保存空配置。
- 清除后父级继承链是否仍然正确。

## 最小验证清单

- `/demand/config/listConfigsByInstance` 覆盖自身配置、父级继承、空配置和无实例路径。
- 模板、场景、日期参数组合查询结果稳定。
- 黑名单和屏蔽空间过滤生效。
- 新增配置字段能保存、查询、继承、输出。
- 对外接口没有破坏已有字段。
- SaaS 和 local 迁移脚本保持一致性。
- MapStruct/Converter 编译通过，避免字段静默丢失。
