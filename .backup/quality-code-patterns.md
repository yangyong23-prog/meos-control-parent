# 代码禁止模式

> **Why**: 这些模式是历史 bug 的根源，影响性能或导致代码腐化。
> **What**: 日期处理、字符串拼接、循环查询、DTO 模式。

---

## 日期创建

```java
// ❌ 禁止：new Date()
LocalDateTime now = LocalDateTime.now(); // 可被 mock，测试不稳定，且不支持多时区

// ✅ 正确：使用项目 DateUtils（支持多时区）
LocalDateTime now = DateUtils.now();
```

---

## 字符串拼接

```java
// ❌ 禁止：+ 拼接（性能差 + SQL 注入风险）
String sql = "SELECT * FROM user WHERE name = '" + name + "'";

// ✅ 正确：StringBuilder 或参数绑定
StringBuilder sb = new StringBuilder();
sb.

append("用户 ").

append(userName).

append(" 已删除");
```

---

## 循环中数据库操作

```java
// ❌ 禁止：N+1 查询（100 个 ID = 100 次往返）
for(Long userId :userIds){
UserDO user = mapper.selectById(userId);
}

// ✅ 正确：批量查询
List<UserDO> users = mapper.selectBatchIds(userIds);
```

---

## DTO 使用 @Builder

```java
// ❌ 禁止：@Builder 在 DTO
@Data
@Builder
public class UserDTO {
}

UserDTO dto = UserDTO.builder().name("张三").build();

// ✅ 正确：@Data + 构造器
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
}

UserDTO dto = new UserDTO("张三", 18);
```

**为什么**: Jackson 序列化失败，MyBatis 构造器冲突。

---

## 方法调用省略 this

```java
// ❌ 禁止：继承时方法搜索顺序导致歧义
public void setName(String name) {
    setName(name); // 递归？父类？还是当前类？
}

// ✅ 正确
public void setName(String name) {
    this.name = name;  // 明确是当前类字段
}
```

---

## 吞掉异常

```java
// ❌ 禁止：空 catch 块
try{
doSomething();
}catch(
Exception e){}

// ✅ 正确：记录或抛出
        try{

doSomething();
}catch(
Exception e){
        LogExecutor.

project().

error(LOGGER, LOG_TAG, "操作失败",e, null);
    throw new

BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), "操作失败");
        }
```

---

## Service 返回 Entity

```java
// ❌ 禁止：暴露内部实体
public UserDO getUser(Long userId) {
    return mapper.selectById(userId);
}

// ✅ 正确：转换为 VO
public UserVO getUser(Long userId) {
    UserDO entity = mapper.selectById(userId);
    return userConvert.toVO(entity);
}
```

---

## DO/DON'T 速查

| DO                           | DON'T                                |
|------------------------------|--------------------------------------|
| `DateUtils.now()`            | `new Date()` / `LocalDateTime.now()` |
| StringBuilder                | `+` 拼接字符串                            |
| 批量查询                         | 循环单条查询                               |
| `@Data + @NoArgsConstructor` | `@Builder`                           |
| `this.method()`              | 省略 this                              |
| catch 后处理或抛出                 | 空 catch 块                            |
| 返回 VO 而非 Entity              | Service 返回 DO                        |
