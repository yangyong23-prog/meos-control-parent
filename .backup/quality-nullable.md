# 空值注解规范

> **Why**: 显式声明方法参数的空值约束，提高代码可读性，防止空指针异常。
> **What**: 所有新增方法必须使用 `@NonNull` 和 `@Nullable` 注解标注参数和返回值。

---

## 依赖

```xml

<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 注解说明

| 注解          | 说明                |
|-------------|-------------------|
| `@NonNull`  | 参数字段返回值不可为 `null` |
| `@Nullable` | 参数字段返回值可能为 `null` |

---

## 使用规则

### 参数注解

```java
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// ✅ 正确：明确标注每个参数

public User getUser(@NonNull String userId, @Nullable String filter) {
    // ...
}

// ❌ 错误：未标注空值约束
public User getUser(String userId, String filter) {
    // ...
}
```

### 返回值注解

```java
// ✅ 正确：明确标注返回值是否可能为 null（注解放在类型前）
public @NonNull String getUsername() {
    return username;
}

public @Nullable String getNickname() {
    return nickname;
}

// ❌ 错误：未标注返回值
public String getUsername() {
    return username;
}
```

### 集合返回值

```java
// ✅ 正确：集合返回非 null 空集合（注解放在类型前）
public @NonNull List<User> listUsers() {
    return Collections.emptyList();
}

// ⚠️ 注意：返回 null 会导致调用方 NPE 风险
```

---

## DO/DON'T 速查

| DO                                 | DON'T                      |
|------------------------------------|----------------------------|
| `@NonNull` 标注不可为 null 的参数          | 参数未标注空值约束                  |
| `@NonNull` 标注不可为 null 的返回值         | 返回值未标注空值约束                 |
| `@Nullable` 标注可能为 null 的参数         | `@Nullable` 用于确定非 null 的参数 |
| `@Nullable` 标注可能为 null 的返回值        |                            |
| `@NonNull` 标注集合类型返回值（返回空集合而非 null） | 集合返回 null                  |

---

## 示例

### Service 层

```java
public interface UserService {

    public @NonNull User getUser(@NonNull String userId);

    public @Nullable String getNickname(@NonNull String userId);

    public @NonNull List<User> listUsers(@Nullable String status);
}
```

### 实现类

```java

@Service
public class UserServiceImpl implements UserService {

    @Override
    public @NonNull User getUser(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public @Nullable String getNickname(@NonNull String userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getNickname() : null;
    }

    @Override
    public @NonNull List<User> listUsers(@Nullable String status) {
        if (status == null) {
            return userRepository.findAll();
        }
        return userRepository.findByStatus(status);
    }
}
```

---

## 与其他规范的关系

- **工具类使用 LogExecutor**：工具类无法实现 LogComponent，使用 LogExecutor 记录日志
- **DTO 禁止实现 LogComponent**：序列化问题，改为 LogExecutor
