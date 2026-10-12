# 乙二醇泵支持开启中关闭中状态

## Goal

大疆项目反馈供冷乙二醇泵（设备类编码 `ACCCGP`）当前没有“开启中 / 关闭中”过渡状态。
本任务要求补齐 `ACCCGP` 设备在开关设定变化时的设备运行状态计算与展示能力，使其与同类水泵设备保持一致。

## Confirmed Facts

- 目标设备类编码：`ACCCGP`，设备类名称：供冷乙二醇泵。
- 产品确认本次范围只处理 `ACCCGP`，不包含供冷乙二醇泵控制组 `ACCGP`。
- 触发信息点编码：`equipSwitchSet`，名称：设备开关设定。
- 仓库中 `ACCCGP` 已存在 `equipSwitchSet`、`onlineStatus`、`runStatus` 三个相关信息点。
- 当前 `ACCCGP equipSwitchSet` 的 `extend_info` 仅包含 `{"view": "1"}`，未配置 `triggerFormulaInfo`。
- 同类设备如 `ACCCCP`（供冷冷冻水泵）、`ACCCOP`（供冷冷却水泵）、`ACCHCH`（冷热水循环泵）的 `equipSwitchSet` 已配置 `triggerFormulaInfo`，通过 `onlineStatus + equipSwitchSet + runStatus` 计算 `rcRunStatus`。
- 既有通用状态语义为：`-1=无效`、`0=停止`、`1=运行`、`2=开启中`、`3=关闭中`。

## Requirements

- 为 `ACCCGP` 的 `equipSwitchSet` 增加触发 `rcRunStatus` 计算的配置，计算规则与同类水泵设备保持一致：
  - 设备离线或在线状态无效时返回 `-1`。
  - 无开关设定值时优先沿用 `runStatus`，无运行状态时返回 `-1`。
  - `equipSwitchSet=0` 且实际仍运行时返回 `3`（关闭中），实际已停止时返回 `0`（停止）。
  - `equipSwitchSet=1` 且实际未运行时返回 `2`（开启中），实际已运行时返回 `1`（运行）。
- 确保 `ACCCGP` 存在可承载 `-1/0/1/2/3` 的 `rcRunStatus`（设备运行状态）配置；如果当前初始化数据缺失，需要补齐。
- 同步覆盖 SaaS 与 Local Edge 两套初始化配置，避免不同部署形态表现不一致。
- 不改变其他设备类已有状态计算行为。

## Acceptance Criteria

- [ ] `ACCCGP equipSwitchSet` 变更后可按既有公式触发 `rcRunStatus` 计算。
- [ ] `ACCCGP rcRunStatus` 可展示 `开启中`（值 `2`）和 `关闭中`（值 `3`）。
- [ ] 计算规则与 `ACCCCP` / `ACCCOP` 等同类泵设备一致。
- [ ] SaaS 与 Local Edge 初始化配置保持一致。
- [ ] 变更不影响其他设备类的 `equipSwitchSet`、`runStatus`、`rcRunStatus` 配置。

## Out Of Scope

- 不新增前端交互或页面布局。
- 不处理供冷乙二醇泵控制组 `ACCGP`。
- 不调整通用 `rcRunStatus` 计算引擎。
- 不处理设备长时间卡在“开启中 / 关闭中”的补偿机制。

## Open Questions

- None.

## Notes

- Keep `prd.md` focused on requirements, constraints, and acceptance criteria.
- Lightweight tasks can remain PRD-only.
- For complex tasks, add `design.md` for technical design and `implement.md` for execution planning before `task.py start`.
