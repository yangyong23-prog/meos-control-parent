package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.opt.OptRealtimeCacheCore.Lookup;
import com.persagy.apccontrol.common.opt.OptRealtimeCacheCore.PointKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class OptRealtimeCacheCoreTest {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final LocalDateTime base = LocalDateTime.of(2026, 11, 3, 12, 0, 0);
    private final AtomicLong now = new AtomicLong(millis(base));
    private final InMemoryRealtimeStore store = new InMemoryRealtimeStore();
    private final OptRealtimeCacheCore core = new OptRealtimeCacheCore(store, new ObjectMapper(), now::get, 120, 60);

    private static long millis(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private OptRealtimeValue value(String v, LocalDateTime dataTime, Integer indate) {
        OptRealtimeValue r = new OptRealtimeValue();
        r.setObjectId("Eq1");
        r.setClassCode("ACCCCC");
        r.setInfoCode("runStatus");
        r.setDataValue(v);
        r.setDataTime(dataTime.format(F));
        r.setDataIndate(indate);
        r.setDataStatus(1);
        r.setSource(OptRealtimeValue.SOURCE_MQTT);
        return r;
    }

    private Lookup lookup() {
        return core.lookup(Collections.singletonList(new PointKey("Eq1", "runStatus")));
    }

    @Test
    void writeThenHit() {
        Assertions.assertTrue(core.write(value("1", base, 15)));
        Lookup l = lookup();
        Assertions.assertEquals(1, l.getHits().size());
        Assertions.assertEquals("1", l.getHits().values().iterator().next().getDataValue());
    }

    @Test
    void olderFrameDoesNotOverwrite() {
        core.write(value("1", base, 15));
        Assertions.assertFalse(core.write(value("0", base.minusSeconds(30), 15)));
        Assertions.assertEquals("1", lookup().getHits().values().iterator().next().getDataValue());
    }

    @Test
    void duplicateFrameFromSecondTopicIsSkipped() {
        core.write(value("1", base, 15));
        int sets = store.sets.get();
        Assertions.assertFalse(core.write(value("1", base, 15)));
        Assertions.assertEquals(sets, store.sets.get());
        // 同一时间不同值仍写入
        Assertions.assertTrue(core.write(value("2", base, 15)));
    }

    @Test
    void firstWriteAfterRestartComparesWithRedis() {
        core.write(value("1", base, 15));
        OptRealtimeCacheCore restarted = new OptRealtimeCacheCore(store, new ObjectMapper(), now::get, 120, 60);
        Assertions.assertFalse(restarted.write(value("0", base.minusMinutes(1), 15)));
        Assertions.assertTrue(restarted.write(value("0", base.plusMinutes(1), 15)));
    }

    @Test
    void offlineMarkerIsNotUsable() {
        core.write(value("1", base, 15));
        core.write(value(OptRealtimeValue.OFFLINE_VALUE, base.plusSeconds(10), 15));
        Lookup l = lookup();
        Assertions.assertTrue(l.getHits().isEmpty());
        Assertions.assertEquals(1, l.getMisses().size());
    }

    @Test
    void expiresByTwinIndate() {
        core.write(value("1", base.minusMinutes(20), 15));
        // 写入时间是刚才，但 dataTime + 15 分钟已过
        Assertions.assertTrue(lookup().getHits().isEmpty());
        core.write(value("1", base.minusMinutes(20), 0));
        Assertions.assertTrue(lookup().getHits().isEmpty(), "旧帧不覆盖，仍是 15 分钟时间常量的那一帧");
    }

    @Test
    void zeroIndateNeverExpiresButStaleWriteTimeDoes() {
        core.write(value("1", base.minusHours(5), 0));
        Assertions.assertEquals(1, lookup().getHits().size());
        now.addAndGet(121_000L);
        Assertions.assertTrue(lookup().getHits().isEmpty(), "超过 120 秒没有新写入视为陈旧（MQTT 可能已断）");
    }

    @Test
    void abnormalStatusIsNotUsable() {
        OptRealtimeValue v = value("1", base, 15);
        v.setDataStatus(2);
        core.write(v);
        Assertions.assertTrue(lookup().getHits().isEmpty());
    }

    @Test
    void negativeCacheShortCircuitsUntilNextWrite() {
        PointKey p = new PointKey("Eq1", "runStatus");
        core.markNegative(p, "twin-invalid-record");
        Lookup l = lookup();
        Assertions.assertTrue(l.getMisses().isEmpty());
        Assertions.assertEquals("twin-invalid-record", l.getNegatives().get(p));
        core.markNegative(p, null);
        Assertions.assertTrue(lookup().getNegatives().containsKey(p));
        Assertions.assertNull(lookup().getNegatives().get(p));
        core.write(value("1", base, 15));
        Assertions.assertEquals(1, lookup().getHits().size());
    }

    @Test
    void foreignOrCorruptValuesAreMisses() {
        // rcRunStatus 由 IotDataCacheServiceImpl.insert 写入的完整 VO（无 writeTime）
        store.data.put(OptRealtimeCacheCore.key("Eq1", "runStatus"),
                "{\"objectId\":\"Eq1\",\"infoCode\":\"runStatus\",\"dataValue\":\"1\",\"dataStatus\":1,\"localName\":\"x\"}");
        Assertions.assertEquals(1, lookup().getMisses().size());
        store.data.put(OptRealtimeCacheCore.key("Eq1", "runStatus"), "not-json");
        Assertions.assertEquals(1, lookup().getMisses().size());
    }

    @Test
    void lookupKeepsRequestOrderForMultiplePoints() {
        core.write(value("1", base, 15));
        Lookup l = core.lookup(Arrays.asList(new PointKey("Eq2", "a"), new PointKey("Eq1", "runStatus")));
        Assertions.assertEquals(1, l.getHits().size());
        Assertions.assertEquals(new PointKey("Eq2", "a"), l.getMisses().get(0));
    }

    @Test
    void compareDataTime() {
        Assertions.assertTrue(OptRealtimeCacheCore.compareDataTime("20261103120001", "20261103120000") > 0);
        Assertions.assertTrue(OptRealtimeCacheCore.compareDataTime("20261103115959", "20261103120000") < 0);
        Assertions.assertTrue(OptRealtimeCacheCore.compareDataTime("2026", "20261103120000") > 0, "格式不同时允许写");
        Assertions.assertTrue(OptRealtimeCacheCore.compareDataTime(null, "20261103120000") > 0);
    }
}
