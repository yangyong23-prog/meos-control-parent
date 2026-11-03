package com.persagy.apccontrol.opt;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

public class OptMqttPayloadsTest {

    /**
     * 与运行时 CommonConst.JSON_MAPPER 同配置（见 BeanUtil 静态块）。不直接用 CommonConst：
     * 它的静态字段依赖 Spring 上下文，单元测试里会初始化失败。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new SimpleDateFormat("yyyyMMddHHmmss"));

    private static final String PAYLOAD = "{\"objectId\":\"Eq1\",\"classCode\":\"ACCCCC\",\"infoCode\":\"runStatus\","
            + "\"dataTime\":\"20261103120000\",\"value\":\"1\",\"status\":1,\"dataIndate\":15}";

    /**
     * 单次 Jackson 解析与原来的 fastjson 两次规整 + Jackson 结果一致。
     */
    @Test
    void parseOnceMatchesLegacyPath() throws Exception {
        FeedbackInfoMsgMqttVO parsed = OptMqttPayloads.parseTwin(PAYLOAD, MAPPER);
        JSONObject obj = JSONObject.parseObject(PAYLOAD);
        JSONObject jsonValue = JSONObject.parseObject(obj.toString());
        FeedbackInfoMsgMqttVO legacy = MAPPER.readValue(jsonValue.toJSONString(), FeedbackInfoMsgMqttVO.class);
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
        Assertions.assertNull(OptMqttPayloads.parseTwin("null", MAPPER));
        Assertions.assertNull(OptMqttPayloads.parseTwin("not json", MAPPER));
    }
}
