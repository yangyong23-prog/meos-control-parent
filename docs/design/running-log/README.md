# 运行日志程序设计文档

运行日志模块由运行组维护，负责日志查询、分类、设备筛选、导出下载、页面播报、今日汇总和日志展示字段映射。日志数据来源可以是运行组上报，也可以是控制组上报；运行组维护日志消费和展示链路，不接管控制动作本身。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 模块职责、维护边界、核心包结构和总体关系。 |
| [接口与查询流程](./api-and-query-flow.md) | `/runningLog` 入口、旧版日志、新版日志、播报、导出和分类查询流程。 |
| [领域模型](./domain-model.md) | `RcRunningLog`、`RunningLog`、日志类型、分类枚举、查询参数和输出 VO。 |
| [分类与对象查询](./category-and-object-query.md) | 日志分类构建、系统/设备对象查询策略、分类工厂和设备筛选。 |
| [数据来源与上报边界](./data-source-and-reporting.md) | 运行组/控制组上报边界、资源层、Mapper、分区和状态更新。 |
| [扩展与维护指南](./extension-guide.md) | 新增日志分类、字段、查询、导出、播报和上报来源时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| Controller | `common/src/main/java/com/persagy/apccontrol/basic/controller/RcRunningLogController.java` |
| 旧版日志 Service | `common/src/main/java/com/persagy/apccontrol/basic/service/runninglog/RunningLogService.java` |
| 新版日志 Service | `common/src/main/java/com/persagy/apccontrol/basic/service/runninglog/NowRunningLogService.java` |
| 页面播报 Service | `common/src/main/java/com/persagy/apccontrol/basic/service/common/PageBroadcastMsgQueryService.java` |
| 日志分类服务 | `common/src/main/java/com/persagy/apccontrol/basic/service/runninglog/category` |
| 新版日志资源层 | `common/src/main/java/com/persagy/apccontrol/basic/resource/RunningLogServiceResource.java` |
| 旧版日志资源层 | `common/src/main/java/com/persagy/apccontrol/basic/resource/RcRunningLogService.java` |
| 实体 | `common/src/main/java/com/persagy/apccontrol/basic/model/entity/RunningLog.java`、`RcRunningLog.java` |
| Mapper | `common/src/main/java/com/persagy/apccontrol/basic/mapper/RunningLogMapper.java`、`RcRunningLogMapper.java` |
| Mapper XML | `common/src/main/resources/mapper/RunningLogMapper.xml`、`RcRunningLogMapper.xml` |

## 维护原则

- 运行日志是独立设计模块，不放在运行监测策略文档下维护。
- 日志展示、查询、分类、导出和播报归运行组；控制动作和控制结果判定归控制组。
- 日志数据可以由运行组和控制组共同上报，运行组负责承接分类、文案、对象信息和查询筛选。
- 新增日志能力要同时区分旧版 `RcRunningLog` 和新版 `RunningLog` 是否受影响。
- 不要把日志查询或日志写入放进 `RunningMonitorStrategy`。
