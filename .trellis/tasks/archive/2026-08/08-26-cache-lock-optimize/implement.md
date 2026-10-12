# Cache 锁体系优化 — 实施计划

> 任务：08-26-cache-lock-optimize
> 设计依据：design.md（三层锁架构 + 保存阶段事务化）

---

## 实施阶段总览

```
阶段1：锁基础设施改造（CacheLockKey）
阶段2：保存层改造（CacheRebuild 拆细 + 批量保存入口）
阶段3：计算层改造（triggerBuildCache 报错 + common 去模板重建）
阶段4：触发层改造（Trigger 锁 + 入口接入）
阶段5：策略保存阶段事务化
阶段6：验证与回归
```

---

## 阶段1：锁基础设施改造

### 1.1 `CacheLockKey` 新增 Trigger 命名空间

**文件**：`common/.../basic/model/base/CacheLockKey.java`

```java
interface Trigger {
    /** 缓存触发统一锁：DTX发布、初始化任务、定时任务共用 */
    static Key cache() {
        return new Key(Duration.ofMinutes(30), "trigger", ContextUtil.getProjectId());
    }
}
```

### 1.2 `CacheRebuild` 增加按类型接口

```java
interface CacheRebuild {
    /** 按类型锁定（保存层主用） */
    static Key byType(@NonNull RunningMonitorTypeEnum type) {
        return new Key(Duration.ofMinutes(30), "cacheRebuild",
                ContextUtil.getProjectId(), type.name());
    }
    /** 项目级兜底（add 等未知类型入口） */
    static Key cache() {
        return new Key(Duration.ofMinutes(30), "cacheRebuild", ContextUtil.getProjectId());
    }
}
```

### 1.3 保存锁等待时间常量

`CacheInfoCodeTemplateRepositoryImpl.LOCK_WAIT_SECONDS` 30s → **120s**（长等待）
`CacheObjectModelWriteServiceImpl.LOCK_WAIT_SECONDS` 30s → 120s（若保留）

---

## 阶段2：保存层改造

### 2.1 `CacheInfoCodeTemplateRepositoryImpl` 使用按类型锁

**文件**：`common/.../structure/cacherwd/repository/CacheInfoCodeTemplateRepositoryImpl.java`

| 方法 | 现状 | 目标 |
|---|---|---|
| `rebuildByParam(queryDaoParam)` | `CacheRebuild.cache()` | `CacheRebuild.byType(queryDaoParam.getRunningMonitorType())`；类型为 null 时用 `cache()` 兜底 |
| `add(templateTypeId)` | `CacheRebuild.cache()` | 先查模板类型 → `byType`；查询失败回退 `cache()` |

### 2.2 `CacheObjectModelWriteServiceImpl` 去掉 CacheRebuild 锁

**文件**：`common/.../structure/cacherwd/service/CacheObjectModelWriteServiceImpl.java`

- 删除 `executeWithProjectLock` 两个重载
- `replaceModelCacheKey` / `replaceObjectRelations` 直接调 Internal 方法
- 删除 `RedissonDistributedLockerService` 依赖（若不再使用）
- **注意**：删除前先确认所有调用方处于计算锁或保存锁内（见 design.md 2.3 覆盖矩阵）

### 2.3 新增批量保存编排入口（事务化基础）

**文件**：`CacheObjectModelWriteServiceImpl` + `CacheObjectModelWriteService` 接口

```java
boolean replaceModelCacheKeysAndRelations(
        @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> sourceObjectsByKey,
        @NonNull Collection<CacheWriteAction> writeActions,
        @NonNull Map<DtpRwdRelationTypeEnum, Collection<? extends DtpRwdObjectDTO>> relationsByType,
        @NonNull BooleanSupplier afterReplaceAction);
```

- 事务外：全部 Key 的 `prepareObjectRows` 投影 + 关系范围转换
- 事务内（TransactionTemplate）：逐 Key upsert + replaceObjectIds + 关系替换 + writeActions + afterReplaceAction
- 任一 false → `setRollbackOnly()`

