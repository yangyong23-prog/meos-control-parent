package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class OptParallelLookupTest {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);

    @AfterAll
    static void shutdown() {
        POOL.shutdownNow();
    }

    /** 模拟 queryAcccccGroupBySystemId：耗时不一、可返回 null */
    private static final Function<String, String> REMOTE = id -> {
        try {
            Thread.sleep(id.hashCode() & 7);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return id.startsWith("empty") ? null : "group-of-" + id;
    };

    @Test
    void parallelResultEqualsSequentialLoop() {
        Set<String> systemIds = new LinkedHashSet<>();
        for (int i = 0; i < 30; i++) {
            systemIds.add(i % 7 == 0 ? "empty" + i : "Sy" + i);
        }
        Map<String, String> before = OptParallelLookup.map(systemIds, REMOTE, null);
        Map<String, String> after = OptParallelLookup.map(systemIds, REMOTE, POOL);
        Assertions.assertEquals(before, after, "改造前后结果一致（含 null 值）");
        Assertions.assertEquals(new ArrayList<>(systemIds), new ArrayList<>(after.keySet()), "按 key 顺序");
        Assertions.assertTrue(after.containsKey("empty0") && after.get("empty0") == null);
    }

    @Test
    void duplicateKeysQueriedOnce() {
        AtomicInteger calls = new AtomicInteger();
        Map<String, String> r = OptParallelLookup.map(Arrays.asList("a", "b", "a"), k -> {
            calls.incrementAndGet();
            return k;
        }, POOL);
        Assertions.assertEquals(2, r.size());
        Assertions.assertEquals(2, calls.get());
    }

    @Test
    void firstFailureByKeyOrderIsRethrownAsIs() {
        IllegalStateException b = new IllegalStateException("b");
        List<String> keys = Arrays.asList("a", "b", "c", "d");
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> OptParallelLookup.map(keys, k -> {
            if ("b".equals(k)) {
                throw b;
            }
            if ("d".equals(k)) {
                throw new IllegalArgumentException("d");
            }
            return k;
        }, POOL));
        Assertions.assertSame(b, thrown);
    }

    @Test
    void emptyKeys() {
        Assertions.assertTrue(OptParallelLookup.map(new ArrayList<String>(), REMOTE, POOL).isEmpty());
        Assertions.assertTrue(OptParallelLookup.map(null, REMOTE, POOL).isEmpty());
    }
}
