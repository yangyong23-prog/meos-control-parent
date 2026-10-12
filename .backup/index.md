# 后端开发规范

> 云边架构能源监控系统，Java 8 + Spring Boot。

---

## ⚠️ 实现前检查清单（强制）

> **⚠️ 实现任何代码前必须阅读并遵守规范索引中的所有规范文件**

---

## 规范索引

### 核心规范

| 文件                                          | 说明                       |
|---------------------------------------------|--------------------------|
| [目录结构](directory-structure.md)              | 模块布局、包结构                 |
| [API 设计](api-url-convention.md)             | URL 规范                   |
| [API 响应](api-response.md)                   | 统一响应格式                   |
| [Controller 基础](api-controller-basic.md)    | 模板、参数校验                  |
| [Controller 进阶](api-controller-advanced.md) | 异常、分页                    |
| [数据库规范](database-guidelines.md)             | MyBatis、SQL              |
| [异常处理](error-handling.md)                   | 异常类型、全局处理器               |
| [日志规范](logging-guidelines.md)               | LogComponent、LogExecutor |

### 代码质量

| 文件                                 | 说明                 |
|------------------------------------|--------------------|
| [Javadoc 规范](quality-javadoc.md)   | @since 标签          |
| [代码禁止模式](quality-code-patterns.md) | 危险模式、安全替代          |
| [安全禁止模式](quality-security.md)      | 敏感数据、注入防护          |
| [工具使用](quality-tools.md)           | 日期、集合、字符串工具        |
| [空值注解](quality-nullable.md)        | @NonNull/@Nullable |

### 跨领域规范

| 文件                                | 说明            |
|-----------------------------------|---------------|
| [命名规范](../guides/naming.md)       | Java 和 API 命名 |
| [架构规范](../guides/architecture.md) | 分层架构、包组织      |

---

## 技术栈

| 分类     | 技术                   |
|--------|----------------------|
| 框架     | Spring Boot + Nacos  |
| 数据库    | MyBatis Plus + Druid |
| 缓存     | Redis + Caffeine     |
| 消息     | RabbitMQ、Kafka       |
| 定时     | XXL-JOB              |
| API 文档 | Apifox               |

---

## 快速参考

### 禁止新增的包

`handler/`、`po/`、`page/`

### 禁止的模式

| 禁止                                   | 替代                                                 |
|--------------------------------------|----------------------------------------------------|
| `${}` in SQL                         | `#{xxx}`                                           |
| `new Date()` / `LocalDateTime.now()` | `DateUtils.now()`                                  |
| `@Builder` 在 DTO                     | `@Data + @NoArgsConstructor + @AllArgsConstructor` |
| 空 catch 块                            | 记录日志或抛出异常                                          |
| 循环中单条查询                              | 批量查询                                               |
| `@Slf4j` + `log.xxx()` 在业务类          | 实现 `LogComponent` 接口                               |
| `@Slf4j` + `log.xxx()` 在工具类          | 使用 `LogExecutor`                                   |

---

## 规范维护

- 每个规范文件都有 **Why**（为什么）和 **What**（是什么）
- 代码示例优先于文字描述
- 发现与代码不一致时，立即更新
