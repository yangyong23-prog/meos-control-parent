package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.persagy.apccontrol.basic.opt.OptMetrics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/**
 * opt 分支 B8：Twin 配置写入台账（纯逻辑，单测覆盖）。
 * <p>
 * 按 payload 中的每一项记账：{@code apc:twinWrite:{group}:{projectId}:{identity}} = 上次<b>成功</b>写入时
 * “操作名 + 该项内容”的摘要和时间。identity 由该项的 objectId/classCode/paramCode/infoCode 组成。
 * 同一组的保存和删除共用 key（如 saveClassConfs 与 deleteByClassConf），删除后再保存一定会写。
 * <p>
 * 只有 payload 中<b>每一项</b>都与台账一致且距上次成功写入不到 {@code fullRewriteHours} 时才跳过；
 * 任一项变化就按原样发送整个 payload（不裁剪，避免依赖下游是否为逐项 upsert）。写失败不记账，下一轮自动重写。
 */
public class OptTwinWriteLedger {

    public static final String KEY_PREFIX = "apc:twinWrite:";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .findAndRegisterModules();
    private static final String[] IDENTITY_FIELDS = {"objectId", "classCode", "paramCode", "infoCode"};

    private final OptRealtimeStore store;
    private final LongSupplier clock;
    private final long fullRewriteMillis;

    public OptTwinWriteLedger(OptRealtimeStore store, LongSupplier clock, long fullRewriteHours) {
        this.store = store;
        this.clock = clock;
        this.fullRewriteMillis = TimeUnit.HOURS.toMillis(fullRewriteHours);
    }

    /**
     * @param group     记账分组（保存和对应的删除用同一组）
     * @param op        写操作名
     * @param projectId 项目
     * @param payload   写入内容，每项应为 Map（JSONObject）；否则不做判断直接写
     * @param call      实际写入，返回是否成功；抛异常视为失败并原样抛出
     * @return true 表示实际调用了写入
     */
    public boolean write(String group, String op, String projectId, Collection<?> payload, BooleanSupplier call) {
        List<String> keys = new ArrayList<>();
        List<String> digests = new ArrayList<>();
        if (!prepare(group, op, projectId, payload, keys, digests)) {
            invoke(call);
            return true;
        }
        long now = clock.getAsLong();
        if (allUnchanged(store.mget(keys), digests, now)) {
            OptMetrics.inc("twinWrite.skipped");
            return false;
        }
        if (invoke(call)) {
            for (int i = 0; i < keys.size(); i++) {
                store.set(keys.get(i), digests.get(i) + "|" + now);
            }
            OptMetrics.inc("twinWrite.written");
        }
        return true;
    }

    private static boolean invoke(BooleanSupplier call) {
        boolean ok;
        try {
            ok = call.getAsBoolean();
        } catch (RuntimeException e) {
            OptMetrics.incAlways("twinWrite.fail");
            throw e;
        }
        if (!ok) {
            OptMetrics.incAlways("twinWrite.fail");
        }
        return ok;
    }

    private boolean allUnchanged(List<String> stored, List<String> digests, long now) {
        if (stored == null || stored.size() != digests.size()) {
            return false;
        }
        for (int i = 0; i < digests.size(); i++) {
            String last = stored.get(i);
            int sep = last == null ? -1 : last.indexOf('|');
            if (sep <= 0 || !last.substring(0, sep).equals(digests.get(i))) {
                return false;
            }
            try {
                if (now - Long.parseLong(last.substring(sep + 1)) >= fullRewriteMillis) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /** @return false 表示无法判断（空 payload、非 Map 项、序列化失败），应直接写 */
    static boolean prepare(String group, String op, String projectId, Collection<?> payload,
                           List<String> keys, List<String> digests) {
        if (payload == null || payload.isEmpty()) {
            return false;
        }
        Map<String, String> byKey = new LinkedHashMap<>();
        for (Object item : payload) {
            if (!(item instanceof Map)) {
                return false;
            }
            String digest = digest(op, item);
            if (digest == null) {
                return false;
            }
            String key = KEY_PREFIX + group + ":" + projectId + ":" + identity((Map<?, ?>) item);
            if (byKey.containsKey(key)) {
                // 同一 payload 内 identity 重复（字段不足以区分），无法逐项记账
                return false;
            }
            byKey.put(key, digest);
        }
        keys.addAll(byKey.keySet());
        digests.addAll(byKey.values());
        return true;
    }

    static String identity(Map<?, ?> item) {
        StringBuilder sb = new StringBuilder();
        for (String f : IDENTITY_FIELDS) {
            Object v = item.get(f);
            sb.append(v == null ? "" : v).append('|');
        }
        return sha256(sb.toString()).substring(0, 16);
    }

    static String digest(String op, Object item) {
        try {
            return sha256(op + "\n" + MAPPER.writeValueAsString(item));
        } catch (Exception e) {
            return null;
        }
    }

    private static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
