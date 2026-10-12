# 信息点配置程序设计文档

信息点配置模块负责把“模板类型 + RWD 对象 + 信息点配置 + 分组配置”组装成前端可展示、可取值的对象分组结构。模块入口位于 `common/src/main/java/com/persagy/apccontrol/basic/controller/infocodeconfig`，核心编排位于 `service/infocodeconfig` 与 `structure/infocodeconfig`。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 模块职责、边界、核心包结构和总体调用链。 |
| [接口与查询流程](./api-and-query-flow.md) | `/infoCodeConfig` 接口、Service 编排、按模板/对象查询流程。 |
| [聚合模型](./aggregate-model.md) | `InfoCodeAggregate`、模板类型、配置、分组和标识模型。 |
| [网关与生命周期](./gateway-and-lifecycle.md) | `InfoCodeGateway`、`InfoCodeAggregateInitContext`、生命周期扩展点。 |
| [解析策略与取值](./analyze-strategy.md) | `InfoCodeTypeEnum`、`InfoCodeAnalyzeStrategy`、定义和值填充策略。 |
| [数据源与缓存](./data-source-and-cache.md) | 配置表资源、RWD 对象、IOT 数据、知识/风险事件以及缓存键。 |
| [扩展与维护指南](./extension-guide.md) | 新增信息点类型、生命周期处理器、模板/分组配置时的落点和校验清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/infocodeconfig/InfoCodeConfigController.java` |
| Service | `common/src/main/java/com/persagy/apccontrol/basic/service/infocodeconfig/InfoCodeConfigService.java` |
| Service 实现 | `common/src/main/java/com/persagy/apccontrol/basic/service/infocodeconfig/impl/InfoCodeConfigServiceImpl.java` |
| 查询上下文 | `common/src/main/java/com/persagy/apccontrol/basic/model/context/infocodeconfig/InfoCodeConfigQueryContext.java` |
| 领域网关 | `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/gateway/InfoCodeGateway.java` |
| 聚合模型 | `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeAggregate.java` |
| 解析策略 | `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/strategy/InfoCodeAnalyzeStrategy.java` |

## 维护原则

- 查询入口可以不同，但聚合根创建、定义填充和值填充必须复用 `InfoCodeGateway`；历史接口差异需要在网关外层适配。
- 请求参数新增字段时，需要检查 Controller、Service、Converter、DAO 参数和旧版接口是否同时受影响。
- 信息点值填充不要绕过 `InfoCodeTypeEnum` 与 `InfoCodeAnalyzeStrategy`，否则容易破坏不同类型的信息点扩展能力。
- 涉及 RWD source、placeholder、关系方向或对象绑定逻辑时，需要同步检查取值表达式和关系绑定规则。
