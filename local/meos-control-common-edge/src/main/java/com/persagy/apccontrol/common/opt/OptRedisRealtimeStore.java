package com.persagy.apccontrol.common.opt;

import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.data.redis.core.RedisCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * B2：用 RedisCacheUtil 所用连接直接读写原始字节，保证 key 为裸 key、值为普通 JSON（不受模板序列化器影响），
 * 与 bdtp / dtp-meos 的契约格式一致。MGET 每 500 个 key 一批。
 */
public class OptRedisRealtimeStore implements OptRealtimeStore {

    private static final int MGET_BATCH = 500;

    private final RedisCacheUtil redisCacheUtil;

    public OptRedisRealtimeStore(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public List<String> mget(List<String> keys) {
        List<String> result = new ArrayList<>(keys.size());
        for (int from = 0; from < keys.size(); from += MGET_BATCH) {
            List<String> part = keys.subList(from, Math.min(keys.size(), from + MGET_BATCH));
            byte[][] raw = new byte[part.size()][];
            for (int i = 0; i < part.size(); i++) {
                raw[i] = bytes(part.get(i));
            }
            List<byte[]> values = redisCacheUtil.getRedisTemplate()
                    .execute((RedisCallback<List<byte[]>>) connection -> connection.mGet(raw));
            for (int i = 0; i < part.size(); i++) {
                byte[] v = values == null || values.size() <= i ? null : values.get(i);
                result.add(v == null ? null : new String(v, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    @Override
    public String get(String key) {
        byte[] v = redisCacheUtil.getRedisTemplate()
                .execute((RedisCallback<byte[]>) connection -> connection.get(bytes(key)));
        return v == null ? null : new String(v, StandardCharsets.UTF_8);
    }

    @Override
    public void set(String key, String json) {
        redisCacheUtil.getRedisTemplate()
                .execute((RedisCallback<Boolean>) connection -> connection.set(bytes(key), bytes(json)));
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
