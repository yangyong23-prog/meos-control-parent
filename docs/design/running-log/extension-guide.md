# 扩展与维护指南

运行日志扩展前，先判断需求影响旧版日志、新版日志，还是两者都影响。旧版围绕 `RcRunningLog`，新版围绕 `RunningLog`。

## 新增日志分类

检查清单：

- 是否影响旧版 `RunningLogCategoryEnum`。
- 是否影响新版 `NowRunningLogCategory`。
- 是否需要按 classCode 映射二级分类。
- 是否需要 `codeRule` 规则计算二级分类。
- 是否需要新增 `RunningLogCategoryQueryObjStrategy` 实现。
- 是否影响设备列表筛选。
- 是否影响前端分类树和二级分类查询。

## 新增日志字段

检查清单：

- 字段属于 `RcRunningLog`、`RunningLog`，还是两者都需要。
- 查询参数是否需要新增筛选字段。
- Mapper XML 是否需要查询、过滤或排序该字段。
- `RunningLogDto`、`RunningLogResDto`、`RunningLogVO`、`RunningLogDownloadVO` 是否需要返回该字段。
- 旧版导出是否需要新增 `@Excel` 注解。
- 新版下载是否需要更新下载 VO。
- 页面播报和今日汇总是否需要展示该字段。

## 新增查询能力

检查清单：

- 接口是否放在 `RcRunningLogController`。
- 旧版查询走 `RunningLogService`，新版查询走 `NowRunningLogService`。
- 是否需要新增资源层方法或 Mapper XML。
- 是否影响分页总数、导出下载和对外接口。
- 是否需要同步设备列表或分类过滤。

## 新增上报来源

检查清单：

- 上报方是运行组、控制组，还是外部系统。
- 是否能提供项目、对象、系统、类别、日志类型、分类、时间和内容。
- 控制组上报时，控制动作和结果判定仍由控制组负责。
- 是否需要通过 `RunningLogAddService#saveByMeosMsg` 承接内部日志。
- 是否需要状态未知后续更新，若需要必须提供 `traceId`、`objectId`、`infoCode`。

## 新增导出或播报字段

检查清单：

- 旧版导出使用 `RcRunningLog` 和 EasyPOI 注解。
- 新版下载使用 `RunningLogDownloadVO` 和 EasyPOI。
- 页面播报使用 `NowRunningLogService#listPageBroadcastMsg` 和 `RunningLogVO` 映射。
- 今日汇总依赖 `countWithTypeByToday` 和最新日志查询。

## 跨模块边界

- 运行监测策略不查询、不写入运行日志。
- 控制组可以上报控制相关日志，但控制动作本身不归运行日志模块维护。
- 运行日志模块负责把不同来源的日志数据转换为统一可查询、可展示、可导出的结果。
