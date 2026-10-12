package com.persagy.apccontrol.basic.config.aspect;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 切面基类
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
public abstract class BaseAspect implements LogComponent {

    protected static final int REQUEST_DEBUG_ASPECT_ORDER = 0;
    protected static final int REQUEST_STOP_WATCH_ASPECT_ORDER = 100;
    protected static final int REQUEST_PERFORMANCE_TRACKER_ASPECT_ORDER = 200;

    /**
     * 解析 HttpServletRequest
     *
     * @param point point
     * @return HttpServletRequest
     * @since 7.250302.0
     */
    protected @Nullable HttpServletRequest analyzeHttpServletRequest(@NonNull ProceedingJoinPoint point) {
        if (Objects.isNull(analyzeMethodSignature(point))) {
            logProjectInstance().error("ProceedingJoinPoint.getSignature() is not a MethodSignature instanceof");
            return null;
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        logProjectInstance().error("RequestContextHolder.getRequestAttributes() is not a HttpServletRequest instanceof");
        return null;
    }

    /**
     * 解析 MethodSignature
     *
     * @param point point
     * @return MethodSignature
     * @since 7.250302.0
     */
    protected @Nullable MethodSignature analyzeMethodSignature(@NonNull ProceedingJoinPoint point) {
        if (point.getSignature() instanceof MethodSignature) {
            return (MethodSignature) point.getSignature();
        }
        return null;
    }
}
