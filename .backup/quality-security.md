# 安全禁止模式

> **Why**: 安全漏洞可能导致数据泄露或系统被攻陷，必须强制禁止。
> **What**: 敏感数据处理、注入防护、参数校验。

---

## SQL 注入

### 危险模式

```java
// ❌ 禁止：${} 直接拼接 SQL
@Select("SELECT * FROM user WHERE name = '${name}'")
List<UserDO> searchUsers(String name);
```

**为什么危险**: 攻击者输入 `' OR '1'='1` 可获取全部数据。

### 安全模式

```java
// ✅ 正确：#{} 参数绑定
@Select("SELECT * FROM user WHERE name = #{name}")
List<UserDO> searchUsers(@Param("name") String name);
```

---

## 日志记录敏感数据

### 禁止记录

| 敏感数据  | 禁止            | 替代           |
|-------|---------------|--------------|
| 密码    | `password={}` | 只记录 username |
| Token | `token={}`    | 只记录 userId   |
| 密钥    | `secret={}`   | 不记录          |
| 身份证   | `idCard={}`   | 只记录前三位后四位    |
| 银行卡   | `bankCard={}` | 只记录后四位       |

### 危险模式

```java
// ❌ 禁止
log.info("Login: password={}",password);
log.

info("Token: {}",token);
log.

info("User: idCard={}",idCard);
```

### 安全模式

```java
// ✅ 正确
LogExecutor.project().

info(LOGGER, LOG_TAG, "Login failed",
        null,Arrays.asList(Pair.of("username", username)));
        LogExecutor.

project().

info(LOGGER, LOG_TAG, "Token generated",
        null,Arrays.asList(Pair.of("userId", userId)));
// 不记录 idCard
```

---

## 参数篡改

### 危险模式

```java
// ❌ 禁止：信任前端传来的价格
public void createOrder(Long userId, BigDecimal price) {
    // price 由前端传入，可被篡改！
    order.setPrice(price);
}
```

### 安全模式

```java
// ✅ 正确：从服务端获取价格
public void createOrder(Long userId, Long productId) {
    ProductDO product = productService.getById(productId);
    order.setPrice(product.getPrice()); // 从服务端获取
}
```

---

## 文件上传安全

### 危险模式

```java
// ❌ 禁止：保存用户提供的文件名
String fileName = uploadFile.getOriginalFilename();
File file = new File("/uploads/" + fileName);
```

### 安全模式

```java
// ✅ 正确：重命名文件，使用 UUID
String originalFilename = uploadFile.getOriginalFilename();
String extension = FilenameUtils.getExtension(originalFilename);
String newFileName = UUID.randomUUID().toString() + "." + extension;

File file = new File("/uploads/" + newFileName);
uploadFile.

transferTo(file);
```

---

## DO/DON'T 速查

| DO         | DON'T           |
|------------|-----------------|
| `#{}` 参数绑定 | `${}` 拼接        |
| 服务端获取价格    | 信任前端传来价格        |
| UUID 文件名   | 原始文件名           |
| 不记录敏感字段    | log 密码/Token/密钥 |
