# 外部查询与缓存联动

关系图发布后不只服务页面展示，还被 Dashboard、外部接口、运行监控、本地缓存和统计逻辑用来判断系统图是否存在，以及解析图内包含哪些设备、设备组、空间或系统对象。

## Dashboard 系统卡片

入口：`POST /dashboard/systemGraph/listSystemByFirstMenuCode/{menuCode}`

主流程位于 `DashboardSystemGraphServiceImpl#listSystemByFirstMenuCode`：

1. 读取菜单配置 `CommonConfigService#getMenuConfig`。
2. 读取图配置 `CommonConfigService#getGraphConfig`。
3. 根据一级菜单编码找到菜单配置中的图名称。
4. 通过图名称反查图配置，得到基准对象类编码和图类型编码。
5. 查询符合类编码和图类型的 `SystemGraph`。
6. 查询 RWD 对象列表，可按 `QuerySystemCardParam#filterMonitorStatus` 过滤监测状态。
7. 剔除无 RWD 对象或没有 `systemGraphDuplicateId` 的图。
8. 批量解压副本图纸数据。
9. 输出 `SystemCardVO`，填充：
   - `isExistsSystemGraph`
   - `graphTypeCode`
   - `graphObjectIds`

配电菜单 `SE` 当前有特殊处理：会额外加入“低压变配电关系图”。

## 外部系统图列表

入口：`POST /external/systemGraph/querySystemGraphList`

主流程位于 `SystemGraphExternalServiceImpl#querySystemGraphList`：

1. 如果请求类编码包含需要转换为项目级的类编码，则额外加入 `project`。
2. 按当前 `projectId`、`classCode in (...)`、`systemGraphDuplicateId is not null` 查询 `system_graph`。
3. 读取图配置和菜单配置。
4. 为“低压变配电关系图”补充一条菜单配置。
5. 逐条转换为外部 `SystemGraphVO`：
   - `classCode`
   - `graphTypeCode`
   - `systemObjectId`
   - `sysName`
   - `systemCode`
   - `isProject`
   - `sourceClassCode`

项目级图会通过 `ToProjectClassCodeEnum` 将图类型反推源类编码。

## 关系图对象解析

`SystemRelationGraphHandler` 负责从发布图中实时解析对象集合。

主要入口：

| 方法 | 作用 |
| --- | --- |
| `getObjectIdsBySystemGraph(projectId, systemId, systemClassCode, graphTypeCode)` | 返回图内所有对象 ID 集合。 |
| `getObjectIdsBySystemGraphClass(projectId, systemGraphId, systemClassCode)` | 按图 ID 返回 `classCode -> objectIds`。 |
| `getObjectIdsBySystemGraphClass(projectId, systemId, systemClassCode, graphTypeCode)` | 按系统对象和图类型返回 `classCode -> objectIds`。 |

处理步骤：

1. 如当前上下文缺少 `projectId`，先写入 `ContextUtil`。
2. 通过 `ConsumptionSystemGraphService#getNoDataPoBySystemObjectIdAndClassCode` 查找发布图入口。
3. 调用 `ConsumptionSystemGraphService#querySystemGraphDataNoProcess` 读取未加工图纸数据。
4. 根据系统类编码选择解析策略：
   - `ACAT` 使用 `wirteACAT2DSystemCache`。
   - 其他系统使用 `wirteACCC2DSystemCache`。
5. 从设备节点和设备组中提取对象 ID 和类编码。

`ACAT` 末端图的设备节点会从 `dataObject.items` 提取多个对象；冷站、冷热站等图一般从节点 `dataObject.id` 直接提取。

## LocalRedisCacheHandler 联动

`LocalRedisCacheHandler` 将关系图对象解析暴露给其他业务：

| 方法 | 作用 |
| --- | --- |
| `getObjectIdsBySystemGraph` | 委托 `SystemRelationGraphHandler#getObjectIdsBySystemGraph`。 |
| `getObjectIdsBySystemId` | 根据系统图 ID 或系统对象、类编码和图类型解析对象分组。 |

运行监控等逻辑会通过 `LocalRedisCacheHandler` 获取关系图对象集合，例如 `AbstractRunningMonitorServer` 会基于图内设备做运行参数和日志判断。

## 发布状态对外部联动的影响

外部查询和缓存解析都依赖 `SystemGraph.systemGraphDuplicateId`：

- 发布成功后该字段指向当前副本，外部和缓存可以读到图。
- 下架图纸会清空该字段，外部查询将不再把它视为可用图。
- 删除未发布副本不会影响当前发布图，除非删除的是错误状态数据。

维护时不要只改副本状态而忘记同步 `SystemGraph.systemGraphDuplicateId`，否则页面、Dashboard、外部接口和缓存解析会出现状态不一致。
