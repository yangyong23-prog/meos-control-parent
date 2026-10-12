# 运行分析六类适配需求变更处理

## Background

- 2026-09-11 用户告知：产品需求有变更，"不改了"。原始需求【PRD-260902】运行分析主页兼容空调供热和空调冷热源系统由临时文档提出（评审用，已不保留）。
- 前置任务 `09-09-run-analysis-adapt-acah-acai` 已实施完毕并归档：代码提交 `cdc80a9c9f`（六类系统适配：能效统一 8 项池、季节门控、蓄冷上数范围、室内温度范围），后续 docs 修正 `bbc5062823`；见 `.trellis/tasks/archive/2026-09/`。
- 分支 `feat_v7.260902.0_运行分析`，尚未合入 qa。

## Known Facts

- 受影响已提交内容：7 个文件（RunAnalysisServiceImpl / RunAnalysisService / DtpRwdClassCodeEnum / DashBoardInfoCodeEnum / i18n ×2 / HistoryStorageTaskHandler）；无 SQL 改动（已回退）。
- 原任务遗留项：Test 环境验证、ACAI 字典链路（未闭环）、ACCI 卡片可能新增行需产品确认、蓄冷历史回填、前端 classCode 渲染联调。

## Open Questions (blocking)

- ~~Q1~~ 已解决（2026-09-11）：产品需求变更导致现有实现不再适用，**回滚 cdc80a9c9f 的六类适配代码**。

## Requirements

- R1 回滚 `cdc80a9c9f` 的 7 个代码文件到适配前内容（内容级回滚：取 `cdc80a9c9f^` 版本；含此前该文件内手工格式化一并回到原样）。
- R2 保留 `bbc5062823`（归档文档路径修正，与产品需求无关）及 archive 内任务产物（任务历史不回滚）。
- R3 回滚后全量编译验证通过；对这 7 个文件与 `cdc80a9c9f^` 逐文件比对无差异。
- R4 归档任务 prd.md 补充一行变更记录：2026-09-11 产品需求变更，代码已回滚（附回滚 commit）。
- R5 提交（提交信息按 meos-commit-message 规范，复用任务号 UYAI-1323，用户确认后执行）。

## Acceptance Criteria

- [ ] `git diff cdc80a9c9f^` 对 7 个文件无差异（内容完全回到适配前）。
- [ ] 全量编译 EXIT=0（JDK 8）。
- [ ] 归档任务文档含回滚记录行。
- [ ] 回滚提交完成，工作区 clean。

## Out of Scope

- 不删除任务历史与 archive 产物；不回滚 `bbc5062823` 文档修正。
- 不复用六类适配的任何遗留项（随回滚全部失效）；后续如需求再变另行开任务。

## Notes

- 若涉及回滚：需确认回滚方式（`git revert` 还是反向修改）与是否保留部分改动。
- 若仅记录变更：关闭归档任务遗留项，并确认已提交代码保持现状的风险（qa 合入后产品可见）。
