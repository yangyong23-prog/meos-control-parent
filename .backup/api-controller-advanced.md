# Controller 进阶规范

> **Why**: 异常处理和分页是 API 健壮性的关键。
> **What**: 异常处理、分页查询、网关权限。

---

## 权限认证

### 网关统一处理

**权限认证由网关统一处理**，Controller 层无需处理用户认证。

```java
// ❌ 禁止：自行处理权限
if(!hasPermission(userId, "admin")){
        throw new

BusinessException("NO_PERMISSION");
}

// ✅ 正确：网关已处理，Controller 只负责业务逻辑
@PostMapping
public R<Void> createOrder(@RequestBody OrderRequest request) {
    orderService.create(request); // 权限由网关处理
}
```

**为什么**: 统一在网关处理，避免权限逻辑分散。

---

## 异常处理

### 业务异常抛出

```java
// Service 层
if(entity ==null){
        throw new

BusinessException(ErrorCodeConstants.POINT_CONFIG_NOT_FOUND);
}

// Controller 无需 try-catch
// GlobalExceptionHandler 统一处理
```

### 全局异常处理器

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public R<?> handle(ApplicationException e) {
        return R.fail(e.getErrorCode(), e.getUserTip()).setCause(e.getLocalizedMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public R<?> handle(BusinessException e) {
        return R.fail(e.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handle(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.fail(ErrorCodeConstants.USER_ERROR_A0402.getCode(), msg);
    }
}
```

### 常见异常处理

| 异常                                | 处理方式                                             |
|-----------------------------------|--------------------------------------------------|
| `ApplicationException`            | 业务错误，返回 `R.fail(code, msg)`                      |
| `BusinessException`               | 框架异常，返回 `R.fail(code, msg)`                      |
| `MethodArgumentNotValidException` | 参数校验错误                                           |
| `FeignException`                  | 外部服务调用错误，由 `BaseFeignClientService` 统一处理，调用方无需捕获 |

---

## 分页查询

### Controller 实现

```java

@GetMapping
public R<List<PointConfigVO>> list(
        @RequestParam(defaultValue = "1") Long page,
        @RequestParam(defaultValue = "10") Long size,
        PointConfigQueryParam param) {

    return R.success(pointConfigService.listPage(page, size, param));
}
```

### 响应格式

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

### 分页参数约定

| 参数   | 类型   | 默认值 | 说明      |
|------|------|-----|---------|
| page | Long | 1   | 页码，从1开始 |
| size | Long | 10  | 每页条数    |

---

## DO/DON'T 速查

| DO                | DON'T                  |
|-------------------|------------------------|
| 权限由网关处理           | Controller 自行校验权限      |
| 业务异常 Service 层抛出  | Controller 层 try-catch |
| `R.success(data)` | `R.ok()`               |
| page 从1开始         | page 从0开始              |
