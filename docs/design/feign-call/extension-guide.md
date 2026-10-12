# 扩展与维护指南

## 新增 Feign 调用流程

1. 确认运行侧：SaaS、边缘 local，还是云边都需要。
2. 查找已有 Feign `*Client` 和 `*ClientService`，优先扩展现有封装。
3. 新增 Feign `*Client` 方法时，明确服务名、URL 配置、接口路径、请求头、请求体和返回类型。
4. 新增 `*ClientService` 方法时，定义系统内部稳定的入参和响应模型。
5. 新增 `*ClientService` 时必须实现 `BaseFeignClientService`，新增 `*ClientServiceImpl` 方法时复用 `query/operation` 能力。
6. 根据业务风险定义失败语义、默认值、重试和日志策略，不新增 `isThrows`、`isCache`、`queryCache` 这类布尔开关。
7. 更新对应业务设计文档，说明业务如何消费 Feign 返回数据。

## 放置规则

| 场景 | 放置位置 |
| --- | --- |
| 云边共用 Feign 客户端 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/client` |
| 云边共用 ClientService 接口 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service` |
| 云边共用 ClientService 实现 | `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/impl` |
| SaaS 专属适配 | `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/outside` |
| 边缘专属 Feign 客户端 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/feign` |
| 边缘专属适配实现 | `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside` |
| 请求/响应模型 | 优先放在 `common/.../outside/feign/model`；边缘专属模型放在 local 对应 outside model 下。 |

## Feign 客户端约定

- `@FeignClient` 的 `name` 使用外部服务名，`url` 使用配置占位符。
- URL 配置优先沿用 `inner-url.*`、`external-url.*`、`url.cloud-*` 命名。
- 请求头中的 `groupCode`、`projectId`、`userId` 要显式标注来源。
- 返回类型优先使用外部服务真实响应：`CustomerResponse<T>`、`R<T>`、`DmpResult<T>` 或专用响应 DTO。
- `*Client` 不提供给业务模块直接注入，只作为 `*ClientServiceImpl` 的远程协议依赖。

## ClientService 约定

- `*ClientService` 必须实现 `BaseFeignClientService`。
- 业务模块只能依赖 `*ClientService` 或内部已封装 `*ClientService` 的更高层 Adapter，不直接依赖 Feign `*Client`。
- `*ClientService` 应定义系统内部稳定的入参和响应模型，用于兼容外部服务字段、响应结构和接口版本变化。
- 新方法应提供业务语义名称，例如 `listBuilding`、`getAirSeasonType`、`mapObjectIdAndLocalName`。
- 对空参数先返回空集合或抛参数异常，避免无意义远程调用。
- 批量查询优先返回 Map，减少调用方循环远程请求。
- 如果调用可能返回空，应通过方法语义和返回类型明确默认值。
- 不新增 `isThrows`、`isCache`、`queryCache` 这类历史布尔字段。
- 如果涉及实时数据，不要使用静态数据缓存。

## 云边实现清单

| 问题 | 说明 |
| --- | --- |
| SaaS 是否真实支持？ | 如果不支持，应像 `DtpTwinClientServiceImpl` 一样显式抛不支持异常。 |
| 边缘是否真实支持？ | 如果只在边缘实现，客户端和配置应放在 local 侧。 |
| 是否需要同名抽象？ | 类似 `AbstractDtpPersistServer`，业务层可以注入抽象，由云边实现决定数据来源。 |
| 是否允许离线？ | 边缘端调用云端时必须明确云端不可达的降级行为。 |
| 是否跨租户/跨项目？ | 后台线程和批处理必须显式设置上下文。 |

## 验证清单

- 没有新增业务代码直接注入 Feign `*Client`。
- 新增或改造的 `*ClientService` 实现了 `BaseFeignClientService`。
- `*ClientServiceImpl` 统一处理外部响应和异常。
- 入参/响应模型没有把外部字段直接扩散到业务层。
- groupCode、projectId、userId 的来源明确。
- 大集合查询有分页或参数拆分策略。
- 关键失败日志包含目标服务、接口路径、入参摘要和上下文。
