# 日志程序设计文档

本文档用于沉淀项目内代码日志的程序设计。重点描述 `LogProvider`、`LogExecutor`、`LogComponent`、日志上下文、异常输出、请求调试和序列化边界。

日志设计的核心原则是：除非 Bean 的普通实体类外，新增代码都必须使用 `LogProvider` 能力；非 Bean 类不要实现 `LogProvider`、`LogComponent` 或 `LogComponentProvider`，因为这类接口会影响对象的序列化和反序列化。非 Bean 类确实需要记录日志时，才使用 `LogExecutor`。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | 日志模块范围、代码入口、类关系、总体链路和当前主干设计。 |
| [LogProvider 设计](./log-provider.md) | `LogProvider` 的输出能力、日志格式、异常处理、上下文写入和组件化使用方式。 |
| [LogExecutor 设计](./log-executor.md) | `LogExecutor` 的工厂职责、实体类兼容场景、历史边界和禁止扩散规则。 |
| [使用边界](./usage-boundary.md) | 不同代码类型选择 `LogProvider` 或 `LogExecutor` 的决策表和序列化边界。 |
| [上下文与观测](./context-and-observability.md) | group/project 上下文、RPC 参数、XXL-JOB 日志、请求调试和日志级别设计。 |
| [扩展与维护指南](./extension-guide.md) | 新增日志、实体类日志、异常日志、历史迁移和验证清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| 日志能力接口 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogProvider.java` |
| 日志执行器工厂 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogExecutor.java` |
| 日志组件接口 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponent.java` |
| 组件日志能力接口 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponentProvider.java` |
| 基础执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/BaseLogExecutor.java` |
| 简单执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogSimpleExecutor.java` |
| 项目执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogProjectExecutor.java` |
| RPC 执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogRpcExecutor.java` |
| 组件简单执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponentSimpleExecutor.java` |
| 组件项目执行器 | `common/src/main/java/com/persagy/apccontrol/basic/executor/log/LogComponentProjectExecutor.java` |
| 请求上下文缓存 | `common/src/main/java/com/persagy/apccontrol/basic/config/cache/LocalContextStorage.java` |

## 维护原则

- 新增业务类、服务类、策略类、任务类、处理器和上下文对象必须通过 `LogComponent` 使用 `LogProvider` 能力。
- 非 Bean 的普通实体类不实现 `LogProvider`、`LogComponent` 或 `LogComponentProvider`。
- 非 Bean 类确实需要日志时，使用 `LogExecutor` 静态工厂创建日志执行器。
- `LogExecutor` 不是新增业务代码的默认日志入口，历史直接调用保留为历史边界，不作为新代码参考。
- 项目相关日志必须使用 `logProjectInstance()`，无法区分项目时才使用 `logInstance()`。
- 一般使用 `logProjectInstance()` 时不指定项目 ID，项目 ID 自动从当前上下文获取。
- 非 Bean 类使用 `LogExecutor` 打印项目日志时，也使用 `LogExecutor.project()`，不指定项目 ID。
- `logProjectInstance(projectId)` 和 `LogExecutor.project(projectId)` 都是历史兼容写法；定时任务、多项目循环等场景应先设置当前上下文，再使用无参日志入口。
- 异常日志要传入原始异常和关键上下文，不使用空 `catch`，不吞异常。
- 不记录 password、token、secret、身份证、银行卡等敏感数据。