---

## 阶段3：计算层改造

### 3.1 `triggerBuildCache` 报错语义

**文件**：`common/.../structure/cacherwd/service/CacheRwdDomainServiceImpl.java`

`buildSingleType` 现状：tryLock 失败返回 false。
`CacheRwdServiceImpl.triggerBuildCache` 现状：直接透传 false。

目标：**接口入口层**把 false 转为明确报错：

```java
// CacheRwdServiceImpl.triggerBuildCache
Boolean result = cacheRwdDomainService.buildCache(runningMonitorType);
if (!Boolean.TRUE.equals(result)) {
    throw new BusinessException(..., "缓存构建任务执行中，请稍后重试");
}
return result;
```

> `buildCache` 内部路径（xxl-job、DTX 事件）保持 false 跳过语义不变。

### 3.2 `buildCommonStrategies` 去掉模板重建

**文件**：`CacheRwdDomainServiceImpl.java`

现状：
```java
boolean commonStructureSuccess = this.executeStrategiesBuild(null, true, true);   // 公共结构
boolean commonTemplateSuccess = this.executeStrategiesBuild(null, true, false);  // 模板重建（冗余）
```

目标：只保留公共结构阶段：
```java
boolean commonStructureSuccess = this.executeStrategiesBuild(null, true, true);
```

- `CommonStrategyBuildResult` 简化为单布尔（或保留结构、兼容调用方）
- **验证点**：`buildCache(null)` 后 `cache_info_code_template` 数据完整（由逐类型构建覆盖）

---

## 阶段4：触发层改造

### 4.1 `CacheDtxPublishChangedListener` 换锁

**文件**：`common/.../structure/cacherwd/listener/CacheDtxPublishChangedListener.java`

```java
// 3 个方法统一：
CacheLockKey.Key key = CacheLockKey.Trigger.cache();
event.executeWithLock(() -> "缓存DTX发布变更监听器", key, () -> publishEvent(...));
```

### 4.2 初始化任务接入触发锁（抢占中断）

**文件**：
- `saas/.../task/cacheobjectmodel/CacheObjectModelInitTaskHandler.java`
- `saas/.../task/DemandConfigResultInitTaskHandler.java`

> ✅ **已确认决策**：初始化/定时任务与 DTX 一样，**全部用抢占中断**（手动执行只跑一次，不能被跳过）。xxl-job 没有现成事件对象，通过构造**轻量 BaseEvent 子类**调用 `executeWithLock`。

实现方式：

```java
// 新增轻量触发事件（common 模块，供 xxl-job 和调试接口复用）
public class CacheTriggerEvent extends BaseEvent {
    public CacheTriggerEvent() {
        super("");
    }
}

// xxl-job process() 内（已有 ContextUtils.initGroupCodeProjectId）
CacheLockKey.Key key = CacheLockKey.Trigger.cache();
new CacheTriggerEvent().executeWithLock(
        this, key,
        () -> {
            // 逐类型构建 / 刷新
            for (DemandTemplateTypeEnum templateType : this.resolveTemplateTypes(param)) {
                this.demandConfigResultRefreshRepository.refreshByTemplateType(templateType);
            }
            return null;
        });
```

- `BaseEvent.getGroupCode()/getProjectId()` 从 `ContextUtil` 读取，xxl-job 已初始化线程上下文 ✅
- 锁 Key 与 DTX 共用 `Trigger.cache()` → 与在跑的 DTX 构建互斥，且手动执行可抢占中断

### 4.3 调试接口接入触发锁（抢占中断）

**文件**：`saas/.../controller/debug/cache/DebugCacheController.java`

- `initDemandConfigResult` / `initCacheObjectModel` 与初始化任务一致：构造 `CacheTriggerEvent` 调用 `executeWithLock`（抢占中断）
- 手动执行只跑一次，若正在构建则抢占中断后重新构建

---

## 阶段5：策略保存阶段事务化

