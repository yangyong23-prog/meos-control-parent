# 技术设计：缓存表并发写入竞态修复

## 1. 现状梳理

### 1.1 触发链路

`rebuildByParam` → `CacheTemplateRebuildService.rebuildTemplates`（对象缓存重建）→ `CacheInfoCodeTemplateServiceImpl.replaceByCacheKeys`（元数据"先删后插"）。

并发触发源（均可能同时触达同一项目）：

| 触发源 | 入口 | 现状锁 |
|---|---|---|
| 配置表上传 | `InfoCodeImportServiceImpl.CacheRebuildExecutor` → `rebuildByParam`（逐项目） | 无 |
| DTX 发布事件 | `CacheDtxPublishChangedListener`（`DtxPublish.cache()` 锁内）→ `CacheRwdBuildTriggerEvent` → `CacheRwdInfoCodeConfigStrategy` → `rebuildByParam`（full scope） | 锁在事件监听边界，**与上传链路不互斥** |
| xxl-job 初始化重建 | `CacheObjectModelInitTaskHandler` → `buildCache` → `rebuildByParam` | 无 |
| 调试接口 | `DebugCacheController` → `buildCache` → `rebuildByParam` | 无 |

### 1.2 缓存表写入模式矩阵（排查结论）

| 表 | 写入方法 | 模式 | 风险 |
|---|---|---|---|
| `cache_info_code_template` | `replaceByCacheKeys` / `add→insertBatch` | 先删后插 / 纯插入 | **高（本次 bug）** |
| `cache_object_key_rel` | `insertObjectIds` + `removeByCacheKeys`（编排于 `replaceModelCacheKey`） | 先删后插 | **高（同类）** |
| `cache_rule_key_rel` | `replaceByCacheKey` | 先删后插 | **高（同类）** |
| `cache_object_rel` | `replaceRelations` | 先删后插 | **高（同类）** |
| `cache_template_object_detail` | `insertDetails` + `removeByTemplateKey`（编排） | 先删后插 | **高（同类）** |
| `cache_object` | `upsertBatch` | 按主键幂等 | 低（无需处理） |
| `cache_object_model_view` | `upsertBatch` | 按主键幂等 | 低（无需处理） |
| `cache_rule` | `upsertBatch` | 按主键幂等 | 低（无需处理） |

**关键结论**：除 `cache_info_code_template` 元数据写入（`replaceByCacheKeys`）为独立入口外，其余 4 张风险表全部由 `CacheObjectModelWriteService.replaceModelCacheKey` / `removeCacheKeys` / `replaceObjectRelations` 在**同一条 rebuild 链路上**编排。因此锁落在 `rebuildByParam`（及 `add`）入口即可一次性覆盖全部风险表。

### 1.3 扩展排查：重建/构建链路之外的写入路径（v7.260802.0 补充）

重建链路内的写入已由 `rebuildByParam` / `buildSingleType` 锁覆盖；**链路外的独立写入路径存在同类竞态**：

| 表 | 写入口 | 模式 | 链路外调用方 | 风险 |
|---|---|---|---|---|
| `cache_object_key_rel` | `replaceObjectIds`（`replaceModelCacheKey` 内） | 先删后插 | `CacheRwdServiceImpl` 白名单保存（`saveWhitelist` 等） | **高（本次补充修复）** |
| `cache_object_rel` | `replaceRelations`（`replaceObjectRelations` 内） | 先删后插 | 同上 | **高（本次补充修复）** |

修复：`CacheObjectModelWriteServiceImpl.replaceModelCacheKey` / `replaceObjectRelations` 入口加与 `CacheRebuild` 同一把项目级锁——白名单保存与模板重建、物理世界构建（`buildSingleType` 内再次进入时重入直通）互斥，锁覆盖 `TransactionTemplate` 短事务提交。

## 2. 方案设计

### 2.1 核心方案：项目级分布式锁串行化重建

在 `CacheInfoCodeTemplateRepositoryImpl` 的写入口加 Redisson 分布式锁（按项目维度），使"查询旧键 → 删除 → 插入"整段互斥，消除 TOCTOU 竞态。

