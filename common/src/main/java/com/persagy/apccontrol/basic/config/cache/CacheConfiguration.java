package com.persagy.apccontrol.basic.config.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.stream.Collectors;

/**
 * 缓存配置
 *
 * @author JiYinchuan
 * @see CacheKey
 * @since v5.1.0
 */
@EnableConfigurationProperties(CacheProperties.class)
@SpringBootConfiguration
@Slf4j
public class CacheConfiguration extends CachingConfigurerSupport {

    private static final String LOG_TAG = "缓存配置";

    @Autowired
    private ObjectMapper objectMapper;

    @Bean(CacheKey.REDIS_CACHE_MANAGER)
    @Primary
    public CacheManager redisCacheManager(
            CacheProperties cacheProperties, CacheManagerCustomizers cacheManagerCustomizers,
            RedisCacheConfiguration redisCacheConfiguration,
            ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers,
            RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheConfigurationImpl.createInstance(cacheProperties, cacheManagerCustomizers,
                redisCacheConfiguration, redisCacheManagerBuilderCustomizers, redisConnectionFactory);
    }

    @Bean(CacheKey.CAFFEINE_CACHE_MANAGER)
    public CacheManager caffeineCacheManager(
            CacheProperties cacheProperties, CacheManagerCustomizers customizers,
            ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec,
            ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        return CaffeineCacheConfigurationImpl.createInstance(cacheProperties, customizers,
                caffeine, caffeineSpec, cacheLoader);
    }

    @Bean
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
    }

    /**
     * KeyGenerator
     *
     * @return KeyGenerator
     * @since v5.1.0
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            String paramString;
            try {
                paramString = objectMapper.writeValueAsString(params).replaceAll(":", ";");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return target.getClass().getName()
                    + CacheKeyPrefix.SEPARATOR
                    + method.getName()
                    + CacheKeyPrefix.SEPARATOR
                    + paramString;
        };
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    @NonNull RuntimeException exception,
                    @NonNull Cache cache, @NonNull Object key) {
                log.warn("[{}] 获取缓存失败 [cacheName: {}, key: {}]", LOG_TAG, cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(
                    @NonNull RuntimeException exception, @NonNull Cache cache,
                    @NonNull Object key, @Nullable Object value) {
                log.warn("[{}] 更新缓存失败 [cacheName: {}, key: {}, targetValue: {}]", LOG_TAG,
                        cache.getName(), key, value, exception);
            }

            @Override
            public void handleCacheEvictError(
                    @NonNull RuntimeException exception,
                    @NonNull Cache cache, @NonNull Object key) {
                log.warn("[{}] 删除缓存失败 [cacheName: {}, key: {}]", LOG_TAG, cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                log.warn("[{}] 清空缓存失败 [cacheName: {}]", LOG_TAG, cache.getName(), exception);
            }
        };
    }
}
