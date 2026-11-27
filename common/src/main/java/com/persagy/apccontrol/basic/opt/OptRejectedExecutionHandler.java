package com.persagy.apccontrol.basic.opt;

import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B0：async 池拒绝策略。
 *
 * <p>开关 {@code switch.opt.asyncDiscardOnFull} 关闭时完全委托给原策略（CallerRuns）；
 * 打开时只丢弃“没人等结果”的任务，避免把 @Async 任务压回 MQTT 消费线程：
 * <ul>
 *   <li>{@code CompletableFuture.runAsync/supplyAsync} 提交的任务：调用方通常会 join，丢弃会让 join 永远等不到。
 *       2026-11-23 4.91 上 Tomcat 200 个线程全部挂在 join 上（关系图、需求配置等接口全部无响应）。
 *       这类任务一律交回原策略（CallerRuns）执行，计数 {@code async.callerRunsAwaited}；</li>
 *   <li>{@link Future}（@Async / submit 提交）：取消后丢弃，等待方会立即拿到 CancellationException 而不是永远阻塞，
 *       计数 {@code async.drop}；</li>
 *   <li>拆不开、认不出的任务：同样交回原策略，宁可慢也不挂死。</li>
 * </ul>
 */
@Slf4j
public class OptRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final long WARN_INTERVAL_MS = 60_000L;
    private static final int MAX_UNWRAP_DEPTH = 8;

    private final RejectedExecutionHandler fallback;
    private final AtomicLong lastWarn = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong awaitedCallerRuns = new AtomicLong();

    public OptRejectedExecutionHandler(RejectedExecutionHandler fallback) {
        this.fallback = fallback;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!OptSwitch.on(OptSwitch.ASYNC_DISCARD_ON_FULL)) {
            fallback.rejectedExecution(r, executor);
            return;
        }
        Runnable task = unwrap(r);
        if (!(task instanceof Future) || task instanceof CompletableFuture.AsynchronousCompletionTask) {
            // 可能有人在 join/get 等它：不能丢
            awaitedCallerRuns.incrementAndGet();
            OptMetrics.incAlways("async.callerRunsAwaited");
            fallback.rejectedExecution(r, executor);
            return;
        }
        ((Future<?>) task).cancel(false);
        long total = dropped.incrementAndGet();
        OptMetrics.incAlways("async.drop");
        long now = System.currentTimeMillis();
        long last = lastWarn.get();
        if (now - last >= WARN_INTERVAL_MS && lastWarn.compareAndSet(last, now)) {
            log.warn("[opt] async 池已满，丢弃任务。累计丢弃 {}，交回调用线程执行 {}，active={}，queue={}",
                    total, awaitedCallerRuns.get(), executor.getActiveCount(), executor.getQueue().size());
        }
    }

    /** 拆掉 TTL 和上下文包装，拿到真正提交的任务 */
    static Runnable unwrap(Runnable r) {
        Runnable cur = r;
        for (int i = 0; i < MAX_UNWRAP_DEPTH && cur != null; i++) {
            if (cur instanceof TtlRunnable) {
                cur = ((TtlRunnable) cur).getRunnable();
            } else if (cur instanceof WrappedRunnable) {
                cur = ((WrappedRunnable) cur).getDelegate();
            } else {
                break;
            }
        }
        return cur;
    }

    public long droppedCount() {
        return dropped.get();
    }

    public long awaitedCallerRunsCount() {
        return awaitedCallerRuns.get();
    }

    /** 上下文包装类的基类：暴露被包装的任务，供拒绝策略拆包 */
    public abstract static class WrappedRunnable implements Runnable {
        private final Runnable delegate;

        protected WrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        public Runnable getDelegate() {
            return delegate;
        }
    }
}
