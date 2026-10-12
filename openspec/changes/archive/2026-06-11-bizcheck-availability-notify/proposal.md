## Why

业务可用性检查（BizCheck）当前仅在系统内部生成检查结果，运维人员/业务顾问需主动登录系统才能获知异常，信息滞后导致故障响应不及时。需要在不侵入既有检查执行链路的前提下，将异常结果通过钉钉主动推送给项目相关业务顾问，实现「检查完成—精准触达」的闭环。

## What Changes

- 新增**云端专属**的异常通知能力：在 `BizCheckDomainServiceImpl#startCheck` 全部检查项入库完成后，调用 `BizCheckServiceImpl#getCheckResults` 拉取最新结果，按规则筛选并组装文本，通过钉钉推送给业务顾问。
- 新增**云边解耦接口** `BizCheckNotifier`（位于 `common`），仅 `saas/meos-control-common` 提供实现；`BizCheckDomainServiceImpl#startCheck` 末尾通过 `applicationContext.getBeansOfType(BizCheckNotifier.class)` 自动发现并触发，边端无任何实现 → 不感知不执行。
- 新增**渲染策略族** `NotifyTextRenderer`（按 `BizCheckStrategyTypeEnum` 路由），覆盖 4 个检查项的明细渲染规则。
- 新增**收件人解析与发送适配**：复用 `mid-organization-api` 的 `GroupUserClient#queryByPositionNamesAndProjectId` 实时同步查询岗位手机号；复用 `mid-message-api` 的 `IMessageApi#sendDingTalkPersonalMessage` 同步循环发送。
- 新增配置 `meos.bizcheck.notify.{enabled, group-code, position-names}`，岗位列表通过 yml 配置（不做前端配置、不做 scope 路由）。
- **不去重**：手动连点、自动多次触发，只要 startCheck 走完入库就推送。
- **统计口径**：异常筛选 = 外层 `checkStatus=REJECTED` AND 内层 `status=EXCEPTION`；异常总条数 = 渲染后明细行数。
- 失败保护：通知发送失败仅记日志，不影响 `startCheck` 主流程返回。

## Capabilities

### New Capabilities
- `bizcheck-notify`: 业务可用性检查异常的钉钉通知能力，包含触发钩子、异常筛选、文本渲染、收件人解析与同步发送。

### Modified Capabilities
<!-- 暂无现有 capability 需要变更其需求；BizCheck 自身的检查执行行为不变。 -->

## Impact

- **代码改动**
  - `common/.../structure/bizcheck/service/BizCheckDomainServiceImpl.java`：`startCheck` 末尾追加约 6~8 行调用 `getBeansOfType(BizCheckNotifier.class)` 并循环 try/catch 调用。
  - `common/.../structure/bizcheck/notify/BizCheckNotifier.java`：新增纯接口（无 Spring 注解）。
  - `saas/meos-control-common/.../saas/bizcheck/notify/`：新增包，含通知编排实现、配置类、渲染器策略族、收件人解析、钉钉发送器。
- **依赖**
  - 复用已有 `mid-organization-api`（`GroupUserClient`）与 `mid-message-api`（`IMessageApi`），无需新增 SDK。
- **配置**
  - `application-saas*.yml` 新增 `meos.bizcheck.notify` 配置块。
- **运行侧**
  - 仅云端（saas）启用；边端（local）零文件、零感知。
- **风险**
  - 第三方 Feign（`GroupUserClient` / `IMessageApi`）不可用时，通知失败但检查主流程不受影响。
  - 同步发送会占用 startCheck 调用线程的尾部时间，预计 < 几秒（一组检查 + 同步发送 N 条钉钉），可接受。
