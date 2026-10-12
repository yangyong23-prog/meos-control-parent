package com.persagy.apccontrol.basic.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.persagy.apccontrol.basic.exception.CustomFeignException;
import com.persagy.apccontrol.basic.utils.IpUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.exception.EnumIllegalArgumentException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局Controller异常处理
 *
 * <p>该类具体实现了全局请求异常拦截, 对于不同的异常有不同的拦截并返回, 无论是自定义异常/校验异常/未知异常等都具有详细的实现,
 * 拦截后给予客户端友好返回, 同时将异常规范的写出到日志文件中, 便于项目运行中通过日志排查问题
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@RestControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    private static final String LOG_TAG = "GlobalControllerAdvice";

    private static final Pattern ENUM_ILLEGAL_ARGUMENT_PRINT_PATTERN = Pattern.compile("(\\[.*])\\s");

    // ------------------------- 基础验证 -------------------------

    /**
     * 自定义异常全局捕获
     *
     * @param e       {@link ApplicationException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(ApplicationException.class)
    public R<?> generalErrorHandle(ApplicationException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 自定义异常信息 ------------------------- [Begin]", LOG_TAG);
        printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "自定义异常信息", clientIp, getRequestInfo(), null, true);
        LOGGER.warn("[{}] ------------------------- 自定义异常信息 ------------------------- [ End ]", LOG_TAG);
        return R.fail(String.valueOf(e.getErrorCode()), e.getUserTip()).setCause(e.getLocalizedMessage());
    }

    /**
     * 框架异常全局捕获
     *
     * @param e       {@link BusinessException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(BusinessException.class)
    public R<?> generalErrorHandle(BusinessException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 框架异常信息 ------------------------- [Begin]", LOG_TAG);
        printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "自定义异常信息", clientIp, getRequestInfo(), null, true);
        LOGGER.warn("[{}] ------------------------- 框架异常信息 ------------------------- [ End ]", LOG_TAG);
        return R.fail(e.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get()).setCause(e.getLocalizedMessage());
    }

    /**
     * Feign异常全局捕获
     *
     * @param e       {@link FeignException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(FeignException.class)
    public R<?> generalErrorHandle(FeignException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- Feign异常信息 ------------------------- [Begin]", LOG_TAG);
        printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "Feign异常信息", clientIp, getRequestInfo(), null, true);
        LOGGER.warn("[{}] -------------------------Feign异常信息 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(),
                e instanceof CustomFeignException
                        ? ((CustomFeignException) e).getUserTip()
                        : ApplicationException.DEFAULT_ERROR_USER_TIP.get(),
                e.getLocalizedMessage());
    }

    /**
     * 系统异常全局捕获
     *
     * @param e       {@link Exception}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(Exception.class)
    public R<?> serverErrorHandler(Exception e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.error("[{}] ------------------------- 系统异常信息 ------------------------- [Begin]", LOG_TAG);
        LOGGER.error("[{}] 系统异常信息 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                LOG_TAG, request.getRequestURI(), request.getMethod(), clientIp, getRequestInfo(), e.getLocalizedMessage(), e);
        LOGGER.error("[{}] ------------------------- 系统异常信息 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ResponseCode.SYSTEM_ERROR_B0001.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get())
                .setCause(ResponseCode.SYSTEM_ERROR_B0001.getDescription());
    }

    // ------------------------- 请求参数验证 -------------------------

    /**
     * 参数为空全局捕获
     * <p>RequestType = <b>{@link org.springframework.web.bind.annotation.RequestParam}</b>
     * <p>捕获来自 {@link org.springframework.web.bind.annotation.RequestParam}
     *
     * @param e       {@link MissingServletRequestParameterException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<?> validationExceptionHandler(MissingServletRequestParameterException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 参数为空 ------------------------- [Begin]", LOG_TAG);
        LOGGER.warn("[{}] 参数为空 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                LOG_TAG, request.getRequestURI(), request.getMethod(), clientIp, getRequestInfo(), e.getLocalizedMessage());
        LOGGER.warn("[{}] ------------------------- 参数为空 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.USER_ERROR_A0410).setCause(e.getMessage());
    }

    /**
     * 参数转换异常全局捕获
     *
     * @param e       {@link MethodArgumentTypeMismatchException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<?> validationExceptionHandler(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 参数转换异常 ------------------------- [Begin]", LOG_TAG);
        String errorMessage = this.printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "参数转换异常", clientIp, getRequestInfo(), e.getLocalizedMessage(), false);
        LOGGER.warn("[{}] ------------------------- 参数转换异常 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.USER_ERROR_A0421).setCause(e.getLocalizedMessage().equals(errorMessage)
                ? errorMessage : ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证异常全局捕获
     * <p>捕获来自 {@link javax.validation.constraints}
     *
     * @param e       {@link ValidationException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(ValidationException.class)
    public R<?> validationExceptionHandler(ValidationException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 验证异常 ------------------------- [Begin]", LOG_TAG);
        LOGGER.warn("[{}] 验证异常 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                LOG_TAG, request.getRequestURI(), request.getMethod(), clientIp, getRequestInfo(), e.getLocalizedMessage());
        this.printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "验证异常", clientIp, getRequestInfo(), null, true);
        LOGGER.warn("[{}] ------------------------- 验证异常 ------------------------- [ End ]", LOG_TAG);
        R<Object> response = R.fail(ResponseCode.USER_ERROR_A0402.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get());
        if (e instanceof ConstraintViolationException) {
            List<String> userTipMessages = new ArrayList<>();
            Set<ConstraintViolation<?>> constraintViolations = ((ConstraintViolationException) e).getConstraintViolations();
            for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                userTipMessages.add(constraintViolation.getMessage());
            }
            response.setCause(String.join(",", userTipMessages));
        } else {
            response.setMessage(e.getLocalizedMessage());
        }
        return response;
    }

    /**
     * 请求体异常全局捕获
     * <p>RequestType = <b>{@link org.springframework.web.bind.annotation.RequestBody}</b>
     * <p>Also throw to {@link InvalidFormatException}
     *
     * @param e       {@link HttpMessageNotReadableException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @see com.fasterxml.jackson.databind.DeserializationContext#weirdStringException(String, Class, String)
     * <p>Enum deserialize failed exception is unsolved
     * @since v5.7.0
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<?> validationExceptionHandler(HttpMessageNotReadableException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 请求体异常 ------------------------- [Begin]", LOG_TAG);
        String errorMessage = this.printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "请求体异常", clientIp, getRequestInfo(), e.getLocalizedMessage(), false);
        LOGGER.warn("[{}] 请求体异常 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                LOG_TAG, request.getRequestURI(), request.getMethod(), clientIp, getRequestInfo(), errorMessage);
        LOGGER.warn("[{}] ------------------------- 请求体异常 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.USER_ERROR_A0421).setCause(errorMessage);
    }

    /**
     * 绑定异常全局捕获
     *
     * <p>RequestType = <b>{@link org.springframework.web.bind.annotation.RequestParam}</b>
     *
     * @param e       {@link BindException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(BindException.class)
    public R<?> validationExceptionHandler(BindException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 绑定异常 ------------------------- [Begin]", LOG_TAG);
        List<String> errorMessages = analyzeAndPrintBindErrorMessage(e, request.getRequestURI(), request.getMethod(),
                LocaleMessageUtil.getMessage("key_b7103a545ce240ab9117505bbf9b7828"), clientIp, getRequestInfo());
        LOGGER.warn("[{}] ------------------------- 绑定异常 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.USER_ERROR_A0402).setCause(String.join(", ", errorMessages));
    }

    /**
     * 请求体验证全局捕获
     * <p>RequestType = <b>{@link org.springframework.web.bind.annotation.RequestBody}</b>
     * <p>捕获来自 {@link javax.validation.constraints}
     *
     * @param e       {@link MethodArgumentNotValidException}
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> validationExceptionHandler(MethodArgumentNotValidException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 请求体验证异常 ------------------------- [Begin]", LOG_TAG);
        List<String> errorMessages = analyzeAndPrintBindErrorMessage(e, request.getRequestURI(), request.getMethod(),
                LocaleMessageUtil.getMessage("key_715924608b3f4f758ea80e9c26ae0bd0"), clientIp, getRequestInfo());
        LOGGER.warn("[{}] ------------------------- 请求体验证异常 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ErrorCodeConstants.USER_ERROR_A0402).setCause(String.join(", ", errorMessages));
    }

    /**
     * 方法不被允许全局捕获
     *
     * @param e       HttpRequestMethodNotSupportedException
     * @param request {@link HttpServletRequest}
     * @return 结果响应
     * @since v5.7.0
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> validationExceptionHandler(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String clientIp = IpUtils.getIpv4Address(request);
        LOGGER.warn("[{}] ------------------------- 方法不被允许 ------------------------- [Begin]", LOG_TAG);
        LOGGER.warn("[{}] 方法不被允许 [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorMessage: {}]",
                LOG_TAG, request.getRequestURI(), request.getMethod(), clientIp, getRequestInfo(), e.getLocalizedMessage());
        this.printStackTraceFormat(request.getRequestURI(), request.getMethod(), e, e.getCause(),
                "方法不被允许", clientIp, getRequestInfo(), null, false);
        LOGGER.warn("[{}] ------------------------- 方法不被允许 ------------------------- [ End ]", LOG_TAG);
        return R.fail(ResponseCode.USER_ERROR_A0402.getCode(), ApplicationException.DEFAULT_ERROR_USER_TIP.get())
                .setCause(e.getLocalizedMessage());
    }

    /**
     * 格式化异常信息|堆栈异常打印
     *
     * <ul>
     *     <li>
     *         <b>EnumIllegalArgumentException</b> -
     *         为了保证输出安全, 采用正则表达式 {@code "(\\[.*])\\s"}
     *         来匹配 {@link BaseEnum#analyze(Object, Class)}
     *         中解析异常所抛出的 {@link EnumIllegalArgumentException} 异常信息, 将异常信息的全限定名进行删除后输出
     *     </li>
     *     <li>
     *         <b>DateTimeParseException</b> - 时间解析异常直接打印原始信息
     *     </li>
     * </ul>
     *
     * @param requestUri              请求地址
     * @param requestMethod           请求方法
     * @param tp                      {@code t} 父节点, 即原始节点
     * @param t                       {@code tp} 子节点, 当子节点未 {@code null} 时则不进行递归打印
     * @param tag                     日志标签
     * @param clientIp                客户端IP
     * @param requestInfo             自定义请求信息
     * @param originalErrorMessage    原始错误信息
     * @param isPrintStackTraceDetail 是否输出堆栈详细信息
     * @return 格式化后的错误信息
     * @see EnumIllegalArgumentException
     * @since v5.7.0
     */
    @Nullable
    private String printStackTraceFormat(@NonNull String requestUri, @NonNull String requestMethod,
                                         @NonNull Throwable tp, @Nullable Throwable t,
                                         @NonNull String tag, @NonNull String clientIp,
                                         @Nullable Object requestInfo, @Nullable String originalErrorMessage,
                                         boolean isPrintStackTraceDetail) {
        if (t == null) {
            if (tp instanceof EnumIllegalArgumentException) {
                Matcher matcher = ENUM_ILLEGAL_ARGUMENT_PRINT_PATTERN.matcher(tp.getLocalizedMessage());
                originalErrorMessage = tp.getLocalizedMessage();
                if (matcher.find()) {
                    originalErrorMessage = originalErrorMessage.replace(matcher.group(), "");
                }
            } else if (tp instanceof DateTimeParseException) {
                originalErrorMessage = tp.getLocalizedMessage();
            } else if (tp instanceof InvalidFormatException) {
                originalErrorMessage = tp.getLocalizedMessage().split(":")[0];
            }
            Throwable printThrowable = isPrintStackTraceDetail ? tp : null;
            LOGGER.warn("[{}] {} [requestUri: {}, requestMethod: {}, clientIp, {}, requestInfo: {}, errorMessage: {}]",
                    LOG_TAG, tag, requestUri, requestMethod, clientIp, requestInfo,
                    tp.getLocalizedMessage(), printThrowable);
            return originalErrorMessage;
        } else {
            return this.printStackTraceFormat(requestUri, requestMethod, t, t.getCause(),
                    tag, clientIp, requestInfo, originalErrorMessage, isPrintStackTraceDetail);
        }
    }

    /**
     * 获取请求信息
     *
     * @return 请求信息
     * @since v5.7.0
     */
    private String getRequestInfo() {
        return null;
    }

    /**
     * 解析并打印验证错误消息
     *
     * @param e             绑定异常
     * @param requestUri    请求地址
     * @param requestMethod 请求方法
     * @param tag           日志标签
     * @param clientIp      客户端IP
     * @param requestInfo   自定义请求信息
     * @return 解析后的验证错误信息
     * @since v5.7.0
     */
    private List<String> analyzeAndPrintBindErrorMessage(@NonNull BindException e, @NonNull String requestUri,
                                                         @NonNull String requestMethod, @NonNull String tag,
                                                         @NonNull String clientIp, @Nullable Object requestInfo) {
        List<String> result = new ArrayList<>();
        for (FieldError fieldError : e.getFieldErrors()) {
            result.add(String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()));
            LOGGER.warn("[{}] {} [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorType: @{}, errorMessage: {}, field: {}={}]",
                    LOG_TAG, tag, requestUri, requestMethod, clientIp, requestInfo, fieldError.getCode(),
                    fieldError.getDefaultMessage(), fieldError.getField(), fieldError.getRejectedValue());
        }
        if (!result.isEmpty()) {
            return result;
        }

        for (ObjectError globalError : e.getGlobalErrors()) {
            result.add(String.format("%s: %s", globalError.getObjectName(), globalError.getDefaultMessage()));
            LOGGER.warn("[{}] {} [requestUri: {}, requestMethod: {}, clientIp: {}, requestInfo: {}, errorType: @{}, errorMessage: {}, object: {}]",
                    LOG_TAG, tag, requestUri, requestMethod, clientIp, requestInfo, globalError.getCode(),
                    globalError.getDefaultMessage(), globalError.getObjectName());
        }
        if (!result.isEmpty()) {
            return result;
        }

        result.add(printStackTraceFormat(requestUri, requestMethod, e, e.getCause(),
                tag, clientIp, requestInfo, e.getLocalizedMessage(), false));
        return result;
    }
}
