# Cache 锁体系优化 — 技术设计

> 任务：08-26-cache-lock-optimize
> 目标：解决 Cache 锁体系复杂、嵌套超时、线程中断等问题，按"触发→计算→保存"三层职责重组

---

## 1. 设计目标

1. 消除 `CacheRwdBuild` 锁内嵌套 `CacheRebuild` 锁导致的 30s 超时（B0001）
2. 统一锁机制，明确各层语义，降低心智负担
3. 保证同一类型缓存数据"同时成功或同时失败"（事务 + READY 双保险）
4. 不引入新的 TOCTOU 竞态（"先删后插"仍受保护）

---

## 2. 三层锁架构

### 2.1 第一层：触发锁（Trigger Lock）— 抢占式中断

| 属性 | 设计 |
|---|---|
| **机制** | `BaseEvent.executeWithLock`（抢占式中断，保留现有语义） |
| **Key** | 新建 `CacheLockKey.Trigger.cache()` = `LOCK_CACHE_trigger::{projectId}` |
| **语义** | 新触发到来时中断正在执行的旧触发；最新事件状态优先 |
| **覆盖入口** | 见下方清单 |

**入口清单：**

| 入口 | 类 | 改动 |
|---|---|---|
| DTX 发布 | `CacheDtxPublishChangedListener.onDtxPublishEvent` | 锁从 `DtxPublish.cache()` 改为 `Trigger.cache()` |
| DTX 规则发布 | `CacheDtxPublishChangedListener.onDtxRulePublishEvent` | 同上 |
| 需求实例配置变更 | `CacheDtxPublishChangedListener.onDemandInstanceConfigChangeEvent` | 同上 |
| 缓存初始化（xxl-job） | `CacheObjectModelInitTaskHandler.process` | 新增：包 `Trigger.cache()` 锁 |
| 需求配置结果初始化（xxl-job） | `DemandConfigResultInitTaskHandler.process` | 新增：包 `Trigger.cache()` 锁 |
| 调试初始化接口 | `DebugCacheController.initDemandConfigResult` / `initCacheObjectModel` | 新增：包 `Trigger.cache()` 锁 |

**说明**：
- `DtxPublish.lighting` 锁保持不动（照明领域独立，未纳入本次范围）
- `DtxPublish.demandConfigResult` 锁合并进 `Trigger.cache()`（需求配置结果刷新入口已包含在触发层）
- 触发锁只保护"事件发布/任务发起"动作；真正的构建互斥由第二层计算锁负责

### 2.2 第二层：计算锁（Compute Lock）— 非阻塞快速失败

| 属性 | 设计 |
|---|---|
| **机制** | `RedissonDistributedLockerService.tryLock`（非阻塞） |
| **Key** | `MEOS:cacheRwdBuild::{projectId}:{RunningMonitorType}`（现有 CacheRwdBuild 锁） |
| **语义** | 拿不到锁 = 已有构建任务正在执行 → **直接报错** |
| **等待** | 0 秒，租期 -1（watchdog） |

**改动点：**

| 位置 | 现状 | 目标 |
|---|---|---|
| `CacheRwdServiceImpl.triggerBuildCache` | 拿不到锁返回 false（静默） | 拿不到锁**抛业务异常**（明确失败） |
| `CacheRwdDomainServiceImpl.buildSingleType` | 拿不到锁返回 false | 保持返回 false（内部路径，由上层决定跳过语义） |
| `CacheRwdDomainServiceImpl.buildCommonStrategies` | 含模板重建（`rebuildByParam(null)`） | **去掉模板重建**，只保留公共结构（`onlyBuildInCommonPhase=true`） |

**为什么去掉 common 阶段模板重建**：
- `buildCache(null)` 先执行 `buildCommonStrategies`（ALL 锁），再逐类型 `buildSingleType`（类型锁）
- 类型构建内部已包含 `CacheRwdInfoCodeConfigStrategy.buildCacheData(type)` → `rebuildByParam(type)`
- common 阶段的 `rebuildByParam(null)` 是**冗余重建**，且造成 ALL 锁与类型锁的竞争窗口
- 去掉后：模板重建只发生在类型锁内，ALL 锁与类型锁不再对 `cache_info_code_template` 产生竞争

