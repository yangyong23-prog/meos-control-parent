package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import com.alibaba.ttl.TtlRunnable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OptRejectedExecutionHandlerTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    @Test
    void switchOffDelegatesToFallback() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        AtomicInteger fallbackCalls = new AtomicInteger();
        OptRejectedExecutionHandler handler = new OptRejectedExecutionHandler((r, e) -> fallbackCalls.incrementAndGet());
        handler.rejectedExecution(() -> { }, newExecutor());
        Assertions.assertEquals(1, fallbackCalls.get());
        Assertions.assertEquals(0, handler.droppedCount());
    }

    @Test
    void switchOnCancelsAndDropsFutureTask() {
        OptSwitch.setEnvironmentForTest(switchOn());
        OptRejectedExecutionHandler handler = new OptRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        AtomicInteger ran = new AtomicInteger();
        ThreadPoolExecutor executor = newExecutor();
        FutureTask<Void> plain = new FutureTask<>(ran::incrementAndGet, null);
        FutureTask<Void> wrapped = new FutureTask<>(ran::incrementAndGet, null);
        handler.rejectedExecution(plain, executor);
        // 模拟 TaskDecorator + TTL 的包装层
        handler.rejectedExecution(TtlRunnable.get(new OptRejectedExecutionHandler.WrappedRunnable(wrapped) {
            @Override
            public void run() {
                wrapped.run();
            }
        }), executor);
        Assertions.assertEquals(0, ran.get());
        Assertions.assertTrue(plain.isCancelled());
        Assertions.assertTrue(wrapped.isCancelled());
        Assertions.assertEquals(2, handler.droppedCount());
        executor.shutdownNow();
    }

    @Test
    void switchOnNeverDropsCompletableFutureTask() throws Exception {
        OptSwitch.setEnvironmentForTest(switchOn());
        OptRejectedExecutionHandler handler = new OptRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        CountDownLatch release = new CountDownLatch(1);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), handler);
        // 占满线程和队列
        executor.execute(() -> awaitQuietly(release));
        executor.execute(() -> awaitQuietly(release));
        AtomicInteger ran = new AtomicInteger();
        CompletableFuture<Void> f = CompletableFuture.runAsync(ran::incrementAndGet, executor);
        // 被拒绝后在调用线程执行，join 不会挂死
        f.get(5, TimeUnit.SECONDS);
        Assertions.assertEquals(1, ran.get());
        Assertions.assertEquals(0, handler.droppedCount());
        Assertions.assertEquals(1, handler.awaitedCallerRunsCount());
        release.countDown();
        executor.shutdownNow();
    }

    @Test
    void switchOnRunsUnknownTaskOnCaller() {
        OptSwitch.setEnvironmentForTest(switchOn());
        OptRejectedExecutionHandler handler = new OptRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        AtomicInteger ran = new AtomicInteger();
        ThreadPoolExecutor executor = newExecutor();
        handler.rejectedExecution(ran::incrementAndGet, executor);
        Assertions.assertEquals(1, ran.get());
        Assertions.assertEquals(0, handler.droppedCount());
        executor.shutdownNow();
    }

    private static MockEnvironment switchOn() {
        return new MockEnvironment().withProperty("switch.opt.asyncDiscardOnFull", "true");
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadPoolExecutor newExecutor() {
        return new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
    }
}
