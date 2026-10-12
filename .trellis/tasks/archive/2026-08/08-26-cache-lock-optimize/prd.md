# Cache 锁体系优化

## 背景

当前 Cache 表相关的分布式锁共 **9 把**，分散在 3 种锁机制（`executeWithLock`、`tryLock`、`BaseEvent.executeWithLock`）和 3 种锁粒度（项目级、类型级、方法级）中，存在以下问题：

### 已知问题

1. **锁嵌套导致超时（已现网触发）**
   - `CacheRwdBuild` 锁（非阻塞 tryLock） → `CacheRebuild` 锁（阻塞 wait 30s）
   - 两层语义不同，外层进入后内层阻塞 30s 超时，无降级机制
   - 错误信息：`获取分布式锁超时: LOCK_CACHE_cacheRebuild::Pj9522457558`

2. **锁粒度不一致相互牵连**
   - `CacheRebuild` 是项目级全局锁，AIR_TERMINAL 和 LIGHTING 等不同类型的缓存重建互斥
   - `CacheRwdBuild` 是按 `RunningMonitorType` 粒度的锁
   - 两层锁粒度不对等

3. **`buildCommonStrategies` 冗余模板重建**
   - 全量构建先 `rebuildByParam(null)`（ALL 锁）再逐类型 `rebuildByParam(type)`（类型锁）
   - 造成 ALL 锁与类型锁对 `cache_info_code_template` 的竞争窗口

4. **`BaseEvent.executeWithLock` 的线程中断副作用**
   - 并发请求时中断已有线程的 DB 操作，产生 `java.sql.SQLException: interrupt`（保留该语义，但通过事务回滚保证数据一致）

5. **`DtxPublish.cache` / `.lighting` / `.demandConfigResult` 三把触发锁并存**
   - 触发源语义不清，初始化任务和定时任务无触发锁

## 目标

按"触发 → 计算 → 保存"三层职责重组锁体系，消除嵌套超时，统一触发源，同时保证同一类型缓存数据"同时成功或同时失败"。

## 设计决策（已确认）

| # | 决策 | 内容 |
|---|---|---|
| 1 | **三层锁架构** | 触发锁（抢占中断）/ 计算锁（非阻塞报错）/ 保存锁（细粒度长等待） |
| 2 | **触发锁统一** | 新建 `Trigger.cache()` Key；DTX 监听器 + 初始化任务 + 定时任务共用 |
| 3 | **全部触发源统一抢占中断** | DTX 监听器、初始化任务、需求配置结果定时任务**全部**用 `BaseEvent.executeWithLock`（抢占中断）；xxl-job 构造轻量 BaseEvent 子类调用。原因：初始化/定时是手动执行只跑一次，不能被跳过，拿不到锁必须抢占 |
| 4 | **CacheRebuild 拆细粒度** | `byType(type)` 按类型保护 `cache_info_code_template`；`cache()` 项目级兜底（未知类型 add） |
| 5 | **写服务去锁** | `CacheObjectModelWriteServiceImpl` 去掉 CacheRebuild 锁（所有写入口已被计算锁或保存锁覆盖） |
| 6 | **common 去模板重建** | `buildCommonStrategies` 只保留公共结构阶段，模板重建由逐类型构建覆盖 |
| 7 | **triggerBuildCache 报错** | 拿不到计算锁直接抛异常（原返回 false 静默） |
| 8 | **保存阶段事务化** | 内部短事务保持；外部批量保存编排入口包 TransactionTemplate（REQUIRED 传播合并），同类型保存原子化 |
| 9 | **保存锁等待加长** | 30s → 120s（几十万数据写入需要长等待） |
| 10 | **不引入唯一约束** | 维持项目规范（cache 表禁止 UNIQUE 约束），一致性由锁 + 事务 + READY 保证 |

## 验收标准

- [ ] **AC1**：`CacheLockKey` 新增 `Trigger.cache()` 和 `CacheRebuild.byType(type)`，旧 `DtxPublish.cache` 锁 Key 不再用于缓存触发路径
- [ ] **AC2**：DTX 发布 / 初始化任务 / 需求配置结果定时任务共用 `Trigger.cache()` Key，全部使用 `BaseEvent.executeWithLock` 抢占中断语义
- [ ] **AC3**：`CacheRebuild` 锁按 RunningMonitorType 拆细；`CacheObjectModelWriteServiceImpl` 不再使用 CacheRebuild 锁
- [ ] **AC4**：`buildCommonStrategies` 不再执行模板重建；`buildCache(null)` 后 `cache_info_code_template` 数据完整
- [ ] **AC5**：`/cacheRwd/triggerBuildCache` 构建中重复调用时明确报错（非静默 false）
- [ ] **AC6**：策略保存阶段统一走批量保存编排入口（TransactionTemplate 外层事务）；任一保存失败时同类型全部回滚
- [ ] **AC7**：并发构建（DTX 中断场景）不产生 `cache_info_code_template` / `cache_object_key_rel` / `cache_object_rel` 重复记录
- [ ] **AC8**：保存锁等待时间 120s，大数据量（几十万）构建无锁超时
- [ ] **AC9**：全部现有单元测试更新并通过；新增批量保存事务回滚测试

## 约束

- 不能移除必要的数据竞争保护（"先删后插" TOCTOU）
- 需要覆盖配置上传、DTX 事件、定时任务、调试接口全部入口
- 优化方案需经过回归验证
- 项目规范：cache 表禁止 UNIQUE 约束，唯一性由业务代码维护

## 参考文档

- `01-cache-lock-analysis.md` — 现状 9 把锁全景分析
- `02-cache-rebuild-lock-analysis.md` — CacheRebuild 锁去留分析
- `03-split-plan.md` — CacheRebuild 锁拆分方案
- `04-lock-layers-design.md` — 三层锁架构设计
- `05-transaction-grouping.md` — 事务分组设计
- `design.md` — 最终技术设计
- `implement.md` — 实施计划
