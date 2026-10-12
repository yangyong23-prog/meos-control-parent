# LogProvider 设计

`LogProvider` 是统一日志能力接口。它负责把日志级别、日志源、日志标签、日志详情、异常和附加字段组装成统一格式，再输出到 SLF4J、XXL-JOB 和请求调试上下文。

## 职责

| 职责 | 说明 |
| --- | --- |
| 统一级别方法 | 提供 `trace`、`debug`、`info`、`warn`、`error` 和通用 `log` 方法。 |
| 统一日志格式 | 输出格式为 `[logTag] logDetailTag [key: value, ...]`。 |
| 附加字段组装 | 支持 `Pair<String, Object>`、可变参数和 `List<Pair<String, Object>>`。 |
| 异常特化 | 对业务异常、应用异常和 Feign 异常使用可读字段输出。 |
| 多目标输出 | 写 SLF4J，INFO 及以上同步写 XXL-JOB，请求调试时写入本地上下文。 |

## 组件化使用

业务类通常不直接实现 `LogProvider`，而是实现 `LogComponent`。`LogComponent` 主要通过 `logProjectInstance()` 和 `logInstance()` 返回 `LogComponentProvider`，最终使用的仍是 `LogProvider` 能力。`logProjectInstance(projectId)` 属于历史兼容写法，新增日志打印不使用。

```java
public class InfoCodeImportServiceImpl implements LogComponent {

    @Override
    public String logTag() {
        return "InfoCodeImportService";
    }

    public void importFile(String fileName) {
        logProjectInstance().info("开始导入信息点配置", Pair.of("fileName", fileName));
    }
}
```

组件化调用有三个好处：

- `Logger` 由 `LogComponent#log()` 根据当前类生成。
- `logTag()` 由类自己定义，日志标签稳定。
- 项目上下文由 `logProjectInstance()` 自动绑定，调用处不重复传 `LOGGER`、`LOG_TAG` 和项目 ID。

## 方法选择

| 方法 | 使用场景 |
| --- | --- |
| `logProjectInstance()` | 项目内业务日志的规范入口，项目 ID 自动从当前上下文获取。 |
| `logProjectInstance(projectId)` | 历史兼容写法，新增日志打印不使用；定时任务、多项目循环等场景应先设置当前上下文。 |
| `logInstance()` | 不区分项目，或日志只与集团、系统任务、框架行为相关时使用。 |
| `globalStopWatch()` | 需要按 `logTag()` 记录请求级耗时片段时使用。 |

## 异常处理

`LogProvider#log` 对不同异常类型做了特化：

| 异常类型 | 输出策略 |
| --- | --- |
| `BusinessException` | 输出 `errorCode` 和 `errorMessage`，不重复输出堆栈。 |
| `ApplicationException` | 输出 `errorCode`、`errorMessage` 和 `userTip`，不重复输出堆栈。 |
| `FeignException` | 输出 Feign 异常消息，不重复输出堆栈。 |
| 其他异常 | 输出统一日志消息，并保留原始异常堆栈。 |

异常日志应保留原始异常对象，并补充关键上下文：

```java
try {
    rebuildCurrentProjectCache();
} catch (Exception e) {
    logProjectInstance().error("项目缓存重建失败", e);
    throw e;
}
```

## 附加字段

附加字段统一使用 `Pair<String, Object>` 表达，字段名应稳定、可搜索、不过度冗长。

规范写法：

```java
logProjectInstance().warn("未查询到实例关联空间", Arrays.asList(
        Pair.of("instanceId", instanceId),
        Pair.of("relationType", relationType)
));
```

避免：

```java
logProjectInstance().warn("未查询到实例关联空间: " + instanceId + "," + relationType);
```

拼接字符串会降低检索和排查效率，也容易把空值、集合和敏感字段混在不可控文本里。
