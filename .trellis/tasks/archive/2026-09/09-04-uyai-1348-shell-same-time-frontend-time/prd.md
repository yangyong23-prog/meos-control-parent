# 壳配置 same_time 时前端时间参数未生效修复 (UYAI-1348)

## Goal

修复 v7.260901.0 分精度改造缺陷：壳配置 `persistent_data_query_logic` = same_time（查询同一个时刻点的数据）时，底层 IOT 实时查询错误地使用 `persistent_data_time_interval` 滚动窗口并跳过前端时间参数替换，导致实时数据/最新值页面前端传的时间不生效。修复后仅"壳配置 latest **且** 请求头 `removeDefaultIpType=true`"双条件同时满足才走壳配置窗口逻辑，same_time 及无标识请求完全恢复改造前行为。

## Background

- 08-26 改造设计（`.trellis/tasks/archive/2026-08/08-26-iot-persistence-query-rule-rework/design.md` §2.3）：门控条件为 `标识=true && 壳配置=latest` 才走最新数据查询；其余组合维持现状。PRD 验收标准明确："请求头 =true + 壳配置'同一时刻点'（默认）→ 行为与改造前一致"。
- 实际实现（commit `4c561d6189`）：`DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` 与 `queryIpDataOnMomentInOneHour` 将分支条件简化成仅判断 `isRemoveDefaultIpType()`，same_time + 标识时也强行走 `[now-interval, now]` 窗口，且该分支**不调用 substitutionParameter** → 前端传入的 `IPDataStartTime/IPDataEndTime` 被忽略，与设计及验收标准不符。
- 测试反馈（UYAI-1348）：壳配置配为 same_time 时，前端传了时间参数，期望按前端时间点查询固定分精度数据，实际未生效。
- 用户澄清修复语义：壳配置 same_time 时一律走以前的逻辑；壳配置 latest 且前端请求头传 `removeDefaultIpType` 才处理。修复范围同时覆盖 HTTP 实时查询与 SSE 推送链路。

## Requirements

### R1 双条件门控（核心修复）

`queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` 与 `queryIpDataOnMomentInOneHour(objectIds, infoCodes)` 两个底层方法：

- 仅当 `LocalContextStorage.isRemoveDefaultIpType()=true` 且壳配置 `persistent_data_query_logic=latest` 时，走"查询距离当前最新的数据"逻辑：`startTime=now-interval`、`endTime=now`、**不设 ipType**
- 其余一切组合（壳配置 same_time / 缺失 / 读取异常，或无请求头标识）恢复改造前行为：
  - 4 参版：`ipType="15min"`，随后执行 `substitutionParameter`（命中替换系统白名单时用前端 `IPDataStartTime/IPDataEndTime/IPType` 覆盖）
  - InOneHour 版：`ipType="15min"` + `startTime=-1h` + `endTime=now`，随后执行 `substitutionParameter`
- 壳配置读取异常或缺失回退 same_time 语义（走旧逻辑），保留现有 error 日志

### R2 行为矩阵（HTTP 实时查询）

| 请求头 `removeDefaultIpType` | 壳配置 | 行为 |
|---|---|---|
| true | latest | interval 窗口 + 不传分精度（与现状一致，防回归） |
| true | same_time / 缺失 / 异常 | 与改造前一致：15min + substitutionParameter（前端时间参数生效 ← bug 修复点） |
| 无（任意配置） | — | 与改造前一致，不读壳配置（短路优化保持） |

### R3 SSE 推送链路同步生效

- SSE 推送查询（`CloudIotDataEvent` 等）走同一底层方法，修复后自动生效，无需额外改逻辑
- 现有订阅参数转存 + ThreadLocal set/clear（`SseSubscribeParam`、`LocalContextStorage.setRemoveDefaultIpType/clearRemoveDefaultIpType`）保持不变，仅验证回归

### R4 不改动范围

- 显式分精度查询（5 参版 `queryIpDataOnMoment`、DAY/5min 等显式 ipType 调用）不动
- 无标识请求的壳配置读取短路优化保持，不增加 DB 读取
- 壳配置枚举/场景定义、前端请求头协议、SSE 订阅协议不变

## Acceptance Criteria

- [ ] same_time + 标识=true + 前端传 `IPDataStartTime/IPDataEndTime`（替换系统白名单场景）→ 使用前端时间点查询，返回对应时刻固定分精度数据（UYAI-1348 复现场景）
- [ ] same_time + 标识=true + 未传时间参数 → 行为与改造前一致（4 参版默认 15min；InOneHour 版 -1h~now）
- [ ] latest + 标识=true → 现状行为不变（interval 窗口 + 不传分精度），08-26 的 latest 验收项不回退
- [ ] 任意壳配置 + 无标识 → 行为与改造前一致
- [ ] 壳配置读取异常/缺失 → 回退 same_time 旧逻辑
- [ ] SSE：订阅带标识 + same_time → 推送查询回退旧逻辑；订阅带标识 + latest → 现状不变
- [ ] 显式分精度查询（DAY、5min 等）不受影响
- [ ] 回归：latest + 标识（interval=1h 与 168h 各验一次）查询正常

## Notes

- 复现场景：实时数据/最新值页面（冷源/冷热源等替换系统），前端带时间参数请求
- 预期改动文件：`common/.../outside/feign/service/impl/DtpRwdClientServiceImpl.java`（两个查询方法 + 私有辅助方法语义调整）
- 历史设计对照：`.trellis/tasks/archive/2026-08/08-26-iot-persistence-query-rule-rework/prd.md`、`design.md`
