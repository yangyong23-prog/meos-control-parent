# 使用边界

日志入口选择的第一判断不是“哪里写起来更短”，而是“当前类是否是非 Spring Bean、非容器管理的普通数据或模型类”。只要是 Bean 或业务组件，新增代码都必须使用 `LogProvider` 能力；只有非 Bean 类才使用 `LogExecutor`。

## 决策表

| 代码类型 | 新增代码入口 | 原因 |
| --- | --- | --- |
| Controller | `LogComponent` -> `LogProvider` | 有明确组件身份，可提供稳定 `logTag()`。 |
| Service / ServiceImpl | `LogComponent` -> `LogProvider` | 业务过程日志应自动带项目上下文。 |
| Strategy / Handler / Listener | `LogComponent` -> `LogProvider` | 策略和消息处理需要稳定标签与异常上下文。 |
| XXL-JOB Task | `LogComponent` -> `LogProvider` | INFO 及以上会同步到 `XxlJobHelper`。 |
| Import / Context / Aggregate 行为对象 | `LogComponent` -> `LogProvider` | 只要不是普通数据实体，就按业务组件处理。 |
| 非 Bean 的 Entity / DTO / VO / POJO | `LogExecutor` | 避免日志接口影响序列化和反序列化。 |
| 静态工具类 | 必须由调用方记录日志 | 新增工具类不得为了日志扩散 `LogExecutor`；无法由调用方记录且具备业务行为时，必须改造成组件。 |
| 日志基础设施类 | 按当前日志包内部设计使用 | `LogExecutor`、执行器和 Provider 自身属于框架实现。 |

## 普通实体类定义

本文档中的普通实体类指非 Spring Bean、非容器管理类。满足以下任一条件时，按普通实体类处理：

- 类主要用于承载字段，没有稳定的业务组件生命周期。
- 类会作为接口请求或响应被 Jackson 序列化。
- 类会进入 Redis、Caffeine、MQ、Kafka、Feign 或本地缓存。
- 类会被 MyBatis、MyBatis Plus 或其他 ORM 映射。
- 类需要无参构造、字段反射、Bean 拷贝或跨进程传输。

普通实体类不要实现：

- `LogProvider`
- `LogComponent`
- `LogComponentProvider`
- 任何只为日志服务而引入的组件接口

## Bean 与实体类的区别

`common/.../model/context` 下存在一些上下文对象实现了 `LogComponent`。这类对象不是本文档所说的普通实体类，它们承载导入、查询、计算或编排行为，有明确执行生命周期，或者作为 Bean/业务组件使用，因此可以使用 `LogProvider` 能力。

判断标准是是否由容器管理和是否承载业务组件职责，而不是包名。即使类放在 `model` 包下，只要它是行为上下文、导入上下文或聚合执行上下文，也可以按组件对象处理。反过来，即使类放在业务包下，只要它只是非 Bean 的数据传输对象，也不要实现日志接口。

## 序列化边界

普通实体类实现日志接口可能产生以下问题：

- 序列化框架识别到额外 getter 或 default 方法。
- 反序列化时出现无法构造、无法绑定或字段歧义。
- Redis、MQ 或 Feign 传输时混入运行期日志能力。
- MyBatis 或 Bean 工具扫描到非业务属性。
- 实体对象在跨模块传输时暴露日志实现细节。

因此实体类即使已经通过 `@JsonIgnore` 屏蔽部分方法，也不应再新增日志接口实现。实体类需要日志时，用 `LogExecutor` 在方法内部局部输出。

## 日志位置选择

日志必须写在业务组件边界：

- 参数进入业务流程时记录关键输入摘要。
- 远程调用失败时记录目标服务、业务参数和异常。
- 分支跳过时记录跳过原因。
- 数据为空但属于可预期结果时使用 `warn` 或 `info`。
- 实体内部只有在解析、转换、兼容历史字段等确实无法外移时才记录日志。

不要为了输出一条日志，把普通实体类改造成业务组件。
