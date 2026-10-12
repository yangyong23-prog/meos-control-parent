# Cache 表缓存构建锁等待超时实施记录

## 1. 实施清单

- [x] 确认 `cache_object`、`cache_object_model_view` 均以 `id` 为主键，现有 UPSERT 无需修改 SQL。
- [x] 新增按项目隔离的 `CacheObjectModelWrite` 锁，120 秒等待值直接写在 `CacheLockKey` 构造中。
- [x] 为锁服务增加退避参数重载，只在 `tryLock` 返回 `false` 时按参数重试。
- [x] 四个对象表写入口统一在项目锁内执行一次原有短事务。
- [x] 设置最多 3 次获取，退避 5 秒、10 秒；Redis、线程中断和动作异常不重试。
- [x] 将 `CacheBatchExecutor.BATCH_SIZE` 从 2000 恢复为 1000。
- [x] 补充锁服务和对象模型写服务单元测试。
- [x] 更新缓存对象模型规范。

## 2. 最终改动范围

生产代码：

- `CacheLockKey.java`
- `CacheBatchExecutor.java`
- `RedissonDistributedLockerService.java`
- `RedissonDistributedLockerServiceImpl.java`
- `CacheObjectModelWriteServiceImpl.java`

测试：

- `RedissonDistributedLockerServiceImplTest.java`
- `CacheObjectModelWriteServiceImplTest.java`
- `CacheBatchExecutorTest.java`

规范：

- `.trellis/spec/backend/cache-object-model.md`

明确排除：模板删除、纯关系或 Key 删除入口、运行监测策略、锁句柄、专用异常、Spring Retry。

## 3. 验证结果

- [x] 锁相关定向测试：20 个通过。
- [x] `common` 全部纯单元测试：183 个通过；锁服务、写服务和批次定向测试 22 个通过。
- [x] MySQL 集成类因未配置 JDBC 执行 0 个用例。
- [x] `CacheBatchExecutorTest`：批次恢复为 1000 后通过。
- [x] JDK 8 根项目 10 个 Reactor 模块编译通过。
- [x] `git diff --check` 通过。
- [ ] 隔离 MySQL 的真实跨实例并发验证未执行；发布后需在全部实例升级完成后验证。

## 4. 上线观察

- 观察 `CacheObjectMapper.upsertBatch` 的 `CannotAcquireLockException` 是否消失。
- 区分保存锁单次超时、等待重试和最终耗尽日志。
- 滚动发布中新旧实例混部时避免主动并发重建同一项目；旧实例不会遵守新锁。
