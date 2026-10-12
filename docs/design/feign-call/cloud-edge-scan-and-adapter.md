# 云边扫描与适配

## 扫描差异

| 侧别 | 启动类 | Feign 扫描包 |
| --- | --- | --- |
| SaaS | `saas/meos-control-server/.../ApcControlServerApplication.java` | `basic.outside.feign.client`、`common.outside` |
| 边缘 | `local/meos-control-server-edge/.../ApcControlServerEdgeApplication.java` | `basic.outside.feign.client`、`common.outside.feign`、`common.outside` |

边缘端额外扫描 `common.outside.feign`，因此只有边缘端可以直接注册 `DtpTwinClient`、`PaasDatatransferClient`、`AlgorithmFeignClient`、`AlgorithmChillerPlantFeignClient`、`RcSaasClient` 等边缘专属 Feign 客户端。

## 同名接口差异

同一个 `*ClientService` 在 SaaS 与边缘端可能有不同实现。判断能力是否可用，不能只看接口是否存在，还要看当前运行侧是否有真实实现。

| 接口 | SaaS 侧 | 边缘侧 |
| --- | --- | --- |
| `DtpTwinClientService` | `saas/.../outside/DtpTwinClientServiceImpl.java` 所有方法抛不支持异常。 | `local/.../outside/twin/DtpTwinClientServiceImpl.java` 调用边缘 `DtpTwinClient`。 |
| `PaasDataTransferClientService` | `saas/.../outside/PaasDataTransferClientServiceImpl.java` 所有方法抛不支持异常。 | `local/.../outside/service/impl/PaasDataTransferClientServiceImpl.java` 调用边缘 `PaasDatatransferClient`。 |
| `AbstractDtpPersistServer` | `DtpPersistServerCloud` 直接调用 `DtpPersistServerClient`。 | `DtpPersistServerEdge` 会结合 Twin 当前值查询。 |

## 边缘专属 Feign 客户端

| 客户端 | 目标 URL 配置 | 用途边界 |
| --- | --- | --- |
| `DtpTwinClient` | `${inner-url.bdtp.dtp-twin.service:}` | 边缘端 Twin 当前值、配置和指令相关 HTTP 调用。 |
| `PaasDatatransferClient` | `${inner-url.paas.paas-datatransfer.service:}` | 边缘端调用 PAAS 数据传输 HTTP 接口。 |
| `AlgorithmFeignClient` | `${inner-url.bdtp.ai-au.service:}` | 边缘端算法 HTTP 调用。 |
| `AlgorithmChillerPlantFeignClient` | `${inner-url.bdtp.ai-au.service:}` | 边缘端 AI 策略评估 HTTP 调用。 |
| `RcSaasClient` | `${external-url.meoi.meos-control.service:}` | 边缘端调用云端群控 HTTP 接口。 |

边缘专属客户端不应放入 SaaS 扫描包。SaaS 侧如果需要保留同名接口，应提供明确的不支持实现，避免启动或注入失败。

## 维护规则

- 新增云边共用接口时，必须同时确认 SaaS 和边缘侧是否真实支持。
- 只在边缘端存在的 Feign 客户端放入 `local/meos-control-common-edge/.../common/outside/feign`。
- 业务层必须依赖同名 `*ClientService` 或封装后的抽象类，由云边实现决定实际调用来源；不直接依赖 Feign `*Client`。
- 当前运行侧不支持的能力要显式失败，不做静默空实现。
