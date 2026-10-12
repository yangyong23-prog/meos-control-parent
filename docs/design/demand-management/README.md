# 需求管理程序设计文档

需求管理模块负责维护不同业务类型的需求模板、日期设置、场景、实例配置和具体配置数据，并按项目、建筑、楼层、空间的实例层级完成配置查询、继承、覆盖和对外输出。

模块通用代码主要位于 `common/src/main/java/com/persagy/apccontrol/basic`，SaaS 侧黑名单 Controller 和部分迁移/任务代码位于 `saas/`，本地边缘侧也有对应数据库迁移和实例组消息实现。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 模块职责、边界、核心包结构和总体关系。 |
| [接口与查询流程](./api-and-query-flow.md) | `/demand`、模板、场景、实例组、对外接口和值选接口的入口和主流程。 |
| [领域模型](./domain-model.md) | 模板、场景、实例、配置、分组、聚合根和值对象。 |
| [网关、生命周期与继承](./gateway-lifecycle-inheritance.md) | `DemandConfigGateway`、初始化上下文、生命周期和配置继承规则。 |
| [模板、场景与日历](./template-scene-calendar.md) | 模板标签、日期设置、场景配置、复制和事件联动。 |
| [业务类型与策略](./strategy-and-business-types.md) | 空调、照明、安防、通风需求类型和策略扩展点。 |
| [黑名单与值选](./blacklist-and-value-selection.md) | 需求黑名单、屏蔽空间过滤、空调值选配置和趋势数据接口。 |
| [数据源与存储](./data-source-and-storage.md) | 资源层、仓储层、数据库表、RWD/知识/IOT 外部数据依赖。 |
| [扩展与维护指南](./extension-guide.md) | 新增需求类型、模板类型、配置字段、生命周期处理器和接口字段时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/demand` |
| 通用 Service | `common/src/main/java/com/persagy/apccontrol/basic/service/demand` |
| 需求结构层 | `common/src/main/java/com/persagy/apccontrol/basic/structure/demand` |
| 资源层 | `common/src/main/java/com/persagy/apccontrol/basic/resource/demand` |
| 实体与参数 | `common/src/main/java/com/persagy/apccontrol/basic/model/entity/demand`、`model/param/demand` |
| Mapper | `common/src/main/java/com/persagy/apccontrol/basic/mapper/demand`、`common/src/main/resources/mapper` |
| SaaS 黑名单入口 | `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/demand/DemandBlacklistController.java` |

## 维护原则

- 需求配置读取和保存必须通过 `DemandConfigGateway`，不要在接口层直接拼装继承后的最终配置。
- 业务类型差异通过 `DemandTypeEnum`、`DemandTemplateTypeEnum`、`DemandStrategy` 和 `DemandConfigAnalyzeStrategy` 承接。
- 配置 JSON 的类型化解析必须经过对应模板类型的 `DemandConfigAnalyzeStrategy#analyzeConfig`。
- 涉及继承、黑名单、屏蔽空间、空间功能类型分组时，需要同时检查查询上下文和聚合根最终配置。
- 新增字段时要检查 Controller 参数、Converter、Gateway 参数、Repository 参数、VO 输出和历史/对外接口。
