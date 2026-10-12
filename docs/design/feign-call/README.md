# Feign 调用程序设计文档

本文档用于沉淀项目内 Feign 调用的程序设计。重点描述 Feign 客户端声明、`ClientService` 防腐层、`BaseFeignClientService` 统一封装、请求上下文、响应解码、云边扫描差异、失败观测和扩展规则。

Feign 调用设计的核心原则是：业务代码不直接调用 Feign `*Client`，必须通过 `*ClientService` 转一层。`*Client` 只描述外部 HTTP 协议，`*ClientService` 面向系统内部定义稳定的入参和响应模型，用于兼容外部接口字段、路径、响应包装和版本变化。`*ClientService` 需要实现 `com.persagy.apccontrol.basic.outside.BaseFeignClientService`。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [模块总览](./overview.md) | Feign 调用范围、分层模型、核心原则和主要代码入口。 |
| [ClientService 防腐层](./client-service-boundary.md) | `*Client`、`*ClientService`、`*ClientServiceImpl` 的职责边界和调用规则。 |
| [Feign 基础设施](./feign-infrastructure.md) | `FeignConfiguration`、拦截器、响应解码、日期转换、分页和批量拆分。 |
| [云边扫描与适配](./cloud-edge-scan-and-adapter.md) | SaaS 与边缘端 Feign 扫描包、边缘专属客户端和同名接口实现差异。 |
| [失败边界与观测](./failure-and-observability.md) | 异常转换、默认返回、RPC 日志、请求统计和历史字段边界。 |
| [扩展与维护指南](./extension-guide.md) | 新增 Feign 调用、模型转换、上下文透传、云边实现和验证清单。 |

## 核心代码入口

| 层次 | 主要位置 |
| --- | --- |
| Feign 配置 | `common/src/main/java/com/persagy/apccontrol/basic/config/FeignConfiguration.java` |
| Feign 调用基类 | `common/src/main/java/com/persagy/apccontrol/basic/outside/BaseFeignClientService.java` |
| 公共 Feign 客户端 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/client` |
| 公共 ClientService 接口 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service` |
| 公共 ClientService 实现 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/impl` |
| 公共请求/响应模型 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/model` |
| Feign 响应解码 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/decoder/FeignResponseDecoder.java` |
| 请求头拦截 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/interceptor/RequestCustomHeaderInterceptor.java` |
| Token 拦截 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/interceptor/RequestTokenInterceptor.java` |
| 请求统计拦截 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/interceptor/FeignRequestStatisticsInterceptor.java` |
| SaaS 侧 Feign 适配 | `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/outside` |
| 边缘侧 Feign 适配 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside` |
| 边缘侧 Feign 客户端 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/feign` |

## 维护原则

- 业务代码只能依赖 `*ClientService` 或更高层 Adapter，不直接注入 Feign `*Client`。
- `*ClientService` 必须实现 `BaseFeignClientService`，`*ClientServiceImpl` 通过统一方法处理上下文、响应、异常、日志和默认值。
- `*Client` 只声明外部 HTTP 协议，不承载系统内部业务语义。
- `*ClientService` 负责定义系统内部稳定的入参和响应模型，并兼容外部接口变动。
- 通用响应为 `CustomerResponse` 或 `R` 时交给 `FeignResponseDecoder` 统一判断成功失败。
- 新增 Feign 调用不再设计 `isThrows`、`isCache`、`queryCache` 这类布尔开关；历史方法中出现时只作为兼容边界保留。
- 云边同名能力必须确认真实支持端；不支持的一侧应显式抛出不支持异常，避免误用。
- 边缘端独有 Feign 客户端放在 `local/meos-control-common-edge`，SaaS 端不新增边缘专属客户端。
