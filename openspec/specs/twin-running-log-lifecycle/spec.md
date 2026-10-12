# twin-running-log-lifecycle Specification

## Purpose

定义边缘端孪生自动运行日志（`source_type=TWIN_COMMAND`）的完整生命周期契约：入库白名单判定、建档帧 per-`(traceId,objectId,infoCode)` 锁内查重建档、终态帧锁内三态幂等 Upsert、`state` 单调不回退、`content` 快照与 `state` 解耦、以及建档/补建/终态更新后的 Kafka 同步与 SSE 播报。用于根治 `@Async` 乱序并发下“自动日志全是 `state=0`、终态丢失”（Bug B）与“同一指令重复建档”（Bug A）两类缺陷。

## Requirements

### Requirement: 孪生自动日志入库白名单判定

系统 SHALL 仅对同时满足以下条件的孪生控制帧入库：`type == 1`(CTRL)、`user == "none"`、`traceData` 非空且不含 `"dtp-meos"`，且命中白名单三来源之一——`RunningLogTemplateConfig.twinClassCodeInfoCodes.get(classCode)` 包含该 `infoCode`，或 `MdQueryObjStrategy.classList` 包含该 `classCode`，或 `VentilationQueryObjStrategy.classList` 包含该 `classCode`。建档帧（`result=0`）与终态补建帧（`result≠0`）MUST 复用**同一套**白名单判定。

#### Scenario: 白名单类码建档
- **WHEN** 收到 `classCode=ACATFC`、`infoCode=equipSwitchSet`、`result=0`、`user=none` 的孪生帧
- **THEN** 系统入库一行 `state=0`、`source_type=TWIN_COMMAND`

#### Scenario: 非白名单类码不入库
- **WHEN** 收到 `classCode=LTLTCC`（照明回路）或 `classCode=GeneralZone`（空间）的孪生帧
- **THEN** 系统既不建档也不补建，该帧被丢弃

#### Scenario: 终态补建同样受白名单约束
- **WHEN** 收到 `classCode=LTLTCC`、`result=4` 的终态帧且库中无对应行
- **THEN** 系统 MUST NOT 补建（避免把本应丢弃的类码补进来）

### Requirement: 建档帧 per-traceId 锁内查重建档

系统 SHALL 在 per-`(traceId, objectId, infoCode)` 分布式锁（`RedisKey.buildRunningLogTwinLockKey`）内处理 `result=0` 建档帧：先用 `existsByTraceId`（查所有状态）查重，不存在才 `save`，已存在则跳过。

#### Scenario: 首次建档
- **WHEN** 某 `(traceId, objectId, infoCode)` 首次收到建档帧且库中无行
- **THEN** 系统在锁内 `save` 一行 `state=0`

#### Scenario: 重复建档帧跳过
- **WHEN** 同一 `(traceId, objectId, infoCode)` 再次收到 `result=0` 建档帧（设备多帧推送 / 重复消费）
- **THEN** `existsByTraceId` 为真，系统跳过，MUST NOT 产生第二行

### Requirement: 终态帧锁内三态幂等 Upsert

系统 SHALL 对 `result≠0` 且 `RunningLogSourceStateEnum.resolve(result)` 解析为非 `null`、非 `UNKNOWN` 的终态帧，在与建档路径**同一把** per-`(traceId, objectId, infoCode)` 锁内按三态决策处理：`getIdByUnknown`（`state∈{-1,0}`）命中则更新为终态；未命中且 `existsByTraceId`（全状态）为真则跳过冻结；未命中且不存在则按终态补建一行。终态帧 MUST NOT 再因 `getIdByUnknown` 返回 `null` 而被静默丢弃。

#### Scenario: 建档先到，终态更新
- **WHEN** 库中已存在 `state=0` 行，随后收到同指令 `result=1` 终态帧
- **THEN** `getIdByUnknown` 命中，系统将该行更新为 `state=1`

#### Scenario: 终态先到，补建终态行
- **WHEN** 终态帧早于建档 INSERT 提交执行，`getIdByUnknown` 返回 `null` 且 `existsByTraceId` 为假
- **THEN** 系统按终态补建一行（如 `state=1`），MUST NOT 丢弃该终态

#### Scenario: 只有终态帧、无建档帧
- **WHEN** 某指令仅推送 `result=4` 终态帧、从无 `result=0` 建档帧
- **THEN** 系统补建一行 `state=4`

#### Scenario: 已终态，迟到终态跳过
- **WHEN** 库中该行已为终态，又收到同指令的另一终态帧
- **THEN** `getIdByUnknown` 返回 `null` 且 `existsByTraceId` 为真，系统跳过冻结

### Requirement: 状态单调不回退

系统 SHALL 保证 `state` 只能从非终态 `{-1, 0}` 单向流转到终态 `{1,2,3,4,5}`。`updateStateById` MUST 为条件更新（`WHERE state IN (-1, 0)`）；已终态行 MUST NOT 被任何后续帧覆盖，包括迟到的 `result=0` 建档帧与后到的其他终态帧（首个终态冻结）。

#### Scenario: 迟到 result=0 建档帧不覆盖终态
- **WHEN** 某行已为 `state=1`，随后迟到的 `result=0` 建档帧到达
- **THEN** 建档路径 `existsByTraceId` 为真而跳过，该行保持 `state=1`，MUST NOT 回退为 `0`

#### Scenario: 首个终态冻结
- **WHEN** 某行已为 `state=4`，随后收到 `result=5` 终态帧
- **THEN** 系统跳过，该行保持 `state=4`

#### Scenario: 非终态可被终态更新
- **WHEN** 某行为 `state=0` 或 `state=-1`，收到 `result=3` 终态帧
- **THEN** 条件更新命中，该行更新为 `state=3`

### Requirement: content 快照与 state 解耦

系统 SHALL 保持既有不变量：`updateStateById` 只更新 `state` 一列，MUST NOT 回写 `content`；`content` 永久保留建档（或终态补建）时刻的报文快照。因此 `content.result` 与外层 `state` 不一致（如 `content.result=0` 而 `state=3`）是设计使然。

#### Scenario: 终态更新不改 content
- **WHEN** 对 `state=0` 行执行终态更新为 `state=3`
- **THEN** 该行 `content` 保持建档时的 `result=0` 快照不变

#### Scenario: 终态补建 content 为该终态帧快照
- **WHEN** 系统按 `result=4` 终态帧补建一行
- **THEN** 该行 `content` 为该终态帧报文快照，`state=4`

### Requirement: 建档/补建/终态更新后的下游同步与播报

系统 SHALL 在成功建档、终态补建、终态更新后各触发一次 Kafka 同步与 SSE 播报；对跳过分支（已存在建档、已终态冻结、非白名单丢弃）MUST NOT 触发任何下游同步或播报。

#### Scenario: 终态更新后同步并播报
- **WHEN** 终态帧成功将某行更新为终态
- **THEN** 系统触发一次 Kafka 同步与一次 SSE 播报

#### Scenario: 终态补建后同步并播报
- **WHEN** 终态帧触发补建一行
- **THEN** 系统触发一次 Kafka 同步与一次 SSE 播报

#### Scenario: 跳过时不触发下游
- **WHEN** 终态帧因该行已终态而跳过
- **THEN** 系统 MUST NOT 触发 Kafka 同步或 SSE 播报

