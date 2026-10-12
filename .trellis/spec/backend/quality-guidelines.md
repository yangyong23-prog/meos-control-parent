# Quality Guidelines

> **Rule**: These patterns MUST be followed. They are the root cause of historical bugs.

---

## Date Creation

```java
// ❌ FORBIDDEN
LocalDateTime.now()
new Date()

// ✅ CORRECT
DateUtils.now()
```

---

## LocalTime Boundary Loops

```java
// ❌ FORBIDDEN — LocalTime wraps around midnight, so this may never exit
LocalTime beforeTime = currentTime.minusMinutes(15);
while (!beforeTime.isBefore(startTime)) {
    beforeTime = beforeTime.minusMinutes(15);
}
```

```java
// ✅ CORRECT — use a LocalDateTime timeline before stepping across midnight
LocalDate baseDate = LocalDate.of(2000, 1, 1);
LocalDateTime startDateTime = LocalDateTime.of(baseDate, startTime);
LocalDateTime beforeDateTime = LocalDateTime.of(baseDate, currentTime).minusMinutes(15);
while (!beforeDateTime.isBefore(startDateTime)) {
    // check prior point
    beforeDateTime = beforeDateTime.minusMinutes(15);
}
```

`LocalTime` has no date component. `LocalTime.MIN.minusMinutes(15)` becomes `23:45`, so any loop that compares only `LocalTime.isBefore(...)` can cross midnight and keep running.

---

## DTO Pattern

```java
// ❌ FORBIDDEN — Jackson serialization failure, MyBatis constructor conflict
@Data @Builder
public class UserDTO {}

// ✅ CORRECT
@Data @NoArgsConstructor @AllArgsConstructor
public class UserDTO {}
```

---

## Object Mapping

Use project utility wrappers for Jackson conversion in new and touched business code.

```java
// ❌ FORBIDDEN — bypasses project-wide ObjectMapper utility behavior
private final ObjectMapper objectMapper;
JsonNode json = this.objectMapper.convertValue(config, JsonNode.class);
```

```java
// ✅ CORRECT
String json = ObjectMapperUtils.writeValueAsStringFailSafe(config);
JsonNode jsonNode = StringUtils.isBlank(json)
        ? null
        : ObjectMapperUtils.readValueFailSafe(json, JsonNode.class);
```

Rules:

- 缓存对象模型的对象转换使用 `writeValueAsStringFailSafe(...) + readValueFailSafe(...)`，不得在 Converter 中自行捕获或包装 Jackson 异常。
- fail-safe 返回值按 `@Nullable` 处理：缓存对象模型在写入方破坏旧数据前完成业务完整性校验，读取方返回实际查询和转换到的结果，不重复做整批完整性判断。
- 标注 `@NonNull` 的参数由调用方保证，方法内部不重复判空；集合元素未声明非空时仍需按业务契约检查。
- Keep direct `ObjectMapper` usage only when a supported `ObjectMapperUtils` API cannot express the operation.
- If direct `ObjectMapper` is required, add a short comment explaining the unsupported operation.

---

## Project Context

Service and repository business methods should read the current project from context instead of passing `projectId` through ordinary method signatures.

```java
// ❌ FORBIDDEN — projectId is duplicated across service/repository call chains
public void refresh(@NonNull String projectId, @NonNull Long templateId) {
    param.setProjectId(projectId);
}
```

```java
// ✅ CORRECT — project context is centralized
public void refresh(@NonNull Long templateId) {
    param.setProjectId(ContextUtil.getProjectId());
}
```

Rules:

- Use `ContextUtil.getProjectId()` in service/repository implementation code for current-project data.
- Mapper interfaces and XML SQL boundaries may still accept explicit `projectId` when it is required as a SQL condition.
- Do not pass `projectId` only to forward it unchanged through several layers.

---

## Null Handling

Do not use `Objects.requireNonNull(...)` to force business data to exist. Handle absence according to the business contract.

```java
// ❌ FORBIDDEN — throws a generic NullPointerException with poor business context
DemandTemplate template = Objects.requireNonNull(param.queryAndGetTemplate());
```

```java
// ✅ CORRECT — explicit, internationalized business error
DemandTemplate template = param.queryAndGetTemplate();
if (Objects.isNull(template)) {
    throw new BusinessException(
            ErrorCodeConstants.USER_ERROR_A0402.getCode(),
            LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
}
```

```java
// ✅ CORRECT — absence means no work to do
List<User> users = repository.list(param);
if (CollectionUtils.isEmpty(users)) {
    return Collections.emptyList();
}
```

Rules:

- Throw `BusinessException` with an internationalized message when the missing value violates a
  business rule. Use a user error code only when the caller can correct the request.
- Return an empty collection or return early when the missing value means there is no matching work.
- Reserve `Objects.requireNonNull(...)` for internal invariant checks where a failure is a programming error, not a business condition.

---

## Use this / super

```java
// ❌ FORBIDDEN — ambiguous on inheritance, may cause infinite recursion
public void setName(String name) {
    setName(name); // Recursive call to itself?
}

// ✅ CORRECT — use this for field assignment
public void setName(String name) {
    this.name = name;
}
```

