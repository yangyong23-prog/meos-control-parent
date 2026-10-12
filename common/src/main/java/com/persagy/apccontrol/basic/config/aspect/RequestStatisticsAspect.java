package com.persagy.apccontrol.basic.config.aspect;

import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.properties.RequestStatisticsProperties;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * 请求统计切面
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Aspect
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestStatisticsAspect implements HandlerInterceptor, LogComponent {

    private final RequestStatisticsProperties requestStatisticsProperties;
    private final RedisCacheTemplate redisCacheTemplate;

    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerMethodPointcut() {
    }

    @Around("controllerMethodPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            if (!(point.getSignature() instanceof MethodSignature)) {
                logInstance().error("ProceedingJoinPoint.getSignature() is not a MethodSignature instanceof");
                return point.proceed();
            }
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (!(requestAttributes instanceof ServletRequestAttributes)) {
                logInstance().error("RequestContextHolder.getRequestAttributes() is not a HttpServletRequest instanceof");
                return point.proceed();
            }
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestURI = request.getRequestURI();
            String method = request.getMethod();
            String ipv4Address = StringUtils.defaultIfBlank(IpUtils.getIpv4Address(request), "unknown");
            String redisKey = requestStatisticsProperties.getRedisKeyPrefix();
            LocalDateTime now = LocalDateTime.now();
            Duration expireTime = Duration.ofHours(24);
            // groupByUri
            String groupBuUriKey = generateKey(now, redisKey, "groupByUri", method, requestURI);
            redisCacheTemplate.hash().increment(groupBuUriKey, generateHashKey(ipv4Address), 1);
            redisCacheTemplate.expire(groupBuUriKey, expireTime);
            // groupByIp
            String groupByIpKey = generateKey(now, redisKey, "groupByIp", ipv4Address);
            redisCacheTemplate.hash().increment(groupByIpKey, generateHashKey(method, requestURI), 1);
            redisCacheTemplate.expire(groupByIpKey, expireTime);
            // statisticByUri
            String statisticByUriKey = generateKey(now, redisKey, "statisticByUri");
            redisCacheTemplate.hash().increment(statisticByUriKey, generateHashKey(method, requestURI), 1);
            redisCacheTemplate.expire(statisticByUriKey, expireTime);
            // statisticByIp
            String statisticByIpKey = generateKey(now, redisKey, "statisticByIp");
            redisCacheTemplate.hash().increment(statisticByIpKey, generateHashKey(ipv4Address), 1);
            redisCacheTemplate.expire(statisticByIpKey, expireTime);
        } catch (Throwable e) {
            logProjectInstance().warn("请求统计执行失败", e);
        }
        return point.proceed();
    }

    /**
     * 生成HashKey
     *
     * @param items 元素
     * @return HashKey
     * @since 7.250403.0
     */
    private String generateHashKey(@NonNull String @NonNull ... items) {
        StringJoiner joiner = new StringJoiner("-");
        if (ArrayUtils.isNotEmpty(items)) {
            for (String item : items) {
                joiner.add(item);
            }
        }
        return joiner.toString();
    }

    /**
     * 生成Key
     *
     * @param dateTime  时间
     * @param originKey 原始Key
     * @param key       子Key
     * @param items     元素
     * @return 最终Key
     * @since 7.250403.0
     */
    private String generateKey(
            @NonNull LocalDateTime dateTime,
            @NonNull String originKey,
            @NonNull String key,
            @NonNull String @NonNull ... items) {
        StringJoiner joiner = new StringJoiner("-");
        joiner.add(originKey + CacheKeyPrefix.SEPARATOR + key);
        joiner.add(DateTimeFormatter.ofPattern("yyyyMMddHH").format(dateTime));
        if (ArrayUtils.isNotEmpty(items)) {
            for (String item : items) {
                joiner.add(item);
            }
        }
        return joiner.toString();
    }

    @Override
    public String logTag() {
        return "RequestStatistics";
    }
}
