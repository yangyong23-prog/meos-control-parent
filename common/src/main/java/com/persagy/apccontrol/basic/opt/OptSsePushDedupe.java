package com.persagy.apccontrol.basic.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * opt 分支 B9（switch.opt.ssePushDedupe，默认关）：定时全量推送“无变化不推”。
 * <p>
 * 同一 eventKey、同一事件类型的推送内容与上次完全相同，且距上次真正推送不到 {@code opt.sse.full-push-minutes}
 * （默认 15）分钟时跳过发送；超过即照常推送一次作为兜底。eventKey 有新连接加入时清除记录，保证新连接立即拿到数据。
 */
@Component
public class OptSsePushDedupe {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Map<String, Entry> last = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    @Value("${opt.sse.full-push-minutes:15}")
    private long fullPushMinutes = 15L;

    public OptSsePushDedupe() {
        this(System::currentTimeMillis);
    }

    OptSsePushDedupe(LongSupplier clock) {
        this.clock = clock;
    }

    void setFullPushMinutes(long minutes) {
        this.fullPushMinutes = minutes;
    }

    /**
     * @return true 表示内容未变化且未到兜底时间，应跳过
     */
    public boolean shouldSkip(String eventKey, String eventType, Object data) {
        if (!OptSwitch.on(OptSwitch.SSE_PUSH_DEDUPE) || eventKey == null) {
            return false;
        }
        byte[] digest = digest(data);
        if (digest == null) {
            return false;
        }
        String key = eventKey + "|" + eventType;
        long now = clock.getAsLong();
        Entry prev = last.get(key);
        if (prev != null && Arrays.equals(prev.digest, digest)
                && now - prev.pushedAt < TimeUnit.MINUTES.toMillis(fullPushMinutes)) {
            OptMetrics.inc("sse.pushSkipped");
            return true;
        }
        last.put(key, new Entry(digest, now));
        return false;
    }

    /** eventKey 有新连接或连接全部断开时调用 */
    public void forget(String eventKey) {
        if (eventKey == null || last.isEmpty()) {
            return;
        }
        String prefix = eventKey + "|";
        last.keySet().removeIf(k -> k.startsWith(prefix));
    }

    private static byte[] digest(Object data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(MAPPER.writeValueAsBytes(data));
        } catch (NoSuchAlgorithmException | com.fasterxml.jackson.core.JsonProcessingException | RuntimeException e) {
            // 无法序列化时不做去重
            return null;
        }
    }

    private static final class Entry {
        final byte[] digest;
        final long pushedAt;

        Entry(byte[] digest, long pushedAt) {
            this.digest = digest;
            this.pushedAt = pushedAt;
        }
    }
}
