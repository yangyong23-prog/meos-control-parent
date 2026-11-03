package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

public class OptRealtimeTwinReaderTest {

    private static final String NOW = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    private final InMemoryRealtimeStore store = new InMemoryRealtimeStore();
    private final OptRealtimeCacheCore core = new OptRealtimeCacheCore(store, new ObjectMapper(), System::currentTimeMillis, 120, 60);
    private final OptRealtimeTwinReader reader = new OptRealtimeTwinReader(core);
    private final List<QueryTwinDataDTO> twinCalls = new ArrayList<>();

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static void mode(String mode) {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.realtimeCacheRead", mode));
    }

    private static QueryTwinDataDTO request(String objectId, String... infoCodes) {
        return new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(
                new QueryTwinDataOnMomentDTO("Pj1", objectId, new LinkedHashSet<>(Arrays.asList(infoCodes))))));
    }

    private static DtpTwinIotData twinRecord(String objectId, String infoCode, String value, int flag) {
        DtpTwinIotData d = new DtpTwinIotData();
        d.setObjectId(objectId);
        d.setInfoCode(infoCode);
        d.setDataValue(value);
        d.setDataTime(NOW);
        d.setDataIndate("15");
        d.setDataFlag(flag);
        d.setDataStatus(1);
        d.setClassCode("ACCCCC");
        return d;
    }

    /** Twin 桩：对请求的每个点返回固定值，"bad" 信息点返回无效记录 */
    private final Function<QueryTwinDataDTO, List<DtpTwinIotData>> twin = dto -> {
        twinCalls.add(dto);
        List<DtpTwinIotData> r = new ArrayList<>();
        for (QueryTwinDataOnMomentDTO o : dto.getObjects()) {
            for (String info : o.getInfoCodes()) {
                r.add(twinRecord(o.getObjectId(), info, "5", "bad".equals(info) ? 2 : 1));
            }
        }
        return r;
    };

    @Test
    void offModeIsNotHandled() {
        mode("off");
        Assertions.assertFalse(reader.handles(request("Eq1", "a")));
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        Assertions.assertFalse(reader.handles(request("Eq1", "a")));
    }

    @Test
    void historicalOrUnitQueriesAreNotHandled() {
        mode("on");
        QueryTwinDataDTO dto = request("Eq1", "a");
        dto.setTwinTime("20261101000000");
        Assertions.assertFalse(reader.handles(dto));
    }

    @Test
    void onModeMissThenBackfillThenHit() {
        mode("on");
        List<DtpTwinIotData> first = reader.query(request("Eq1", "a"), twin);
        Assertions.assertEquals(1, twinCalls.size());
        Assertions.assertEquals("5", first.get(0).getDataValue());

        List<DtpTwinIotData> second = reader.query(request("Eq1", "a"), twin);
        Assertions.assertEquals(1, twinCalls.size(), "第二次命中缓存，不再调 Twin");
        Assertions.assertEquals("5", second.get(0).getDataValue());
        Assertions.assertEquals(Integer.valueOf(1), second.get(0).getDataFlag());
        Assertions.assertEquals("15", second.get(0).getDataIndate());
    }

    @Test
    void invalidTwinRecordIsNegativeCachedAndReturnedAsIs() {
        mode("on");
        List<DtpTwinIotData> first = reader.query(request("Eq1", "bad"), twin);
        Assertions.assertEquals(Integer.valueOf(2), first.get(0).getDataFlag());
        List<DtpTwinIotData> second = reader.query(request("Eq1", "bad"), twin);
        Assertions.assertEquals(1, twinCalls.size(), "60 秒内不重复回源");
        Assertions.assertEquals(Integer.valueOf(2), second.get(0).getDataFlag(), "返回与 Twin 相同形状的无效记录");
    }

    @Test
    void rcRunStatusAndEmptyInfoCodesPassThrough() {
        mode("on");
        reader.query(request("Eq1", "rcRunStatus"), twin);
        reader.query(request("Eq1", "rcRunStatus"), twin);
        Assertions.assertEquals(2, twinCalls.size());
        Assertions.assertTrue(store.data.isEmpty(), "不写 rcRunStatus");
    }

    @Test
    void twinIsCalledInChunksOf200Objects() {
        mode("on");
        List<QueryTwinDataOnMomentDTO> objects = new ArrayList<>();
        for (int i = 0; i < 450; i++) {
            objects.add(new QueryTwinDataOnMomentDTO("Pj1", "Eq" + i, new LinkedHashSet<>(Collections.singletonList("a"))));
        }
        List<DtpTwinIotData> r = reader.query(new QueryTwinDataDTO(objects), twin);
        Assertions.assertEquals(450, r.size());
        Assertions.assertEquals(3, twinCalls.size());
        Assertions.assertEquals(200, twinCalls.get(0).getObjects().size());
        Assertions.assertEquals(50, twinCalls.get(2).getObjects().size());
    }

    @Test
    void twinFailureReturnsCachedPartOrRethrowsWhenNothingCached() {
        mode("on");
        reader.query(request("Eq1", "a"), twin);
        Function<QueryTwinDataDTO, List<DtpTwinIotData>> down = dto -> {
            throw new IllegalStateException("twin down");
        };
        List<DtpTwinIotData> partial = reader.query(request("Eq1", "a", "b"), down);
        Assertions.assertEquals(1, partial.size());
        Assertions.assertThrows(IllegalStateException.class, () -> reader.query(request("Eq9", "z"), down));
    }

    @Test
    void verifyModeReturnsTwinResultAndDoesNotWrite() {
        mode("verify");
        List<DtpTwinIotData> r = reader.query(request("Eq1", "a"), twin);
        Assertions.assertEquals(1, twinCalls.size());
        Assertions.assertEquals("5", r.get(0).getDataValue());
        Assertions.assertTrue(store.data.isEmpty());
    }

    @Test
    void compareTreatsNumericFormatsAsEqual() {
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.match, OptRealtimeTwinReader.compare("1", "1.0"));
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.mismatch, OptRealtimeTwinReader.compare("1", "2"));
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.cacheMiss, OptRealtimeTwinReader.compare("1", null));
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.mismatch, OptRealtimeTwinReader.compare(null, "1"));
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.bothEmpty, OptRealtimeTwinReader.compare(null, null));
        Assertions.assertEquals(OptRealtimeTwinReader.Outcome.match, OptRealtimeTwinReader.compare("on", "on"));
    }

    @Test
    void notifiesMissListenerOnlyForValidTwinFills() {
        mode("on");
        List<String> misses = new ArrayList<>();
        reader.setMissListener((c, i) -> misses.add(c + ":" + i));
        reader.query(request("Eq1", "a", "bad"), twin);
        Assertions.assertEquals(Collections.singletonList("ACCCCC:a"), misses);
        reader.query(request("Eq1", "a"), twin);
        Assertions.assertEquals(1, misses.size(), "缓存命中不再回调");
    }

    @Test
    void verifyModeNotifiesOnCacheMiss() {
        mode("verify");
        List<String> misses = new ArrayList<>();
        reader.setMissListener((c, i) -> {
            misses.add(c + ":" + i);
            throw new IllegalStateException("监听器异常不影响查询");
        });
        Assertions.assertEquals(1, reader.query(request("Eq1", "a"), twin).size());
        Assertions.assertEquals(Collections.singletonList("ACCCCC:a"), misses);
    }
}
