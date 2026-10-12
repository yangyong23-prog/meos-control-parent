# 领域模型

运行日志当前保留旧版 `RcRunningLog` 和新版 `RunningLog` 两套实体。维护时必须先判断需求影响哪套模型，再决定同步范围。

## 旧版实体

`RcRunningLog` 对应表 `rc_running_log`，继承 `SynDTO`，主要字段包括：

| 字段 | 说明 |
| --- | --- |
| `projectId`、`systemId`、`objectId` | 项目、系统和对象标识。 |
| `objectName`、`classCode`、`objectCategory` | 对象展示和分类信息。 |
| `firstLevelCategory`、`secondLevelCategory`、`thirdLevelCategory` | 旧版三级日志分类，类型为 `RunningLogCategoryEnum`。 |
| `type` | 日志标识，旧注释中表示自动/人工。 |
| `keyWord`、`content`、`detail` | 日志关键字、内容和操作详情。 |
| `effectStartTime`、`effectEndTime` | 生效时间范围。 |
| `reason`、`createUser`、`createTime` | 操作原因、操作人和创建时间。 |

旧版实体带有 EasyPOI `@Excel` 注解，直接影响旧版导出字段。`getPageBroadcastContent` 会拼接页面播报内容。

## 新版实体

`RunningLog` 对应表 `running_log`，主要字段包括：

| 字段 | 说明 |
| --- | --- |
| `projectId` | 项目 ID。 |
| `type` | 日志类型，类型为 `RunningLogTypeEnum`。 |
| `objectId`、`objectName`、`classCode` | 操作对象信息。 |
| `systemId`、`systemName`、`systemClassCode` | 所属系统信息。 |
| `state` | 来源状态，类型为 `RunningLogSourceStateEnum`。 |
| `keyWord`、`content` | 操作关键字和内容 JSON。 |
| `dataTime`、`createTime` | 数据时间和创建时间。 |
| `firstLevelCategory`、`secondLevelCategory` | 新版一级分类和二级分类。 |
| `effectStartTime`、`effectEndTime` | 生效时间范围。 |
| `traceId`、`infoCode`、`infoValue` | 链路追踪、信息点和值。 |
| `sourceType` | 来源数据类型，类型为 `RunningLogSourceTypeEnum`。 |
| `knowledgeInfoCode` | 展示时补充的信息点定义，不入库。 |

`RunningLog#buildSecondLevelCategory` 会根据 `firstLevelCategory` 的规则或系统类别映射构建二级分类。

## 类型和分类枚举

| 枚举 | 用途 |
| --- | --- |
| `RunningLogTypeEnum` | 日志类型，如自动、人工、报警等。 |
| `RunningLogCategoryEnum` | 旧版日志分类。 |
| `NowRunningLogCategory` | 新版日志一级/二级分类，并支持按系统类别映射。 |
| `RunningLogSourceStateEnum` | 来源状态，如未知、成功、失败、超时等。 |
| `RunningLogSourceTypeEnum` | 来源数据类型。 |
| `RunningLogObjectCategpryEnum` | 旧版对象分类。 |
| `RunningLogInfoCodeDataSourceEnum` | 信息点数据来源。 |
| `RunningLogKeyWordEnum`、`RunningLogTemplateEnum` | 关键字和模板配置。 |

## 查询参数

| 参数 | 用途 |
| --- | --- |
| `RunningLogQueryParam` | 旧版按日期、分类、类型等查询。 |
| `RunningLogPageQueryParam` | 旧版分页和对外查询。 |
| `NowRunningLogQueryParam` | 新版分页、下载和分类筛选。 |
| `QueryLogCategoryParam` | 新版二级分类查询。 |
| `RunningLogEquipmentListQueryParam` | 设备列表筛选。 |
| `NowPageBroadcastQueryParam` | 页面播报查询。 |
| `RunningLogInsertParam` | 旧版日志插入参数。 |

## 输出对象

| 输出对象 | 用途 |
| --- | --- |
| `RunningLogDto` | 旧版日志列表 DTO。 |
| `RunningLogResDto` | 对外接口日志返回 DTO。 |
| `RunningLogEquipmentDto` | 设备列表 DTO。 |
| `RunningLogCategoryDto` | 日志分类树 DTO。 |
| `RunningLogVO` | 新版日志展示 VO。 |
| `RunningLogDownloadVO` | 新版下载 VO。 |
| `RunningLogHomePageVO` | 首页最新日志摘要。 |
| `TodayLogSummaryVo` | 今日日志汇总。 |

## 维护要点

- 旧版导出字段直接依赖 `RcRunningLog` 上的 `@Excel` 注解。
- 新版展示字段依赖 `NowRunningLogServiceImpl#mappingRunningLogVO`。
- 新增分类时要同时确认旧版 `RunningLogCategoryEnum` 和新版 `NowRunningLogCategory` 是否都需要调整。
- 新增字段时要检查实体、查询参数、Mapper XML、VO、导出和播报。
