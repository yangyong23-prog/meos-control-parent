# 扩展与维护指南

新增或修改日志时，先判断当前类是不是非 Bean 的普通实体类。不是非 Bean 类就使用 `LogProvider` 能力，是非 Bean 类才使用 `LogExecutor`。

## 新增业务类日志

必须流程：

1. 类实现 `LogComponent`。
2. 实现稳定的 `logTag()`。
3. 项目内日志一般使用 `logProjectInstance()`，项目 ID 自动从当前上下文获取。
4. 不使用 `logProjectInstance(projectId)`；定时任务、多项目循环等场景先设置当前上下文，再调用 `logProjectInstance()`。
5. 非项目日志使用 `logInstance()`。
6. 附加字段使用 `Pair<String, Object>`。
7. 异常日志传入原始异常对象。

示例：

```java
public class DemandQueryServiceImpl implements LogComponent {

    @Override
    public String logTag() {
        return "DemandQueryService";
    }

    public void query(String spaceId) {
        try {
            // query
        } catch (Exception e) {
            logProjectInstance().error("查询需求配置失败", e, Pair.of("spaceId", spaceId));
            throw e;
        }
    }
}
```

## 新增非 Bean 类日志

非 Bean 的普通实体类不要实现日志接口。确实需要日志时：

1. 保持实体类构造、字段和序列化结构简单。
2. 增加 `private static final Logger LOGGER`。
3. 增加 `private static final String LOG_TAG`。
4. 使用 `LogExecutor.project()` 或 `LogExecutor.instance()` 局部输出，项目日志不传项目 ID。
5. 不把 `LogProvider`、`LogComponent`、`LogComponentProvider` 作为字段或父接口。

如果非 Bean 类中的日志变多，必须把解析、转换或计算逻辑外移到行为组件，再由组件使用 `LogProvider`。

## 异常日志规则

- `catch` 后必须记录日志或继续抛出，不允许空 `catch`。
- 需要排查的问题使用 `error` 并传原始异常。
- 可恢复但需要关注的问题使用 `warn`。
- 业务异常和应用异常会被 `LogProvider` 特化输出，不要手动拼接错误码。
- Feign 异常必须在 ClientService 防腐层记录目标服务、接口和入参摘要。

## 历史 LogExecutor 调用迁移

迁移时按以下规则处理：

| 历史写法位置 | 处理方式 |
| --- | --- |
| 非 Bean 的普通实体类 | 保留 `LogExecutor`，不要改为日志接口。 |
| Service / Controller / Strategy | 实现 `LogComponent`，改为 `logProjectInstance()` 或 `logInstance()`。 |
| Task / Handler / Listener | 实现 `LogComponent`，保留 XXL-JOB 和请求调试链路。 |
| 静态工具类 | 必须由调用方记录日志；无法由调用方记录且具备业务行为时，必须改造成可注入组件。 |
| 日志基础设施 | 保持当前工厂和执行器设计。 |

不要为了快速迁移把 `LogProvider` 或 `LogComponent` 加到实体类上。

## 验证清单

- 新增 Bean 或业务组件没有直接使用 `LogExecutor.*` 作为默认日志入口。
- 新增非 Bean 的普通实体类没有实现 `LogProvider`、`LogComponent` 或 `LogComponentProvider`。
- 项目日志一般使用 `logProjectInstance()`，不显式传项目 ID。
- 非 Bean 类使用 `LogExecutor` 打印项目日志时，使用 `LogExecutor.project()`，不显式传项目 ID。
- 异常日志保留原始异常对象。
- 附加字段没有敏感数据。
- 大对象、集合和响应体只记录必要摘要。
- 需要请求调试收集的日志没有绕过 `LogProvider`。
