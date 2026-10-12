# 异常处理

> **Why**: 统一异常处理让错误信息一致，便于追踪和用户理解。
> **What**: 异常类型、全局处理器、禁止吞异常。

---

## 异常类型

### 业务异常

```java
// 抛出业务异常
throw new BusinessException(ErrorCodeConstants.XXX.getCode(), "错误提示信息");
```

**错误码**: 使用 `ErrorCodeConstants` 中定义的值。

### 外部服务异常

Feign 异常由 `BaseFeignClientService` 统一处理，**调用方无需捕获**。

---

## 全局异常处理器

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public R<?> handle(ApplicationException e) {
        return R.fail(e.getErrorCode(), e.getUserTip()).setCause(e.getLocalizedMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public R<?> handle(BusinessException e) {
        return R.fail(e.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get())
                .setCause(e.getLocalizedMessage());
    }

    @ExceptionHandler(FeignException.class)
    public R<?> handle(FeignException e) {
        return R.fail(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(),
                e instanceof CustomFeignException
                        ? ((CustomFeignException) e).getUserTip()
                        : ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handle(Exception e) {
        return R.fail(ResponseCode.SYSTEM_ERROR_B0001.getCode(),
                ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }
}
```

---

## 禁止吞异常

### 错误模式

```java
// ❌ 禁止：空 catch
catch(Exception e){}
```

### 正确模式

```java
// ✅ 抛出
catch(Exception e){throw new

BusinessException(...); }

// ✅ 返回默认值
        catch(
Exception e){return defaultValue; }

// ✅ 记录日志
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

**为什么禁止空 catch**: 异常被隐藏，问题难以排查。

---

## 异常信息脱敏

```java
// ❌ 禁止：敏感信息暴露
throw new BusinessException("Password incorrect: "+password);

// ✅ 正确
throw new

BusinessException("Username or password incorrect");
```

**禁止在日志/消息中打印**: 密码、密钥、Token、身份证号、银行卡号。

---

## 常见错误

1. **吞掉异常** — catch 块必须有处理逻辑
2. **暴露敏感数据** — 禁止在日志/消息中打印敏感信息
3. **笼统异常处理** — 先捕获具体异常
4. **未记录异常** — 应记录以便调试
5. **抛出原始 Exception** — 抛出具体异常类型
