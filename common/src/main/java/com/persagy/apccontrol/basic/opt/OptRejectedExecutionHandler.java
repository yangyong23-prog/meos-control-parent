package com.persagy.apccontrol.basic.opt;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B0：async 池拒绝策略。
 *
 * <p>开关 {@code switch.opt.asyncDiscardOnFull} 关闭时完全委托给原策略（CallerRuns）；
 * 打开时丢弃任务并计数（{@code async.drop}），每分钟最多打一条告警，
 * 避免把 @Async 任务压回 MQTT 消费线程。
 */
@Slf4j
public class OptRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final long WARN_INTERVAL_MS = 60_000L;

    private final RejectedExecutionHandler fallback;
    private final AtomicLong lastWarn = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();

    public OptRejectedExecutionHandler(RejectedExecutionHandler fallback) {
        this.fallback = fallback;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!OptSwitch.on(OptSwitch.ASYNC_DISCARD_ON_FULL)) {
            fallback.rejectedExecution(r, executor);
            return;
        }
        long total = dropped.incrementAndGet();
        OptMetrics.incAlways("async.drop");
        long now = System.currentTimeMillis();
        long last = lastWarn.get();
        if (now - last >= WARN_INTERVAL_MS && lastWarn.compareAndSet(last, now)) {
            log.warn("[opt] async 池已满，丢弃任务。累计丢弃 {}，active={}，queue={}",
                    total, executor.getActiveCount(), executor.getQueue().size());
        }
    }

    public long droppedCount() {
        return dropped.get();
    }
}
