# Root Cause Analysis

## Symptom

`SpaceAnalyseTaskHandlerV2.computeQualifyRate` 在计算达标率时可能卡死，表现为任务不返回。

## Trigger Path

- 进入 `computeQualifyRate(...)`
- 命中“连续可调度时长”分支
- 以 `minute.minusMinutes(15)` 计算 `beforeTime`
- 使用 `while (!beforeTime.isBefore(minimum))` 作为回溯边界

## Root Cause

`LocalTime` 不是单调递减的时间轴，`minusMinutes(15)` 会在跨过 `00:00:00` 时回绕到 `23:45`。
一旦 `minimum` 是 `00:00:00`，`beforeTime.isBefore(minimum)` 永远不会变成 `true`，循环就失去退出条件。

## Evidence

- 关键代码位于 `SpaceAnalyseTaskHandlerV2.java:497-514`
- 营业时间来自日历服务，直接以 `Range<LocalTime>` 形式进入分析逻辑
- 代码库中已有跨天时间处理模式，例如 `AbstractDemandSecurityConfigAnalyzeStrategy`
- `SpaceAnalyseTaskHandlerV2` 底部已有 `calculateTimeDifference(LocalTime start, LocalTime end)`，说明这里本来就要考虑时间回绕语义

## Likely Fix Direction

- 不再用 `LocalTime` 自身顺序作为唯一边界
- 改成基于“当天分钟数”或“可回溯步数”的有限循环
- 在午夜边界前正确停止，不让时间值回绕后继续扩展

## Regression Focus

- 营业时间从 `00:00:00` 开始时
- 连续可调度时长配置非空时
- 单点/多点连续回溯都要能正常结束
