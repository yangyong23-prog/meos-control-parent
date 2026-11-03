package com.persagy.apccontrol.basic.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * opt 分支 B9：SSE 客户端租约。
 * <p>
 * 只有调用过 ping 的 eventKey 才受租约管理（老前端不 ping，不受影响）；超过租约时长没再 ping 的 eventKey 判定为失联。
 * 用于“拔网线”这类 TCP 写不会立即失败的场景：心跳写入只进内核缓冲，要十几分钟才报错。
 */
public class OptSseLeases {

    private final Map<String, Long> lastPing = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public OptSseLeases(LongSupplier clock) {
        this.clock = clock;
    }

    public void ping(String eventKey) {
        if (eventKey != null && !eventKey.isEmpty()) {
            lastPing.put(eventKey, clock.getAsLong());
        }
    }

    public void forget(String eventKey) {
        if (eventKey != null) {
            lastPing.remove(eventKey);
        }
    }

    /**
     * 取出并移除已过期的 eventKey。
     *
     * @param leaseMillis 租约时长，≤0 表示不启用
     */
    public List<String> drainExpired(long leaseMillis) {
        List<String> expired = new ArrayList<>();
        if (leaseMillis <= 0) {
            return expired;
        }
        long deadline = clock.getAsLong() - leaseMillis;
        lastPing.forEach((key, at) -> {
            if (at < deadline && lastPing.remove(key, at)) {
                expired.add(key);
            }
        });
        return expired;
    }

    public boolean isTracked(String eventKey) {
        return lastPing.containsKey(eventKey);
    }

    public int size() {
        return lastPing.size();
    }
}
