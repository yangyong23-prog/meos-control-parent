package com.persagy.apccontrol.basic.config.aspect;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * 请求性能追踪切面
 *
 * <p>拦截所有 Controller 方法，自动进行 {@link PerformanceTracker} 的入口埋点和结束后输出，
 * 无需在 Controller 中手动编写埋点代码。
 *
 * <p>执行顺序位于 {@link RequestStopWatchAspect}（order=100）之后，
 * 确保 StopWatch 先完成初始化再执行性能追踪。
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
@Aspect
@Component
public class RequestPerformanceTrackerAspect extends BaseAspect implements LogComponent, PriorityOrdered {

    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethodPointcut() {
    }

    @Around("controllerMethodPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = analyzeHttpServletRequest(point);
        if (Objects.isNull(request)) {
            return point.proceed();
        }
        MethodSignature methodSignature = analyzeMethodSignature(point);
        if (Objects.isNull(methodSignature)) {
            return point.proceed();
        }
        Method method = methodSignature.getMethod();
        Operation annotation = method.getAnnotation(Operation.class);
        String trackerId = Objects.nonNull(annotation) ? annotation.summary() : method.getName();
        String methodSimpleName = point.getTarget().getClass().getSimpleName() + "#" + method.getName();
        String methodFullPath = point.getTarget().getClass().getName() + "#" + method.getName();

        // 追踪器初始化：若初始化本身失败，直接降级执行主流程，确保主流程 100% 不受追踪逻辑干扰
        PerformanceTracker tracker;
        PerformanceTracker.SafeNode safeNode;
        try {
            tracker = PerformanceTracker.bind(trackerId);
            safeNode = tracker.track(methodSimpleName, methodFullPath);
        } catch (Throwable trackerInitError) {
            log().warn("[性能追踪] 初始化失败，降级执行主流程，原因: {}", trackerInitError.toString());
            // 清理可能半初始化的上下文，防止残留
            try {
                PerformanceTracker.clear();
            } catch (Throwable ignored) {
                // 清理失败也不影响主流程
            }
            return point.proceed();
        }

        // 追踪初始化成功，正常执行主流程
        try {
            try {
                return point.proceed();
            } finally {
                // SafeNode.close() 内部有幂等保护，不会抛异常影响主流程结果
                try {
                    safeNode.close();
                } catch (Throwable ignored) {
                    // 追踪关闭失败，静默处理，不影响主流程
                }
            }
        } finally {
            // 无论追踪是否完成，都必须清理上下文，防止线程池线程持有旧 tracker 导致内存泄漏
            try {
                if (tracker.isFinished()) {
                    LogComponent logComponent = new LogComponent() {

                        @Override
                        public Logger log() {
                            return LoggerFactory.getLogger(point.getTarget().getClass());
                        }

                        @Override
                        public String logTag() {
                            return "性能追踪器" + Thread.currentThread().getId();
                        }
                    };
                    if (logComponent.isDebugEnabled()) {
                        logComponent.logProjectInstance().debug(trackerId,
                                Pair.of("data", tracker.prettyPrint()));
                    }
                }
            } catch (Throwable ignored) {
                // 日志输出失败不影响主流程
            } finally {
                PerformanceTracker.clear();
            }
        }
    }

    @Override
    public String logTag() {
        return "请求性能追踪切面";
    }

    @Override
    public int getOrder() {
        return REQUEST_PERFORMANCE_TRACKER_ASPECT_ORDER;
    }
}
