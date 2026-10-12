# 工具使用规范

> **Why**: 统一工具类确保行为一致，避免重复造轮子。
> **What**: 使用项目已集成的 Apache Commons Lang3/Collections 工具类。

---

## 日期时间

### 当前时间

```java
// ✅ 正确：使用项目 DateUtils（支持多时区）
LocalDateTime now = DateUtils.now();

// ❌ 禁止：new Date()
Date now = new Date();
```

### 格式化与解析

```java
// 格式化
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formatted = now.format(formatter);

// 解析
LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
```

---

## 集合处理

### 集合判断

```java
import org.apache.commons.collections4.CollectionUtils;

// 判断非空
if(CollectionUtils.isNotEmpty(list)){...}

// 判断为空
        if(CollectionUtils.

isEmpty(list)){...}
```

### Stream 操作

```java
// 提取字段
List<String> names = users.stream()
                .map(User::getName)
                .collect(Collectors.toList());

// 过滤
List<UserDO> activeUsers = users.stream()
        .filter(u -> u.getStatus() == 1)
        .collect(Collectors.toList());
```

---

## 字符串处理

### 空判断

```java
import org.apache.commons.lang3.StringUtils;

if(StringUtils.isNotBlank(str)){...}
        if(StringUtils.

isBlank(str)){...}
```

### 常用操作

```java
// 去空格
StringUtils.trim(str)

// 判等
StringUtils.

equals(str1, str2)
```

---

## DO/DON'T 速查

| DO                                                | DON'T                                  |
|---------------------------------------------------|----------------------------------------|
| `DateUtils.now()`                                 | `new Date()`                           |
| `org.apache.commons.collections4.CollectionUtils` | `list != null && !list.isEmpty()`      |
| `org.apache.commons.lang3.StringUtils`            | `str != null && !str.trim().isEmpty()` |
