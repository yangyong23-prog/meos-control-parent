package com.persagy.apccontrol.basic.opt;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

/**
 * 边缘调用优化的本地度量：进程内 LongAdder 计数，每分钟输出一行日志后清零。
 *
 * <p>只在 {@code switch.opt.metrics=true} 时累加和输出；关闭时 {@link #inc} 直接返回，无额外开销。
 * 用独立的单线程调度器输出，不占用 Spring 的 @Scheduled 线程。
 *
 * <p>指标命名约定：{@code <分类>.<名字>}，如 {@code feign.dtp-twin.DtpTwinClient#queryTwinDataOnMoment}、
 * {@code mqtt.recv}、{@code mqtt.drop}、{@code async.drop}、{@code redis.rt.hit}。
 */
@Slf4j
@Component
public class OptMetrics {

    private static final Map<String, LongAdder> COUNTERS = new ConcurrentHashMap<>();

    private final ScheduledExecutorService reporter;

    public OptMetrics() {
        reporter = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "opt-metrics");
            t.setDaemon(true);
            return t;
        });
        reporter.scheduleAtFixedRate(OptMetrics::reportSafely, 60, 60, TimeUnit.SECONDS);
    }

    public static void inc(String name) {
        add(name, 1);
    }

    public static void add(String name, long delta) {
        if (!OptSwitch.on(OptSwitch.METRICS)) {
            return;
        }
        COUNTERS.computeIfAbsent(name, k -> new LongAdder()).add(delta);
    }

    /**
     * 不受 metrics 开关控制的计数，用于各改造项自身的丢弃计数（丢弃必须可见）。
     */
    public static void incAlways(String name) {
        addAlways(name, 1);
    }

    public static void addAlways(String name, long delta) {
        COUNTERS.computeIfAbsent(name, k -> new LongAdder()).add(delta);
    }

    /**
     * 取出并清零当前计数，按名字排序。
     */
    static Map<String, Long> drain() {
        Map<String, Long> snapshot = new TreeMap<>();
        for (Map.Entry<String, LongAdder> e : COUNTERS.entrySet()) {
            long v = e.getValue().sumThenReset();
            if (v != 0) {
                snapshot.put(e.getKey(), v);
            }
        }
        return snapshot;
    }

    private static void reportSafely() {
        try {
            Map<String, Long> snapshot = drain();
            if (!snapshot.isEmpty()) {
                log.info("[opt-metrics] 1min {}", snapshot);
            }
        } catch (Throwable t) {
            log.warn("[opt-metrics] 输出失败", t);
        }
    }

    @PreDestroy
    public void shutdown() {
        reporter.shutdownNow();
    }
}
