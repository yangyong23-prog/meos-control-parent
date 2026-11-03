package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * B2 契约测试：apc:iot_real_time_data:{objectId}:{infoCode} 的 key 格式和值 JSON 字段。
 * dtp-meos 侧应有一份对应的读方契约测试。改动本测试即是改契约，须同步 bdtp。
 */
public class OptRealtimeContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void keyFormat() {
        Assertions.assertEquals("apc:iot_real_time_data:Eq123:supplyTemp",
                OptRealtimeCacheCore.key("Eq123", "supplyTemp"));
    }

    @Test
    void valueJsonFieldsAreStable() throws Exception {
        OptRealtimeValue v = new OptRealtimeValue();
        v.setObjectId("Eq123");
        v.setClassCode("ACCCCC");
        v.setInfoCode("supplyTemp");
        v.setDataValue("7.5");
        v.setDataTime("20261103120000");
        v.setDataIndate(15);
        v.setDataStatus(1);
        v.setSource("mqtt");
        v.setWriteTime(1793700000000L);
        JsonNode json = mapper.readTree(mapper.writeValueAsString(v));
        Set<String> fields = new HashSet<>();
        for (Iterator<String> it = json.fieldNames(); it.hasNext(); ) {
            fields.add(it.next());
        }
        Assertions.assertEquals(new HashSet<>(Arrays.asList("objectId", "classCode", "infoCode", "dataValue",
                "dataTime", "dataIndate", "dataStatus", "source", "writeTime")), fields);
        Assertions.assertTrue(json.get("dataValue").isTextual(), "dataValue 是字符串（Twin 原始值，未换算）");
        Assertions.assertTrue(json.get("dataIndate").isInt(), "dataIndate 是整数（分钟）");
        Assertions.assertTrue(json.get("writeTime").isNumber(), "writeTime 是毫秒时间戳");
        Assertions.assertFalse(json.has("@class"), "值是普通 JSON，不带类型信息");
    }

    @Test
    void nullFieldsAreOmittedAndUnknownFieldsTolerated() throws Exception {
        OptRealtimeValue v = new OptRealtimeValue();
        v.setObjectId("Eq1");
        v.setInfoCode("a");
        v.setDataValue("~");
        String json = mapper.writeValueAsString(v);
        Assertions.assertFalse(json.contains("dataIndate"));
        OptRealtimeValue back = mapper.readValue("{\"objectId\":\"Eq1\",\"infoCode\":\"a\",\"dataValue\":\"~\",\"futureField\":1}",
                OptRealtimeValue.class);
        Assertions.assertTrue(back.isOffline());
    }
}