### 5.1 策略改造清单

| 策略 | 保存调用 | 改造 |
|---|---|---|
| `CacheRwdAirTerminalStrategy` | space + equipment + relations | 改用 `replaceModelCacheKeysAndRelations` |
| `CacheRwdVentilationStrategy` | space + equipment + relations | 同上 |
| `CacheRwdLightingStrategy` | 多 Key + relations | 同上 |
| `CacheRwdSecurityStrategy` | 已是 `replaceModelCacheKeys` | 扩展为含 relations 的编排入口 |
| `CacheRwdComputerRoomSecurityStrategy` | 多 Key + 规则 + relations | 同上（含 cache_rule 写入动作） |
| `CacheRwdProjectStructureStrategy` | buildings + floors | 同上 |
| `CacheRwdInfoCodeConfigStrategy` | rebuildByParam（内部已是短事务） | 保持（模板链路已有 REPLACE 语义） |
| `CacheRwdAirTerminalWhiteListStrategy` | 单 Key | 保持（单事务） |

### 5.2 事务超时设置

外层 TransactionTemplate 设置 timeout（如 300s），防止超长事务：
```java
transactionTemplate.setTimeout(300);
```

---

## 阶段6：验证与回归

### 6.1 单元测试更新

| 测试 | 改动 |
|---|---|
| `CacheInfoCodeTemplateRepositoryImplTest` | 锁 Key 断言改为 `byType` 形式 |
| `CacheObjectModelWriteServiceImplTest` | 移除 CacheRebuild 锁 mock 断言 |
| `CacheRwdDomainServiceImplCommonStrategyTest` | common 阶段不再执行模板策略 |
| 新增：`replaceModelCacheKeysAndRelations` 事务回滚测试 | 多 Key 中一个失败 → 全部回滚 |

### 6.2 编译验证

```bash
mvn compile -T 1C
mvn test -pl common -Dtest="CacheInfoCodeTemplateRepositoryImplTest,CacheObjectModelWriteServiceImplTest,CacheRwdDomainServiceImplCommonStrategyTest" -DfailIfNoTests=false
```

### 6.3 手动验证场景

| # | 场景 | 预期 |
|---|---|---|
| 1 | 单类型构建成功 | 各表数据完整，READY 标记 |
| 2 | 单类型构建中并发 triggerBuildCache | 第二次调用直接报错 |
| 3 | 构建中触发 DTX（抢占中断） | 旧构建事务回滚，新构建成功，无重复数据 |
| 4 | 配置导入与 buildCache 并发 | 模板表无重复记录（byType 锁互斥） |
| 5 | 全量 buildCache(null) | 公共结构一次，各类型一次，模板表完整 |
| 6 | 大数据量（几十万）构建 | 保存阶段单事务完成，无超时 |

### 6.4 数据校验 SQL

```sql
-- 重复记录检查（所有 cache 表）
SELECT project_id, custom_key, COUNT(*) c FROM cache_info_code_template
GROUP BY project_id, custom_key HAVING c > 1;

SELECT project_id, cache_key, object_id, COUNT(*) c FROM cache_object_key_rel
GROUP BY project_id, cache_key, object_id HAVING c > 1;
```

---

## 回滚点

| 阶段 | 回滚方式 |
|---|---|
| 阶段1-2（锁 Key 变更） | Key 是新增的，旧 Key 仍在兜底使用；删除新增代码即可 |
| 阶段3（报错语义） | 恢复 false 透传 |
| 阶段4（触发锁接入） | 恢复原监听器锁 Key；初始化任务移除 tryLock |
| 阶段5（事务化） | 策略改回逐个调用内部方法 |

---

## 依赖与顺序

- 阶段2 依赖阶段1（锁 Key 先行）
- 阶段3.2 依赖阶段2.1（common 去模板重建后，模板写入只在类型锁内）
- 阶段4 独立可并行
- 阶段5 依赖阶段2.3（批量保存入口先行）
- 阶段6 最后统一验证
