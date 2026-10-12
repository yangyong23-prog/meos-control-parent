# 数据源与存储

关系图数据由外部 ADM/RWD/知识/IOT 服务、公共配置和本地数据库共同组成。本地数据库保存发布入口、副本、压缩图纸、细节调整、生成参数、异步任务、模板状态、图例和图标。

## 主要数据表

| 表 | 实体 | 作用 |
| --- | --- | --- |
| `system_graph` | `SystemGraph` | 已发布关系图入口，指向当前发布副本。 |
| `system_graph_duplicate` | `SystemGraphDuplicate` | 图纸副本，承载编辑态和发布状态。 |
| `system_graph_data` | `SystemGraphData` | 压缩保存 `SystemGraphInstanceVo` JSON。 |
| `system_graph_detail` | `SystemGraphDetail` | 图纸细节调整。 |
| `system_graph_generate_parameter` | `SystemGraphGenerateParameter` | 管线重算参数。 |
| `system_graph_async_job` | `SystemGraphAsyncJob` | 异步生成任务记录。 |
| `system_graph_template_status` | `SystemGraphTemplateStatus` | 模板状态、名称和别名。 |
| `graph_legend` | `GraphLegend` | 图例资源。 |
| `graph_icon` | `GraphIcon` | 条件图标资源。 |

## Mapper 与资源层

常用 Mapper XML：

| 文件 | 作用 |
| --- | --- |
| `common/src/main/resources/mapper/SystemGraphMapper.xml` | `system_graph` 查询和基础映射。 |
| `common/src/main/resources/mapper/SystemGraphDataMapper.xml` | `system_graph_data` 数据访问。 |
| `common/src/main/resources/mapper/SystemGraphDetailMapper.xml` | `system_graph_detail` 数据访问。 |
| `common/src/main/resources/mapper/SystemGraphGenerateParameterMapper.xml` | `system_graph_generate_parameter` 数据访问。 |
| `common/src/main/resources/mapper/RcObjectRelationCfgMapper.xml` | 部分关系和对象配置查询。 |

服务层大多基于 MyBatis Plus `ServiceImpl`，编辑态逻辑集中在 `SystemGraphEditorServiceImpl`、`SystemGraphDuplicateServiceImpl`、`SystemGraphDataServiceImpl`。

## 压缩 JSON 存储

`SystemGraphData.data` 保存的是 `SystemGraphInstanceVo` 序列化后的压缩字符串。

保存入口：

```java
data.setData(CompressUtil.compress(JSON.toJSONString(vo)));
```

读取入口：

```java
JSON.parseObject(CompressUtil.decompress(systemGraphData.getData()), SystemGraphInstanceVo.class);
```

压缩 JSON 中包含图纸主体结构，如设备节点、小附件、设备组、管线、区域、文本框、自定义内容等。细节调整不作为主体结构的一部分持久化，而是单独保存在 `system_graph_detail`。

## 状态存储

副本和图纸数据都有 `EnumSystemGraphStatus`：

| 状态 | 说明 |
| --- | --- |
| `UN_PUBLISHED` | 未发布，可编辑。 |
| `PUBLISHED` | 已发布，运行态可消费。 |
| `OFF_LINE` | 已下架，不再作为当前图消费。 |

发布时会同步更新副本和图纸数据状态，并更新或创建 `system_graph`。下架时会把副本置为 `OFF_LINE` 并清空 `system_graph.systemGraphDuplicateId`。

## 外部数据源

| 外部依赖 | 用途 |
| --- | --- |
| `DtpRelationalGraphClientService` | 生成 ADM 关系图实例数据；新增 ADM Feign 能力也应补到该类或对应 `*ClientService`。 |
| `DtpRelationalGraphClient` | 历史直接调用，用于同步图例、附件类型、设备类树；新增或改造时必须收敛到 `DtpRelationalGraphClientService`。 |
| `DtpRwdClientService` | 查询对象、对象名称、类编码、关系、静态属性。 |
| `DtpKnowledgeClientService` | 查询类编码名称和信息点定义。 |
| `AbstractIotDataCacheServer` | 查询近一小时信息点值。 |
| `DmpFileServiceClient` | 历史直接调用，用于将文件 ID 转换为可访问下载 URL；新增或改造时必须补充对应 `*ClientService` 防腐层。 |
| `CommonConfigService` | 提供图配置、菜单、管线、滤镜、交互、运行参数等。 |
| `LocalRedisCacheHandler` | 提供信息点配置缓存、对象解析和其他本地缓存能力。 |

## Flyway 脚本

关系图表和初始化数据分布在 SaaS 与 local 的 Flyway 脚本中，常见路径包括：

- `saas/meos-control-server/src/main/resources/db/flyway/meos_control`
- `saas/meos-control-server/src/main/resources/db/flyway/meos_control_common`
- `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control`
- `local/meos-control-server-edge/src/main/resources/db/flyway/meos_control_common`

历史脚本中包含：

- `V5.240602.1__meos_control.sql`：图例、图标基础表和初始化数据。
- `V6.0.0.37__meos_control.sql`、`V6.0.0.42__meos_control.sql`：关系图相关表升级。
- `V7.250202.0.1__system_graph.sql`、`V7.250802.0.0__system_graph.sql`：关系图后续升级。

维护数据库结构时需要同时检查 SaaS 和 local 脚本，避免云边表结构不一致。

## 数据一致性要求

- `SystemGraph.systemGraphDuplicateId` 必须指向存在的 `SystemGraphDuplicate`。
- `SystemGraphDuplicate.systemGraphDataId` 必须指向存在的 `SystemGraphData`。
- 发布入口和副本状态要同步更新。
- 删除副本时要处理关联的图纸数据和细节数据。
- 图例图标同步后，生成链路依赖的 `legendId`、`classCode`、`legendCode` 不能丢失。
