# 图例图标与公共配置

关系图图形展示依赖两类配置：图例图标资源和公共业务配置。图例图标决定节点如何画，公共配置决定图类型、菜单、信息点、滤镜、交互和运行参数如何生效。

## 图例服务

`GraphLegendServiceImpl` 管理 `graph_legend` 表。

| 方法 | 作用 |
| --- | --- |
| `queryAllGraphLegend` | 查询本地图例并转换为 `GraphLegendVo`。 |
| `getGraphLegendMap` | 返回 `legendId -> GraphLegendVo`，供生成图纸时快速匹配。 |
| `syncGraphLegend` | 从 ADM 关系图服务同步图例，并刷新本地图例表。 |
| `queryLegendList` | 查询某类编码下图例，并补充图标列表。 |
| `queryLegendListGuest` | 对外查询图例，并把文件 ID 转成可下载 URL。 |
| `updateLegend` | 更新图例图片、锚点和关联图标。 |
| `typeTree` | 查询 ADM 设备类树，并标识本地需要更新图例的类。 |
| `initPicUrl` | 根据文件 ID 初始化 `picUrl`。 |

图例同步当前历史实现中直接调用 ADM Feign Client：

- `DtpRelationalGraphClient#graphLegendSelect`
- `DtpRelationalGraphClient#attachTypeList`
- `DtpRelationalGraphClient#graphCommonTree`

图例文件 URL 解析依赖 `DmpFileServiceClient#querySimpleFiles`。

这些直接调用 Client 的写法属于历史现状。后续新增或改造图例、附件类型、设备类树、文件 URL 等外部调用时，必须先补充对应 `*ClientService` 方法，并确保 `*ClientService` 实现 `BaseFeignClientService`，业务侧不直接调用 Feign Client。

## 图标服务

`GraphIconServiceImpl` 管理 `graph_icon` 表。

| 方法 | 作用 |
| --- | --- |
| `queryAllGraphIcon` | 查询全部图标。 |
| `getGraphIconMap` | 返回 `legendId -> List<GraphIconVo>`，供生成图纸时匹配。 |
| `syncGraphIcon` | 根据图标更换配置和图例生成条件图标。 |
| `queryLegendList` | 按类编码查询图标。 |
| `updateLegend` | 更新某个条件图标文件。 |
| `getLegendByLegendId` | 按图例 ID 查询图标。 |
| `delete` | 按删除参数删除图标。 |
| `initPicUrl` | 根据文件 ID 初始化 `picUrl`。 |

`syncGraphIcon` 的输入来自 `CommonConfigService#getDiagramIconChangeConfig`。它会按 `classCode` 找到对应图例，再按配置中的 `legendCode`、`legendName`、`infos` 生成图标记录。若本地已有同一 `legendId + classCode + legendCode` 的图标且有 `fileUrl`，会保留本地文件；否则沿用图例图片。

## 图例图标在生成链路中的使用

`SystemGraphServiceImpl#initSystemGraphData` 会在生成开始时加载：

- `GraphLegendService#getGraphLegendMap`
- `GraphIconService#getGraphIconMap`

生成后的设备组、设备节点和小附件会通过 ADM 返回的 `legendOriId` 匹配本地图例。匹配到图例后会写入 `legendUrlVo`；匹配到 ADM 生成的动态图标信息后，会写入 `legendNode.legendIcon`。

设备节点还会调用 `buildIconUrlVos` 把同一图例下的条件图标放入 `GraphLegendVo.iconVoList`，供前端按条件切换。

## 公共配置

关系图大量行为来自 `CommonConfigService`。

| 配置入口 | 作用 |
| --- | --- |
| `getGraphConfig` | 图类型和图名称映射、基准对象类、是否不可编辑、展示方式等。 |
| `getGraphInformation` / `getGraphInformationGroupByClassCode` | 固定标签和图上信息点展示配置。 |
| `getDiagramIconChangeConfig` | 条件图标同步配置。 |
| `getInteractiveConfig` | 节点跳转和交互配置。 |
| `pipeLineStyleConfig` | 管线样式匹配配置。 |
| `areaStyleConfigVos` | 区域样式配置。 |
| `multipleConfig` | 管道和文字尺寸倍数配置。 |
| `getModelDiagramPrimitives` | 模型图图元配置。 |
| `getSystemRunParam` | 系统运行参数配置。 |
| `getExceptionEventConfig` | 异常事件配置。 |
| `getOrderFailAlarm` | 控反不一致配置。 |
| `getSystemGraphFilterStatus` | 关系图滤镜状态配置。 |
| `getMenuConfig` | Dashboard、菜单和外部查询使用的菜单映射。 |

常见初始化 SQL 位于 `R__init_common_config.sql` 及各版本 Flyway 脚本中，配置键包括 `GRAPH_CONFIG`、`EXCEPTION_EVENT_CONFIG`、`SYSTEM_GRAPH_FILTER_STATUS` 等。

## 维护注意点

- 图例 `legendId` 是 ADM 资源和本地图纸节点之间的连接点，不要随意改成本地主键。
- `fileUrl` 可能是文件 ID，也可能已经是 URL；`picUrl` 是给前端使用的可访问地址。
- 图标条件 `condition` 是 JSON 字符串，来源于图标更换配置中的 `infos`。
- 更新图例图片时，如果同时带有多个 `iconVoList`，会同步更新对应图标。
- 图例、图标同步会清空并重建本地表，执行前需要确认是否允许覆盖本地调整。
