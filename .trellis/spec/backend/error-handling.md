# Error Handling

> **Rule**: Business validation and business-rule failures use `BusinessException` with
> `ErrorCodeConstants`; messages that may cross an application boundary use `LocaleMessageUtil`.

---

## Exception Types

| Type | When to Use |
|------|-------------|
| `BusinessException` | Business rule violations |
| `ApplicationException` | Existing application-level framework errors; do not introduce it for business validation |
| `FeignException` | External service errors (handled by `BaseFeignClientService`, caller does NOT catch) |

---

## Throw Business Exception

```java
throw new BusinessException(
        ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
        LocaleMessageUtil.getMessage("key_xxx"));
```

Rules:

- Use a user error code only when the caller can correct a request parameter.
- Use `SYSTEM_ERROR_B0001` for internal cache construction, conversion, and consistency failures.
- Preserve the original cause with the three-argument constructor for serialization or other technical failures.
- Do not concatenate raw JSON, object IDs, tokens, or database rows into exception messages.

---

## ❌ FORBIDDEN: Empty catch block

```java
// ❌ FORBIDDEN — exception is hidden, impossible to debug
catch(Exception e) {}

// ✅ CORRECT — log and throw
catch(Exception e) {
    LogExecutor.project().error(LOGGER, LOG_TAG, "Operation failed", e);
    throw new BusinessException(
            ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
            LocaleMessageUtil.getMessage("key_xxx"),
            e);
}

// ✅ CORRECT — controlled degradation with an explicit contract and diagnostic log
catch(BusinessException e) {
    this.logProjectInstance().warn("Cache content is invalid; use the old path", e);
    return defaultValue;
}
```

Returning a default is allowed only when the method contract defines that fallback. It is not a
license to hide write failures, transaction failures, or database connectivity errors.

---

## Scenario: Cache Object Model Read Boundary

### 1. Scope / Trigger

Apply this scenario to the cache object model's object, relation, model-view, template-detail, and
rule reads. Once `READY` selects the new model, a request does not fall back to the old path. It does
not apply to cache writes, initialization, validation jobs, or database infrastructure errors.

### 2. Signatures

```java
@NonNull Map<BusinessKey, Result> mapResultsByBusinessKeys(
        @NonNull Collection<BusinessKey> businessKeys);

void replaceScope(@NonNull Scope scope, @NonNull Collection<Record> records)
        throws BusinessException;
```

### 3. Contracts

- Cache read resource services trust the caller-provided query object and do not repeatedly validate
  or de-duplicate collection elements. Write resource services remain strict before destructive
  replacement. Cache converters reuse `ObjectMapperUtils.writeValueAsStringFailSafe/readValueFailSafe`
  and do not catch or wrap Jackson exceptions themselves.
- Write validation is the single integrity boundary. It validates business keys, required content,
  and fail-safe conversion results before replacing the previous scope.
- After SQL succeeds, read services assemble whatever rows were returned. A missing object,
  relation, detail, or rule affects only that item; it does not clear the rest of the batch or select
  the old path.
- SQL, connection, timeout, delete, insert, and transaction exceptions continue upward.
- Write validation and conversion finish before destructive replacement begins; failures keep the
  previous complete cache and must not mark the cache `READY`.

### 4. Validation & Error Matrix

| Condition | Result |
| --- | --- |
| Invalid write business key | Throw internationalized `BusinessException` before replacement |
| Missing cache row | Return the other rows that were found |
| Duplicate association row | Let the result collection de-duplicate it |
| Valid persisted `[]` result | Return a valid empty business result |
| SQL or connection failure | Propagate; do not disguise as cache miss |
| Invalid write record, fail-safe write result is `null`, or conflicting business key | Throw `BusinessException` before delete |
| Delete or insert failure | Propagate and roll back |

### 5. Good / Base / Bad Cases

- Good: a batch object lookup returns all rows found even when one requested ID is absent.
- Base: a missing template-detail key becomes an empty result for that aggregate only.
- Bad: catching the mapper call and returning empty on a database outage.
- Bad: comparing requested and returned ID sets at every consumer and clearing valid results when one
  row is absent.

### 6. Tests Required

- Assert invalid caller data throws `BusinessException` with an existing i18n key in Chinese and English.
- Assert a missing row does not clear other rows and does not trigger the old path.
- Assert valid `[]` remains a hit.
- Assert mapper and transaction exceptions are not swallowed.
- Assert write validation occurs before delete and insert failures roll back.

### 7. Wrong vs Correct

```java
// Wrong: hides infrastructure failure and may enable incomplete data.
try {
    return mapper.list(param);
} catch (Exception exception) {
    return Collections.emptyList();
}

// Correct: query once and map the rows that were actually returned.
List<Record> records = mapper.list(param);
return records.stream()
        .map(converter::toResult)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
```

---

## ❌ FORBIDDEN: Expose sensitive data

```java
// ❌ FORBIDDEN — leaks internal details
String password = "secret";
throw new BusinessException("Password incorrect: " + password);

// ✅ CORRECT — use generic message
throw new BusinessException("Username or password incorrect");
```

**Never in logs or messages**: password, token, secret, ID card, bank card.

---

## Global Handler

`GlobalControllerAdvice` converts uncaught exceptions to `R.fail()`. Changing an exception from
`IllegalArgumentException` to `BusinessException` does not make it a normal response; non-fatal cache
read failures must be handled at the cache boundary before they reach the controller.
