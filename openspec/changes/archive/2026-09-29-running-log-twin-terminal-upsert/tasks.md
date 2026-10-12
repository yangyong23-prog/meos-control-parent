## 1. 前置验证 spike（量化竞态，推荐先做）

- [x] 1.1 在 `RunningLogEdgeNowServiceImpl.updateByTwinMsg` 的 `id==null` 分支加临时计数/日志，量化终态被静默丢弃的规模
- [x] 1.2 采集一段现网孪生报文，统计建档帧(result=0)与终态帧(result≠0)的到达顺序与间隔，确认"终态早到"竞态假设

## 2. 抽取公共建档逻辑（common-edge 模块，对应 D4）

- [x] 2.1 在 `RunningLogEdgeNowServiceImpl` 抽出「白名单判定」方法：`twinClassCodeInfoCodes.get(classCode)` 含 infoCode，或 `MdQueryObjStrategy.classList` / `VentilationQueryObjStrategy.classList` 含 classCode
- [x] 2.2 抽出「从 `FeedbackInfoMsgMqttVO` 构建 `RunningLog`」方法：含 `setCategory`、`content` 快照、traceId/objectId/infoCode/sourceType，`state` 参数化（建档=0，补建=`resolve(result)`）
- [x] 2.3 `twinMsgLog` 改用 2.1/2.2 抽取方法，保持既有建档行为与 Bug A 去重不变（回归保护）

## 3. 终态路径改造：进锁 + 三态 Upsert（common-edge 模块，对应 D1/D2）

- [x] 3.1 `updateByTwinMsg` 保留前置校验：`type==1`、`twinMsgLog` 短路、`traceData` 合法、`resolve(result)` 非 null 且非 UNKNOWN
- [x] 3.2 计算 `lockKey = RedisKey.buildRunningLogTwinLockKey(traceId, objectId, infoCode)`，与建档路径使用**同一把**锁
- [x] 3.3 锁**外**完成补建所需的 `setCategory` 重远程查询；锁**内**只做「双查 + 写」，保持持锁极短
- [x] 3.4 锁内三态决策：`getIdByUnknown` 命中 → `updateStateById(终态)`；未命中且 `existsByTraceId`(全状态) 为真 → 跳过冻结；未命中且不存在 → 按终态补建 `save`
- [x] 3.5 补建分支复用 2.1/2.2 的白名单 + build，`state=resolve(result)`，确保 `LTLTCC`/`GeneralZone` 不被误补建

## 4. DB 层单调守卫（common 模块，对应 D3）

- [x] 4.1 `RunningLogServiceResourceImpl.updateStateById` 改为条件更新：`WHERE id=? AND state IN(-1,0)`
- [x] 4.2 返回受影响行数，调用方据此判断是否真正更新（0 行 = 已终态/被冻结 → 不触发下游）
- [x] 4.3 确认 `getIdByUnknown`（`{-1,0}` + `LIMIT_ONE`）与 `existsByTraceId`（全状态）语义不变

## 5. 下游同步与播报（common-edge 模块，对应 spec Req6）

- [x] 5.1 终态**更新**成功后触发一次 `sendLogDataToKafkaUpdate` + `ssePush`
- [x] 5.2 终态**补建**成功后触发一次 `sendLogDataToKafka` + `ssePush`
- [x] 5.3 跳过分支（已存在建档 / 已终态冻结 / 非白名单丢弃）MUST NOT 触发任何下游同步或播报

## 6. 测试验证（覆盖 spec 场景）

> 按用户决策：**本次不写自动化测试**。核心三态逻辑位于 `local/meos-control-common-edge`，该模块无测试脚手架；
> 以下 6.1-6.8 转为 **staging 真实 MQTT 报文人工验证清单**（结合内置 `[twin-...]` 日志观测），不在本次代码变更范围内实现自动化断言。

- [ ] 6.1 [staging 人工] 建档先到 → 终态更新（`state 0→1`）
- [ ] 6.2 [staging 人工] 终态先到 → 补建终态行（无 `state=0` 残留、无重复行）
- [ ] 6.3 [staging 人工] 只有终态帧、无建档帧 → 补建成功
- [ ] 6.4 [staging 人工] 迟到 `result=0` 建档帧不覆盖终态（不回退）
- [ ] 6.5 [staging 人工] 首个终态冻结，后续终态跳过
- [ ] 6.6 [staging 人工] 非白名单类码（`LTLTCC`/`GeneralZone`）建档与补建均被拒
- [ ] 6.7 [staging 人工] `content` 快照与 `state` 解耦：终态更新不回写 `content`
- [ ] 6.8 [staging 人工] 并发：同 `traceId` 建档帧与终态帧并发提交，最终**单行且为终态**
- [x] 6.9 编译验证 `mvn -pl local/meos-control-common-edge -am compile` BUILD SUCCESS；完整 `mvn test` 需 DB/Redis 集成环境，按用户决策随测试一并延迟到 staging

## 7. 收尾与后续评估

- [x] 7.1 移除或降级 1.1 的临时计数日志（可转为 DEBUG 常态埋点）
- [x] 7.2 `openspec validate running-log-twin-terminal-upsert` 通过，文档与实现一致
- [x] 7.3 Open Questions 已定：**终态多帧 = first-wins 冻结**（保持当前实现，无需改代码）；`(trace_id,object_id,info_code)` 唯一索引兜底 与 BDTP `saveByBdtpMsg` 同类风险 **保持 Non-goal**，不纳入本次范围
