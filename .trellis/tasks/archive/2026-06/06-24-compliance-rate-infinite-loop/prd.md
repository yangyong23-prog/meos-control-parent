# 解决达标率计算死循环问题

## Goal

修复 `SpaceAnalyseTaskHandlerV2.computeQualifyRate` 在达标率计算阶段可能出现的死循环，确保任务在合法营业时间配置下可正常结束。

## Requirements

- 达标率计算不能因为营业时间起点为 `00:00:00` 或其他边界配置而卡死。
- 修复后应保持现有达标率判定语义，不改变正常场景的计算结果。
- 修复范围仅限 `SpaceAnalyseTaskHandlerV2` 的达标率计算逻辑，不调整营业时间来源和需求配置结构。
- 需要补充回归验证，覆盖午夜边界场景。

## Acceptance Criteria

- [x] `computeQualifyRate` 在包含 `00:00:00` 的营业时间下可正常返回。
- [x] 连续可调度时长的判断结果与当前规则一致。
- [x] 有回归测试或等价验证覆盖该边界，避免死循环再次出现。
- [x] 变更未影响其它空间分析流程。

## Notes

- Keep `prd.md` focused on requirements, constraints, and acceptance criteria.
- Lightweight tasks can remain PRD-only.
- For complex tasks, add `design.md` for technical design and `implement.md` for execution planning before `task.py start`.

## Confirmed Facts

- 问题点在 `saas/meos-control-compute/src/main/java/com/persagy/apccontrol/task/SpaceAnalyseTaskHandlerV2.java:498`。
- 该处用 `LocalTime.minusMinutes(15)` 向前回溯，并用 `while (!beforeTime.isBefore(minimum))` 作为边界。
- `LocalTime` 会按 24 小时回绕，`00:00` 之前再减 15 分钟会变成 `23:45`，边界条件因此失效。
- 文件底部已有 `calculateTimeDifference(LocalTime start, LocalTime end)`，说明代码库里已经存在处理时间回绕的模式。
- 当前任务目录下没有现成测试文件覆盖这个方法。

## Out of Scope

- 不重构营业日历服务。
- 不修改需求配置的数据结构。
- 不同步修改 V1 空间分析处理器，除非后续证据表明也存在同类问题。
