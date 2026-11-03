package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class OptCallMemoTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static void enable(boolean on) {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.scheduleSeasonMemo", String.valueOf(on)));
    }

    /** 同一组 key 序列，开关开/关返回的结果必须逐项相同，只是远程调用次数不同 */
    private static List<String> run(OptCallMemo<String, String> memo, AtomicInteger calls) {
        Function<String, String> remote = k -> {
            calls.incrementAndGet();
            return k == null || "none".equals(k) ? null : k.toUpperCase();
        };
        List<String> out = new ArrayList<>();
        for (String k : Arrays.asList("d1", "d1", "none", "d2", "none", "d1", null)) {
            out.add(memo.get(k, remote));
        }
        return out;
    }

    @Test
    void offCallsEveryTimeOnDedupesWithSameResults() {
        enable(false);
        AtomicInteger offCalls = new AtomicInteger();
        List<String> before = run(OptCallMemo.create("t", OptSwitch.SCHEDULE_SEASON_MEMO), offCalls);
        enable(true);
        AtomicInteger onCalls = new AtomicInteger();
        List<String> after = run(OptCallMemo.create("t", OptSwitch.SCHEDULE_SEASON_MEMO), onCalls);

        Assertions.assertEquals(before, after, "改造前后结果一致");
        Assertions.assertEquals(7, offCalls.get());
        Assertions.assertEquals(4, onCalls.get(), "d1、none、d2 各一次，null key 不去重");
    }

    @Test
    void failureIsNotMemoized() {
        enable(true);
        OptCallMemo<String, String> memo = OptCallMemo.create("t", OptSwitch.SCHEDULE_SEASON_MEMO);
        AtomicInteger calls = new AtomicInteger();
        Assertions.assertThrows(IllegalStateException.class, () -> memo.get("k", k -> {
            calls.incrementAndGet();
            throw new IllegalStateException("down");
        }));
        Assertions.assertEquals("ok", memo.get("k", k -> {
            calls.incrementAndGet();
            return "ok";
        }));
        Assertions.assertEquals(2, calls.get());
    }

    @Test
    void switchIsReadOnceAtCreation() {
        enable(false);
        OptCallMemo<String, String> memo = OptCallMemo.create("t", OptSwitch.SCHEDULE_SEASON_MEMO);
        enable(true);
        Assertions.assertFalse(memo.enabled(), "调用中途切换开关不影响本次调用");
    }
}
