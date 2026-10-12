# 修复缓存模板重建僵尸锁与中断锁释放缺陷

## Goal

修复 JTZGZDHCS5 项目中 `LOCK_CACHE_cacheRebuild::Pj9909990000::LIGHTING` 僵尸分布式锁导致照明模板缓存键（DETAIL/TABLE_LIST 共 7 条）连续多轮无法更新的问题，并防止同类问题复发。

## 背景（实测证据）

- 9/2 18:17:41：`.24345` 轮构建线程在持 `cacheRebuild::LIGHTING` 锁执行模板重建时被 `BaseEvent.executeWithLock` 抢占机制 interrupt，抛出 `InvalidDataAccessApiUsageException: InterruptedException`（HikariCP SQL interrupt）。
- 9/2 18:52 / 20:50 / 22:51 及后续每轮：LIGHTING 段 `CacheRwdInfoCodeConfigStrategy` 等同一把锁 30 分钟（= `CacheRebuild.byType` 过期时间 1800s 被当作 waitSeconds）后抛 `BusinessException: 获取分布式锁超时`，7 条模板键从此冻结（最后成功写入 16:46:06）。
- 同一轮中 `CacheRwdLightingStrategy`（写 5 个 listEquipment Key，77 条监控项）正常，因不使用该锁。

## Requirements

1. **模板重建改为“锁外准备 + 锁内短写”**（核心）：`CacheInfoCodeTemplateRepositoryImpl.rebuildByParam` 把远程源查询与对象准备（三方依赖，可能数小时）移出 `CacheRebuild` 锁，锁内只保留本地 DB 替换（旧键复核、键替换、stale 清理、元数据同步）；`CacheTemplateRebuildService` 拆出 `prepareTemplates` / `executePreparedTemplates` 两段入口。锁内复核元数据键集合与锁外快照一致，并发 add/del 导致不一致时放弃本轮（warn，不抛异常）。
2. **中断后锁释放可靠性修复**：`RedissonDistributedLockerServiceImpl.executeWithLock` finally 必须保证被 interrupt 的持锁线程能释放锁：解锁前清除中断标志（只在持锁路径），unlock 包 try/catch 并打 error（当前 unlock 抛异常会覆盖原异常且锁残留无感知）；中断状态用局部变量显式记录并恢复（日志调用会消费中断标志，不能依赖探测值）。
3. **抢占语义保留**：`BaseEvent.executeWithLock` 的抢占中断设计不变（DTX 触发需要严格互斥），只修复被抢占线程的退出清理。
4. 模板重建失败路径已有日志覆盖（`物理世界缓存-类型构建异常`），不新增告警通道；本次不改变 READY 提交/回退语义。

## Acceptance Criteria

- [x] 等锁时间统一由 `CacheLockKey.CacheRebuild.byType` 的 expire 承载（30 分钟，覆盖百万数据本地写排队；用户拍板），`executeWithProjectLock` 取 `lockKey.getExpire().getSeconds()`，调用方不单独定义；等锁时间不影响持锁租期（两参 tryLock + 看门狗续期保留）
- [x] `executeWithLock` finally 在中断标志下仍能可靠 unlock；unlock 异常有 error 日志（含 lockKey），不覆盖原异常；中断标志恢复语义由单测验证（`shouldRestoreInterruptAndStopRetry` / 新增 `shouldReleaseLockWhenActionInterrupted`）
- [x] 编译通过（common 模块），既有单元测试全部通过（194 个，含新增用例）；模板重建单测适配两段式语义，新增“准备期间键集合变化放弃写入”用例
- [x] 代码评审确认无其他 `executeWithLock`/`executeWithProjectLock` 调用方受等锁时间变更影响（template add/rebuildByParam 为仅有两个入口，共用同一 Key）
- [x] 修复说明更新 `.trellis/spec/backend/cache-object-model.md` 锁语义章节（模板重建锁边界 + executeWithLock 释放可靠性）

## Notes

- 运营止血（DEL 僵尸锁 key）已由测试/运维侧执行，不属于代码改动范围。
- 修改后需在 qa 环境观察一轮 DTX：7 条模板键 update_time 应恢复刷新。
