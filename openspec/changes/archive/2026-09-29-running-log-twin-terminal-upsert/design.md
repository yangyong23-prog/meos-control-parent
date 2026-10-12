## Context

孪生自动运行日志（`source_type=TWIN_COMMAND`）有两条写入路径，入口 `RunningLogHandle.handleLog` 带 `@Async`（edge 线程池 `core=5/max=10/queue=50`，见 `bootstrap.yml`），多线程无序并发：

- **建档路径** `RunningLogEdgeNowServiceImpl.twinMsgLog`（L329-396）：闸门只认 `result==UNFINISHED(0)` + 类码/信息点白名单（L342-346）。合并 `d0ef021e87` 后，已在 per-`(traceId,objectId,infoCode)` 锁（`RedisKey.buildRunningLogTwinLockKey`）内做 `existsByTraceId` 查重再 `save`（L383-392）。INSERT 前有重的设备归类远程查询 `setCategory`（L372）。
- **终态路径** `RunningLogEdgeNowServiceImpl.updateByTwinMsg`（L283-320）：`getIdByUnknown`（L307）仅匹配 `state∈{-1,0}` 且 `LIMIT_ONE`；`if(id!=null)` 才更新（L309-318），**`id==null` 时静默丢弃**，且**整段未进锁**。

关键事实（已核实）：
- `RunningLogServiceResourceImpl.getIdByUnknown` 只查 `{-1,0}`；`existsByTraceId` 查所有状态；`updateStateById` 是 `WHERE id` 无条件 `set state`（无单调守卫）。
- `RedissonDistributedLockerService.executeWithLock` 契约：**锁在动作返回后释放，晚于动作内事务提交**——这是本设计提交可见性的基石。
- 建档路径耗时（含远程归类查询）≫ 终态路径（裸 SELECT），实测建档→终态中位间隔 ~30ms，终态几乎总是早到。

**结论**：合并的锁只关闭了重复建档（Bug A），终态早到时 `getIdByUnknown` 返回 `null` → 终态被丢弃 → 行永停 `state=0`（Bug B），表现为"自动日志全是 0、没有成功"。

## Goals / Non-Goals

**Goals:**
- 终态帧在任意到达顺序下都不丢失：建档先到→更新；终态先到/只有终态→补建。
- 同一指令在并发下**只产生一行**，且 `state` **单调不回退**（终态不被迟到 `result=0` 或后到终态覆盖）。
- 非白名单类码（`LTLTCC`/`GeneralZone` 等）在补建分支同样被拒绝。
- 保留既有不变量：`content` 为建档/补建时刻快照，`updateStateById` 不回写 `content`。

**Non-Goals:**
- 不清洗历史 `state=0` 脏数据（纯前向修复）。
- 本次不加 `(trace_id,object_id,info_code)` 唯一索引（需先清历史重复，列为可选兜底）。
- 不改旧链路 `equipSwitchSetHandler`（`dataStatus==2` 口径不同）。
- 不改 BDTP 路径 `saveByBdtpMsg`（可后续套用同法）。
- 不处理同分支另一独立缺陷"实例名称变更"（`objectName` 快照 + `OBJECT_DETAIL_MAP` 只 put 不失效）。

## Decisions

### D1. 采用「三态幂等 Upsert」而非其他方案

`getIdByUnknown==null` 是**二义**的——可能是"已终态"（应跳过）或"从未建档"（应补建）。必须用 `existsByTraceId`（全状态）消歧：

| `getIdByUnknown` | `existsByTraceId` | 语义 | 动作 |
|:---:|:---:|---|---|
| 非 null | — | 有 `-1/0` 行 | 更新为终态 |
| null | true | 已终态 | 跳过（冻结） |
| null | false | 从未建档 | 按终态补建 |

**Alternatives considered:**
- **只在终态帧单点建档（Model B）**：结构上无回退，但丢失"执行中/未完成"中间态，且无终态帧则无日志。因业务仍希望中间态即时可见，未采用。
- **终态重试等待建档**：引入延迟与重试复杂度，且"只有终态帧无建档帧"仍无解。
- **traceId 顺序化线程池**：改动面大、影响其他日志类型吞吐。
- **DB 唯一索引兜底**：一致性最强但需 DDL + 清历史，暂缓。

