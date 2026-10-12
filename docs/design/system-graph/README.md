# 关系图程序设计文档

关系图模块负责把 RWD 对象、ADM 关系图模板、图例图标、管线和信息点配置组合成可编辑、可发布、可运行态消费的系统图数据。代码中同时使用“系统图”和“关系图”两个名称：接口和实体多以 `SystemGraph` 命名，业务说明和前端入口多称为关系图。

模块通用代码主要位于 `common/src/main/java/com/persagy/apccontrol/basic`；SaaS 侧图例和图标管理 Controller 位于 `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/graph`；SaaS 与 local 都有关系图相关 Flyway 初始化和升级脚本。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 模块职责、边界、核心包结构和总体关系。 |
| [接口与查询流程](./api-and-query-flow.md) | `/system/graph`、`/systemGraphEditor`、Dashboard、外部接口、图例图标接口的入口和主流程。 |
| [领域模型](./domain-model.md) | `SystemGraph`、副本、压缩数据、细节调整、异步任务、模板状态、图例图标和图纸 VO。 |
| [生成、编辑与发布](./generation-editing-publish.md) | 初始化、重新计算、编辑、复制、细节调整、发布、下架、删除的生命周期和约束。 |
| [运行态数据加工](./runtime-data-processing.md) | 已发布关系图查询后的图例、管线样式、固定标签、信息点值、滤镜、交互配置加工。 |
| [图例图标与公共配置](./legend-icon-and-config.md) | `GraphLegendService`、`GraphIconService`、`CommonConfigService` 相关配置和同步规则。 |
| [外部查询与缓存联动](./external-cache-integration.md) | Dashboard 查询、外部系统查询、`SystemRelationGraphHandler` 与本地缓存解析。 |
| [数据源与存储](./data-source-and-storage.md) | 数据表、Mapper、Flyway 脚本、压缩 JSON 和外部依赖。 |
| [扩展与维护指南](./extension-guide.md) | 新增图类型、模板、节点字段、图例图标、运行态字段和缓存联动时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| 展示查询 Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/systemgraph/SystemGraphController.java` |
| 编辑器 Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/systemgraph/SystemGraphEditorController.java` |
| Dashboard Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/dashboard/DashboardSystemGraphController.java` |
| 外部接口 Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/outside/SystemGraphExternalController.java` |
| 核心 Service | `common/src/main/java/com/persagy/apccontrol/basic/service/systemgraph` |
| 运行态加工 | `common/src/main/java/com/persagy/apccontrol/basic/service/systemgraph/impl/ConsumptionSystemGraphService.java` |
| 关系图对象解析 | `common/src/main/java/com/persagy/apccontrol/basic/handler/SystemRelationGraphHandler.java` |
| 实体与 VO | `common/src/main/java/com/persagy/apccontrol/basic/model/entity/systemgraph`、`model/vo/systemgraph` |
| Mapper XML | `common/src/main/resources/mapper/SystemGraphMapper.xml`、`SystemGraphDataMapper.xml`、`SystemGraphDetailMapper.xml`、`SystemGraphGenerateParameterMapper.xml` |
| 图例图标 SaaS 管理 | `saas/meos-control-server/src/main/java/com/persagy/apccontrol/controller/graph` |

## 维护原则

- 编辑态以 `SystemGraphDuplicate` 为中心，发布态以 `SystemGraph.systemGraphDuplicateId` 指向当前发布副本。
- 图纸主体 JSON 存在 `SystemGraphData.data` 中，并通过 `CompressUtil` 压缩；不要绕过 `SystemGraphDataService` 直接写压缩字段。
- 运行态查询使用 `ConsumptionSystemGraphService#querySystemGraphById`，它会继续补充图例、信息点值、滤镜、交互和管线样式。
- 只需要解析图内对象集合时，必须使用未加工数据查询链路，避免触发 IOT、知识和信息点补值开销。
- 新增图类型时需要同步检查公共配置、ADM 模板、图例图标、模板状态、发布入口、Dashboard/外部查询和缓存解析。
