package com.persagy.apccontrol.common.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OptTwinWriteLedgerTest {

    private final InMemoryRealtimeStore store = new InMemoryRealtimeStore();
    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final OptTwinWriteLedger ledger = new OptTwinWriteLedger(store, now::get, 24);
    private final AtomicInteger calls = new AtomicInteger();

    private static Map<String, Object> item(String objectId, String paramCode, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("objectId", objectId);
        m.put("paramCode", paramCode);
        if (value != null) {
            m.put("paramArray", value);
        }
        return m;
    }

    private boolean write(String op, List<?> payload) {
        return ledger.write("conf", op, "Pj1", payload, () -> {
            calls.incrementAndGet();
            return true;
        });
    }

    @Test
    void unchangedPayloadIsSkippedUntilDailyRewrite() {
        List<Map<String, Object>> p = Arrays.asList(item("S1", "equipExclusions", Arrays.asList("a", "b")), item("S2", "equipExclusions", Collections.singletonList("c")));
        Assertions.assertTrue(write("saveExclusions", p));
        Assertions.assertFalse(write("saveExclusions", p));
        // 键顺序不同、内容相同也视为未变化
        Map<String, Object> reordered = new LinkedHashMap<>();
        reordered.put("paramArray", Arrays.asList("a", "b"));
        reordered.put("paramCode", "equipExclusions");
        reordered.put("objectId", "S1");
        Assertions.assertFalse(write("saveExclusions", Arrays.asList(p.get(1), reordered)));
        now.addAndGet(24 * 3_600_000L);
        Assertions.assertTrue(write("saveExclusions", p), "每天一次全量写兜底");
        Assertions.assertEquals(2, calls.get());
    }

    @Test
    void anyChangedItemSendsWholePayload() {
        write("saveExclusions", Arrays.asList(item("S1", "e", 1), item("S2", "e", 2)));
        Assertions.assertTrue(write("saveExclusions", Arrays.asList(item("S1", "e", 1), item("S2", "e", 3))));
        Assertions.assertTrue(write("saveExclusions", Arrays.asList(item("S1", "e", 1), item("S3", "e", 1))), "新增对象");
        Assertions.assertEquals(3, calls.get());
    }

    @Test
    void deleteThenSaveSameConfigIsWrittenAgain() {
        List<Map<String, Object>> save = Collections.singletonList(item("S1", "e", Arrays.asList("a", "b")));
        write("saveExclusions", save);
        Assertions.assertTrue(write("deleteByConf", Collections.singletonList(item("S1", "e", null))));
        Assertions.assertFalse(write("deleteByConf", Collections.singletonList(item("S1", "e", null))), "重复删除跳过");
        Assertions.assertTrue(write("saveExclusions", save), "删除后恢复同样配置必须重写");
    }

    @Test
    void failureIsNotRecordedSoNextRoundRetries() {
        List<Map<String, Object>> p = Collections.singletonList(item("S1", "e", 1));
        Assertions.assertTrue(ledger.write("conf", "saveList", "Pj1", p, () -> false));
        Assertions.assertThrows(IllegalStateException.class, () -> ledger.write("conf", "saveList", "Pj1", p, () -> {
            throw new IllegalStateException("twin 500");
        }));
        Assertions.assertTrue(write("saveList", p), "失败后下一轮照常写");
        Assertions.assertFalse(write("saveList", p));
    }

    @Test
    void unjudgeablePayloadsAreAlwaysWritten() {
        Assertions.assertTrue(write("saveList", new ArrayList<>()));
        Assertions.assertTrue(write("saveList", new ArrayList<>()));
        Assertions.assertTrue(write("saveList", Collections.singletonList("not-a-map")));
        List<Map<String, Object>> dupIdentity = Arrays.asList(item("S1", "e", 1), item("S1", "e", 2));
        Assertions.assertTrue(write("saveList", dupIdentity));
        Assertions.assertTrue(write("saveList", dupIdentity), "identity 重复无法逐项记账，照常写");
        Assertions.assertEquals(5, calls.get());
    }

    @Test
    void projectsAndGroupsAreIsolated() {
        List<Map<String, Object>> p = Collections.singletonList(item("S1", "e", 1));
        write("saveList", p);
        Assertions.assertTrue(ledger.write("conf", "saveList", "Pj2", p, () -> true));
        Assertions.assertTrue(ledger.write("classConf", "saveList", "Pj1", p, () -> true));
    }
}
