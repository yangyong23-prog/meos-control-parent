# 运行监测程序设计文档

运行监测当前维护范围以 `RunningMonitorStrategy` 策略体系为核心，负责不同运行监测类型下的空间、设备、系统和 Dashboard 对象查询，以及策略级异常事件过滤条件。运行日志已拆成独立设计模块，见 [运行日志程序设计文档](../running-log/README.md)。其他以 `/running-monitor` Controller、`AbstractRunningMonitorServer`、边缘控制为主的代码属于历史老代码，不作为后续运行组迭代入口；控制相关能力已经迁移到控制组。

后续新增或调整运行监测能力时，必须围绕策略枚举、策略实现和策略查询参数展开；历史接口只在兼容、迁移或删除时阅读，不应继续扩展。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 当前运行组维护范围、非维护范围、核心包结构和总体关系。 |
| [运行监测策略模型](./strategy-model.md) | `RunningMonitorTypeEnum`、二级类型、策略接口、基础策略和静态策略。 |
| [策略查询契约](./strategy-query-contracts.md) | 空间、设备、系统查询参数，以及 `BaseObjectQueryParam` 的通用过滤和缓存契约。 |
| [数据源与缓存](./strategy-data-and-cache.md) | 策略层使用的 RWD、需求、IOT 规则过滤、MySQL 缓存 key 和字段裁剪规则。 |
| [历史代码边界](./historical-code-boundary.md) | 当前不再作为运行组维护入口的旧 Controller、旧 Service、2D 运行态和控制链路。 |
| [扩展与维护指南](./extension-guide.md) | 新增运行监测类型、二级类型、策略实现和策略查询能力时的检查清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| 运行监测策略 | `common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy` |
| 运行监测策略实现 | `common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl` |
| 运行监测类型枚举 | `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/RunningMonitorTypeEnum.java` |
| 运行监测二级类型枚举 | `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/RunningMonitorSecondTypeEnum.java` |
| 空间查询参数 | `common/src/main/java/com/persagy/apccontrol/basic/model/param/common/RunningMonitorSpaceQueryParam.java` |
| 设备查询参数 | `common/src/main/java/com/persagy/apccontrol/basic/model/param/common/RunningMonitorEquipmentQueryParam.java` |
| 系统查询参数 | `common/src/main/java/com/persagy/apccontrol/basic/model/param/common/RunningMonitorSystemQueryParam.java` |
| 查询参数基类 | `common/src/main/java/com/persagy/apccontrol/basic/model/param/common/BaseObjectQueryParam.java` |

## 维护原则

- 运行组只把 `RunningMonitorStrategy` 体系作为当前核心维护对象。
- 新增运行监测类型差异时，必须通过 `RunningMonitorTypeEnum`、`RunningMonitorSecondTypeEnum` 和策略实现承接。
- 运行日志归运行组维护，但作为独立模块维护，运行监测策略文档只保留跨模块边界说明。
- Controller、旧 Service、2D 系统图运行态、控制、手自动、强控等历史链路不再作为运行组新增能力入口。
- 控制相关功能归控制组维护，运行组文档不描述其程序设计和扩展方式。
- 历史代码如需处理，目标应是兼容、迁移或删除，而不是继续在原链路上扩展。
