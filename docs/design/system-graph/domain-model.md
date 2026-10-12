# 领域模型

关系图领域模型分为发布入口、副本、图纸数据、细节调整、生成参数、异步任务、模板状态、图例图标和图纸 VO。发布入口用于运行态定位当前图，副本和图纸数据用于编辑态保存内容。

## 发布入口：SystemGraph

`SystemGraph` 映射 `system_graph` 表，表示某个项目下某个系统对象、某个图类型当前可消费的图。

| 字段 | 说明 |
| --- | --- |
| `classCode` | 系统对象或项目级对象类编码。 |
| `systemObjectId` | 系统实例 ID。 |
| `graphTypeCode` | 图类型编码，如供电关系图、源端供水关系图等。 |
| `systemGraphDuplicateId` | 当前发布副本 ID，运行态查询的核心指针。 |
| `graphName` | 图名称，发布时来自模板状态名称或别名。 |
| `projectId` | 项目 ID。 |
| `status` | 成功/失败标识。 |
| `passBy` | 是否存在小附件。 |

`SystemGraph` 不直接保存图纸 JSON，它只指向当前发布的 `SystemGraphDuplicate`。

## 副本：SystemGraphDuplicate

`SystemGraphDuplicate` 映射 `system_graph_duplicate` 表，是编辑器的核心实体。

| 字段 | 说明 |
| --- | --- |
| `classCode` | 基准对象类编码。 |
| `templateId` | 模板 ID，当前部分逻辑已注释，更多依赖 `graphTypeCode` 和模板状态。 |
| `systemObjectId` | 系统实例 ID。 |
| `graphTypeCode` | 图类型编码。 |
| `systemGraphDataId` | 关联的图纸数据 ID。 |
| `name` | 副本名称，原图/副本默认来自 `EnumGraphType#getReasonPhrase`。 |
| `isNotEdit` | 是否不可编辑，来自图配置 `notEdit`。 |
| `projectId` | 项目 ID。 |
| `generateId` | ADM 生成 ID，用于重算缓存读取判断。 |
| `status` | `UN_PUBLISHED`、`PUBLISHED`、`OFF_LINE`。 |
| `order` | 副本顺序，原图为 0，副本递增。 |
| `graphType` | `ORIGINAL` 或 `COPY`。 |

同一个 `systemObjectId + graphTypeCode + classCode` 下发布时只允许一个副本处于 `PUBLISHED`。

## 图纸数据：SystemGraphData

`SystemGraphData` 映射 `system_graph_data` 表。

| 字段 | 说明 |
| --- | --- |
| `requestParam` | 访问参数，当前保存图纸时多传 `null`。 |
| `data` | 压缩后的 `SystemGraphInstanceVo` JSON。 |
| `projectId` | 项目 ID。 |
| `status` | 与副本联动的发布状态。 |

保存和读取必须经过 `CompressUtil.compress`、`CompressUtil.decompress`。`SystemGraphDataService#saveSystemGraphData` 和 `SystemGraphEditorService#getSystemGraphDataNoDetailDataById` 是主要入口。

## 细节调整：SystemGraphDetail

`SystemGraphDetail` 映射 `system_graph_detail` 表，用于保存前端针对某个副本的局部调整。

| 字段 | 说明 |
| --- | --- |
| `projectId` | 项目 ID。 |
| `code` | 细节编码。 |
| `systemGraphDuplicateId` | 所属副本 ID。 |
| `type` | 细节类型，注释约定为 1 设备、2 信息点、3 标签、4 分组。 |
| `data` | 细节调整 JSON，如信息点列表、名称、位置等。 |

编辑器读取图纸时，如果 `isDetailData=true`，会把同副本的细节调整查询出来，并把 `data` 解析成 JSON 对象后放入 `SystemGraphInstanceVo.systemGraphDetailVOs`。

## 生成参数、异步任务和模板状态

| 模型 | 表 | 作用 |
| --- | --- | --- |
| `SystemGraphGenerateParameter` | `system_graph_generate_parameter` | 保存重算管线时的 `RequestSystemGraphDTO`，支持多次合并排序、文本尺寸、设备组详情等参数。 |
| `SystemGraphAsyncJob` | `system_graph_async_job` | 记录关系图异步任务的项目、系统对象、图类型、模板、请求参数、任务 ID、任务状态和消息。 |
| `SystemGraphTemplateStatus` | `system_graph_template_status` | 记录某对象某图类型的模板 ID、当前序号、名称、别名和是否更新。 |

`SystemGraphTemplateStatusService#getTemplateName` 在发布时决定写入 `SystemGraph.graphName` 的名称：有 `aliasName` 优先使用别名，否则使用原名称。

## 图例和图标

| 模型 | 表 | 作用 |
| --- | --- | --- |
| `GraphLegend` | `graph_legend` | 设备类图例，包含图例 ID、类编码、图片、文本框、尺寸、锚点、小附件标识、版本和状态。 |
| `GraphIcon` | `graph_icon` | 条件图标，包含图例 ID、类编码、图标编码、条件、图片和状态。 |

图例用于给图纸节点补充基础图片、尺寸和锚点。图标用于同一图例在不同信息点条件下切换图片。

## 图纸 VO

`SystemGraphInstanceVo` 继承 ADM 生成 DTO `GraphGenerateInstanceDto`，并扩展本系统运行态字段。

| 字段 | 说明 |
| --- | --- |
| `equipmentNodeVos` | 设备节点。 |
| `passByVos` | 小附件节点。 |
| `groupVos` | 设备组或区域组。 |
| `lineNodeVos` | 管道节点信息。 |
| `lineVos` | 管道信息，运行态匹配 `pipelineStyleCode`。 |
| `mainPipeVos` | 主管道、母线、立管、环管等。 |
| `areaVos` | 区域，如楼层、竖井。 |
| `textMap` | 图纸文本框。 |
| `systemGraphDetailVOs` | 编辑器细节调整列表。 |
| `isUpdateDetail` | 图纸主体是否发生细节更新。 |
| `displayMode` | 展示适配方式。 |
| `drawerAllInfoCodes` | 抽屉信息点列表。 |
| `legendIcons` | ADM 图例图标信息列表。 |
| `customizeContent` | 前端自定义内容，后端不消费。 |

`EquipmentNodeVo` 和 `GraphGroupVo` 会进一步承载 `legendUrlVo`、`fixedInfoCodes`、`filterStatusVO`、`interactiveConfigVo`、`drawerAllInfoCodes`、`webCustomData` 等运行态字段。