**锁 Key**：独立定义于 `CacheLockKey.CacheRebuild.cache()`（`LOCK_CACHE_::cacheRebuild::{projectId}`，过期 30 分钟）——配置上传、DTX 事件、定时任务与调试接口等**全部重建路径最终都汇合于 `rebuildByParam` 入口**，汇合点的同一把锁即可保证互斥，无需与 `DtxPublish.cache()` 共享锁。

**加锁入口**（`CacheInfoCodeTemplateRepositoryImpl`）：
- `rebuildByParam`：核心（覆盖上传、DTX、xxl-job、调试全部重建路径）
- `add`：纯插入路径（当前 SaveEvent 无业务触发，防御性覆盖）

**不加锁入口**：
- `del`：纯删除无重复风险（与 rebuild 并发仅可能短暂缺失后由 rebuild 恢复），保持现状

### 2.2 关键时序约束：锁必须包住事务提交

`replaceByCacheKeys` 是 `@Transactional` 代理事务，事务提交发生在方法返回时。若锁在事务方法内部获取/释放，`unlock` 时事务尚未提交，后获锁线程的删除看不到前线程未提交的插入，竞态依旧。

`rebuildByParam` 本身**无事务注解**，其内部调用的 `replaceByCacheKeys`（经注入 bean 代理）返回时事务已提交。因此：

```
rebuildByParam 入口获取锁
  → rebuildTemplates（对象缓存重建，含远程准备）
  → replaceByCacheKeys（代理事务：删除+插入，返回即提交）
  → 锁释放（finally）
```

时序正确：锁释放严格晚于事务提交。`add` 同理（内部 `insertBatch` 为独立事务，`NOT_SUPPORTED` 传播下同样在方法返回时提交）。

### 2.3 锁获取策略

- 复用 `RedissonClient`（`common` 模块已有依赖，`BaseEvent` 同款）。
- `tryLock(waitTime, leaseTime)`：等待获取锁；等待超时抛业务异常（明确失败，可重试），避免静默不更新缓存。
- 锁粒度：项目级（与 `DtxPublish.cache()` 一致），单项目重建耗时通常在秒级，30 分钟过期时间充足。

### 2.4 辅助防御：目标集合按 key 去重

`rebuildByParam` 构造 `targetTemplates` 前，按 `templateTypeKey` 去重（保留首个），防止源配置异常（如跨文件 uniqueKey 碰撞）导致同一批次内重复插入。`targetKeys` 计算本就是 `Set`，插入集合对齐其语义。

## 3. 数据治理（存量清理）

提供清理脚本（Flyway 迁移或独立 SQL，执行前需人工确认）：

```sql
-- 保留每组 project_id + custom_key 中 id 最大的一条，其余删除
delete from cache_info_code_template
where id not in (
    select keep_id from (
        select max(id) as keep_id
        from cache_info_code_template
        group by project_id, custom_key
        having count(*) > 1
    ) t
);
```

> 注：`cache_info_code_template` 无外键引用（`rel_template_id` 仅业务关联），清理后重建链路会按最新配置重新生成，风险可控。执行前在测试环境验证数据形态。

## 4. 决策记录

| 决策点 | 结论 | 理由 |
|---|---|---|
| 为何不用唯一索引 | 不加 | 云边同步规范禁止，UPSERT 冲突检测同样依赖唯一索引，一并排除 |
| 锁落点为何选 `rebuildByParam` | 入口统一 | 覆盖全部 4 类触发源 + 全部风险表（含对象模型七表） |
| 为何锁独立定义于 `CacheRebuild` | 依赖方向正确 | 底层 Repository 不应依赖上层"DTX 发布"领域的锁 Key；全部重建路径汇合于 `rebuildByParam` 入口，汇合点独立锁即可保证互斥 |
| 为何 `del` 不加锁 | 纯删除无重复 | 与 rebuild 并发仅可能短暂缺失，由重建恢复 |
| 为何等待锁失败抛异常 | 显式失败 | 避免静默不更新缓存造成数据不一致 |
