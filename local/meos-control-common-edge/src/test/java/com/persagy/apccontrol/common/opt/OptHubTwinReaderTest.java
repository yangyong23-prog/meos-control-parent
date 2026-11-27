package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OptHubTwinReaderTest {

    private static final ObjectMapper M = new ObjectMapper();

    private final List<Map<String, Object>> hubCalls = new ArrayList<>();
    private final List<QueryTwinDataDTO> twinCalls = new ArrayList<>();

    /** meos-hub 桩：对请求的每个点返回 val=1，"missing" 信息点不返回 */
    @SuppressWarnings("unchecked")
    private final Function<Map<String, Object>, JsonNode> hub = body -> {
        hubCalls.add(body);
        ObjectNode resp = M.createObjectNode();
        resp.put("code", "00000");
        ArrayNode data = resp.putArray("data");
        for (Map<String, Object> p : (List<Map<String, Object>>) body.get("pointSet")) {
            for (String info : (Collection<String>) p.get("infoCodeSet")) {
                if ("missing".equals(info)) {
                    continue;
                }
                ObjectNode row = data.addObject();
                row.put("objectId", (String) p.get("objectId"));
                row.put("infoCode", info);
                row.put("val", "1");
                row.put("time", "2026-11-27T04:59:48");
                row.put("onlineStatus", "online");
            }
        }
        return resp;
    };

    private final Function<QueryTwinDataDTO, List<DtpTwinIotData>> twin = dto -> {
        twinCalls.add(dto);
        return Collections.emptyList();
    };

    private final OptHubTwinReader reader = new OptHubTwinReader(hub, "PjDefault", ZoneId.of("Asia/Shanghai"), 0);

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static void mode(String mode) {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.twinSource", mode));
    }

    private static QueryTwinDataOnMomentDTO obj(String projectId, String objectId, String... infoCodes) {
        return new QueryTwinDataOnMomentDTO(projectId, objectId, new LinkedHashSet<>(Arrays.asList(infoCodes)));
    }

    @Test
    void defaultModeIsTwinAndUntouched() {
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a"))));
        Assertions.assertFalse(reader.handles(dto));
        reader.fetch(dto, twin);
        Assertions.assertEquals(1, twinCalls.size());
        Assertions.assertTrue(hubCalls.isEmpty());
    }

    @Test
    void unknownModeFallsBackToTwin() {
        mode("on");
        Assertions.assertEquals(OptHubTwinReader.MODE_TWIN, OptHubTwinReader.mode());
    }

    @Test
    void hubModeReadsHubOnly() {
        mode("hub");
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a", "missing"))));
        Assertions.assertTrue(reader.handles(dto));
        List<DtpTwinIotData> r = reader.fetch(dto, twin);
        Assertions.assertTrue(twinCalls.isEmpty());
        Assertions.assertEquals(1, hubCalls.size());
        Assertions.assertEquals("Pj1", hubCalls.get(0).get("projectId"));
        Assertions.assertEquals(1, r.size());
        Assertions.assertEquals("a", r.get(0).getInfoCode());
        Assertions.assertEquals("20261127125948", r.get(0).getDataTime());
    }

    @Test
    void historyQueriesStayOnTwin() {
        mode("hub");
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a"))));
        dto.setTwinTime("20261127120000");
        Assertions.assertFalse(reader.handles(dto));
        reader.fetch(dto, twin);
        Assertions.assertEquals(1, twinCalls.size());
        Assertions.assertTrue(hubCalls.isEmpty());
    }

    @Test
    void splitsBy200ObjectsAndProject() {
        mode("hub");
        List<QueryTwinDataOnMomentDTO> objects = new ArrayList<>();
        for (int i = 0; i < 450; i++) {
            objects.add(obj(i < 400 ? "Pj1" : null, "Eq" + i, "a"));
        }
        List<DtpTwinIotData> r = reader.fetch(new QueryTwinDataDTO(objects), twin);
        Assertions.assertEquals(450, r.size());
        Assertions.assertEquals(3, hubCalls.size());
        Assertions.assertEquals("PjDefault", hubCalls.get(2).get("projectId"));
        Assertions.assertEquals(50, ((List<?>) hubCalls.get(2).get("pointSet")).size());
    }

    @Test
    void verifyReturnsTwinAndAlsoQueriesHub() {
        mode("verify");
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a"))));
        List<DtpTwinIotData> r = reader.fetch(dto, twin);
        Assertions.assertTrue(r.isEmpty());
        Assertions.assertEquals(1, twinCalls.size());
        Assertions.assertEquals(1, hubCalls.size());
    }

    @Test
    void verifySwallowsHubFailure() {
        mode("verify");
        OptHubTwinReader failing = new OptHubTwinReader(b -> {
            throw new IllegalStateException("down");
        }, "Pj", ZoneId.of("Asia/Shanghai"), 0);
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a"))));
        Assertions.assertDoesNotThrow(() -> failing.fetch(dto, twin));
    }

    @Test
    void hubModeBadResponseThrows() {
        mode("hub");
        OptHubTwinReader bad = new OptHubTwinReader(b -> M.createObjectNode().put("code", "500"), "Pj", ZoneId.of("Asia/Shanghai"), 0);
        QueryTwinDataDTO dto = new QueryTwinDataDTO(new ArrayList<>(Collections.singletonList(obj("Pj1", "Eq1", "a"))));
        Assertions.assertThrows(IllegalStateException.class, () -> bad.fetch(dto, twin));
    }

    @Test
    void compareOutcomes() {
        Assertions.assertEquals(OptHubTwinReader.Outcome.match, OptHubTwinReader.compare("1.0", "1"));
        Assertions.assertEquals(OptHubTwinReader.Outcome.hubMiss, OptHubTwinReader.compare("1", null));
        Assertions.assertEquals(OptHubTwinReader.Outcome.twinMiss, OptHubTwinReader.compare(null, "1"));
        Assertions.assertEquals(OptHubTwinReader.Outcome.bothEmpty, OptHubTwinReader.compare(null, null));
        Assertions.assertEquals(OptHubTwinReader.Outcome.mismatch, OptHubTwinReader.compare("1", "2"));
    }
}
