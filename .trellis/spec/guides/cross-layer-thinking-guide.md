# Cross-Layer Thinking Guide

> **Purpose**: Think through data flow across layers before implementing.

---

## The Problem

**Most bugs happen at layer boundaries**, not within layers.

Common cross-layer bugs:
- API returns format A, frontend expects format B
- Database stores X, service transforms to Y, but loses data
- Multiple layers implement the same logic differently

---

## Before Implementing Cross-Layer Features

### Step 1: Map the Data Flow

Draw out how data moves:

```
Source → Transform → Store → Retrieve → Transform → Display
```

For each arrow, ask:
- What format is the data in?
- What could go wrong?
- Who is responsible for validation?

### Step 2: Identify Boundaries

| Boundary | Common Issues |
|----------|---------------|
| API ↔ Service | Type mismatches, missing fields |
| Service ↔ Database | Format conversions, null handling |
| Backend ↔ Frontend | Serialization, date formats |
| Component ↔ Component | Props shape changes |

### Step 3: Define Contracts

For each boundary:
- What is the exact input format?
- What is the exact output format?
- What errors can occur?

---

## Common Cross-Layer Mistakes

### Mistake 1: Implicit Format Assumptions

**Bad**: Assuming date format without checking

**Good**: Explicit format conversion at boundaries

### Mistake 2: Scattered Validation

**Bad**: Validating the same thing in multiple layers

**Good**: Validate once at the entry point

### Mistake 3: Leaky Abstractions

**Bad**: Component knows about database schema

**Good**: Each layer only knows its neighbors

### Mistake 4: Treating Lifecycle Beans as Replaceable Helpers

**Bad**: Deleting or delegating a Spring lifecycle/strategy component after finding equivalent logic, without checking
injected component lists, ordering, manual constructors, and every legacy entrypoint.

**Good**: Preserve the original bean and dispatch contract unless migration is explicitly approved; isolate new behavior
behind a new entrypoint and add tests for component presence, invocation count, and order.

### Mistake 5: 读写共用的缓存解析方法缺少场景契约

**Bad**: 同一解析方法默认复用缓存快照（DB/Redis），写入/重建路径直接调用同一入口——源配置变更后
重建把旧快照原样写回，过期数据永久固化，且只修"坏数据"不修"复用边界"时同类问题会换个形态复发。

**Good**: 解析方法按调用场景提供显式入口（如 `forceRefresh` 参数）：写入路径强制实时解析、
读取路径保持复用；共用逻辑通过"先强制解析填充内存缓存、后续调用命中内存值"保证一次流程只解析
一次。新增调用方必须先确认所属场景（读取 or 写入），修复缓存问题时同时补齐两类回归测试
（读取复用、写入强制重算）。

### Mistake 6: "先删后插"并发写入缺少跨层契约

**Bad**: 应用层无锁串行化的"先删后插"写入（`DELETE ... WHERE ...` + `INSERT ...`），数据库层
无唯一约束兜底（云边同步架构禁止 `UNIQUE`），两者间的并发安全契约未被任何一方覆盖——两个
并发事务同时执行 DELETE（RC 隔离级别下对不存在行不加间隙锁）→ 都通过 → 都 INSERT →
无唯一约束 → 重复记录。

**Good**: 所有"先删后插"写入路径必须在应用层以分布式锁按项目串行化；锁必须包住事务提交
（锁释放晚于事务提交）；同一项目内全部写入路径（模板重建、对象模型写入、白名单保存等）
共用同一把项目级锁（`CacheLockKey.CacheRebuild.cache()`），保证并发写入同一缓存键时互斥；
当前线程已持有该锁时直接执行（可重入）避免锁中套锁。

涉及"先删后插"的表清单（按项目规范禁止 UNIQUE 索引）：
- `cache_info_code_template`
- `cache_object_key_rel`
- `cache_object_rel`
- `cache_rule_key_rel`（已有独立锁保护）
- 新增缓存表时必须确认写入模式并决定锁保护方案

---

## Checklist for Cross-Layer Features

Before implementation:
- [ ] Mapped the complete data flow
- [ ] Identified all layer boundaries
- [ ] Defined format at each boundary
- [ ] Decided where validation happens
- [ ] Enumerated injected lifecycle/strategy collections, bean ordering, manual constructors, and all legacy entrypoints

After implementation:
- [ ] Tested with edge cases (null, empty, invalid)
- [ ] Verified error handling at each boundary
- [ ] Checked data survives round-trip
- [ ] Verified legacy entrypoints still invoke the same lifecycle components in the same order unless migration was approved

---

## When to Create Flow Documentation

Create detailed flow docs when:
- Feature spans 3+ layers
- Multiple teams are involved
- Data format is complex
- Feature has caused bugs before
