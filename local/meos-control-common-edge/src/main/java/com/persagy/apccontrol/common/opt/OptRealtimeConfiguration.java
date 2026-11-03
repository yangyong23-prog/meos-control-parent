package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * B2：实时值缓存的 Bean。
 *
 * <p>契约序列化用独立的 ObjectMapper（不受应用全局 Jackson 配置影响，字段名固定）。
 * {@code opt.realtime.stale-seconds}（默认 120）：超过该时长没有新写入的缓存值视为陈旧，必须回源；
 * {@code opt.realtime.negative-seconds}（默认 60）：回源仍无有效值的点的负缓存时长。均为启动时读取。
 */
@Configuration
public class OptRealtimeConfiguration {

    @Bean
    public OptRealtimeCacheCore optRealtimeCacheCore(RedisCacheUtil redisCacheUtil,
                                                     @Value("${opt.realtime.stale-seconds:120}") long staleSeconds,
                                                     @Value("${opt.realtime.negative-seconds:60}") long negativeSeconds) {
        return new OptRealtimeCacheCore(new OptRedisRealtimeStore(redisCacheUtil), new ObjectMapper(),
                System::currentTimeMillis, staleSeconds, negativeSeconds);
    }
}