### 2.3 第三层：保存锁（Save Lock）— 细粒度 + 长等待

| 属性 | 设计 |
|---|---|
| **机制** | `RedissonDistributedLockerService.executeWithLock`（可重入、自动释放） |
| **Key** | 细粒度（按类型 / 模板 / 场景） |
| **等待** | 长等待（默认 120s，可配置） |

**保存锁清单：**

| 锁 | Key | 保护表 | 现状 → 目标 |
|---|---|---|---|
| `CacheRebuild.byType(type)` | `LOCK_CACHE_cacheRebuild::{projectId}:{type}` | `cache_info_code_template` | 新增（拆细） |
| `CacheRebuild.cache()` | `LOCK_CACHE_cacheRebuild::{projectId}` | `cache_info_code_template` | 保留（兜底：未知类型 add） |
| 规则写锁 | `APC:cache_rule_write_lock:{projectId}` | `cache_rule` + `cache_rule_key_rel` | 保持现状 |
| 结果写锁 | 模板级 + 场景级 | `demand_config_result` + snapshot | 保持现状 |
| ~~CacheRebuild（项目级）~~ | ~~`LOCK_CACHE_cacheRebuild::{projectId}`~~ | ~~`cache_object_key_rel` / `cache_object_rel`~~ | **去掉**（由计算锁+保存锁覆盖） |

**为什么 `CacheObjectModelWriteServiceImpl` 可以去掉 CacheRebuild 锁**：

`cache_object` / `cache_object_model_view` 是 UPSERT（主键 id 幂等，并发不产生重复）；
`cache_object_key_rel` / `cache_object_rel` 是"先删后插"，其并发写入路径：

| 写入路径 | 保护锁 | Key 范围 |
|---|---|---|
| 策略 `buildSingleType`（如 AIR_TERMINAL_LIST_SPACE） | 计算锁 `CacheRwdBuild(AIR_TERMINAL)` | 固定 Key（类型专属） |
| 配置导入/模板重建 `rebuildByParam` | 保存锁 `CacheRebuild.byType(type)` | 模板 Key（templateTypeKey） |

同一 Key 的所有写入口都已被锁覆盖 → 不会交叉删插 → 无 TOCTOU。
不同 Key 之间无共享数据 → 无需互斥。

---

## 3. 事务设计：保存阶段事务化

### 3.1 原则

```
内部方法：每个保存操作保持短事务（现状，REQUIRED 传播）
外部编排：调用了多个内部保存方法时，外层再包一个事务
         → 内部方法通过 REQUIRED 传播自动加入外层事务
```

### 3.2 事务边界

```
buildCacheData(AIR_TERMINAL)
  ├── ① RWD 查询（远程 Feign 调用）           ← 事务外
  ├── ② 异步关系查询（CompletableFuture）      ← 事务外
  └── ③ 最终保存阶段
        ┌─ 外层事务（TransactionTemplate）──────┐
        │  replaceModelCacheKey(spaceKey)        │
        │  replaceModelCacheKey(equipmentKey)    │
        │  replaceObjectRelations(...)           │
        │  任一失败 → setRollbackOnly → 全部回滚 │
        └────────────────────────────────────────┘
```

### 3.3 实现方式：统一批量保存入口

在 `CacheObjectModelWriteServiceImpl` 新增编排方法（参考现有 `replaceModelCacheKeys` 模式）：

```java
/**
 * 一次事务内保存多个缓存 Key 的完整快照和关系。
 */
public boolean replaceModelCacheKeysAndRelations(
        @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> sourceObjectsByKey,
        @NonNull Collection<CacheWriteAction> writeActions,
        @NonNull Map<DtpRwdRelationTypeEnum, Collection<? extends DtpRwdObjectDTO>> relationsByType,
        @NonNull BooleanSupplier afterReplaceAction);
```

