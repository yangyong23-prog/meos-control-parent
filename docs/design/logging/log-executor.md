# LogExecutor 设计

`LogExecutor` 是日志执行器工厂，负责创建实现 `LogProvider` 的执行器。它可以创建普通、集团、项目、RPC 和 ProjectDTO 上下文的日志执行器，也可以为 `LogComponent` 包装出 `LogComponentProvider`。

## 工厂方法

| 方法 | 返回能力 | 说明 |
| --- | --- | --- |
| `instance()` | `LogProvider` | 使用当前上下文的普通日志执行器。 |
| `instance(LogComponent)` | `LogComponentProvider` | 为组件对象创建普通日志执行器。 |
| `group(groupCode)` | `LogProvider` | 指定集团上下文。 |
| `project()` | `LogProvider` | 使用当前上下文的项目日志执行器，普通日志打印使用该方法。 |
| `project(projectId)` | `LogProvider` | 历史兼容重载，规范日志打印不指定项目 ID。 |
| `project(LogComponent, projectId)` | `LogComponentProvider` | 保留重载；组件日志必须通过 `logProjectInstance()` 从上下文取项目。 |
| `rpc(param, response)` | `LogRpcExecutor` | 输出 RPC 入参和响应摘要。 |
| `project(ProjectDTO)` | `LogProvider` | 输出项目 ID、集团编码、本地项目 ID 和本地项目名称。 |

## 当前定位

`LogExecutor` 的核心定位有两个：

- 日志基础设施内部使用的执行器工厂。
- 非 Bean 的普通实体类无法实现 `LogProvider` 能力时的兼容日志入口。

新增业务代码禁止把 `LogExecutor` 当默认入口。除非 Bean 的普通实体类外，必须通过 `LogComponent` 获取 `LogProvider` 能力。仓库内已有较多历史 `LogExecutor.*` 直接调用，后续迭代时可以保留，但不再按这些历史写法扩散。

## 实体类兼容场景

这里的普通实体类指非 Spring Bean、非容器管理的普通数据或模型类，包括未注册为 Bean 的持久化实体、DTO、VO、普通 POJO，以及会被 Jackson、Redis、MyBatis、MQ、Feign 或缓存框架序列化和反序列化的数据承载对象。这类对象不要实现 `LogProvider`、`LogComponent` 或 `LogComponentProvider`。

实体类确实需要日志时，使用静态 `Logger`、稳定 `LOG_TAG` 和 `LogExecutor`。项目日志使用 `LogExecutor.project()`，不传项目 ID：

```java
public class DtpRwdObjectDTO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtpRwdObjectDTO.class);

    private static final String LOG_TAG = "DtpRwdObjectDTO";

    public void parseInfoCode(String infoCode) {
        try {
            // parse
        } catch (Exception e) {
            LogExecutor.project().trace(LOGGER, LOG_TAG, "解析信息点失败", e,
                    Pair.of("infoCode", infoCode));
        }
    }
}
```

这个写法的目的不是鼓励实体类承载复杂业务逻辑，而是在无法避免日志时，不把日志接口混入实体类型结构。

## 禁止扩散

以下新增写法禁止：

```java
public class SomeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeService.class);

    private static final String LOG_TAG = "SomeService";

    public void handle() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "处理完成");
    }
}
```

服务、策略、任务和处理器应改为：

```java
public class SomeService implements LogComponent {

    @Override
    public String logTag() {
        return "SomeService";
    }

    public void handle() {
        logProjectInstance().info("处理完成");
    }
}
```

## 历史迁移边界

历史类中如果已经直接使用 `LogExecutor`，可以在相关功能迭代时逐步迁移：

- 如果类是业务组件，增加 `implements LogComponent` 和 `logTag()`。
- 将 `LogExecutor.project().info(LOGGER, LOG_TAG, "...")` 改为 `logProjectInstance().info("...")`。
- 普通日志打印不要使用 `LogExecutor.project(projectId)`，项目 ID 由当前上下文提供。
- 定时任务、多项目循环等场景应先设置当前上下文，再使用 `LogExecutor.project()` 或 `logProjectInstance()`。
- 如果类是非 Bean 的普通实体类，保持 `LogExecutor`，不要迁移为 `LogComponent`。