### D2. 终态路径必须进「同一把」per-traceId 锁

终态补建分支若不进锁，会与建档帧并发出两行（丢失变重复）：

```
危险窗口（终态补建不进锁）：
t0 终态帧B: getIdByUnknown=null, existsByTraceId=false → 补建中(setCategory 慢查询)…未 INSERT
t1 建档帧A: 进锁 → existsByTraceId=false(B未提交) → INSERT state=0 → 提交 → 放锁
t2 终态帧B: INSERT state=终态 → 提交
→ state=0 与 终态 两行并存 → 查询命中 0 → "完成显示成未完成" + 重复
```

进同一把锁后（`executeWithLock` 保证提交后放锁），任意顺序都收敛单行：

```
A 先拿锁: 建 state=0 → 放锁; B 拿锁: getIdByUnknown 命中 → 更新终态   ✓单行
B 先拿锁: 补建终态   → 放锁; A 拿锁: existsByTraceId=true → 跳过       ✓单行
```

**实现约束**：锁内只做"双查 + 写"（`getIdByUnknown` / `existsByTraceId` / `updateStateById` / `save`），重的 `setCategory` 远程查询放**锁外**，与现有建档路径持锁极短的风格一致。

### D3. `updateStateById` 收紧为条件更新（DB 层单调守卫）

改为 `WHERE id=? AND state IN(-1,0)`。这是**独立于锁的第二道防线**：即使 Redis 锁异常导致并发写，DB 层也保证终态不被覆盖、不回退。与 D2 的应用层锁形成纵深防御。

### D4. 抽取公共「build + 白名单」判定

把 `twinMsgLog` 中"从 vo 构建 RunningLog + 白名单判定（`twinClassCodeInfoCodes` / `MdQueryObjStrategy.classList` / `VentilationQueryObjStrategy.classList`）"抽为公共方法，建档帧与终态补建帧共用。避免补建分支绕过白名单，把 `LTLTCC`/`GeneralZone` 误补进来。补建时 `state` 取 `resolve(result)`（终态），建档时取 `0`。

### D5. 首个终态冻结（first-wins）

多个终态帧到达时，首个写入者冻结该行，后续终态跳过。语义简单、与单调守卫天然一致。（若业务需要"优先级覆盖"，见 Open Questions。）

## Risks / Trade-offs

- **[Redis 锁不可用]** → 退化为当前行为（不新增回归）；D3 的 DB 条件更新仍防回退，但不防重复。可接受，因锁不可用属异常态。
- **[补建走重 setCategory 远程查询]** → 仅在"无行"时触发（终态先到/只有终态帧），非全量；查询放锁外，不放大持锁时间。
- **[content 快照非确定]** → 取决于哪帧先到：建档先到则 `content.result=0`、终态补建则 `content.result=终态`。符合 D 中"`content`/`state` 解耦"不变量，渲染字段（keyWord/objectName/infoValue/oldValue）两帧均齐备。
- **[持锁范围含 Kafka/SSE 推送]** → 与现有建档路径一致，per-traceId 持锁极短，已被现网接受。
- **[历史 state=0 脏数据不回溯]** → 明确 Non-goal；部署后新指令即正确，历史行保持现状。

## Migration Plan

1. 纯代码前向修复，无 DDL、无数据迁移。
2. 部署后新到达的孪生指令即按三态 Upsert 收敛；历史 `state=0` 行不受影响。
3. 回滚：还原 `RunningLogEdgeNowServiceImpl` / `RunningLogServiceResourceImpl` 改动即可（无 schema 变更）。
4. 验证建议：落地前先在 `updateByTwinMsg` 的 `id==null` 分支加计数日志做 spike，量化终态丢弃规模；落地后对比 `state` 分布应从"全 0"变为含 `1/2/3/4/5`。

## Open Questions

- 终态多帧是否需要「优先级覆盖」（如 `5 控制异常` 覆盖 `4 强制终止`）？当前定为 first-wins 冻结。
- 是否补做 `(trace_id,object_id,info_code)` 唯一索引兜底？需先确定历史重复数据清理口径。
- BDTP 路径 `saveByBdtpMsg` 是否存在同类终态丢失，是否纳入同一批修复？
