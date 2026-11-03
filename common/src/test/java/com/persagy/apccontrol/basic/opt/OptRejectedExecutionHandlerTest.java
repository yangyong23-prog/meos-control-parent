package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.ArrayBlockingQueue;
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
    void switchOnDropsAndCountsWithoutRunningOnCaller() {
        MockEnvironment env = new MockEnvironment().withProperty("switch.opt.asyncDiscardOnFull", "true");
        OptSwitch.setEnvironmentForTest(env);
        OptRejectedExecutionHandler handler = new OptRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        AtomicInteger ran = new AtomicInteger();
        ThreadPoolExecutor executor = newExecutor();
        handler.rejectedExecution(ran::incrementAndGet, executor);
        handler.rejectedExecution(ran::incrementAndGet, executor);
        Assertions.assertEquals(0, ran.get());
        Assertions.assertEquals(2, handler.droppedCount());
        executor.shutdownNow();
    }

    private static ThreadPoolExecutor newExecutor() {
        return new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
    }
}
