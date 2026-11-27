package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.List;

public class OptHubTwinMappingTest {

    private static final ObjectMapper M = new ObjectMapper();
    private static final ZoneId CST = ZoneId.of("Asia/Shanghai");

    private static JsonNode json(String s) throws Exception {
        return M.readTree(s.replace('\'', '"'));
    }

    @Test
    void pointTwinValidRowMapsToTwinShape() throws Exception {
        DtpTwinIotData d = OptHubTwinMapping.fromPointTwin(json("{'objectId':'Eq1','classCode':'ACATFC','infoCode':'runStatus',"
                + "'val':'1','time':'2026-11-27T04:59:48','onlineStatus':'online','transfiniteType':'normal'}"), CST, 0);
        Assertions.assertEquals("Eq1", d.getObjectId());
        Assertions.assertEquals("ACATFC", d.getClassCode());
        Assertions.assertEquals("runStatus", d.getInfoCode());
        Assertions.assertEquals("1", d.getDataValue());
        Assertions.assertEquals("20261127125948", d.getDataTime());
        Assertions.assertEquals("0", d.getDataIndate());
        Assertions.assertEquals(1, d.getDataFlag());
        Assertions.assertEquals(1, d.getDataStatus());
        Assertions.assertEquals(1, DtpTwinIotData.getVaildIotDatas(java.util.Collections.singletonList(d)).size());
    }

    @Test
    void pointTwinInvalidCases() throws Exception {
        Assertions.assertEquals(2, OptHubTwinMapping.fromPointTwin(json("{'objectId':'E','infoCode':'i','val':'~'}"), CST, 0).getDataFlag());
        Assertions.assertEquals(2, OptHubTwinMapping.fromPointTwin(json("{'objectId':'E','infoCode':'i','val':null}"), CST, 0).getDataFlag());
        Assertions.assertEquals(2, OptHubTwinMapping.fromPointTwin(json("{'objectId':'E','infoCode':'i','val':'9','transfiniteType':'oversize'}"), CST, 0).getDataFlag());
        // TWC / 规则点没有数据标记，视为有效
        Assertions.assertEquals(1, OptHubTwinMapping.fromPointTwin(json("{'objectId':'E','infoCode':'i','val':'0'}"), CST, 0).getDataFlag());
        Assertions.assertEquals(2, OptHubTwinMapping.fromPointTwin(json("{'objectId':'E','infoCode':'i','val':'1','onlineStatus':'offline'}"), CST, 0).getDataStatus());
        Assertions.assertNull(OptHubTwinMapping.fromPointTwin(json("{'infoCode':'i','val':'1'}"), CST, 0));
    }

    @Test
    void isoTimeVariants() {
        Assertions.assertEquals("20261127125948", OptHubTwinMapping.isoToDataTime("2026-11-27T04:59:48.123", CST));
        Assertions.assertEquals("20261127125948", OptHubTwinMapping.isoToDataTime("2026-11-27T04:59:48Z", CST));
        Assertions.assertNull(OptHubTwinMapping.isoToDataTime("bad", CST));
        Assertions.assertNull(OptHubTwinMapping.isoToDataTime(null, CST));
    }

    @Test
    void exportSplitsSamples() throws Exception {
        // 2026-11-27T04:59:48Z = 1795755588000
        List<FeedbackInfoMsgMqttVO> list = OptHubTwinMapping.fromExport(json("{'projectId':'Pj1','objectId':'Eq1','classCode':'ACATFC','reportTime':1,"
                + "'samples':[{'infoCode':'runStatus','val':'1','time':1795755588000,'validity':'VALID'},"
                + "{'infoCode':'temp','val':null,'time':1795755588000,'validity':'STALE'},{'val':'x'}]}"), CST, 15);
        Assertions.assertEquals(2, list.size());
        FeedbackInfoMsgMqttVO a = list.get(0);
        Assertions.assertEquals("Eq1", a.getObjectId());
        Assertions.assertEquals("ACATFC", a.getClassCode());
        Assertions.assertEquals("20261127125948", a.getDataTime());
        Assertions.assertEquals(15, a.getDataIndate());
        Assertions.assertEquals(1, a.getDataStatus());
        Assertions.assertEquals(0, a.getInvalid());
        FeedbackInfoMsgMqttVO b = list.get(1);
        Assertions.assertEquals(2, b.getDataStatus());
        Assertions.assertEquals(1, b.getInvalid());
        Assertions.assertTrue(OptHubTwinMapping.fromExport(json("{'objectId':'Eq1'}"), CST, 0).isEmpty());
    }
}
