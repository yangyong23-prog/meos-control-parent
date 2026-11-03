package com.persagy.apccontrol.opt;

import java.util.concurrent.atomic.AtomicLong;

/**
 * B1：MQTT 收包状态（最近收包时间、累计条数），供健康检查和度量使用。
 */
public final class OptMqttStatus {

    private static final long STARTED_AT = System.currentTimeMillis();
    private static final AtomicLong LAST_RECEIVED_AT = new AtomicLong();
    private static final AtomicLong RECEIVED = new AtomicLong();

    private OptMqttStatus() {
    }

    public static void markReceived() {
        LAST_RECEIVED_AT.set(System.currentTimeMillis());
        RECEIVED.incrementAndGet();
    }

    /** 0 表示启动后尚未收到消息 */
    public static long lastReceivedAt() {
        return LAST_RECEIVED_AT.get();
    }

    public static long received() {
        return RECEIVED.get();
    }

    public static long startedAt() {
        return STARTED_AT;
    }
}
