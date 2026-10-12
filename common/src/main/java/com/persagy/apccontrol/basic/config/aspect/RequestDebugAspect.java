package com.persagy.apccontrol.basic.config.aspect;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.entity.R;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.event.Level;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求调试切面
 *
 * @author JiYinchuan
 * @since 5.241001.0
 */
@Aspect
@Component
public class RequestDebugAspect extends BaseAspect implements LogComponent, PriorityOrdered {

    /**
     * Header-是否输出调试信息
     */
    public static final String REQUEST_DEBUG_HEADER = "outputDebugMessage";

    /**
     * Key-调试信息键
     */
    private static final String REQUEST_DEBUG_KEY = "debugMessage";

    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethodPointcut() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Around("controllerMethodPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            HttpServletRequest request = analyzeHttpServletRequest(point);
            if (Objects.isNull(request)) {
                return point.proceed();
            }
            String level = request.getHeader(REQUEST_DEBUG_HEADER);
            if (StringUtils.isNotBlank(level)) {
                LocalContextStorage.requestDebugLevel(parseRequestDebugLevel(level));
                LocalContextStorage.REQUEST_DEBUG_DETAIL.set(Lists.newArrayList());
            }
            Object result = point.proceed();
            if (LocalContextStorage.containsRequestDebug() && result instanceof R) {
                R tempResult = (R) result;
                Map extra = Optional.ofNullable(tempResult.getExtra()).orElseGet(HashMap::new);
                extra.put(REQUEST_DEBUG_KEY, LocalContextStorage.getRequestDebug());
                tempResult.setExtra(extra);
                return tempResult;
            }
            return result;
        } finally {
            LocalContextStorage.clearRequestDebug();
        }
    }

    /**
     * 解析请求调试级别
     *
     * @param level 请求调试级别
     * @return 请求调试级别
     * @since 5.241201.0
     */
    private Level parseRequestDebugLevel(@Nullable String level) {
        if (StringUtils.isBlank(level)) {
            return Level.DEBUG;
        }
        return Level.valueOf(level.toUpperCase());
    }

    @Override
    public String logTag() {
        return "请求调试切面";
    }

    @Override
    public int getOrder() {
        return REQUEST_DEBUG_ASPECT_ORDER;
    }
}
