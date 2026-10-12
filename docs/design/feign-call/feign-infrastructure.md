# Feign 基础设施

## 启用方式

SaaS 启动类 `ApcControlServerApplication` 通过 `@EnableFeignClients` 扫描：

| 包 | 说明 |
| --- | --- |
| `com.persagy.apccontrol.basic.outside.feign.client` | 公共 Feign 客户端。 |
| `com.persagy.apccontrol.common.outside` | SaaS 侧 Feign 适配实现和少量服务接口。 |

边缘启动类 `ApcControlServerEdgeApplication` 额外扫描：

| 包 | 说明 |
| --- | --- |
| `com.persagy.apccontrol.common.outside.feign` | 边缘专属 Feign 客户端，例如 `DtpTwinClient`、`PaasDatatransferClient`、`AlgorithmFeignClient`、`RcSaasClient`。 |

两个启动类都排除了平台默认的 `OpenFeignAutoConfiguration`，使用项目内 `FeignConfiguration` 统一注册解码器、请求拦截器和格式转换器。

## `FeignConfiguration`

`FeignConfiguration` 位于 `common/src/main/java/com/persagy/apccontrol/basic/config/FeignConfiguration.java`。

| Bean | 职责 |
| --- | --- |
| `FeignFormatterRegistrar` | 注册日期、时间、年月、时区等类型转换器。 |
| `Decoder` | 包装 `FeignResponseDecoder`，统一处理 `R`、`CustomerResponse` 和普通返回值。 |
| `RequestTokenInterceptor` | `local`、`dev` profile 下从 `token` 配置注入 Bearer Token。 |
| `RequestCustomHeaderInterceptor` | 统一透传上下文请求头，补充 `requestTimestamp` 和 `appId=meos`。 |
| `FeignRequestStatisticsInterceptor` | 按配置统计指定服务的调用来源和次数。 |

## 请求上下文

`RequestCustomHeaderInterceptor` 在平台请求头基础上补充本项目所需信息：

| 字段 | 来源/规则 | 用途 |
| --- | --- | --- |
| `groupCode` | 当前上下文请求头 | 集团维度路由和租户识别。 |
| `projectId` | 当前上下文请求头 | 项目维度数据过滤。 |
| `requestTimestamp` | 当前毫秒时间戳 | `FeignResponseDecoder` 计算调用耗时。 |
| `appId` | 固定查询参数 `meos` | 外部服务识别调用方。 |

后台线程、定时任务和批处理不能假设一定存在 HTTP 请求上下文，需要由调用封装层显式传入或补齐 group/project/user 信息。

## 响应解码

`FeignResponseDecoder` 统一识别三类返回：

| 返回类型 | 成功判断 | 失败行为 |
| --- | --- | --- |
| `R<?>` | `res.getIsFail()` 为 false | 抛 `FeignServerException`，响应体写入异常内容。 |
| `CustomerResponse<?>` | `resultCode` 成功或 `result=success` | 抛 `FeignServerException`，使用 `resultMsg` 作为错误信息。 |
| 其他类型 | 直接交给 SpringDecoder | 仅按空/非空打印调试日志。 |

解码器会通过 `requestTimestamp` 输出 `executeTime`。TRACE、ERROR 或请求调试 TRACE 时打印请求体和响应体，常规 DEBUG 日志只保留 URL、headers、耗时等摘要。

## 分页和批量拆分

| 工具 | 职责 |
| --- | --- |
| `FeignPageExecutor` | 当外部服务分页大小超过限制时，按 `page.size.limit` 拆分页并使用业务线程池并发补齐。 |
| `FeignParamPageExecutor` | 当请求参数里的对象列表过长时，按固定阈值拆分并合并结果。 |
| `DtpRwdClientPageWrapper`、`DangerEventClientPageWrapper`、`ExceptionEventClientPageWrapper` | 对具体 Feign 查询做分页包装。 |

新增大批量 Feign 调用时，应先确认外部服务单次请求限制，再选择分页拆分或参数拆分，不应让业务层直接传超大集合到 Feign 客户端。

## Feign 配置

边缘端 `local/meos-control-common-edge/src/main/resources/application-paas.yml` 明确配置：

| 配置 | 当前值/规则 |
| --- | --- |
| `feign.client.config.default.read-timeout` | `300000` |
| `feign.httpclient.enabled` | `false` |
| `feign.okhttp.enabled` | `true` |

新增配置时应区分 SaaS 与 local 所属目录，只在实际运行侧补充。