```java
// ❌ FORBIDDEN — missing super call when overriding parent method
public void doSomething() {
    doAnotherThing(); // Which method? Parent or current class?
}

// ✅ CORRECT — use super when overriding parent methods
public void doSomething() {
    super.doSomething();
    doAnotherThing();
}
```

When calling inherited helpers from a subclass, make the parent dispatch explicit:

```java
// ✅ CORRECT — inherited helper is explicit
resultList.forEach(result -> super.fillInsertDefaultValue(result));
```

Rules:

- Access fields and call instance methods declared by the current class through `this.`.
- Call inherited methods through `super.` when the implementation intentionally depends on parent behavior.
- Use `this(...)` or `super(...)` explicitly for constructor delegation.
- Reference static fields and methods through their declaring class; static members cannot use `this.` or `super.`.
- Apply the same rule to production code and tests so examples do not introduce a conflicting style.

---

## Null Annotations

```java
// ✅ CORRECT — annotate all parameters and return values
public @NonNull User getUser(@NonNull String id, @Nullable String filter);

// ✅ CORRECT — collection returns must be non-null
public @NonNull List<User> listUsers() {
    return Collections.emptyList();
}
```

Rules:

- Use `org.jspecify.annotations.NonNull` and `org.jspecify.annotations.Nullable`; do not use `org.springframework.lang.NonNull` or `org.springframework.lang.Nullable`.
- Annotate reference-type method returns and parameters unless the contract intentionally accepts both null and non-null.
- Do not annotate primitive types such as `boolean`, `int`, or `long`.
- Do not add nullness annotations to database entity fields; entity nullability is defined by table schema and persistence behavior.

### JSpecify annotations on nested return types

For a qualified nested type, put the JSpecify annotation on the nested simple-name rather than on
the outer type qualifier. The wrong position can interrupt the current Java 8 MapStruct/Lombok
annotation-processing chain and produce misleading module-wide errors such as missing `Fields`
classes and missing `RequiredArgsConstructor.onConstructor_`.

```java
// ❌ FORBIDDEN — @NonNull is attached to the Outer qualifier
@Mapper
public interface UserConverter {
    @NonNull UserVO.DetailVO toDetail(@NonNull UserDTO source);
}

// ✅ CORRECT — @NonNull is attached to the nested DetailVO type
@Mapper
public interface UserConverter {
    UserVO.@NonNull DetailVO toDetail(@NonNull UserDTO source);
}
```

Top-level types continue to use the normal form such as `@Nullable UserVO`. Do not remove JSpecify
or replace it with Spring nullness annotations to work around this nested-type placement issue.

---

## Javadoc @since

```java
/**
 * Get user by ID
 *
 * @param id user ID, should not be null
 * @param filter optional filter string, may be null
 * @return user info, never null
 * @since v7.260401.0
 */
public @NonNull User getUser(@NonNull String id, @Nullable String filter);
```

| Scenario | Javadoc | @since |
|----------|---------|--------|
| New class/interface/method | Required | Required (`v7.260401.0`) |
| Modify existing | Required | **DO NOT update** |
| @Override method | Not needed | Not needed |

**Version format**: `v7.{YY}{MM}{iteration}.{sequence}` e.g., `v7.260401.0`

Class Javadocs for newly created code must include the current developer author and the current iteration:

```java
/**
 * Demand config result service.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigResultServiceImpl {
}
```

### Javadoc and Complex Logic Comments

Javadoc is part of the implementation contract and must be reviewable together with code:

- New classes must describe their responsibility and include `@author` and the current iteration `@since`.
- New constants and instance fields must document their responsibility and include the current iteration `@since`.
- New public, protected, and package-visible methods or constructors must document every parameter, non-void return value, observable exception, and `@since`.
- Private methods that implement business rules, data transformations, transaction boundaries, merge precedence, filtering, recursion, batching, or compatibility behavior must have Javadoc or an equivalent block comment explaining the invariant and reason.
- Add short inline comments before critical branches or complex blocks when the reason is not evident from code, especially for data precedence, atomic replacement, failure retention, and performance-sensitive batching.
- Comments must explain **why** the code is structured this way or what invariant it protects. Do not narrate obvious assignments or repeat the method name.

```java
// ❌ FORBIDDEN — does not explain the contract or precedence
mergedFields.putAll(commonFields); // Put common fields

// ✅ CORRECT — independent columns are authoritative and must override dirty JSON
mergedFields.putAll(commonFields);
```

---

## Method Order

Within the same class or interface, order methods by operation type:

1. `get` single-record queries
2. `list` batch/list queries
3. check/query helper methods
4. add/insert/copy methods
5. edit/update/close methods
6. delete/remove/clear methods

Lifecycle event handlers, private conversion helpers, and `logTag()` should stay after the main business methods unless existing local style clearly groups them elsewhere.

```java
// ✅ CORRECT
public Optional<User> getOptById(Long id);
public List<User> list(UserQueryParam param);
public void insertUsers(Collection<User> users);
public void updateEnabled(Long id, boolean enabled);
public void removeByIds(Collection<Long> ids);
```
