package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 运行标准过渡 B1–B4：开关默认关闭时行为不变；打开后的判定、只读拦截、计数与转发。
 */
class OptStandardsTransitionTest {

    private final MockEnvironment env = new MockEnvironment();
    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final AtomicInteger loads = new AtomicInteger();
    private final ObjectMapper mapper = new ObjectMapper();

    private OptStandardsMode mode;

    @BeforeEach
    void setUp() {
        OptSwitch.setEnvironmentForTest(env);
        mode = new OptStandardsMode(projectId -> {
            loads.incrementAndGet();
            if ("PjBroken".equals(projectId)) {
                throw new IllegalStateException("Table 'demand_custom_config' doesn't exist");
            }
            return "PjNew".equals(projectId);
        }, now::get);
    }

    @AfterEach
    void tearDown() {
        OptSwitch.setEnvironmentForTest(null);
    }

    // ---------------- B1 ----------------

    @Test
    void modeIsLegacyAndDoesNotQueryWhenSwitchOff() {
        Map<String, Object> r = mode.resolve("PjNew");
        Assertions.assertEquals("LEGACY", r.get("mode"));
        Assertions.assertEquals(false, r.get("readOnly"));
        Assertions.assertEquals("switch-off", r.get("source"));
        Assertions.assertEquals(0, loads.get());
    }

    @Test
    void modeFollowsStandardConfigRowAndIsCached() {
        env.setProperty("switch.opt.standardsMode", "true");
        Assertions.assertEquals("STANDARD", mode.resolve("PjNew").get("mode"));
        Assertions.assertEquals("LEGACY", mode.resolve("PjOld").get("mode"));
        Assertions.assertTrue(mode.isStandard("PjNew"));
        Assertions.assertEquals(2, loads.get(), "60 秒内同一项目只查一次");
        now.addAndGet(60_000L);
        mode.isStandard("PjNew");
        Assertions.assertEquals(3, loads.get());
        mode.evict();
        mode.isStandard("PjNew");
        Assertions.assertEquals(4, loads.get());
    }

    @Test
    void readOnlyNeedsBothSwitches() {
        env.setProperty("switch.opt.standardsMode", "true");
        Assertions.assertEquals(false, mode.resolve("PjNew").get("readOnly"));
        env.setProperty("switch.opt.standardsReadonly", "true");
        Assertions.assertEquals(true, mode.resolve("PjNew").get("readOnly"));
        Assertions.assertEquals(false, mode.resolve("PjOld").get("readOnly"));
    }

    @Test
    void queryFailureFallsBackToLegacyWithError() {
        env.setProperty("switch.opt.standardsMode", "true");
        Map<String, Object> r = mode.resolve("PjBroken");
        Assertions.assertEquals("LEGACY", r.get("mode"));
        Assertions.assertNotNull(r.get("error"));
        mode.resolve("PjBroken");
        Assertions.assertEquals(1, loads.get(), "失败结果也缓存");
    }

    @Test
    void listSourceUsesConfiguredProjects() {
        env.setProperty("switch.opt.standardsMode", "true");
        env.setProperty("switch.opt.standards.mode-source", "list");
        env.setProperty("switch.opt.standards.switched-projects", " PjA , PjB ");
        Assertions.assertTrue(mode.isStandard("PjB"));
        Assertions.assertFalse(mode.isStandard("PjNew"));
        Assertions.assertEquals(0, loads.get());
    }

    // ---------------- B2 ----------------

