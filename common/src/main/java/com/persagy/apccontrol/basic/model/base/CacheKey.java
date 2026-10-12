package com.persagy.apccontrol.basic.model.base;

import com.persagy.apccontrol.basic.config.cache.RedisCacheConfigurationImpl;

import java.time.Duration;

/**
 * 缓存常量
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public interface CacheKey {

    /**
     * Redis缓存管理器
     */
    String REDIS_CACHE_MANAGER = "RedisCacheManager";

    /**
     * Caffeine缓存管理器
     */
    String CAFFEINE_CACHE_MANAGER = "CaffeineCacheManager";

    /**
     * 默认过期时间
     */
    Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * 动态缓存Key
     *
     * @author JiYinchuan
     * @see RedisCacheConfigurationImpl
     * @see CacheDynamicKey
     * @since v5.1.0
     */
    class Dynamic implements CacheDynamicKey {

    }

    /**
     * 持久化缓存Key
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    class Persistence implements CachePersistenceKey {
    }

    /**
     * 锁缓存Key
     *
     * @author JiYinchuan
     * @since 7.250602.0
     */
    class Lock implements CacheLockKey {
    }
}
