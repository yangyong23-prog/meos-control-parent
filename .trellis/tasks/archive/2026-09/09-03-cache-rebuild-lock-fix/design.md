# Design: 缓存模板重建锁修复

## 背景结论（来自事故日志与代码）

JTZGZDHCS5 自 9/2 18:17 起每轮全量缓存构建的 LIGHTING 模板重建段失败：
- 18:17:41 持 `cacheRebuild::LIGHTING` 锁执行中被抢占 interrupt → 锁未释放（僵尸锁）
- 此后每轮模板重建等锁 30 分钟（= CacheRebuild.byType 过期时间 1800s 被当 waitSeconds）超时 → 7 条 DETAIL/TABLE_LIST 模板键冻结

## 根因：模板重建锁内包含远程准备，违背项目架构原则

项目缓存写入架构原则（其他所有策略一致遵守）：**RWD 远程调用与异步查询必须置于事务/锁之外；锁只保护本地写临界区**。
AirTerminal/Ventilation/Security 策略均为"事务外 RWD 准备 → 锁内一次性短写"。

模板链路是唯一例外：`CacheInfoCodeTemplateRepositoryImpl.rebuildByParam` 用 `executeWithProjectLock` 把**完整流程**（含 `listTemplateTypes`、模板对象/关系 RWD prepare，受三方依赖影响可能数小时）包在 `CacheRebuild` 锁内 → 持锁数小时 → 等锁被迫 1800s → 僵尸锁危害放大（每轮白等 30 分钟、拖慢整轮构建）。

## 决策 1（核心）：模板重建改为"锁外准备 + 锁内短写"

### 目标结构

```
rebuildByParam(type)                                  // 无锁
  ① 锁外：快照当前元数据键集合 prevKeysAtPrepare      // 本地 DB 快查（全量范围，先于源查询闭合并发窗口）
  ② 锁外：listTemplateTypes + filter(allowCache)      // 远程，可能数小时
     窄范围：源查询后补快照（旧键须以目标模板 ID 定位；范围限定无漏检危害）
  ③ 锁外：service.prepareTemplates(models)            // 远程 RWD 查询/关系准备
  ④ 锁内：executeWithProjectLock(CacheRebuild.byType(type)) {   // 等锁 30 分钟（Key 承载）
        prevKeys = 重新查询元数据键集合                 // 本地 DB
        校验 prevKeys == prevKeysAtPrepare            // 防并发 add/del 造成误删/覆盖
        不一致 → warn + 放弃本次写（不抛异常，下轮自愈）
        staleKeys = prevKeys - targetKeys
        service.executePreparedTemplates(prepared, staleKeys)  // 本地写：键替换+stale清理
        replaceByCacheKeys(元数据同步)                  // 本地写
     }
```

### 并发正确性论证

- **add（模板保存事件）与 rebuild 并发**：
  - add 在 prepare 期间新增/变更模板 → 锁内 `prevKeys != prevKeysAtPrepare` → rebuild 放弃本次（不误删 add 新键、不旧快照覆盖新值），warn 日志；add 自身已完成单模板写入，下轮全量重建自愈。
  - 键集合相同、仅内容变化的窗口（add 更新同键模板）无法被该校验捕获，最坏 = 缓存短暂回到 prepare 时刻源状态，与其余策略"锁外 RWD 准备"语义一致（项目已接受，下轮自愈）。
- **del（模板删除事件）与 rebuild 并发**：del 改变元数据键集合 → 同上校验放弃本次 → 安全。
- **两个 rebuild 并发**：已被上层类型构建锁（buildSingleType tryLock）串行，同类型同刻仅一个全量构建，不会并发。
- 锁内剩余动作全部为本地 DB（旧键查询、键替换短事务、元数据替换），持锁分钟级。

### CacheTemplateRebuildService 拆分（新增两个 public 入口）

- `prepareTemplates(models)` → `List<PreparedTemplateWrite>`：从现 `rebuildTemplates` 抽出 prepare 循环（含 `parseFilterValueObject(true)`）
- `executePreparedTemplates(prepared, staleKeys)`：从现 `rebuildTemplates` 抽出写循环（`executePreparedWrite`）+ stale 清理（`mapTemplateKeysByType` + `prepareRemoveActions` + `removePreparedKeys`）
- `PreparedTemplateWrite` 由 private 改 public static（字段保持 private final，repository 仅作不透明句柄传递）
- `rebuildTemplates(models, staleKeys)` 保留：内部 = prepareTemplates + executePreparedTemplates，兼容 add 路径（addInternal → replaceTemplates → rebuildTemplates 不变）

### 入口差异处理

- `rebuildByParam`：锁外准备 + 锁内短写（重建是轮询任务，等不到/校验失败放弃本轮可接受）
- `add`：保持锁内全流程（单模板远程查询秒级~分钟级、量小）
- 等锁时间统一：等锁时间由 `CacheLockKey.CacheRebuild.byType` 的 expire 统一承载（**30 分钟**，覆盖百万级对象本地写入排队；用户拍板值），`executeWithProjectLock` 取 `lockKey.getExpire().getSeconds()`，各入口不单独定义。事故中 30 分钟等待显得致命的真正原因是锁内包了几小时远程准备 + 僵尸锁每轮叠加，两项均已修复后 30 分钟只是正常竞争兑底；持锁租期由 watchdog 续期，不受该值限制

## 决策 2：executeWithLock finally 释放可靠性（僵尸锁根因修复）

`RedissonDistributedLockerServiceImpl.executeWithLock` finally 缺陷：unlock 未容错、中断标志干扰释放。
修复：finally 内
1. `boolean interrupted = Thread.interrupted();` 捕获并清除中断标志
2. `try { if (lock.isHeldByCurrentThread()) lock.unlock(); } catch (Exception e) { log.error("释放锁失败", lockKey, e); }`
3. `if (interrupted) Thread.currentThread().interrupt();` 恢复标志（上层"被抢占放弃执行"语义不变）

## 决策 3：抢占语义保留

`BaseEvent.executeWithLock`（Trigger 锁）抢占中断是 DTX 触发链路既定设计，不改。本方案使保存层锁内不再有远程调用，被抢占中断落在锁内写库的窗口大幅缩小；中断后的释放由决策 2 兜底。

## 决策 4：del 入口（已知边界，本次不纳入）

`del` 未持 CacheRebuild 锁（现存行为）。决策 1 的"锁内键集合校验"可防止 del 与 rebuild 的误删竞态；del 与 add 的单模板竞态窗口极小且为既有行为，本次不处理，design 记录待后续评估。

## 测试计划

- 单测更新：
  - `RedissonDistributedLockerServiceImplTest`：模拟持锁线程被 interrupt 后 action 抛异常 → 验证 finally unlock 被调用、锁不残留、中断标志恢复
  - `CacheTemplateRebuildService` 相关测试：rebuildTemplates 拆分后行为不变（prepare 与 execute 分离后组合结果一致）
  - `CacheInfoCodeTemplateRepositoryImplTest`：rebuildByParam 重构后全量/窄范围重建边界行为不变；新增"prepare 期间键集合变化 → 放弃写入"用例
- 编译：common 模块 mvn compile（如可行跑全部单测）
- qa 观察：DEL 僵尸锁后一轮 DTX，7 条模板键 update_time 恢复刷新；并发保存模板配置场景无误删
