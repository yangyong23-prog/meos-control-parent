# Backend Spec

> 云边架构能源监控系统，Java 8 + Spring Boot

---

## MUST FOLLOW

These rules are **frequently violated** — always check before writing code:

### Date Creation

```java
// ❌ FORBIDDEN
LocalDateTime.now()
new Date()

// ✅ CORRECT
DateUtils.now()
```

### String Concatenation for SQL

```java
// ❌ FORBIDDEN — SQL injection risk
"SELECT * FROM " + tableName
"SELECT * FROM user WHERE name = '" + name + "'"

// ✅ CORRECT — use StringBuilder or #{} parameter binding
StringBuilder sb = new StringBuilder();
sb.append("SELECT * FROM ").append(tableName);
```

### DTO Pattern

```java
// ❌ FORBIDDEN — Jackson serialization failure, MyBatis constructor conflict
@Data @Builder
public class UserDTO {}

// ✅ CORRECT
@Data @NoArgsConstructor @AllArgsConstructor
public class UserDTO {}
```

### Logging in Business Class

```java
// ❌ FORBIDDEN
@Slf4j
public class UserService {}

// ✅ CORRECT — implement LogComponent
public class UserService implements LogComponent {
    @Override
    public String logTag() { return "UserService"; }
}
```

### Empty Catch Block

```java
// ❌ FORBIDDEN — exception is hidden, impossible to debug
catch(Exception e) {}

// ✅ CORRECT — log and throw
catch(Exception e) {
    LogExecutor.project().error(LOGGER, LOG_TAG, "Operation failed", e);
    throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), "Operation failed");
}
```

### Loop Queries

```java
// ❌ FORBIDDEN — N+1 problem
List<Long> ids = Arrays.asList(1L, 2L, 3L);
for (Long id : ids) {
    mapper.selectById(id);
}
```

```java
// ✅ CORRECT — batch query
List<Long> ids = Arrays.asList(1L, 2L, 3L);
mapper.selectBatchIds(ids);
```

### XML Mapper Location

```
resources/mapper/{business}/{Entity}Mapper.xml
```

---

## DO NOT CREATE

`handler/`、`po/`、`page/`

---

## Spec Index

| File | When to Read |
|------|-------------|
| [Directory Structure](./directory-structure.md) | Creating new module |
| [Database](./database-guidelines.md) | Writing SQL/mapper |
| [Error Handling](./error-handling.md) | Throwing/catching exceptions |
| [Logging](./logging-guidelines.md) | Logging in business/utility class |
| [Quality](./quality-guidelines.md) | Code patterns, null annotations |
| [Demand Config Result Precompute](./demand-config-result-precompute.md) | Changing demand config result precompute, refresh, or cached query behavior |
| [Demand Lighting Circuit Inheritance](./demand-lighting-circuit-inheritance.md) | Changing lighting demand new-circuit inheritance, feedback-adjustment backfill, or DTX circuit sync |
| [Cache Object Model](./cache-object-model.md) | Changing cache object schema, persistence, rebuild, validation, or read behavior |
| [InfoCode API Contracts](./infocode-api-contracts.md) | Changing InfoCode API request/response contracts |
| [InfoCode RWD Take Value](./infocode-rwd-take-value.md) | Updating InfoCode RWD source matching and object binding |
| [RWD Relation Direction](./rwd-relation-direction.md) | Using setRelationFrom/setRelationTo with setObjFrom/setObjTo |
| [Running Monitor Strategy](./running-monitor-strategy.md) | Changing running monitor type strategy dispatch or object ranges |
| [Air Terminal API Contracts](./air-terminal-api-contracts.md) | Changing air terminal API response contracts |
| [IOT Persistence Query Rule](./iot-persistence-query-rule.md) | Changing IOT persistent query default ip-type removal, request-header gate, or the two shell config items |
| [Run Analysis Report Contracts](./run-analysis-report.md) | Changing run analysis report chapter data contracts |
| [Parking Statistics Contracts](./parking-statistics.md) | Changing parking project-level statistics behavior or response contracts |

---

## Tech Stack

Spring Boot + Nacos | MyBatis Plus + Druid | Redis + Caffeine | RabbitMQ, Kafka | XXL-JOB
