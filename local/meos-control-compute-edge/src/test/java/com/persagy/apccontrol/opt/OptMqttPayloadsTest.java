package com.persagy.apccontrol.opt;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptMqttPayloadsTest {

    private static final String PAYLOAD = "{\"objectId\":\"Eq1\",\"classCode\":\"ACCCCC\",\"infoCode\":\"runStatus\","
            + "\"dataTime\":\"20261103120000\",\"value\":\"1\",\"status\":1,\"dataIndate\":15}";

    /**
     * 单次 Jackson 解析与原来的 fastjson 两次规整 + Jackson 结果一致。
     */
    @Test
    void parseOnceMatchesLegacyPath() throws Exception {
        FeedbackInfoMsgMqttVO parsed = OptMqttPayloads.parseTwin(PAYLOAD);
        JSONObject obj = JSONObject.parseObject(PAYLOAD);
        JSONObject jsonValue = JSONObject.parseObject(obj.toString());
        FeedbackInfoMsgMqttVO legacy = CommonConst.JSON_MAPPER.readValue(jsonValue.toJSONString(), FeedbackInfoMsgMqttVO.class);
        Assertions.assertEquals(legacy.getObjectId(), parsed.getObjectId());
        Assertions.assertEquals(legacy.getClassCode(), parsed.getClassCode());
        Assertions.assertEquals(legacy.getInfoCode(), parsed.getInfoCode());
        Assertions.assertEquals(legacy.getDataTime(), parsed.getDataTime());
        Assertions.assertEquals(legacy.getDataValue(), parsed.getDataValue());
        Assertions.assertEquals(legacy.getDataStatus(), parsed.getDataStatus());
        Assertions.assertEquals(legacy.getDataIndate(), parsed.getDataIndate());
        Assertions.assertEquals("1", parsed.getDataValue());
        Assertions.assertEquals(Integer.valueOf(15), parsed.getDataIndate());
    }

    @Test
    void nullAndGarbagePayloadReturnNull() {
        Assertions.assertNull(OptMqttPayloads.parseTwin("null"));
        Assertions.assertNull(OptMqttPayloads.parseTwin("not json"));
    }
}
