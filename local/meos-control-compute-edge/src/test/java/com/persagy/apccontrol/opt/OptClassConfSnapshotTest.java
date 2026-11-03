package com.persagy.apccontrol.opt;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptClassConfSnapshotTest {

    private static JSONObject conf(String paramCode, String value) {
        JSONObject o = new JSONObject();
        o.put("classCode", "ACPPC");
        o.put("paramCode", paramCode);
        if (value != null) {
            o.put("paramValue", value);
        }
        return o;
    }

    private static Map<String, String> values(List<JSONObject> list) {
        return list.stream().collect(Collectors.toMap(o -> o.getString("paramCode"), o -> String.valueOf(o.getString("paramValue"))));
    }

    @Test
    void partialWriteKeepsOtherItemSoItIsNotRewrittenNextRound() {
        List<JSONObject> prev = Arrays.asList(conf("vFDMinFreqCtrl", "30"), conf("vFDMaxFreqCtrl", "50"));
        List<JSONObject> merged = OptClassConfSnapshot.merge(prev, Collections.singletonList(conf("vFDMinFreqCtrl", "35")));
        Assertions.assertEquals(2, merged.size(), "原逻辑这里只剩本轮写入的 min，下轮 max 又被判为变化");
        Assertions.assertEquals("35", values(merged).get("vFDMinFreqCtrl"));
        Assertions.assertEquals("50", values(merged).get("vFDMaxFreqCtrl"));
    }

    @Test
    void deleteRemovesFromSnapshotAndNullsAreTolerated() {
        List<JSONObject> snap = OptClassConfSnapshot.merge(null, Arrays.asList(conf("vFDMinFreqCtrl", "30"), conf("vFDMaxFreqCtrl", "50")));
        List<JSONObject> after = OptClassConfSnapshot.remove(snap, Arrays.asList(conf("vFDMinFreqCtrl", null), conf("vFDMaxFreqCtrl", null)));
        Assertions.assertTrue(after.isEmpty());
        Assertions.assertTrue(OptClassConfSnapshot.remove(null, null).isEmpty());
    }
}