    private MockHttpServletResponse write(String path, String projectId) throws Exception {
        OptStandardsReadonlyInterceptor interceptor = new OptStandardsReadonlyInterceptor(mode, mapper);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", path);
        if (projectId != null) {
            req.addHeader("projectId", projectId);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        boolean pass = interceptor.preHandle(req, resp, new Object());
        resp.setHeader("x-pass", String.valueOf(pass));
        return resp;
    }

    @Test
    void writesPassWhenSwitchOff() throws Exception {
        env.setProperty("switch.opt.standardsMode", "true");
        Assertions.assertEquals("true", write("/valueSelection/saveConfig", "PjNew").getHeader("x-pass"));
    }

    @Test
    void switchedProjectWritesAreBlocked() throws Exception {
        env.setProperty("switch.opt.standardsMode", "true");
        env.setProperty("switch.opt.standardsReadonly", "true");
        MockHttpServletResponse blocked = write("/shellConfig/mpcAgentCfg", "PjNew");
        Assertions.assertEquals("false", blocked.getHeader("x-pass"));
        JsonNode body = mapper.readTree(blocked.getContentAsString());
        Assertions.assertEquals("A0403", body.get("code").asText());
        Assertions.assertTrue(blocked.getContentAsString().contains("由云端运行标准管理"));

        Assertions.assertEquals("true", write("/shellConfig/mpcAgentCfg", "PjOld").getHeader("x-pass"), "未切项目照旧可写");
        Assertions.assertEquals("true", write("/shellConfig/queryMpcAgentCfg", "PjNew").getHeader("x-pass"), "查询不拦");
        Assertions.assertEquals("false", write("/interventionTaskConfig/saveOrUpdate", "PjNew").getHeader("x-pass"), "干预任务归运行标准");
        Assertions.assertEquals("false", write("/shellConfig/save", "PjNew").getHeader("x-pass"), "壳配置归运行标准");
        Assertions.assertEquals("true", write("/intervention/release", "PjNew").getHeader("x-pass"), "解除干预是手动操作，不拦");
        Assertions.assertEquals("true", write("/valueSelection/saveConfig", null).getHeader("x-pass"), "无项目放行");
    }

    @Test
    void readonlyPathsCanBeOverridden() throws Exception {
        env.setProperty("switch.opt.standardsMode", "true");
        env.setProperty("switch.opt.standardsReadonly", "true");
        env.setProperty("switch.opt.standards.readonly-paths", "/demand/scene/**");
        Assertions.assertEquals("false", write("/demand/scene/editSceneConfigs", "PjNew").getHeader("x-pass"));
        Assertions.assertEquals("true", write("/valueSelection/saveConfig", "PjNew").getHeader("x-pass"));
    }

    // ---------------- B3 ----------------

    static class MemoryStore implements OptDtpMeosCallCounter.Store {
        final Map<String, Map<String, String>> data = new HashMap<>();

        @Override
        public void increment(String key, String field, long keepSeconds) {
            data.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(field, "1",
                    (a, b) -> String.valueOf(Long.parseLong(a) + 1));
        }

        @Override
        public void put(String key, String field, String value) {
            data.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(field, value);
        }

        @Override
        public Map<String, String> entries(String key) {
            return data.getOrDefault(key, new LinkedHashMap<>());
        }
    }

    @Test
    void callCounterCountsOnlyWhenOnAndOnlyTheFiveApis() {
        MemoryStore store = new MemoryStore();
        OptDtpMeosCallCounter counter = new OptDtpMeosCallCounter(store, now::get, ZoneId.of("Asia/Shanghai"));
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/valueSelection/currentApplyConfig");
        req.addHeader("projectId", "PjNew");
        Assertions.assertTrue(counter.preHandle(req, new MockHttpServletResponse(), new Object()));
        Assertions.assertTrue(store.data.isEmpty(), "开关关闭不计数");

        env.setProperty("switch.opt.dtpMeosCallCounter", "true");
        counter.preHandle(req, new MockHttpServletResponse(), new Object());
        counter.preHandle(req, new MockHttpServletResponse(), new Object());
        MockHttpServletRequest browser = new MockHttpServletRequest("POST", "/valueSelection/currentApplyConfig");
        browser.addHeader("projectId", "PjNew");
        browser.addHeader("User-Agent", "Mozilla/5.0");
        counter.preHandle(browser, new MockHttpServletResponse(), new Object());
        counter.preHandle(new MockHttpServletRequest("POST", "/valueSelection/saveConfig"), new MockHttpServletResponse(), new Object());

        Map<String, String> today = store.entries(OptDtpMeosCallCounter.KEY_PREFIX + "19700101");
        Assertions.assertEquals("2", today.get("/valueSelection/currentApplyConfig|PjNew|service"));
        Assertions.assertEquals("1", today.get("/valueSelection/currentApplyConfig|PjNew|browser"));
        Assertions.assertEquals(2, today.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> days = (Map<String, Object>) counter.stats(7).get("days");
        Assertions.assertTrue(days.containsKey("19700101"));
    }

    @Test
    void callCounterFailureDoesNotBreakRequest() {
        env.setProperty("switch.opt.dtpMeosCallCounter", "true");
        OptDtpMeosCallCounter counter = new OptDtpMeosCallCounter(new MemoryStore() {
            @Override
            public void increment(String key, String field, long keepSeconds) {
                throw new IllegalStateException("redis down");
            }
        }, now::get, ZoneId.of("Asia/Shanghai"));
        Assertions.assertTrue(counter.preHandle(new MockHttpServletRequest("POST", "/demand/listInstanceConfig"),
                new MockHttpServletResponse(), new Object()));
    }

    // ---------------- B4 ----------------

    @Test
    void displayRequiresSwitchAndForwardsWhitelistedFields() throws Exception {
        AtomicReference<Map<String, Object>> sent = new AtomicReference<>();
        JsonNode reply = mapper.readTree("{\"code\":\"00000\",\"message\":\"ok\",\"data\":[{\"instanceId\":\"Sp1\"}]}");
        OptStandardsDisplay display = new OptStandardsDisplay((p, body) -> {
            sent.set(body);
            return reply;
        }, (p, body) -> reply);
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", "2026-11-01");
        param.put("endDate", "2026-12-01");
        param.put("standardTarget", "TDB");
        param.put("projectId", "PjOther");
        param.put("unknown", "x");

        Assertions.assertThrows(IllegalStateException.class, () -> display.listResult("PjNew", param));
        Assertions.assertNull(sent.get(), "开关关闭不调用 dtp-meos");

        env.setProperty("switch.opt.standardsDisplay", "true");
        JsonNode data = display.listResult("PjNew", param);
        Assertions.assertEquals("Sp1", data.get(0).get("instanceId").asText());
        Assertions.assertEquals("PjNew", sent.get().get("projectId"), "项目以上下文为准");
        Assertions.assertFalse(sent.get().containsKey("unknown"));
        Assertions.assertEquals("TDB", sent.get().get("standardTarget"));
    }

    @Test
    void unwrapRejectsBusinessFailure() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> OptStandardsDisplay.unwrap(
                mapper.readTree("{\"code\":\"B0001\",\"message\":\"err\",\"data\":null}")));
        Assertions.assertTrue(OptStandardsDisplay.unwrap(mapper.readTree("[]")).isArray());
    }
}
