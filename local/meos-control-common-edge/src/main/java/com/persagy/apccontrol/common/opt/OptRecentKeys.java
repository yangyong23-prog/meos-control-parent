package com.persagy.apccontrol.common.opt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * 进程内“最近处理过的 key”集合（Caffeine，写入后 TTL 过期，有上限）。
 *
 * <p>B1 用于自动日志建档按 traceId 去重：同一指令的多帧“未完成”报文只有第一帧需要查库建档。
 */
public class OptRecentKeys {

    private final Cache<String, Boolean> cache;

    public OptRecentKeys(long ttl, TimeUnit unit, long maxSize) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(ttl, unit).maximumSize(maxSize).build();
    }

    public boolean contains(String key) {
        return cache.getIfPresent(key) != null;
    }

    public void mark(String key) {
        cache.put(key, Boolean.TRUE);
    }

    public static String key(String... parts) {
        return String.join("|", parts);
    }
}
