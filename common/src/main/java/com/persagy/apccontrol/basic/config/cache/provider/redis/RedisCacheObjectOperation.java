package com.persagy.apccontrol.basic.config.cache.provider.redis;

import com.persagy.apccontrol.basic.config.cache.provider.CacheObjectOperation;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 对象缓存操作类
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@SuppressWarnings("unchecked")
public class RedisCacheObjectOperation extends AbstractRedisCacheOperation<ValueOperations<String, Object>> implements CacheObjectOperation {

    /**
     * 构造器
     *
     * @param redisCache Redis缓存操作类
     * @since v5.7.0
     */
    public RedisCacheObjectOperation(RedisCacheTemplate redisCache) {
        super(() -> redisCache.delegate().opsForValue());
    }

    @Override
    @Nullable
    public <V> V get(@NonNull String key, @Nullable Supplier<V> defaultNullValue) {
        return getValue((V) opsDelegate().get(key), defaultNullValue);
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value) {
        opsDelegate().set(key, value);
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value, long expireTime, @NonNull TimeUnit timeUnit) {
        opsDelegate().set(key, value, expireTime, timeUnit);
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value, @NonNull Duration expireTime) {
        opsDelegate().set(key, value, expireTime);
    }

    @Override
    @Nullable
    public Long increment(@NonNull String key, long factor) {
        return opsDelegate().increment(key, factor);
    }

    @Override
    @Nullable
    public Long decrement(@NonNull String key, long factor) {
        return opsDelegate().decrement(key, factor);
    }
}
