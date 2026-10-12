# 上下文与观测

日志上下文由执行器和 `LogProvider` 共同组装。调用方只提供日志详情和必要业务字段，执行器负责补齐 group、project、RPC 或 ProjectDTO 上下文，`LogProvider` 负责输出和观测落点。

## 上下文字段

| 字段 | 来源 | 说明 |
| --- | --- | --- |
| `logTag` | `LogComponent#logTag()` 或调用方传入 | 稳定的类级日志标签。 |
| `logDetailTag` | 调用日志方法时传入 | 描述当前日志事件。 |
| `groupCode` | `BaseLogExecutor`、`ContextUtil` 或显式参数 | 集团上下文。 |
| `projectId` | `BaseLogExecutor`、`ContextUtil` | 项目上下文，普通日志打印从当前上下文获取。 |
| `projectLocalId` | `LogProjectExecutor(ProjectDTO)` | 项目本地 ID。 |
| `projectLocalName` | `LogProjectExecutor(ProjectDTO)` | 项目本地名称。 |
| `param` | `LogRpcExecutor` | RPC 或远程调用入参摘要。 |
| `response` | `LogRpcExecutor` | RPC 或远程调用响应摘要。 |
| additional pairs | 调用方传入 | 当前日志事件的业务字段。 |

普通项目日志不需要调用方指定项目 ID。`LogComponent#logProjectInstance()` 和 `LogExecutor.project()` 都会通过执行器从当前线程上下文读取项目信息；带项目 ID 的重载只作为已有代码和基础设施能力保留，不作为新增日志打印方式。

## 输出格式

普通日志格式：

```text
[logTag] logDetailTag [key: value, key2: value2]
```

业务异常格式：

```text
[logTag] logDetailTag [key: value, errorCode: xxx, errorMessage: xxx]
```

应用异常格式：

```text
[logTag] logDetailTag [key: value, errorCode: xxx, errorMessage: xxx, userTip: xxx]
```

Feign 异常格式：

```text
[logTag] logDetailTag [key: value, errorMessage: xxx]
```

## 观测落点

| 落点 | 条件 | 说明 |
| --- | --- | --- |
| SLF4J Logger | 所有级别 | 最终进入项目日志系统。 |
| `XxlJobHelper` | INFO、WARN、ERROR | 定时任务控制台可查看。 |
| `LocalContextStorage` | 请求调试开启且级别满足条件 | 记录请求级调试日志。 |

`LogComponent` 的 `isTraceEnabled()`、`isDebugEnabled()`、`isWarnEnabled()`、`isErrorEnabled()` 会同时考虑 Logger 当前级别和请求调试开关。对成本较高的日志内容，应先判断级别再组装复杂对象。

## 日志级别

| 级别 | 使用场景 |
| --- | --- |
| TRACE | 高频细节、解析过程、现场排查临时细粒度链路。 |
| DEBUG | 关键中间结果、远程调用响应摘要、策略计算过程。 |
| INFO | 正常业务流程节点、任务开始结束、重要状态切换。 |
| WARN | 可恢复异常、配置缺失、数据为空但不阻断流程。 |
| ERROR | 流程失败、数据不可恢复、需要排查的异常。 |

## 字段记录规则

- 字段名使用英文稳定 key，例如 `projectId`、`objectId`、`infoCode`。
- 字段值只放排查必要摘要，不直接放超大对象。
- 集合字段注意数量，必要时只记录 size 和关键 ID。
- 异常日志传原始异常对象，不只记录 `e.getMessage()`。
- 不记录 password、token、secret、身份证、银行卡等敏感数据。

## 请求调试

`LogProvider#log` 在 `LocalContextStorage.isRequestDebug(level)` 为 true 时，会调用 `LocalContextStorage.putRequestDebug(logTag, logDetailTag, message)` 写入请求调试上下文。

这意味着请求级调试依赖统一日志入口。新增代码如果绕过 `LogProvider` 直接使用 `log.info()`，请求调试链路就无法收集到这部分日志。
