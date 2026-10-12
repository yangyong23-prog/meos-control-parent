package com.persagy.apccontrol.basic.config.aspect;

import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.performance.StopWatchContext;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 请求调试切面
 *
 * @author JiYinchuan
 * @since 5.241001.0
 */
@Aspect
@Component
public class RequestStopWatchAspect extends BaseAspect implements LogComponent, PriorityOrdered {

    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethodPointcut() {
    }

    @Around("controllerMethodPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            HttpServletRequest request = analyzeHttpServletRequest(point);
            if (Objects.isNull(request)) {
                return point.proceed();
            }

            LocalContextStorage.request(request);
            MethodSignature methodSignature = analyzeMethodSignature(point);
            if (Objects.isNull(methodSignature)) {
                return point.proceed();
            }
            Method method = methodSignature.getMethod();
            Operation annotation = method.getAnnotation(Operation.class);
            String stopWatchId = Objects.nonNull(annotation) ? annotation.summary() : method.getName();
            LocalContextStorage.stopWatch(stopWatchId);
            Object result = point.proceed();
            StopWatchContext stopWatch = LocalContextStorage.stopWatch(stopWatchId);
            if (Objects.nonNull(stopWatch) && ArrayUtils.isNotEmpty(stopWatch.getTaskInfo())) {
                LogComponent logComponent = new LogComponent() {

                    @Override
                    public Logger log() {
                        return LoggerFactory.getLogger(point.getTarget().getClass());
                    }

                    @Override
                    public String logTag() {
                        return "性能计时器" + Thread.currentThread().getId();
                    }
                };
                if (logComponent.isDebugEnabled()) {
                    logComponent.logProjectInstance().debug(stopWatchId,
                            Pair.of("data", stopWatch.prettyPrintByGroup(TimeUnit.SECONDS)));
                }
            }
            return result;
        } finally {
            LocalContextStorage.clearStopWatch();
            LocalContextStorage.clearRequest();
        }
    }

    @Override
    public String logTag() {
        return "请求计时器切面";
    }

    @Override
    public int getOrder() {
        return REQUEST_STOP_WATCH_ASPECT_ORDER;
    }
}
