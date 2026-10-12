# Implement: 缓存模板重建锁修复

## 验证结果（已完成）

- `common` 模块 `mvn compile`：✅ 通过（JDK8：`D:\Java\LibericaJDK-8`）
- 相关测试类（RedissonDistributedLockerServiceImplTest / CacheInfoCodeTemplateRepositoryImplTest / CacheTemplateRebuildServiceRelationTest）：✅ 通过
- common 全量单测：✅ **194 个测试全部通过，BUILD SUCCESS**
- spec 同步：`.trellis/spec/backend/cache-object-model.md` 保存锁章节已更新（模板重建锁边界 + executeWithLock 释放可靠性）

### 实施中的关键发现

- 日志调用（`logProjectInstance().warn`）会**消费线程中断标志**：catch 内先 `interrupt()` 再打日志无效，标志已丢 → 最终实现用局部变量 `interrupted` 显式记录，finally 中统一恢复；spec 已记录该坑
- 僵尸锁场景（持锁 action 中被 interrupt）新增测试 `shouldReleaseLockWhenActionInterrupted`：锁必须释放、中断标志不残留污染线程池
- CacheInfoCodeTemplateRepositoryImplTest 原用例适配两段式语义：键集合查询由 1 次变 2 次（锁外快照 + 锁内复核）；新增 `shouldSkipWriteWhenPrepareSnapshotChanged` 用例

## 变更清单（按序执行）

### 1. `RedissonDistributedLockerServiceImpl.executeWithLock` finally 释放修复 ✅

最终实现：`interrupted` 局部变量显式记录；finally 先 `Thread.interrupted()` 清标志 → unlock 包 try/catch 打 error → 按局部变量恢复标志。
- 文件：`common/src/main/java/com/persagy/apccontrol/basic/service/cache/impl/RedissonDistributedLockerServiceImpl.java`
- 步骤：
  - finally 内：`boolean interrupted = Thread.interrupted();` 捕获并清除中断标志
  - `try { if (lock.isHeldByCurrentThread()) lock.unlock(); } catch (Exception e) { this.logProjectInstance().error("释放锁失败", ...lockKey, e); }`
  - `if (interrupted) Thread.currentThread().interrupt();` 恢复标志
- 验证：mvn compile（common）

### 2. `CacheTemplateRebuildService` 拆分为 prepare/execute 两段 ✅
- 文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/service/CacheTemplateRebuildService.java`
- 步骤：
  - `PreparedTemplateWrite` private → public static（字段保持 private final；补类注释说明"仅作不透明准备句柄，外部不得读取内部字段"）
  - 新增 `public List<PreparedTemplateWrite> prepareTemplates(@NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels)`：从 `rebuildTemplates` 抽出 prepare 循环（含 `parseFilterValueObject(true)`），保持原注释
  - 新增 `public void executePreparedTemplates(@NonNull List<PreparedTemplateWrite> prepared, @NonNull Collection<String> staleKeys)`：从 `rebuildTemplates` 抽出写循环（`executePreparedWrite`）+ stale 分组/动作准备/删除（原 L138-L147 部分）
  - `rebuildTemplates(models, staleKeys)` 改为：`executePreparedTemplates(this.prepareTemplates(models), staleKeys)`（行为不变，兼容 replaceTemplates/add）
- 验证：既有 CacheTemplateRebuildService 测试通过

### 3. `CacheInfoCodeTemplateRepositoryImpl.rebuildByParam` 重构为锁外准备 + 锁内短写 ✅
- 文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/repository/CacheInfoCodeTemplateRepositoryImpl.java`
- 步骤：
  - `CacheLockKey.CacheRebuild.byType` 等锁时间保持 Key 定义值 **30 分钟**（覆盖百万数据本地写排队；用户确认：等锁时间统一由 Key 承载，不单独定义常量）；事故放大器另有源头：锁内包远程准备（已移出）+ 僵尸锁（已修复），30 分钟等待在修复后仅为正常竞争兑底
  - `rebuildByParam` 重构：
    1. 锁外：`listTemplateTypes + filter(allowCache)` 与全量范围判断（从 `rebuildByParamInternal` 抽出）；空模板且非全量范围时直接 return（保留原语义）
    2. 锁外：快照元数据键集合 `prevKeysAtPrepare`（复用现有 list 查询，仅取 customKey）
    3. 锁外：`templateRebuildService.prepareTemplates(models)`
    4. 锁内 `executeWithProjectLock(byType(type))`（等锁时间取 Key 的 30 分钟）：
       - 重新查询元数据键集合 `prevKeys`；`!prevKeys.equals(prevKeysAtPrepare)` → warn（含数量差异）→ 直接返回（放弃本次写，不抛异常）
       - 计算 `staleKeys = prevKeys - targetKeys`（targetKeys 由 models 的 templateTypeKey 得到）
       - `templateRebuildService.executePreparedTemplates(prepared, staleKeys)`
       - `replaceByCacheKeys(prevKeys ∪ targetKeys, targetTemplates)`（原逻辑保留）
  - `add`：锁内全流程保持（与 rebuild 共用同一 Key 的 30 分钟等锁时间）
  - `rebuildByParamInternal` 删除或收敛（原私有方法逻辑被重构吸收）
- 验证：CacheInfoCodeTemplateRepositoryImplTest 既有用例 + 新增"锁内键集合变化放弃写入"用例

### 4. 单测 ✅
- `RedissonDistributedLockerServiceImplTest`：新增中断后释放用例（mock RLock：首次 action 抛异常、验证 unlock 调用与中断标志恢复）
- `CacheInfoCodeTemplateRepositoryImplTest`：新增/调整 rebuildByParam 用例
- 运行：common 模块相关测试类

## 验证命令

```bash
cd d:\IdeaProjects\meos-control-parent\common
mvn compile -q
mvn test -Dtest=RedissonDistributedLockerServiceImplTest,CacheInfoCodeTemplateRepositoryImplTest,CacheTemplateRebuildServiceRelationTest -q
```

## 验收后
- 更新 `.trellis/spec/backend/cache-object-model.md` 锁语义章节：模板重建 = 锁外准备 + 锁内短写；CacheRebuild.byType 等锁时间 30 分钟统一由 Key 承载，调用方不单独定义
- qa 观察一轮 DTX 验证 7 键恢复刷新
