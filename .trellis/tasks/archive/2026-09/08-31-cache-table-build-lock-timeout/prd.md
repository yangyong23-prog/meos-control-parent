# 修复 Cache 表缓存构建偶发锁等待超时

## Goal

消除 Cache 对象模型构建过程中因跨入口、跨运行监测类型并发写入共享对象行而产生的 MySQL 锁等待超时，同时通过有上限的保存锁获取重试吸收偶发排队，保留远程查询和数据投影阶段的并发能力，保证构建事务、缓存数据和 READY 状态一致。

## Background

- 现场日志显示：集团 `TYXN`、项目 `Pj9909990007` 构建 `VENTILATION` 物理世界缓存时，`CacheObjectMapper.upsertBatch` 执行 `cache_object` 的批量 `INSERT ... ON DUPLICATE KEY UPDATE`，触发 `Lock wait timeout exceeded`，最终转换为 `CannotAcquireLockException`。
- `CacheRwdDomainServiceImpl.buildSingleType` 只按 `RunningMonitorType` 获取计算锁（`common/.../CacheRwdDomainServiceImpl.java:234`），不同类型仍可并发进入保存阶段。
- `cache_object` 和 `cache_object_model_view` 以对象 ID 为主键，是多个缓存 Key、多个运行监测类型共享的对象表；通风、末端、安防和机房安防均可能写入相同空间对象。
- `CacheObjectModelWriteServiceImpl` 当前认为调用方锁已足够，自身不再持有项目级写锁（`common/.../CacheObjectModelWriteServiceImpl.java:125`）；该假设来自任务 `08-26-cache-lock-optimize` 中“不同 Key 之间无共享数据”的判断，但与共享对象表的实际模型不符。
- 同一提交 `855ae01068` 将 `CacheBatchExecutor` 的批次从 1000 提升到 2000；当前代码注释、后端规范和测试契约仍约定 1000，较大批次会扩大单条 SQL 和事务持锁窗口。
- 对象模型写入重试必须只包围保存锁获取，不能包围事务动作；实现保持轻量，不引入 Spring Retry 或锁句柄抽象。

## Requirements

- **R1 保存互斥**：所有会新增、更新或删除 `cache_object` / `cache_object_model_view` 共享对象行的对象模型保存入口，必须按项目串行化本地保存阶段；不同项目互不阻塞。
- **R2 锁与事务时序**：远程 RWD 查询、关系查询、参数校验和对象投影必须在锁外完成；保存锁必须在数据库事务开始前取得，并在事务提交或回滚完成后释放。
- **R3 锁顺序**：已有触发锁、类型计算锁和 `CacheRebuild.byType` 保存锁语义保持不变；对象模型保存锁只能作为更内层的本地提交锁，禁止从对象模型保存锁内反向获取类型锁。
- **R4 批量边界**：缓存 Mapper 多值写入恢复并保持每批最多 1000 条，空集合不执行 SQL，输入顺序和完整事务回滚语义不变。
- **R5 锁获取重试**：单次保存锁获取最多等待 120 秒；仅在明确的保存锁获取超时时进行有上限重试，最多 3 次获取（初次加 2 次重试），重试前分别退避 5 秒、10 秒。重试期间尚未开启数据库事务，锁获取成功后事务动作只执行一次。
- **R6 异常语义**：线程中断必须恢复中断标记并立即失败；Redis 运行异常、SQL、连接、事务、数据校验和 `CannotAcquireLockException` 保留原异常向上抛出且不得重试。3 次锁获取全部超时后明确失败，不执行 Mapper，不写 READY。
- **R7 一致性**：任一保存步骤失败时，对象、模型视图、Key 关联、关系和同事务扩展动作全部回滚；失败构建不得写入新的 READY，已有 READY 不降级。
- **R8 可观测性**：日志可区分对象模型保存锁单次超时重试、最终耗尽和数据库锁等待超时，并保留含项目 ID 的锁 Key、尝试次数和退避时间，便于上线后确认修复效果。

## Acceptance Criteria

- [ ] **AC1 / R1**：同一项目的两个不同运行监测类型或模板/全量入口并发保存重叠对象 ID 时，本地保存临界区不重叠，不再以 MySQL 行锁超时作为并发仲裁；不同项目可并行保存。
- [ ] **AC2 / R2**：测试证明 RWD 查询和对象投影发生在保存锁外，事务 begin/commit/rollback 全部发生在锁持有期间，锁释放晚于事务完成。
- [ ] **AC3 / R3**：锁顺序固定为“触发/类型计算或模板类型锁 -> 项目对象模型保存锁 -> 数据库事务”，代码中不存在反向获取路径。
- [ ] **AC4 / R4**：0、1、1000、1001 条数据分别触发 0、1、1、2 次 Mapper 调用，单批不超过 1000 条且保持输入顺序。
- [ ] **AC5 / R5**：前两次保存锁获取超时、第三次成功时，事务和 Mapper 仅执行一次；3 次均超时时 Mapper 零调用。测试验证单次等待 120 秒、5 秒/10 秒退避和最多 3 次获取边界。
- [ ] **AC6 / R6**：线程中断、Redis 运行异常和数据库异常均不重试；中断标记被恢复，原始数据库异常完整上抛，未添加无限或无差别重试。
- [ ] **AC7 / R7**：并发、事务回滚和后续分片失败测试通过；失败场景不执行 `markReady`，已有缓存读取行为不变。
- [ ] **AC8 / R8**：测试/预发并发触发通风与至少一个同样写空间对象的类型，日志中不再出现 `CacheObjectMapper.upsertBatch` 的 `CannotAcquireLockException`；日志能区分单次锁超时重试与 3 次耗尽。
- [ ] **AC9**：`common` 相关单元测试、缓存对象模型 MySQL 集成测试及构建回归测试通过。

## Constraints

- 不新增数据库表、索引、唯一约束或 Flyway 迁移。
- 不把 RWD/Feign 查询、JSON 投影或完整缓存计算放入项目级保存锁。
- 不改变缓存 Key、对象 ID、READY 判定、云边表结构或读取路径。
- 不能通过捕获并吞掉数据库异常、提高 `innodb_lock_wait_timeout`、重试数据库事务或无限重试掩盖并发缺口。

## Out of Scope

- 重新统一 DTX、XXL-JOB、调试接口和手动构建的触发锁。
- 重构无关缓存表、通用数据访问层或缓存读取业务。
- 对未观察到数据损坏的环境执行存量数据清理。
- 本规划阶段修改业务代码、运行数据库变更或启动 Trellis 实现阶段。
