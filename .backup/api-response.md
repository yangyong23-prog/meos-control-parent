# API 响应规范

> **Why**: 统一响应格式让前端统一处理，错误处理逻辑一致。
> **What**: 统一 R<T> 包装，字段说明，分页格式。

---

## R 类字段说明

| 字段        | 类型      | 说明                             |
|-----------|---------|--------------------------------|
| code      | String  | 结果码，成功为 `"00000"`              |
| result    | String  | 成功标识 `"success"`，失败标识 `"fail"` |
| data      | T       | 响应数据                           |
| message   | String  | 面向用户的提示信息                      |
| count     | Integer | 返回记录条数                         |
| total     | Long    | 当前满足条件总行数                      |
| size      | Long    | 每页显示条数                         |
| current   | Long    | 当前页                            |
| pages     | Long    | 当前分页总页数                        |
| timestamp | long    | 响应时间戳                          |

---

## 成功响应

### 标准格式

```json
{
  "code": "00000",
  "result": "success",
  "data": { ... },
  "message": null,
  "timestamp": 1745272000000
}
```

### 分页格式

```json
{
  "code": "00000",
  "result": "success",
  "data": [
    ...
  ],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10,
  "timestamp": 1745272000000
}
```

---

## 错误响应

### 业务异常

```json
{
  "code": "B0001",
  "result": "fail",
  "data": null,
  "message": "操作失败",
  "cause": "详细错误信息",
  "timestamp": 1745272000000
}
```

### 系统异常

```json
{
  "code": "Z9999",
  "result": "fail",
  "data": null,
  "message": "系统繁忙，请稍候再试",
  "timestamp": 1745272000000
}
```

---

## 响应码规范

### 成功码

| code    | 说明                               |
|---------|----------------------------------|
| "00000" | 操作成功（ErrorCodeConstants.SUCCESS） |

### 错误码

所有 code 均使用 `ErrorCodeConstants` 中定义的值。

**常见错误码**：
| code | 说明 |
|------|------|
| "A0400" | 用户请求参数错误 |
| "A0402" | 无效的用户输入 |
| "A0410" | 请求必填参数为空 |
| "B0001" | 系统执行出错 |
| "C0110" | RPC 服务出错 |

**完整错误码请查询 `ErrorCodeConstants` 枚举类。**

---

## 敏感信息处理

### 响应过滤

```java
// ❌ 禁止：返回敏感字段
public UserVO getUser(Long id) {
    UserDO entity = mapper.selectById(id);
    return userConvert.toVO(entity); // 如果 VO 包含 password 等字段
}

// ✅ 正确：确保 VO 不包含敏感字段
public UserVO getUser(Long id) {
    UserDO entity = mapper.selectById(id);
    UserVO vo = userConvert.toVO(entity);
    vo.setPassword(null); // 额外保险
    return vo;
}
```

---

## DO/DON'T 速查

| DO                       | DON'T                        |
|--------------------------|------------------------------|
| `R.success(data)`        | 直接返回 entity                  |
| 统一 `R<T>` 包装             | `{code, message, data}` 各处定义 |
| `message` 面向用户           | `message` 放详细错误              |
| 分页用 `total/size/current` | 自定义分页字段                      |
| VO 不含敏感字段                | 返回完整 Entity                  |
