package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class OptSsePushDedupeTest {

    private final AtomicLong now = new AtomicLong(0);
    private final OptSsePushDedupe dedupe = new OptSsePushDedupe(now::get);

    @BeforeEach
    void on() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.ssePushDedupe", "true"));
        dedupe.setFullPushMinutes(15);
    }

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    @Test
    void skipsUnchangedUntilFullPushInterval() {
        Object data = Arrays.asList(Collections.singletonMap("id", "s1"), Collections.singletonMap("t", LocalDateTime.of(2026, 1, 1, 0, 0)));
        Assertions.assertFalse(dedupe.shouldSkip("k", "SPACE_WITH_STATUS", data));
        now.addAndGet(60_000);
        Assertions.assertTrue(dedupe.shouldSkip("k", "SPACE_WITH_STATUS", data));
        Assertions.assertFalse(dedupe.shouldSkip("k", "SPACE_WITH_STATUS", Collections.singletonMap("id", "s2")), "变化即推");
        now.addAndGet(15 * 60_000L);
        Assertions.assertFalse(dedupe.shouldSkip("k", "SPACE_WITH_STATUS", Collections.singletonMap("id", "s2")), "15 分钟兜底");
    }

    @Test
    void eventTypesAndKeysAreSeparateAndForgetResets() {
        Object data = Collections.singletonMap("v", 1);
        Assertions.assertFalse(dedupe.shouldSkip("k", "A", data));
        Assertions.assertFalse(dedupe.shouldSkip("k", "B", data));
        Assertions.assertFalse(dedupe.shouldSkip("k2", "A", data));
        dedupe.forget("k");
        Assertions.assertFalse(dedupe.shouldSkip("k", "A", data), "新连接加入后立即推");
        Assertions.assertTrue(dedupe.shouldSkip("k2", "A", data));
    }

    @Test
    void offNeverSkips() {
        OptSwitch.setEnvironmentForTest(null);
        Object data = Collections.singletonMap("v", 1);
        Assertions.assertFalse(dedupe.shouldSkip("k", "A", data));
        Assertions.assertFalse(dedupe.shouldSkip("k", "A", data));
    }
}
