# Cache 表缓存构建锁等待超时修复设计

## 1. 根因

`cache_object.id` 和 `cache_object_model_view.id` 是对象唯一主键，但对象行会被多个缓存 Key、
多个运行监测类型共同写入。现有构建锁按 `RunningMonitorType` 隔离，因此同一项目的不同类型可以
同时进入对象 UPSERT。`ON DUPLICATE KEY UPDATE` 只定义主键冲突后的写法，不能避免两个事务同时
更新同一对象行时等待 MySQL 行锁。

`CacheBatchExecutor` 的实现批次为 2000，但注释、规范和测试均约定 1000。较大的单条 SQL 还会
扩大事务持锁窗口。

## 2. 最终方案

采用最小范围修改：

1. 新增 `CacheLockKey.CacheObjectModelWrite.cache()`，Key 包含当前项目 ID，单次等待 120 秒。
2. 四个对象表写入口在数据库事务外获取同一项目锁：
   - `replaceModelCacheKey`
   - `replaceModelCacheKeys`
   - `replaceModelCacheKeysAndRelations`
   - `replaceObjectClassCodes`
3. 仅当 `RLock.tryLock` 返回 `false` 时重试，最多获取 3 次，重试前等待 5 秒、10 秒。
4. 获取锁成功后只执行一次原有 `TransactionTemplate`，提交或回滚后再释放锁。
5. `CacheBatchExecutor.BATCH_SIZE` 恢复为 1000。

不引入 Spring Retry、锁句柄、专用超时异常、兼容构造器，也不改模板、关系、Key 删除或各类型策略。

## 3. 时序

```text
RWD / Feign 查询、参数校验、对象投影
  -> 获取 CacheObjectModelWrite(project)；超时才按 5s / 10s 重试
       -> 开启一次数据库事务
       -> cache_object / cache_object_model_view / 关联及扩展动作
       -> commit 或 rollback
  -> 释放项目锁
```

调用方已有的触发锁、类型计算锁或模板锁仍在外层。固定顺序为：

```text
业务范围锁 -> 项目对象模型保存锁 -> 数据库事务
```

## 4. 异常边界

| 场景 | 行为 |
| --- | --- |
| 前两次未获取、第三次成功 | 事务与 Mapper 只执行一次 |
| 三次均未获取 | 抛 `BusinessException`，不执行事务或 Mapper |
| Redis 运行异常 | 原异常立即上抛，不重试 |
| 获取锁或退避期间中断 | 恢复中断标记并立即失败 |
| SQL、连接、事务或写动作异常 | 原异常上抛，事务回滚，不重试 |

## 5. 为什么能解决

数据库报错发生在同一项目的共享对象行被不同类型事务并发更新时。新增锁把数据库事务前的并发
竞争改成按项目排队，MySQL 不再承担同项目对象写入的并发仲裁。有限重试只吸收 Redis 锁等待超时，
不会重复已经开始的数据库事务。不同项目使用不同 Key，仍可并行。

该方案不能保护不经过四个写入口的外部数据库写入，也不能让滚动发布中的旧实例遵守新锁；并发验证
应在全部实例升级后执行。

## 6. 验证

- 四个写入口使用同一项目锁，等待参数为 120 秒，退避为 5 秒、10 秒。
- 前两次超时、第三次成功时动作一次；三次耗尽时动作零次。
- Redis 异常、中断和动作异常不重试。
- 事务开始晚于锁获取，提交或回滚早于锁释放。
- 批次 0、1、1000、1001 分别产生 0、1、1、2 次回调。
- JDK 8 编译和 `common` 纯单元测试通过；真实 MySQL 并发验证留给隔离环境。
