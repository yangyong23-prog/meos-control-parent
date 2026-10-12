package com.persagy.apccontrol.basic.outside.feign.interceptor;

import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.properties.feign.children.FeignRequestStatisticsProperties;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 请求统计拦截器
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeignRequestStatisticsInterceptor implements RequestInterceptor, LogComponent {

    private final FeignRequestStatisticsProperties feignRequestStatisticsProperties;
    private final RedisCacheTemplate redisCacheTemplate;

    @Override
    public void apply(RequestTemplate template) {
        List<String> serviceNames = feignRequestStatisticsProperties.getServiceNames();
        String redisKey = feignRequestStatisticsProperties.getRedisKey();
        String feignTargetName = template.feignTarget().name();
        if (!serviceNames.contains(feignTargetName)) {
            return;
        }
        StackTraceElement stackTrace = null;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            String className = stackTraceElement.getClassName();
            if (className.startsWith("com.persagy.apccontrol")
                    && !className.startsWith("com.persagy.apccontrol.basic.outside")) {
                stackTrace = stackTraceElement;
                break;
            }
        }
        if (Objects.nonNull(stackTrace)) {
            LocalDateTime now = LocalDateTime.now();
            String key = new StringJoiner("-")
                    .add(redisKey).add(feignTargetName)
                    .add(DateTimeFormatter.ofPattern("yyyyMMddHH").format(now)).toString();
            String hashKey = ClassUtils.getShortClassName(stackTrace.getClassName())
                    + "." + stackTrace.getMethodName()
                    + ":" + stackTrace.getLineNumber();
            redisCacheTemplate.hash().increment(key, hashKey, 1);
            redisCacheTemplate.expire(key, Duration.ofHours(24));
        }
    }

    @Override
    public String logTag() {
        return "RPC-RequestStatistics";
    }
}