内部：
1. 事务外准备全部 `PreparedObjectRows`（投影）
2. `TransactionTemplate` 内：逐 Key upsert + replaceObjectIds + 关系替换 + writeActions
3. 任一 false → `setRollbackOnly()`

各策略的"保存阶段"改为调用此入口，事务边界收敛到一处。

### 3.4 现有短事务结构（不变）

| 位置 | 事务 | 表 |
|---|---|---|
| `replaceModelCacheKeyInternal` | 组1 短事务 | `cache_object` + `cache_object_model_view` + `cache_object_key_rel` |
| `replaceObjectRelationsInternal` | 组2 短事务 | `cache_object_rel` |
| `replaceByCacheKeys` | 组3 短事务 | `cache_info_code_template` |
| `replaceRules` | 组4 短事务 | `cache_rule` + `cache_rule_key_rel` |

> 全部使用 REQUIRED 传播（`@Transactional` 默认 / `TransactionTemplate` 默认），外层存在时自动加入。

---

## 4. READY 机制（保持不变）

- `markReady(type)` 仅在**全部策略成功**后、构建锁释放前提交
- 读取方通过 `isReady(type)` 决定走缓存还是实时查询
- 构建失败/中断 → 不标记 READY → 半成品不可见

事务提供**物理原子性**（保存阶段全回滚），READY 提供**读取可见性**（不可见半成品）。

---

## 5. 最终锁体系一览

| 层 | 锁 | Key 示例 | 机制 | 等待 | 保护 |
|---|---|---|---|---|---|
| 触发 | `Trigger.cache()` | `LOCK_CACHE_trigger::Pj...` | 抢占式中断 | 不等待 | 事件发布/任务发起 |
| 计算 | `CacheRwdBuild` | `MEOS:cacheRwdBuild:Pj...:AIR_TERMINAL` | tryLock | 0s | 构建流程互斥 |
| 保存 | `CacheRebuild.byType` | `LOCK_CACHE_cacheRebuild::Pj...:AIR_TERMINAL` | executeWithLock | 120s | `cache_info_code_template` |
| 保存 | `CacheRebuild.cache`（兜底） | `LOCK_CACHE_cacheRebuild::Pj...` | executeWithLock | 120s | 未知类型模板写入 |
| 保存 | 规则写锁 | `APC:cache_rule_write_lock:Pj...` | tryLock | 60s | `cache_rule` + rel |
| 保存 | 结果写锁 | 模板级/场景级 | tryLock | 120s 总预算 | `demand_config_result` + snapshot |

---

## 6. 兼容性与回滚

- **锁 Key 变更**：`CacheRebuild.cache()` Key 不变（兜底保留）；新增 `byType` Key；新增 `Trigger` Key
- **行为变更**：
  - `triggerBuildCache` 拿不到锁从 false 改为抛异常 → 调用方感知变化（前端/接口调用方需确认）
  - `buildCommonStrategies` 去掉模板重建 → 全量构建结果不变（类型构建已覆盖），但**公共结构失败时类型构建跳过逻辑依赖 `commonOnlySuccess`**，需保持
  - DTX 监听器锁 Key 变更 → 无持久化状态，可随时回退
- **回滚**：逐层回退（先回计算层，再回触发层），保存层锁 Key 新增不影响旧 Key

---

## 7. 风险与缓解

| 风险 | 缓解 |
|---|---|
| 大事务（几十万条单事务） | 保存锁等待 120s；外层事务设置 timeout；分批 UPSERT 已按 `CacheBatchExecutor` 分片 |
| 抢占中断 → `SQLException: interrupt` | 事务回滚保证数据一致（全回滚）；日志接受中断异常（最终由新触发重建） |
| `triggerBuildCache` 报错语义变化 | 调用方确认；错误信息明确"已有构建任务执行中" |
| 去掉 CacheRebuild 对 key_rel 的保护 | 已分析：所有写入口被计算锁或保存锁覆盖（见 2.3） |
