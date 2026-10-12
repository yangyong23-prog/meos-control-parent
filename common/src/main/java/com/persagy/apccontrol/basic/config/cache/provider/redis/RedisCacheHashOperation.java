package com.persagy.apccontrol.basic.config.cache.provider.redis;

import com.persagy.apccontrol.basic.config.cache.provider.CacheHashOperation;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Hash缓存操作类
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@SuppressWarnings("unchecked")
public class RedisCacheHashOperation extends AbstractRedisCacheOperation<HashOperations<String, String, Object>> implements CacheHashOperation {

    /**
     * 构造器
     *
     * @param redisCache Redis缓存操作类
     * @since v5.7.0
     */
    public RedisCacheHashOperation(RedisCacheTemplate redisCache) {
        super(() -> redisCache.delegate().opsForHash());
    }

    @Override
    public long delete(@NonNull String key, @NonNull String... hashKeys) {
        return getValue(opsDelegate().delete(key, (Object[]) hashKeys), () -> -1L);
    }

    @Override
    public boolean hasKey(@NonNull String key, @NonNull String hashKey) {
        return BooleanUtils.isTrue(opsDelegate().hasKey(key, hashKey));
    }

    @Override
    public <V> V get(@NonNull String key, @NonNull String hashKey, @Nullable Supplier<V> defaultNullValue) {
        return getValue((V) opsDelegate().get(key, hashKey), defaultNullValue);
    }

    @Override
    public <V> List<V> multiGet(@NonNull String key, @NonNull Collection<String> hashKeys) {
        return opsDelegate().multiGet(key, hashKeys).stream().map(item -> (V) item).collect(Collectors.toList());
    }

    @Override
    public void put(@NonNull String key, @NonNull String hashKey, @NonNull Object value) {
        opsDelegate().put(key, hashKey, value);
    }

    @Override
    public void putAll(@NonNull String key, @NonNull Map<String, Object> value) {
        opsDelegate().putAll(key, value);
    }

    @Override
    public Long increment(@NonNull String key, @NonNull String hashKey, long delta) {
        return opsDelegate().increment(key, hashKey, delta);
    }

    @Override
    public Double increment(@NonNull String key, @NonNull String hashKey, double delta) {
        return opsDelegate().increment(key, hashKey, delta);
    }

    @Override
    public Set<String> keys(@NonNull String key) {
        return getValue(opsDelegate().keys(key), HashSet::new);
    }

    @Override
    public long size(@NonNull String key) {
        return getValue(opsDelegate().size(key), () -> -1L);
    }
}
