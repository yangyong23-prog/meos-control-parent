## Why

孪生自动运行日志（`source_type=TWIN_COMMAND`、`user=none`）线上表现为"全是 `state=0` 未完成、看不到任何执行成功/失败/超时的终态记录"。

根因是**建档帧与终态帧的 `@Async` 乱序竞态，且已合并的去重锁只覆盖了一半路径**：

- 建档帧（`result=0`）走 `twinMsgLog`，INSERT 前压了一次重的设备归类远程查询（`setCategory → listEquipments`）；
- 终态帧（`result≠0`）走 `updateByTwinMsg`，只是一条裸 SELECT（`getIdByUnknown`，仅匹配 `state∈{-1,0}`）；
- 两帧入口 `RunningLogHandle.handleLog` 带 `@Async`（edge 线程池 core=5/max=10），实测建档→终态中位间隔仅 ~30ms，终态几乎总是早于建档 INSERT 提交执行 → `getIdByUnknown` 返回 `null` → `updateByTwinMsg` 对 `id==null` **静默丢弃终态**，行永久停在 `state=0`。
- 另有部分指令**只有终态帧、没有 `result=0` 建档帧**，同样无行可更新而整条丢失。

已合并的 `fix_日志重复生成问题`（commit `d0ef021e87`）只关闭了"重复建档(Bug A)"，未触及终态丢失(Bug B)——锁只保证互斥、不保证顺序。

## What Changes

- **终态回写纳入同一把锁**：`updateByTwinMsg` 的"查 + 写"整段进入与建档路径相同的 per-`(traceId,objectId,infoCode)` 分布式锁（复用 `RedisKey.buildRunningLogTwinLockKey` + `RedissonDistributedLockerService.executeWithLock`，其契约保证锁晚于事务提交释放）。
- **锁内三态幂等 Upsert**：
  - `getIdByUnknown` 命中非终态行 → 更新为终态；
  - 未命中且 `existsByTraceId`（全状态）为真 → 已终态，**跳过冻结**（防覆盖、防"完成→未完成"回退）；
  - 未命中且不存在 → **按终态补建**（复用建档白名单与归类逻辑）。
- **状态单调守卫**：`updateStateById` 收紧为条件更新（`WHERE state IN(-1,0)`），保证 `-1/0 → 终态` 单向流转，终态之间不互相覆盖。
- **抽取公共建档逻辑**：把"build RunningLog + 类码/信息点白名单判定"从 `twinMsgLog` 抽出，供建档帧与终态补建两分支共用，避免 `LTLTCC`(照明)、`GeneralZone`(空间) 等非白名单类码被误补建。
- **前向修复**：不处理已产生的历史 `state=0` 脏数据；因不清历史重复，本次**不加** `(trace_id,object_id,info_code)` 唯一索引兜底（列为后续可选项）。

**Non-goals**：不改旧链路 `equipSwitchSetHandler`（其 `dataStatus==2` 口径不同、天然不重复建档）；不改 BDTP 路径 `saveByBdtpMsg`（同类风险可后续套用）；不处理同分支另一独立缺陷"实例名称变更"（`objectName` 建档快照 + `OBJECT_DETAIL_MAP` 只 put 不失效）。

## Capabilities

### New Capabilities

- `twin-running-log-lifecycle`: 孪生自控指令运行日志（`TWIN_COMMAND`）的建档 / 终态回写生命周期与状态单调性契约——同一指令在任意帧到达顺序（建档先到 / 终态先到 / 只有其一 / 迟到 `result=0`）下，都收敛为**单行、终态不丢失、状态不回退、非白名单不入库**。

### Modified Capabilities

<!-- 无。现有 wsdh-running-log 只覆盖分类注册，不涉及建档/终态生命周期持久化契约。 -->

## Impact

**影响的代码**：

- `local/meos-control-common-edge/.../common/service/impl/RunningLogEdgeNowServiceImpl.java`
  - `updateByTwinMsg()`：进锁 + 三态 upsert
  - `twinMsgLog()`：抽出公共 build+白名单判定
- `common/.../basic/resource/impl/RunningLogServiceResourceImpl.java`
  - `getIdByUnknown()` / `existsByTraceId()`（复用）/ `updateStateById()`（改为条件更新）
- 复用：`RedisKey.buildRunningLogTwinLockKey`、`RedissonDistributedLockerService.executeWithLock`

**影响的系统**：边缘计算 / 公共服务模块；`running_log` 表写入路径；下游 Kafka 同步与 SSE 播报（补建/更新后各触发一次）。

**性能影响**：终态路径新增一次锁获取 + 一次 `existsByTraceId` 存在性查询（per-traceId 持锁极短）；补建分支仅在"无行"时触发，复用重的 `setCategory` 远程查询。

**风险评估**：中低。并发正确性依赖 Redis 锁可用性；锁服务异常时退化为当前行为（不新增回归，但也不修复）。行为变化：终态不再丢失，历史"全 0"观感将变为正确的终态分布（成功/失败/超时/终止）。
