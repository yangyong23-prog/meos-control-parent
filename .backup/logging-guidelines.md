# 日志规范

> **Why**: 统一日志组件确保日志格式一致，便于追踪和排查问题。
> **What**: LogComponent 业务类、LogExecutor 工具类、日志级别。

---

## 日志组件选择

### 业务类：LogComponent

Service/Controller 实现 `LogComponent` 接口，禁止使用 `@Slf4j`：

```java
public interface UserService extends LogComponent {
    default String logTag() {
        return "UserService";
    }
}
```

```java
// ❌ 禁止：业务类使用 @Slf4j
@Slf4j
public class UserServiceImpl {
    public void doSomething() {
        log.info("xxx"); // 禁止直接使用 log
    }
}

// ✅ 正确：实现 LogComponent
public class UserServiceImpl implements LogComponent {
    public void doSomething() {
        log.info("xxx"); // 使用继承的 log
    }
}
```

### 工具类：LogExecutor

DTO/工具类使用 `LogExecutor`，禁止实现 LogComponent（序列化问题）：

```java
public class UserDTO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDTO.class);
    private static final String LOG_TAG = "UserDTO";

    public void deserialize(String json) {
        LogExecutor.project().warn(LOGGER, LOG_TAG, "反序列化失败", e,
                Arrays.asList(Pair.of("json", json)));
    }
}
```

**禁止的工具类写法**：

```java
// ❌ 禁止：工具类直接使用 @Slf4j
@Slf4j
public class UserUtil {
    public static void format(String input) {
        log.info("format: {}", input); // 禁止
    }
}

// ✅ 正确：使用 LogExecutor
public class UserUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUtil.class);
    private static final String LOG_TAG = "UserUtil";

    public static void format(String input) {
        LogExecutor.project().info(LOGGER, LOG_TAG, "format", null,
                Arrays.asList(Pair.of("input", input)));
    }
}
```

---

## 方法选择

| 方法                       | 使用场景           |
|--------------------------|----------------|
| `LogExecutor.project()`  | **优先** — 项目级日志 |
| `LogExecutor.instance()` | 不区分集团和项目       |
| `LogExecutor.rpc()`      | RPC 调用日志       |

**为什么优先 project()**: 项目级日志包含集团信息，便于多项目追踪。

---

## 日志级别

| 级别    | 场景      |
|-------|---------|
| DEBUG | 调试信息    |
| INFO  | 正常操作    |
| WARN  | 潜在问题    |
| ERROR | 需要关注的错误 |

### 方法签名

```java
LogExecutor.project().

warn(LOGGER, LOG_TAG, "message",Throwable, List<Pair<String, Object>>)
```

**异常参数可选，结构化参数用 `Pair.of("key", value)`。**

---

## 安全

**禁止记录敏感数据**: 密码、密钥、Token、身份证号、银行卡号。

```java
// ❌ 禁止
LogExecutor.project().

info(LOGGER, LOG_TAG, "Login: password={}",null,...);

// ✅ 正确：记录非敏感信息
LogExecutor.

project().

info(LOGGER, LOG_TAG, "Login failed: username={}",null,
     Arrays.asList(Pair.of("username", username)));
```

---

## 常见错误

| 错误                  | 说明                 |
|---------------------|--------------------|
| 业务类使用 `@Slf4j`      | 实现 LogComponent 接口 |
| DTO 实现 LogComponent | 序列化问题              |
| 记录敏感数据              | 安全风险               |
| 使用 System.out       | 应用日志框架             |
| 工具类使用 `@Slf4j`      | 使用 LogExecutor     |
