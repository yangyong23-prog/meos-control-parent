# 历史代码边界

运行监测目录下存在一些历史接口和服务，它们曾经承载运行态查询、2D 系统图、控制、手自动、强控、异常事件等能力。当前这些代码不再作为运行组后续迭代入口，后续会根据实际情况迁移、兼容或删除。

运行日志不属于这里的历史边界。运行日志仍由运行组维护，并已拆成独立设计模块，见 [运行日志程序设计文档](../running-log/README.md)。

## 历史代码清单

| 历史代码 | 当前边界 |
| --- | --- |
| `common/.../controller/common/RunningMonitorController.java` | 旧通用 `/running-monitor` 查询入口，不作为新能力扩展入口。 |
| `common/.../service/common/RunningMonitorService.java` | 旧通用运行监测 Service，不作为新策略能力承载点。 |
| `common/.../service/common/impl/RunningMonitorServiceImpl.java` | 旧批量点位、手自动统计、累计运行时间查询实现，不再作为运行组维护核心。 |
| `common/.../controller/perception/StatePerceptionController.java` | 旧状态感知和 2D 系统图运行态入口，不作为运行组新设计入口。 |
| `common/.../abstracts/AbstractRunningMonitorServer.java` | 旧 2D 系统图运行态聚合服务，不作为策略层设计依据。 |
| `local/.../controller/system/RunningMonitorController.java` | 旧边缘运行监测/控制入口，控制相关已迁移到控制组。 |
| `local/.../common/service/RunningMonitorService.java` | 旧边缘运行监测 Service，控制能力不归运行组维护。 |
| `local/.../common/service/impl/RunningMonitorServiceImpl.java` | 旧边缘控制、手自动、强控、累计运行时间等实现，不归运行组维护。 |
| `common/.../controller/AbnormalDataController.java` | 独立异常事件旧入口，不是运行监测策略主链路。 |

## 控制能力边界

以下能力已经迁移到控制组，不应继续在运行监测设计中作为运行组能力描述：

- 参数设定和指令下发。
- 手自动切换和切换原因。
- 干预解除和干预记录维护。
- 联动强控和强控状态。
- 累计运行时间清除。
- SSE/WebSocket 控制状态推送。

如果后续需求涉及这些能力，应进入控制组设计文档或迁移任务，而不是扩展 `RunningMonitorStrategy`。

## 历史代码处理原则

- 只在修复线上兼容问题、迁移调用方或删除旧代码时阅读历史链路。
- 不在历史 Controller 或 Service 上新增运行监测能力。
- 不把历史接口的参数和返回结构作为新策略接口契约。
- 删除历史代码前，应先确认调用方、前端页面、定时任务和外部接口是否已经迁移。
- 如必须临时修改历史代码，应在变更说明中标注兼容性质和后续删除方向。
