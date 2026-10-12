package com.persagy.apccontrol.basic.config.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.retry.RetryContext;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 重试方式监听器
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@SpringBootConfiguration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RetryMethodListener extends MethodInvocationRetryListenerSupport implements LogComponent {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    protected <T, E extends Throwable> void doClose(
            RetryContext context, MethodInvocationRetryCallback<T, E> callback, Throwable throwable) {
        boolean isExecuteFinish = BooleanUtils.isNotTrue((Boolean) context.getAttribute(RetryContext.EXHAUSTED));
        String logTag = isExecuteFinish ? "重试执行完毕" : "重试执行失败";
        Level logLevel = isExecuteFinish ? Level.TRACE : Level.ERROR;
        printInvocationRetryMethodLog(logLevel, logTag, callback, throwable,
                Pair.of("总重试次数", context.getRetryCount())
        );
    }

    @Override
    @SneakyThrows
    protected <T, E extends Throwable> void doOnError(
            RetryContext context, MethodInvocationRetryCallback<T, E> callback, Throwable throwable) {
        printInvocationRetryMethodLog(Level.WARN, LocaleMessageUtil.getMessage("key_2bb1a278987047969086d5578f34a36b"), callback, throwable,
                Pair.of(LocaleMessageUtil.getMessage("key_04ebe41764b14263a9b4939c6d07b65c"), context.getRetryCount()));
    }

    @Override
    @SneakyThrows
    protected <T, E extends Throwable> boolean doOpen(
            RetryContext context, MethodInvocationRetryCallback<T, E> callback) {
        printInvocationRetryMethodLog(Level.TRACE, LocaleMessageUtil.getMessage("key_c69c113baae7411a9af1bddfc6d2ca02"), callback, null);
        return true;
    }

    /**
     * 打印重试方法日志信息
     *
     * @param level          日志级别
     * @param logTag         日志标签
     * @param callback       方法重试回调
     * @param throwable      异常
     * @param additionalArgs 附加参数
     * @param <T>            方法返回类型
     * @param <E>            异常类型
     * @since 5.241201.0
     */
    @SafeVarargs
    @SneakyThrows
    private final <T, E extends Throwable> void printInvocationRetryMethodLog(
            @NonNull Level level, @NonNull String logTag,
            @NonNull MethodInvocationRetryCallback<T, E> callback,
            @Nullable Throwable throwable,
            @Nullable Pair<String, Object>... additionalArgs) {
        MethodInvocation invocation = callback.getInvocation();
        String methodName = invocation.getMethod().getDeclaringClass().getName()
                + "#" + invocation.getMethod().getName()
                + "(" + (invocation.getArguments().length > 0 ? "..." : "") + ")";
        List<Pair<String, Object>> additionalMessages = new ArrayList<>();
        additionalMessages.add(Pair.of("方法地址", methodName));
        if (isTraceEnabled() || isErrorEnabled()) {
            additionalMessages.add(Pair.of("方法参数", objectMapper.writeValueAsString(invocation.getArguments())));
        }
        if (Objects.nonNull(additionalArgs) && additionalArgs.length > 0) {
            additionalMessages.addAll(Arrays.asList(additionalArgs));
        }
        logProjectInstance().log(level, logTag, throwable, additionalMessages);
    }

    @Override
    public String logTag() {
        return "重试方法监听器";
    }
}
