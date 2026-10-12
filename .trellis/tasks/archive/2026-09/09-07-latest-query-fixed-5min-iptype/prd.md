# latest 查询逻辑固定传 5min 分精度

## Goal

产品反馈（PRD-260901 迭代）：壳配置"持久化数据查询逻辑"="查询距离当前最新的数据"（latest）生效时，查询目前**不传分精度 ipType**；但持久化服务端未收到 ipType 时默认按 15min 处理，导致查询结果不对。修正：latest 生效分支固定传 `ipType=5min`；传 5min 仍可查到比 5min 更大的分精度数据（如 15min），不影响 latest"不限分精度取最新"的语义。

用户价值：latest 模式下各信息点能按各自实际分精度正确取到最新数据，不再受持久化服务端 15min 默认值干扰。

## Requirements

### R1 latest 分支固定传 5min（核心改动）

- 改动点仅为 `DtpRwdClientServiceImpl` 中两个 latest 分支（门控均为 `LocalContextStorage.isRemoveDefaultIpType() && isLatestDataQueryLogic()`）：
  - `queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)`（约 L1170）：latest 分支在设置时间窗口（当前-壳配置间隔 ~ 当前）的同时，固定 `periodDTO.setIpType("5min")`
  - `queryIpDataOnMomentInOneHour(objectIds, infoCodes)`（约 L1213）：同上
- 时间窗口逻辑不变（仍取壳配置 persistent_data_time_interval 间隔）
- 两处 latest 分支必须同步修改，禁止出现一处传一处不传

### R2 其余场景行为完全不变

- 无请求头标识 / 壳配置 same_time（默认）：走 legacy 分支（"15min" + 前端替换参数），保持现状
- 显式指定分精度的调用点（5 参版 `queryIpDataOnMoment`、DAY/1d 等产品功能指定）：保持指定不变
- SSE 推送、空间分析任务、边缘端等均不受影响（推送线程经订阅参数转存后走同一底层两个方法，自动一致）

### R3 注释与语义描述同步修正

- 两处 latest 分支注释及 `isLatestDataQueryLogic` 的 javadoc（约 L1563/L1567）中"不传 ipType"的描述改为"固定传 5min"
- 核对历史文档（openspec / .trellis/spec / docs 中是否固化"latest=不传分精度"表述），如有则同步修正

## Acceptance Criteria

- [ ] 请求头标识 `=true` + 壳配置 latest + 间隔=1h：查询时间范围（当前-1h ~ 当前），ipType 固定传 `5min`
- [ ] 请求头标识 `=true` + 壳配置 latest + 间隔=168h：同上，ipType 传 `5min`
- [ ] 请求头标识 `=true` + 壳配置 latest：能查到 15min 等大于 5min 分精度的数据（latest 语义不被 5min 限定破坏）
- [ ] 两个 latest 分支（queryIpDataOnMoment 4 参版 / queryIpDataOnMomentInOneHour）行为一致，均传 5min
- [ ] 无请求头标识 / 壳配置 same_time：与改造前完全一致（legacy 分支 15min + 替换参数）
- [ ] 显式分精度调用点（DAY、1d、显式 5min 等）不传参行为不变
- [ ] 注释与 javadoc 同步更新，无残留"latest 不传 ipType"的错误语义描述

## Out of Scope

- 壳配置项本身（枚举、分组、显示规则、前端页面）
- same_time 场景与 legacy 分支的任何调整
- 显式分精度调用点的任何改动
- 控制服务（dtp-meos-server）与前端请求头逻辑

## Notes

- 前序任务：`archive/2026-08/08-26-iot-persistence-query-rule-rework`（PRD-260901 首版，"不传分精度"决策来源），本次为产品对持久化服务默认 15min 行为的修正迭代
- 本次改动与"门控条件"无关，仅调整 latest 分支内的出参，不触碰门控与 legacy 分支，回归风险低
- 显式 5min 与硬编码 "15min" 目前均用字面量，本次沿用同风格（字面量 "5min" 或 `DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES.value()` 由实施确认，行为等价）
