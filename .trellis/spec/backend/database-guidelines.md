# Database Guidelines

> **Rule**: XML mappers in `resources/mapper/`. Use batch queries. Never loop queries.

---

## XML Mapper Location

```
resources/mapper/{business}/{Entity}Mapper.xml
```

---

## Mapper Pattern

```java
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
```

---

## Query Pattern

```java
LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
wrapper
    .eq(UserDO::getId, id)
    .like(UserDO::getName, name)
    .orderByDesc(UserDO::getCreateTime);

List<UserDO> list = mapper.selectList(wrapper);
```

---

## ❌ FORBIDDEN: Loop queries

```java
// ❌ FORBIDDEN — N+1 problem, 100 IDs = 100 DB round trips
List<Long> ids = Arrays.asList(1L, 2L, 3L);
for (Long id : ids) {
    mapper.selectById(id);
}
```

```java
// ✅ CORRECT — batch query, 1 DB round trip
List<Long> ids = Arrays.asList(1L, 2L, 3L);
mapper.selectBatchIds(ids);
```

---

## Batch Inserts

For large inserts, prefer chunked mapper-level batch inserts over MyBatis-Plus `saveBatch` when performance matters.

```java
// ❌ SLOW for very large data sets
this.saveBatch(results);
```

```java
// ✅ CORRECT — chunk and use mapper XML insertBatch
for (int fromIndex = 0; fromIndex < results.size(); fromIndex += INSERT_BATCH_SIZE) {
    int toIndex = Math.min(fromIndex + INSERT_BATCH_SIZE, results.size());
    super.getBaseMapper().insertBatch(results.subList(fromIndex, toIndex));
}
```

When using a hand-written XML `insertBatch`, MyBatis-Plus `IdType.ASSIGN_ID` and `MetaObjectHandler` autofill do not run through the normal `insert` flow. Before calling XML `insertBatch` for entities extending `BaseAssignIdDO`, call the shared base helper:

```java
List<ResultEntity> resultList = new ArrayList<>(results);
resultList.forEach(result -> super.fillInsertDefaultValue(result));
super.getBaseMapper().insertBatch(resultList);
```

This preserves the same fields normally filled by MyBatis-Plus:

- `id`
- `createTime`
- `createUser`
- `updateTime`
- `updateUser`

## Scenario: Cache Mapper Batch Writes

### 1. Scope / Trigger

Apply this scenario when a cache resource service writes multiple rows through a hand-written
Mapper `insertBatch` or `upsertBatch` statement.

### 2. Signatures

```java
public static <T> void execute(
        @NonNull Collection<T> records,
        @NonNull Consumer<List<T>> partitionAction);

void insertBatch(@NonNull @Param("records") Collection<Record> records);
```

### 3. Contracts

- Cache batch writes use `CacheBatchExecutor`, which takes a stable collection snapshot and invokes
  the callback synchronously in partitions of at most 1000 rows.
- The executor does not own transactions, validate records, or fill persistence fields.
- The resource service validates the complete target collection and fills IDs/audit fields before
  invoking the executor. Mapper failures propagate to the caller's transaction.
- Mapper XML uses one `insert ... values <foreach ... separator=",">` statement per partition.
- Do not use MyBatis-Plus `saveBatch` for new cache-model multi-row writes; it issues repeated
  single-row Mapper inserts and relies on JDBC driver rewriting.

### 4. Validation & Error Matrix

| Condition | Result |
| --- | --- |
| Empty collection | Execute no callback and issue no SQL |
| 1-1000 records | Execute one Mapper multi-value statement |
| More than 1000 records | Execute ordered 1000-row partitions in the caller transaction |
| Invalid write record | Fail in the resource service before the first Mapper call |
| Mapper or connection failure | Propagate and roll back the caller transaction |

### 5. Good / Base / Bad Cases

- Good: fill every record, then pass the collection to `CacheBatchExecutor.execute` and a Mapper
  `insertBatch` callback.
- Base: an empty collection returns without touching the Mapper.
- Bad: call `saveBatch(records)` and assume MyBatis-Plus generated one multi-value SQL statement.

### 6. Tests Required

- Assert 0, 1, 1000, and 1001 records invoke the Mapper 0, 1, 1, and 2 times.
- Assert every Mapper partition contains at most 1000 records and preserves input order.
- Assert validation/default-field preparation completes before the first Mapper callback.
- Assert a later partition failure rolls back earlier partitions in the surrounding transaction.

### 7. Wrong vs Correct

```java
// Wrong: SQL shape depends on MyBatis-Plus/JDBC batch implementation details.
this.saveBatch(records);

// Correct: every callback executes one explicit Mapper multi-value SQL.
records.forEach(record -> super.fillInsertDefaultValue(record));
CacheBatchExecutor.execute(
        records, partition -> super.getBaseMapper().insertBatch(partition));
```

---

## Naming Conventions

| Type | Rule | Example |
|------|------|---------|
| Table | lowercase_underscore | `point_config` |
| ID column | `{entity}_id` | `user_id` |
| Time column | `{action}_time` | `create_time` |
| Status | `status` (1=enabled, 0=disabled) | `status` |
