package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class OptPointBatchTest {

    /** 简化的 Twin 记录 */
    static final class Rec {
        final String objectId;
        final String infoCode;
        final String value;

        Rec(String objectId, String infoCode, String value) {
            this.objectId = objectId;
            this.infoCode = infoCode;
            this.value = value;
        }
    }

    private final List<QueryTwinDataDTO> calls = new ArrayList<>();

    /** Twin 桩：infoCode 为 "none" 的点无有效值；"dup" 返回两条，第一条有效 */
    private final Function<QueryTwinDataDTO, List<Rec>> twin = dto -> {
        calls.add(dto);
        List<Rec> out = new ArrayList<>();
        for (QueryTwinDataOnMomentDTO o : dto.getObjects()) {
            for (String i : o.getInfoCodes()) {
                if ("none".equals(i)) {
                    continue;
                }
                out.add(new Rec(o.getObjectId(), i, o.getObjectId() + "." + i));
                if ("dup".equals(i)) {
                    out.add(new Rec(o.getObjectId(), i, "second"));
                }
            }
        }
        return out;
    };

    /** 原逻辑：每个点单独查一次，取 get(0) */
    private String single(String objectId, String infoCode) {
        List<Rec> r = twin.apply(new QueryTwinDataDTO(Collections.singletonList(
                new QueryTwinDataOnMomentDTO("Pj1", objectId, Collections.singleton(infoCode)))));
        return r.isEmpty() ? null : r.get(0).value;
    }

    @Test
    void batchFillsSameValuesAsPerPointQueries() {
        List<String[]> points = new ArrayList<>();
        for (int o = 0; o < 450; o++) {
            points.add(new String[]{"Eq" + o, "a"});
            if (o % 3 == 0) {
                points.add(new String[]{"Eq" + o, "none"});
            }
            if (o % 5 == 0) {
                points.add(new String[]{"Eq" + o, "dup"});
                points.add(new String[]{"Eq" + o, "a"});   // 同一点登记两次
            }
        }
        List<String> before = new ArrayList<>();
        for (String[] p : points) {
            before.add(single(p[0], p[1]));
        }
        int perPointCalls = calls.size();
        calls.clear();

        OptPointBatch<int[]> batch = new OptPointBatch<>();
        String[] after = new String[points.size()];
        for (int i = 0; i < points.size(); i++) {
            batch.add(points.get(i)[0], points.get(i)[1], new int[]{i});
        }
        batch.flush("Pj1", twin, r -> r.objectId, r -> r.infoCode, (t, r) -> after[t[0]] = r == null ? null : r.value);

        List<String> afterList = new ArrayList<>();
        Collections.addAll(afterList, after);
        Assertions.assertEquals(before, afterList, "改造前后各点取值一致（含无值和重复记录）");
        Assertions.assertEquals(points.size(), perPointCalls);
        Assertions.assertEquals(3, calls.size(), "450 个对象，200 一片");
        Assertions.assertTrue(calls.get(0).getObjects().stream().noneMatch(o -> o.getInfoCodes().size() > 3), "不做笛卡尔积");
    }

    @Test
    void emptyBatchDoesNotCallAndFailurePropagates() {
        new OptPointBatch<String>().flush("Pj1", twin, r -> r.objectId, r -> r.infoCode, (t, r) -> Assertions.fail());
        Assertions.assertTrue(calls.isEmpty());
        OptPointBatch<String> b = new OptPointBatch<>();
        b.add("Eq1", "a", "t");
        IllegalStateException boom = new IllegalStateException("twin down");
        Assertions.assertSame(boom, Assertions.assertThrows(IllegalStateException.class,
                () -> b.flush("Pj1", d -> {
                    throw boom;
                }, (Rec r) -> r.objectId, r -> r.infoCode, (t, r) -> {
                })));
    }
}
