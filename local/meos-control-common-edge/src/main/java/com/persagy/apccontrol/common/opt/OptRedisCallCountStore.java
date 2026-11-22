package com.persagy.apccontrol.common.opt;

import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * B3 计数的 Redis 实现：连接级读写裸 key（与 {@link OptRedisRealtimeStore} 同一做法，不受模板序列化器影响）。
 */
public class OptRedisCallCountStore implements OptDtpMeosCallCounter.Store {

    private final RedisCacheUtil redisCacheUtil;

    public OptRedisCallCountStore(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public void increment(String key, String field, long keepSeconds) {
        byte[] k = bytes(key);
        template().execute((RedisCallback<Object>) connection -> {
            Long v = connection.hIncrBy(k, bytes(field), 1L);
            if (v != null && v == 1L) {
                connection.expire(k, keepSeconds);
            }
            return v;
        });
    }

    @Override
    public void put(String key, String field, String value) {
        template().execute((RedisCallback<Boolean>) connection -> connection.hSet(bytes(key), bytes(field), bytes(value)));
    }

    @Override
    public Map<String, String> entries(String key) {
        Map<byte[], byte[]> raw = template().execute((RedisCallback<Map<byte[], byte[]>>) connection -> connection.hGetAll(bytes(key)));
        Map<String, String> result = new LinkedHashMap<>();
        if (raw != null) {
            raw.forEach((f, v) -> result.put(new String(f, StandardCharsets.UTF_8), v == null ? null : new String(v, StandardCharsets.UTF_8)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private RedisOperations<Object, Object> template() {
        return (RedisOperations<Object, Object>) redisCacheUtil.getRedisTemplate();
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
