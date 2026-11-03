package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OptSseCoalescerTest {

    private final OptSseCoalescer coalescer = new OptSseCoalescer(Executors.newSingleThreadScheduledExecutor());

    OptSseCoalescerTest() {
        coalescer.setEvents("LIGHT_LOOP, LIGHT_SPACE_STATUS");
        coalescer.setWindowMs(100);
    }

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
        coalescer.destroy();
    }

    @Test
    void appliesOnlyWhenOnAndConfigured() {
        Assertions.assertFalse(coalescer.applies("LIGHT_LOOP"));
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.sseCoalesce", "true"));
        Assertions.assertTrue(coalescer.applies("LIGHT_LOOP"));
        Assertions.assertFalse(coalescer.applies("SPACE_WITH_STATUS"));
        Assertions.assertFalse(coalescer.applies(null));
    }

    @Test
    void burstRunsOncePerWindowAndLaterTriggerOpensNewWindow() throws Exception {
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch first = new CountDownLatch(1);
        for (int i = 0; i < 50; i++) {
            Assertions.assertTrue(coalescer.offer("k", () -> {
                runs.incrementAndGet();
                first.countDown();
            }));
        }
        Assertions.assertTrue(first.await(2, TimeUnit.SECONDS));
        Assertions.assertEquals(1, runs.get());

        CountDownLatch second = new CountDownLatch(1);
        coalescer.offer("k", second::countDown);
        Assertions.assertTrue(second.await(2, TimeUnit.SECONDS), "窗口结束后新的触发会再执行");
    }

    @Test
    void differentKeysAreIndependent() throws Exception {
        CountDownLatch both = new CountDownLatch(2);
        coalescer.offer("a", both::countDown);
        coalescer.offer("b", both::countDown);
        Assertions.assertTrue(both.await(2, TimeUnit.SECONDS));
    }
}
