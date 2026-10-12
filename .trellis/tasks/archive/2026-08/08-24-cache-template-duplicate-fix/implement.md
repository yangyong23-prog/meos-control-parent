# 执行计划：缓存表并发写入竞态修复

## 实施清单

### 1. 加锁基础设施

- [x] 1.1 确认 `CacheInfoCodeTemplateRepositoryImpl` 可注入 `RedissonClient`（与 `BaseEvent` 同源依赖），不可注入则通过 `SpringUtil` 获取。
- [x] 1.2 新增私有锁工具方法（如 `executeWithProjectLock(Runnable)`）：
  - 锁 Key：`CacheLockKey.DtxPublish.cache()`（复用，与 DTX 全量重建互斥）
  - `tryLock` 等待获取，超时抛 `BusinessException`（明确失败）
  - `finally` 释放锁，释放晚于内部事务提交（方法返回）
  - 日志：获取锁成功/失败/超时（`LogExecutor` 项目级日志）

### 2. 加锁落点

- [x] 2.1 `rebuildByParam`：方法体整体包入项目锁（覆盖上传/DTX/xxl-job/调试全部重建路径）
- [x] 2.2 `add`：方法体整体包入项目锁（纯插入路径，防御性覆盖）
- [x] 2.3 `del`：不加锁（纯删除无重复风险），代码注释说明原因

### 3. 目标集合去重（辅助防御）

- [x] 3.1 `rebuildByParam` 构造 `targetTemplates` 时按 `templateTypeKey` 去重（`LinkedHashMap` 保留首个），与 `targetKeys` 的 Set 语义对齐
- [x] 3.2 `add` 中 `templateTypeModels → cacheInfoCodeTemplates` 同样去重（防御源配置异常）

### 4. 存量数据清理

- [x] 4.1 编写清理 SQL（保留每组 `project_id + custom_key` 的 `max(id)`），作为独立脚本随任务交付（不直接进 Flyway，需人工确认后执行）
- [ ] 4.2 在测试环境验证清理脚本正确性（先备份、再执行、再核对）【待测试环境执行】

### 5. 单元测试

- [x] 5.1 `CacheInfoCodeTemplateRepositoryImplTest` 新增：
  - 并发调用 `rebuildByParam`（两个线程同项目），验证 `replaceByCacheKeys` 仅串行执行且最终无重复（可用锁 mock 或真实 Redisson mock 断言互斥）
  - 目标集合含重复 `templateTypeKey` 时插入集合已去重
  - 获取锁超时抛异常
- [x] 5.2 既有测试回归通过（`CacheInfoCodeTemplateRepositoryImplTest`、`CacheTemplateRebuildServiceRelationTest` 等）

### 6. 验证

- [x] 6.1 并发场景验证：代码层面已通过项目级锁串行化（单测覆盖锁超时失败路径）；【测试环境并发上传验证待执行】
- [x] 6.2 单线程行为回归：既有单测全部通过（键范围、数据内容不变）

> 备注：common 全量单测 127 个中 1 个失败（CacheObjectModelStatusServiceImplTest.shouldExposeOnlyFixedSupportedRunningMonitorTypes），为既有失败：测试断言仍为首版支持范围 [AIR_TERMINAL, LIGHTING]，实现已接入 VENTILATION。与本任务改动无关，未触碰该文件。

## 验证命令

```bash
# 单测
mvn test -pl common -Dtest=CacheInfoCodeTemplateRepositoryImplTest -am

# 编译
mvn compile -pl common -am
```

## 评审关口

1. 代码评审：锁时序（释放晚于事务提交）、锁 Key 复用、去重逻辑
2. 数据评审：清理脚本影响范围（重复数据量、涉及项目）
3. 回归评审：单线程行为不变、并发场景无重复

## 回滚点

- 代码：锁逻辑集中在 `CacheInfoCodeTemplateRepositoryImpl`，可单文件回退
- 数据：清理脚本执行前必须备份，回滚可恢复备份
