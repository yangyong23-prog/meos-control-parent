# 扩展与维护指南

关系图扩展通常同时影响配置、模板、生成、编辑、发布和运行态消费。新增图类型或节点能力时，应按跨层链路检查，而不是只改某个 Controller 或某张表。

## 新增图类型

新增关系图类型时至少检查：

1. ADM 是否存在对应模板，且能按 `referenceObjectId` 和 `graphTypeCode` 生成 `GraphGenerateInstanceDto`。
2. `GRAPH_CONFIG` 是否配置：
   - 图名称 `mapTitle`
   - 基准对象类编码 `diagramBaseObjectCode`
   - 图类型 `diagramTypeOfTheDiagram`
   - 是否不可编辑 `notEdit`
   - 展示方式 `displayMode`
3. 菜单配置是否需要关联该图，Dashboard 是否要展示。
4. 图例 `graph_legend` 是否有对应类编码和 `legendId`。
5. 条件图标是否需要在 `DIAGRAM_ICON_CHANGE_CONFIG` 中补充。
6. `GRAPH_INFORMATION` 是否需要配置固定标签或面板信息点。
7. `SYSTEM_GRAPH_FILTER_STATUS` 是否需要配置滤镜状态。
8. `INTERACTIVE_CONFIG` 是否需要配置节点跳转。
9. 外部接口是否需要把新图纳入 `SystemGraphExternalServiceImpl` 的类编码转换。
10. 缓存解析是否需要在 `SystemRelationGraphHandler` 中新增对象提取规则。

## 新增模板或模板更新状态

模板状态由 `SystemGraphTemplateStatus` 管理。维护模板时注意：

- `templateId` 对应 ADM 模板原始 ID。
- `graphTypeCode` 是本系统按图类型查询模板状态的主键条件之一。
- `sequenceNumberCurrent` 用于判断模板序号是否变化。
- `updatedStatus=true` 表示模板有更新，需要重新生成或提示更新。
- 发布图名称来自 `getTemplateName`，有别名时使用 `aliasName`。

模板重命名入口是 `/systemGraphEditor/updateTemplateName`，它更新的是别名而不是模板原始名称。

## 新增节点字段或编辑类型

如果需要让前端编辑新的图纸字段：

1. 检查字段属于设备节点、小附件、设备组还是自定义内容。
2. 如果属于现有类型，可通过 `UpdateGraphDTO.code` 写入字段。
3. 如果需要新增集合类型，需要扩展 `EnumUpdateGraphType` 和 `SystemGraphDataServiceImpl#editSystemGraph`。
4. 检查 `packNode` 是否应跳过该字段更新。
5. 检查压缩 JSON 反序列化后的 VO 是否能承载该字段。
6. 检查运行态加工是否会覆盖前端编辑字段。
7. 检查副本复制、重算管线和发布是否要保留该字段。

不要直接在 Controller 中拼 JSON 更新，应通过 `SystemGraphDataService` 的压缩数据读写入口统一处理。

## 新增固定标签、面板或抽屉信息点

关系图信息点展示涉及多处：

- 固定标签常量：`SystemGraphService.fixedLabelInfoCodeList`、`FIXED_PASSBY_INFOCODE_LIST`、`FIXED_SWITCH_INFOCODE_LIST` 等。
- 设备组标签：`SystemGraphEditorService.EQUIP_GROUP_RUN_NUM_LABEL`、`EQUIP_GROUP_ABNORMAL_EVENT_NUM_LABEL`。
- 公共配置：`GRAPH_INFORMATION`。
- 绑点规则：`LocalRedisCacheHandler#mapListInfoCodeConfigs` 和对象规则查询。
- 知识定义：`DtpKnowledgeClientService#mapClassCodeInfoCodeAndDefine`。
- IOT 值：`AbstractIotDataCacheServer#listInfoCodeValueInOneHour`。

新增信息点时要确认它是静态属性还是动态 IOT 数据。静态属性会从 RWD 对象 `undefinedProperties` 取值，动态数据从 IOT 缓存取值。

## 新增图例或条件图标

新增图例时：

- 优先从 ADM 资源库同步，保持 `legendId` 与模板节点一致。
- 检查 `classCode`、`objType`、`attachType`、`texts`、`size`、`anchors` 是否完整。
- 如果图片是文件 ID，需要通过 `initPicUrl` 初始化可访问 URL。

新增条件图标时：

- 在图标更换配置中补充类编码、图例编码、图标名称和条件信息。
- 执行 `GraphIconService#syncGraphIcon` 后检查 `graph_icon.condition`。
- 如果需要保留本地上传图片，确认同步逻辑不会覆盖已有 `fileUrl`。

## 调整发布或下架流程

发布流程的关键一致性点：

- 同组其他已发布副本必须退回 `UN_PUBLISHED`。
- 当前副本和当前图纸数据都要置为 `PUBLISHED`。
- `system_graph` 不存在时新增，存在时更新 `systemGraphDuplicateId`。
- 图名称要通过模板状态取值。

下架流程的关键一致性点：

- 只有已发布副本能下架。
- 下架副本后必须清空 `system_graph.systemGraphDuplicateId`。
- 外部接口、Dashboard 和缓存解析都以该字段判断图是否可用。

## 调整缓存解析

缓存解析使用未加工图纸数据，不应依赖运行态补值字段。

新增解析规则时检查：

- 系统类编码是否走 `ACAT` 末端逻辑。
- 节点对象 ID 是在 `dataObject.id` 还是 `dataObject.items`。
- 是否需要解析 `groupVos`。
- 是否需要把结果按 `classCode -> objectIds` 输出。
- 调用方是否只需要对象集合，避免误用运行态查询造成额外 IOT 和知识服务开销。

## 常见风险清单

- 图配置里图名称和菜单配置名称不一致，导致 Dashboard 或外部查询找不到图。
- 发布副本状态已改，但 `SystemGraph.systemGraphDuplicateId` 未更新。
- 重算管线删除旧 `SystemGraphData` 后，细节调整仍指向旧语义，需要确认前端是否保留。
- 图例同步清空本地表后，本地手工图标或图片被覆盖。
- 新增信息点只改了固定标签，没有配置知识定义或绑点规则，运行态会被过滤。
- 外部接口只按类编码查询，项目级图需要额外处理 `project` 和 `ToProjectClassCodeEnum`。
- `GraphIconController` 当前存在重复的 `GET /query/{...}` 路径，扩展时需要先处理路由语义。
