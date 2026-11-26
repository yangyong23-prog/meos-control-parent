# 去 Redis：`switch.opt.localRedis`

目标：边缘盒子上 meos-control-server-edge 不再依赖 Redis 保存**自己的**数据；和 dtp-meos 共享的键在 dtp-meos 改造前原样留在 Redis。

## 做法

不改业务代码，只在 Redis 连接这一层替换：

| 组件 | 关闭（默认） | 打开 |
|---|---|---|
| `RedisConnectionFactory` | Lettuce/Redisson | `OptLocalRedisConnectionFactory`（@Primary），进程内存 `OptLocalRedisStore` |
| RedisTemplate / RedisCacheUtil / RedisCacheTemplate / `@Cacheable`(RedisCacheManager) / 连接回调 | 走 Redis | 自动落到进程内存，调用方不变 |
| `RedissonClient`（RedissonDistributedLockerService、BaseEvent、SystemGraphDataServiceImpl） | Redisson 分布式锁 | `OptLocalLocks` 进程内锁（可重入、租约到期自动释放、按线程归属） |
| 框架 `@RedisLock`（RedisLockUtil） | paas.lock.* 自建 Redisson | 同一个进程内锁 |

- 一条命令一个原子操作（存储层整体串行，与 Redis 单线程一致）；值按原序列化后的字节保存，读出总是新对象。
- 支持 String / Hash / List / Set 及 KEYS/SCAN（Redis glob）、过期（惰性 + 每 30 秒清理）。ZSet、发布订阅、Lua、事务未实现，调用会抛 `InvalidDataAccessApiUsageException`（当前代码未使用）。
- 数据重启即清空。私有数据都可重建：信息点配置缓存由启动初始化 `LocalCacheInit` 重新加载；对象模型 READY 状态由收敛任务重写（MySQL `rc_basics_config` 里也有）；实时值、日志统计会被新数据覆盖。

## 共享键（`opt.local-redis.shared=true`，默认）

以下前缀（原键或去掉 `meos-control::` 应用前缀后）的读写原样转发到真实 Redis（`spring.redis.*`，按需建连接），字节级与改造前一致：

| 前缀 | 用途 | dtp-meos 侧 |
|---|---|---|
| `apc:automatic:` | 手自动 | 读写 |
| `apc:intervene:`（实际键 `apc:intervene::obj:info`） | 干预，带过期 | 读写 |
| `apc:system_running_model:` | 系统运行模式 | 读写 |
| `apc:obj_infocode_forced:` | 信息点强制 | 删除 |
| `apc:iot_ctrl_data:` | 控制值 | StrategyServiceImpl 读 |
| `apc:iot_real_time_data:` | 实时值共享缓存 | 契约写明为读方，待确认是否已实现 |

- 多键命令（DEL/MGET/MSET/EXISTS）按键拆分后合并；KEYS/SCAN 合并本地结果和真实 Redis 里属于共享前缀的键，Redis 中遗留的旧私有键不会被读回。
- `opt.local-redis.shared-prefixes`：逗号分隔，覆盖上表。
- `opt.local-redis.shared=false`：完全不连 Redis。**只有在 dtp-meos 不再读写上表的键之后才能这样配。**

## 配置

```yaml
switch:
  opt:
    localRedis: true          # 需重启；只允许 -edge 服务，否则启动失败
opt:
  local-redis:
    shared: true              # 默认 true
    # shared-prefixes: apc:automatic:,apc:intervene:,apc:system_running_model:,apc:obj_infocode_forced:,apc:iot_ctrl_data:,apc:iot_real_time_data:
    # sweep-seconds: 30
```

启动日志里有 `[opt-local-redis] 已启用 ...` 三行 WARN。回滚：去掉 `switch.opt.localRedis` 重启。

## 下一步

dtp-meos 改为以 MySQL + 接口持有手自动/干预/运行模式后（已有 `/setManualAutoMatic`、`/relieveIntervene`、`/changeManualAutoStatusByObjectId(s)` 等接口），meos-control 改调接口，再把 `opt.local-redis.shared` 设为 false，盒子上 meos-control 即完全不连 Redis。
