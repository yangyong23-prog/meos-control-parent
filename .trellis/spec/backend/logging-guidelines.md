# Logging Guidelines

> **Rule**: Business class implements `LogComponent`. Utility class uses `LogExecutor`.
>
> **Why utility classes use `LogExecutor`**: utility/DTO classes must not implement `LogComponent`, because the `@JsonIgnore` default methods (`log()`, `logInstance()`, ...) on the interface still participate in Jackson serialization/deserialization and can throw on serialize/deserialize. `LogExecutor` takes the logger + tag as plain arguments and avoids this entirely.

---

## Business Class: LogComponent

```java
// ❌ FORBIDDEN
@Slf4j
public class UserService {
    log.info("message");
}

// ✅ CORRECT
public class UserService implements LogComponent {

    @Override
    public String logTag() {
        return "UserService";
    }

    public void doSomething() {
        String userId = "123";
        Exception e = new RuntimeException("error");

        logProjectInstance().info("Operation succeeded");
        logProjectInstance().warn("Potential issue", Pair.of("reason", "timeout"));
        logProjectInstance().error("Operation failed", e,
            Pair.of("userId", userId),
            Pair.of("action", "delete"));
    }
}
```

---

## Utility/DTO Class: LogExecutor

```java
// ❌ FORBIDDEN
@Slf4j
public class UserUtil {}

// ✅ CORRECT
public class UserUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUtil.class);
    private static final String LOG_TAG = "UserUtil";

    public static void doSomething() {
        String key = "value";

        LogExecutor.project().info(LOGGER, LOG_TAG, "Operation message",
            Arrays.asList(Pair.of("key", key)));
    }
}
```

---

## Method Selection

| Method | When |
|--------|------|
| `logProjectInstance()` | **Preferred** — includes project info |
| `logInstance()` | Not distinguishing project |

---

## Log Levels

| Level | Use |
|-------|-----|
| DEBUG | Debug info |
| INFO | Normal operation |
| WARN | Potential issue |
| ERROR | Error needing attention |

---

## ❌ FORBIDDEN: Log sensitive data

```java
// ❌ FORBIDDEN — sensitive data must never be logged
String password = "secret";
String token = "abc123";

LogExecutor.project().info(LOGGER, LOG_TAG, "Login",
    Arrays.asList(Pair.of("password", password)));

// ✅ CORRECT — log non-sensitive data only
String username = "user1";

LogExecutor.project().info(LOGGER, LOG_TAG, "Login failed",
    Arrays.asList(Pair.of("username", username)));
```

**Sensitive data**: password, token, secret, ID card, bank card.

---

## Common Mistake: `warn` has no varargs Pair overload

**Symptom**: `logProjectInstance().warn("msg", Pair.of(...), Pair.of(...))` fails to compile
（匹配到 `warn(String, Throwable, ...)` 重载，Pair 不是 Throwable）

**Cause**: `LogComponentProvider` 中 `trace` / `debug` / `info` 有 `(String, Pair...)` 变参重载，
但 `warn` 只有单 Pair 与 `List<Pair>` 版本

**Fix**:
```java
// ❌ 编译失败
logProjectInstance().warn("msg", Pair.of("a", 1), Pair.of("b", 2));

// ✅ CORRECT — 使用 List<Pair>
logProjectInstance().warn("msg",
        Arrays.asList(Pair.of("a", 1), Pair.of("b", 2)));
```
