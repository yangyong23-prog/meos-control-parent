package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * B2：实时值缓存的核心逻辑（与 Spring、Twin 客户端无关，便于单测）。
 *
 * <ul>
 *     <li>写：比较 dataTime，旧帧不覆盖；同一 dataTime 同值的重复帧（两个主题并存时）不重复写。</li>
 *     <li>读：批量 MGET，按 {@link #isUsable} 判定可用；不可用的再看 60 秒负缓存，剩下的交给调用方回源。</li>
 * </ul>
 */
@Slf4j
public class OptRealtimeCacheCore {

    public static final String KEY_PREFIX = "apc:iot_real_time_data:";

    /** 负缓存里“Twin 未返回该点”的占位 */
    private static final Object NULL_MARKER = new Object();

    private static final DateTimeFormatter DATA_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OptRealtimeStore store;
    private final ObjectMapper mapper;
    private final LongSupplier clock;
    private final long staleMillis;
    /** 本进程写过的 key → dataTime，避免每帧先 GET */
    private final Map<String, String> lastDataTime = new ConcurrentHashMap<>();
    private final Map<String, String> lastValue = new ConcurrentHashMap<>();
    /** 回源后仍无有效值的点：key → 回源原样结果（可能为 null 表示 Twin 未返回该点） */
    private final Cache<String, Object> negative;

    public OptRealtimeCacheCore(OptRealtimeStore store, ObjectMapper mapper, LongSupplier clock,
                                long staleSeconds, long negativeSeconds) {
        this.store = store;
        this.mapper = mapper;
        this.clock = clock;
        this.staleMillis = staleSeconds * 1000L;
        this.negative = Caffeine.newBuilder()
                .expireAfterWrite(negativeSeconds, TimeUnit.SECONDS)
                .maximumSize(200_000)
                .build();
    }

    /**
     * 与 RedisKey.buildIotRealKey 相同的格式。
     */
    public static String key(String objectId, String infoCode) {
        return KEY_PREFIX + objectId + ":" + infoCode;
    }

    // ------------------------------------------------------------------ 写

    /**
     * 写入一帧。返回是否真正写了 Redis。
     */
    public boolean write(OptRealtimeValue value) {
        if (value == null || value.getObjectId() == null || value.getInfoCode() == null) {
            return false;
        }
        String key = key(value.getObjectId(), value.getInfoCode());
        String previousTime = lastDataTime.get(key);
        String previousValue = lastValue.get(key);
        if (previousTime == null) {
            OptRealtimeValue stored = decode(store.get(key));
            if (stored != null) {
                previousTime = stored.getDataTime();
                previousValue = stored.getDataValue();
            }
        }
        int cmp = compareDataTime(value.getDataTime(), previousTime);
        if (cmp < 0) {
            OptMetrics.inc("rt.write.olderSkipped");
            return false;
        }
        if (cmp == 0 && previousTime != null && Objects.equals(previousValue, value.getDataValue())) {
            OptMetrics.inc("rt.write.duplicateSkipped");
            return false;
        }
        value.setWriteTime(clock.getAsLong());
        String json = encode(value);
        if (json == null) {
            return false;
        }
        store.set(key, json);
        if (value.getDataTime() != null) {
            lastDataTime.put(key, value.getDataTime());
        }
        lastValue.put(key, String.valueOf(value.getDataValue()));
        negative.invalidate(key);
        OptMetrics.inc("rt.write");
        return true;
    }

    /**
     * 新 dataTime 比旧的早返回负数；无法比较（缺失或格式不同）时返回正数，即允许写入。
     */
    static int compareDataTime(String incoming, String previous) {
        if (previous == null || incoming == null || incoming.length() != previous.length()) {
            return 1;
        }
        return incoming.compareTo(previous);
    }

    // ------------------------------------------------------------------ 读

    /**
     * 批量查找。
     */
    public Lookup lookup(List<PointKey> points) {
        List<String> keys = new ArrayList<>(points.size());
        for (PointKey p : points) {
            keys.add(key(p.getObjectId(), p.getInfoCode()));
        }
        List<String> raw = keys.isEmpty() ? new ArrayList<>() : store.mget(keys);
        long now = clock.getAsLong();
        Lookup result = new Lookup();
        for (int i = 0; i < points.size(); i++) {
            PointKey p = points.get(i);
            OptRealtimeValue v = raw == null || raw.size() <= i ? null : decode(raw.get(i));
            if (isUsable(v, now)) {
                result.hits.put(p, v);
                continue;
            }
            Object neg = negative.getIfPresent(keys.get(i));
            if (neg != null) {
                result.negatives.put(p, neg == NULL_MARKER ? null : neg);
                continue;
            }
            result.misses.add(p);
        }
        OptMetrics.add("rt.read.hit", result.hits.size());
        OptMetrics.add("rt.read.negative", result.negatives.size());
        OptMetrics.add("rt.read.miss", result.misses.size());
        return result;
    }

    /**
     * 缓存值是否可以代替 Twin 的 on-moment 查询结果：非离线、状态正常、Twin 时间常量内、且最近 staleSeconds 内有写入
     * （Twin 约 45 秒周期推送一次，超过陈旧阈值说明 MQTT 可能已断，必须回源）。
     */
    boolean isUsable(OptRealtimeValue v, long now) {
        if (v == null || v.isOffline() || v.getDataValue() == null) {
            return false;
        }
        if (v.getDataStatus() != null && v.getDataStatus() != 1) {
            return false;
        }
        if (v.getWriteTime() == null || now - v.getWriteTime() > staleMillis) {
            return false;
        }
        return withinIndate(v, now);
    }

    /**
     * 与 FeedbackInfoMsgMqttVO.toDataIndicateTime 相同：dataTime + dataIndate 分钟；dataIndate 为空或 0 不过期。
     */
    static boolean withinIndate(OptRealtimeValue v, long now) {
        Integer indate = v.getDataIndate();
        if (indate == null || indate == 0) {
            return true;
        }
        if (v.getDataTime() == null) {
            return false;
        }
        try {
            LocalDateTime expire = LocalDateTime.parse(v.getDataTime(), DATA_TIME).plusMinutes(indate);
            long expireMillis = expire.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return now <= expireMillis;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 回源后仍没有有效值的点写负缓存，payload 为 Twin 原样返回的记录（可为 null）。
     */
    public void markNegative(PointKey point, Object twinPayload) {
        negative.put(key(point.getObjectId(), point.getInfoCode()), twinPayload == null ? NULL_MARKER : twinPayload);
    }

    // ------------------------------------------------------------------ 编解码

    public String encode(OptRealtimeValue value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("[opt] 实时值序列化失败 {}", value, e);
            return null;
        }
    }

    public OptRealtimeValue decode(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, OptRealtimeValue.class);
        } catch (Exception e) {
            // 例如 rcRunStatus 等其它写方的值，或损坏的数据：当作未命中
            return null;
        }
    }

    // ------------------------------------------------------------------ 类型

    @Getter
    public static class Lookup {
        private final Map<PointKey, OptRealtimeValue> hits = new LinkedHashMap<>();
        /** 负缓存命中：值为当时 Twin 返回的原样记录，可能为 null */
        private final Map<PointKey, Object> negatives = new LinkedHashMap<>();
        private final List<PointKey> misses = new ArrayList<>();
    }

    @Getter
    public static final class PointKey {
        private final String objectId;
        private final String infoCode;

        public PointKey(String objectId, String infoCode) {
            this.objectId = objectId;
            this.infoCode = infoCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PointKey)) {
                return false;
            }
            PointKey that = (PointKey) o;
            return Objects.equals(objectId, that.objectId) && Objects.equals(infoCode, that.infoCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectId, infoCode);
        }

        @Override
        public String toString() {
            return objectId + ":" + infoCode;
        }
    }
}
