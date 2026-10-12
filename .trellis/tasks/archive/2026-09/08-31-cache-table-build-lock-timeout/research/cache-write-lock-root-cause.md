# Cache 共享对象行锁等待根因研究

## Incident Evidence

- 现场：集团 `TYXN`、项目 `Pj9909990007`、类型 `VENTILATION`。
- 异常：`CacheObjectMapper.upsertBatch` 执行 `cache_object` 多值 `INSERT ... ON DUPLICATE KEY UPDATE` 时发生 MySQL `Lock wait timeout exceeded`，Spring 转换为 `CannotAcquireLockException`。
- 失败由 `CacheRwdDomainServiceImpl.buildSingleType` 记录为“物理世界缓存-类型构建异常”。

## Confirmed Code Facts

1. `CacheRwdDomainServiceImpl.buildSingleType` 使用项目 + `RunningMonitorType` 的计算锁，不同类型不互斥：
   `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/CacheRwdDomainServiceImpl.java:234`。
2. `CacheObjectModelWriteServiceImpl` 在提交 `855ae01068` 后移除了项目级写锁，当前直接进入本地事务：
   `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/CacheObjectModelWriteServiceImpl.java:120`。
3. 通风、末端、安防和机房安防均把空间 Key 放在设备 Key 前，并通过同一复合写入口提交；不同类型的空间 ID 可以重叠：
   - `CacheRwdVentilationStrategy.java:94`
   - `CacheRwdAirTerminalStrategy.java:143`
   - `CacheRwdSecurityStrategy.java:172`
   - `CacheRwdComputerRoomSecurityStrategy.java:221`
4. `cache_object.id` 与 `cache_object_model_view.id` 是主键，主表行跨缓存 Key、跨类型共享；UPSERT 幂等不等于无锁竞争。
5. 复合写在一个 `TransactionTemplate` 中执行对象 UPSERT、模型视图 UPSERT、Key 关联和关系替换，行锁保持到整个事务提交或回滚：
   `CacheObjectModelWriteServiceImpl.java:182-197`。
6. `LightingCacheObjectRelationService.writeSnapshot` 直接 upsert 对象与模型视图，但该动作仅作为 `CacheWriteAction` 在对象模型复合写事务内执行：
   `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/LightingCacheObjectRelationService.java:229`。
7. `CacheBatchExecutor` 注释与后端规范约定每批 1000，当前常量为 2000：
   `common/src/main/java/com/persagy/apccontrol/basic/resource/cache/support/CacheBatchExecutor.java:22-30`。
8. 根项目已依赖 Spring Retry；现有业务代码使用最多 3 次尝试和 5 秒起步的指数退避：
   `common/src/main/java/com/persagy/apccontrol/basic/structure/lighitng/listener/LightingDtxPublishChangedListener.java:78`。
9. 当前 `RedissonDistributedLockerService.executeWithLock` 将锁等待超时和线程中断都转换为普通 `BusinessException`，需要结构化区分后才能只重试锁获取超时：
   `common/src/main/java/com/persagy/apccontrol/basic/service/cache/impl/RedissonDistributedLockerServiceImpl.java:79`。

## Corrected Assumption

归档任务 `08-26-cache-lock-optimize/design.md:85-96` 认为：

- `cache_object` / `cache_object_model_view` 使用 UPSERT，因此并发风险低；
- 不同 Key 之间没有共享数据，因此无需项目级互斥。

第二条与当前数据模型不符。对象主表按物理对象 ID 共享，同一空间可同时属于通风、末端、安防等 Key。并发事务更新同一主键时，MySQL 必须加排他行锁；一个大事务持锁期间，另一个事务只能等待并可能超时。

## Required Contracts

- 类型计算和远程 RWD 查询继续并行，只把本地共享对象行提交按项目串行化。
- 保存锁必须在 `TransactionTemplate` 开始前取得，并在 commit/rollback 完成后释放。
- 固定锁顺序：触发/类型计算或模板类型锁 -> 项目对象模型保存锁 -> 数据库事务；不得反向获取。
- 单次锁等待必须有界；只允许对明确的对象模型保存锁获取超时执行最多 3 次获取，初次失败后的退避为 5 秒、10 秒。
- 锁获取 Retry 与事务动作必须分离；获取成功后 Mapper、事务和 `CacheWriteAction` 只能执行一次。
- 中断或 Redis 异常时不得继续重试或降级为无锁写库；获取/退避中断必须恢复线程中断标记。
- SQL、连接、事务和 `CannotAcquireLockException` 保留原异常上抛，首版不做数据库异常自动重试。
- 失败事务不得写 READY；已有 READY 不降级。
- 缓存 Mapper 多值写入恢复为每批最多 1000 条，并覆盖 0、1、1000、1001 条边界测试。
- 不增加 DDL、唯一约束或 `innodb_lock_wait_timeout`，不把 RWD/Feign/JSON 投影放进保存锁。

## Validation Focus

- 同项目重叠对象 ID 的两个保存临界区不能重叠，不同项目可以并行。
- 投影在锁外，事务 begin/commit/rollback 在锁内，锁释放晚于事务完成。
- 前两次锁获取超时、第三次成功时 Mapper 和事务只执行一次；3 次耗尽时 Mapper 零调用。
- 中断、Redis、SQL 和事务异常不进入重试；Mapper 失败时完整回滚并保留原异常。
- 全仓库 `upsertObjects` / `upsertModelViews` 生产调用方均受共享对象保存锁覆盖。
- 发布后观察 `failureType=CannotAcquireLockException`、`CacheObjectMapper.upsertBatch`、新保存锁超时和类型构建耗时。
