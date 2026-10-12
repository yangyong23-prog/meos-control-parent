package com.persagy.apccontrol.basic.service.systemgraph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管线流向规则合并器单元测试
 *
 * <p>覆盖 PATCH 深合并、null 递归删除协议、空壳清理与序列化安全性。
 * 请求数据统一用 LinkedHashMap 模拟 Jackson 反序列化结果（显式 null 保留：
 * containsKey 为 true 且 get 为 null）。
 *
 * @author weiyizhong
 * @since 7.260902.0
 */
public class PipelineFlowRulesMergerTest {

    /**
     * 构造单个模式规则
     */
    private Map<String, Object> rule(String source, String ruleId) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("id", "condition-1");
        condition.put("deviceIds", java.util.Arrays.asList("Eg1", "Eg2"));
        condition.put("activationMode", "ALL_ON");
        Map<String, Object> ruleGroup = new LinkedHashMap<>();
        ruleGroup.put("id", ruleId);
        ruleGroup.put("direction", "F");
        ruleGroup.put("conditions", java.util.Collections.singletonList(condition));
        Map<String, Object> mode = new LinkedHashMap<>();
        mode.put("source", source);
        mode.put("ruleGroups", java.util.Collections.singletonList(ruleGroup));
        return mode;
    }

    /**
     * 构造请求 data
     */
    private Map<String, Object> incoming(Object version, Map<String, Object> lines) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (version != null) {
            data.put("version", version);
        }
        if (lines != null) {
            data.put("lines", lines);
        }
        return data;
    }

    /**
     * 构造单条线的请求数据
     */
    private Map<String, Object> line(Map<String, Object> modes) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("modes", modes);
        return line;
    }

    /**
     * 任务4.1：新增一条线的规则（存储中无该 lineId）
     */
    @Test
    public void shouldAddNewLineWhenLineIdAbsent() {
        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", rule("MANUAL", "rule-1"));
        Map<String, Object> incomingData = incoming(1, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(null, incomingData);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getIntValue("version"));
        JSONObject lines = result.getJSONObject("lines");
        Assertions.assertNotNull(lines);
        Assertions.assertNotNull(lines.getJSONObject("2003").getJSONObject("modes").getJSONObject("1.1"));
        Assertions.assertEquals("MANUAL", lines.getJSONObject("2003").getJSONObject("modes")
                .getJSONObject("1.1").getString("source"));
    }

    /**
     * 辅助：构造 lines Map
     */
    private Map<String, Object> lineOf(String lineId, Object lineValue) {
        Map<String, Object> lines = new LinkedHashMap<>();
        lines.put(lineId, lineValue);
        return lines;
    }

    /**
     * 任务4.1：覆盖某个模式时其他模式不受影响（PATCH 语义）
     */
    @Test
    public void shouldOverwriteModeAndKeepOthersUntouched() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\",\"ruleGroups\":[]},"
                + "\"1.2\":{\"source\":\"AUTO\",\"ruleGroups\":[]}}}}}");

        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", rule("AUTO", "rule-new"));
        Map<String, Object> incomingData = incoming(null, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        JSONObject storedModes = result.getJSONObject("lines").getJSONObject("2003").getJSONObject("modes");
        // 1.1 被新规则整体覆盖
        Assertions.assertEquals("AUTO", storedModes.getJSONObject("1.1").getString("source"));
        Assertions.assertEquals("rule-new", storedModes.getJSONObject("1.1")
                .getJSONArray("ruleGroups").getJSONObject(0).getString("id"));
        // 未提及的 1.2 原样保留
        Assertions.assertEquals("AUTO", storedModes.getJSONObject("1.2").getString("source"));
        // version 未提及保持不变
        Assertions.assertEquals(1, result.getIntValue("version"));
    }

    /**
     * 任务4.1：请求未提及的其他线不受影响
     */
    @Test
    public void shouldKeepUntouchedLinesWhenMerging() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{"
                + "\"2003\":{\"modes\":{\"1.1\":{\"source\":\"MANUAL\"}}},"
                + "\"2004\":{\"modes\":{\"1.1\":{\"source\":\"AUTO\"}}}}}");

        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", rule("AUTO", "rule-x"));
        Map<String, Object> incomingData = incoming(null, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        JSONObject lines = result.getJSONObject("lines");
        Assertions.assertNotNull(lines.getJSONObject("2003"));
        Assertions.assertEquals("AUTO", lines.getJSONObject("2004").getJSONObject("modes")
                .getJSONObject("1.1").getString("source"));
    }

    /**
     * 任务4.1：单次请求混合增删改（一条线新增模式、另一条线删除模式）
     */
    @Test
    public void shouldHandleMixedOperationsInSingleRequest() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{"
                + "\"2003\":{\"modes\":{\"1.1\":{\"source\":\"MANUAL\"}}},"
                + "\"2004\":{\"modes\":{\"1.1\":{\"source\":\"AUTO\"}}}}}");

        Map<String, Object> modes2003 = new LinkedHashMap<>();
        modes2003.put("2.1", rule("MANUAL", "rule-add"));
        Map<String, Object> modes2004 = new LinkedHashMap<>();
        modes2004.put("1.1", null);
        Map<String, Object> lines = new LinkedHashMap<>();
        lines.put("2003", line(modes2003));
        lines.put("2004", line(modes2004));
        Map<String, Object> incomingData = incoming(2, lines);

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        JSONObject linesResult = result.getJSONObject("lines");
        // 2003：新增 2.1 且未提及的 1.1 保留
        Assertions.assertEquals(2, linesResult.getJSONObject("2003").getJSONObject("modes").size());
        // 2004：唯一模式被删，空壳清理移除该线
        Assertions.assertNull(linesResult.getJSONObject("2004"));
        // version 请求带则覆盖
        Assertions.assertEquals(2, result.getIntValue("version"));
    }

    /**
     * 任务4.2（首个锁死路径）：显式 null 删除单个 modeKey，Jackson 语义
     * containsKey == true && get == null 必须穿透到删除逻辑
     */
    @Test
    public void shouldDeleteSingleModeWhenExplicitNull() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\"},"
                + "\"1.2\":{\"source\":\"AUTO\"}}}}}");

        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", null);
        Map<String, Object> incomingData = incoming(null, lineOf("2003", line(modes)));

        // 锁死 Jackson 语义：key 存在且值为 null
        Assertions.assertTrue(modes.containsKey("1.1"));
        Assertions.assertNull(modes.get("1.1"));

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        JSONObject storedModes = result.getJSONObject("lines").getJSONObject("2003").getJSONObject("modes");
        Assertions.assertNull(storedModes.getJSONObject("1.1"));
        Assertions.assertNotNull(storedModes.getJSONObject("1.2"));
    }

    /**
     * 任务4.2：显式 null 删除整条线
     */
    @Test
    public void shouldDeleteWholeLineWhenLineValueIsNull() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{"
                + "\"2003\":{\"modes\":{\"1.1\":{\"source\":\"MANUAL\"}}},"
                + "\"2004\":{\"modes\":{\"1.1\":{\"source\":\"AUTO\"}}}}}");

        Map<String, Object> lines = new LinkedHashMap<>();
        lines.put("2003", null);
        Map<String, Object> incomingData = incoming(null, lines);

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        JSONObject linesResult = result.getJSONObject("lines");
        Assertions.assertNull(linesResult.getJSONObject("2003"));
        Assertions.assertNotNull(linesResult.getJSONObject("2004"));
    }

    /**
     * 任务4.2：null 指向不存在的 key 时 no-op
     */
    @Test
    public void shouldNoopWhenNullPointsToMissingKey() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\"}}}}}");

        // 1.1 为 null 但该 key 不存在于存储
        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.9", null);
        Map<String, Object> incomingData = incoming(null, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getJSONObject("lines").getJSONObject("2003")
                .getJSONObject("modes").getJSONObject("1.1"));
    }

    /**
     * 任务4.3：modes 删空后移除该 lineId（其他线保留）
     */
    @Test
    public void shouldRemoveLineWhenAllModesDeleted() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{"
                + "\"2003\":{\"modes\":{\"1.1\":{\"source\":\"MANUAL\"}}},"
                + "\"2004\":{\"modes\":{\"1.1\":{\"source\":\"AUTO\"}}}}}");

        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", null);
        Map<String, Object> incomingData = incoming(null, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(stored, incomingData);

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getJSONObject("lines").getJSONObject("2003"));
        Assertions.assertNotNull(result.getJSONObject("lines").getJSONObject("2004"));
    }

    /**
     * 任务4.3：全部线删空后返回 null（调用方移除整个 key）
     */
    @Test
    public void shouldReturnNullWhenAllLinesDeleted() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\"}}}}}");

        Map<String, Object> lines = new LinkedHashMap<>();
        lines.put("2003", null);
        Map<String, Object> incomingData = incoming(null, lines);

        Assertions.assertNull(PipelineFlowRulesMerger.merge(stored, incomingData));
    }

    /**
     * 任务4.3：新建线 modes 全为 null 时不产生空壳条目
     */
    @Test
    public void shouldNotCreateEmptyShellForNewLineWithAllNullModes() {
        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", null);
        Map<String, Object> incomingData = incoming(1, lineOf("2003", line(modes)));

        Assertions.assertNull(PipelineFlowRulesMerger.merge(null, incomingData));
    }

    /**
     * 任务4.4：合并结果序列化往返无损、无 null 值残留、rect 等 code 共存
     */
    @Test
    public void shouldSurviveSerializationRoundTripAndCoexistWithOtherCodes() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\",\"ruleGroups\":[]}}}}}");

        Map<String, Object> modes = new LinkedHashMap<>();
        modes.put("1.1", rule("AUTO", "rule-1"));
        modes.put("1.2", null);
        Map<String, Object> incomingData = incoming(2, lineOf("2003", line(modes)));

        JSONObject merged = PipelineFlowRulesMerger.merge(stored, incomingData);
        Assertions.assertNotNull(merged);

        // 模拟存储链路：合并结果放入 customizeContent 与 rect 共存，经 toJSONString 压缩再解析
        JSONObject customizeContent = new JSONObject();
        customizeContent.put("rect", JSON.parseObject("{\"top\":-183,\"left\":0,\"width\":3330,\"height\":3183}"));
        customizeContent.put(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY, merged);
        JSONObject graphDataObject = new JSONObject();
        graphDataObject.put("customizeContent", customizeContent);

        JSONObject parsed = JSON.parseObject(graphDataObject.toJSONString());
        JSONObject parsedCustomize = parsed.getJSONObject("customizeContent");

        // rect 与 pipelineFlowRules 平级共存
        Assertions.assertNotNull(parsedCustomize.getJSONObject("rect"));
        JSONObject parsedRules = parsedCustomize.getJSONObject("pipelineFlowRules");
        Assertions.assertNotNull(parsedRules);
        Assertions.assertEquals(2, parsedRules.getIntValue("version"));
        JSONObject parsedModes = parsedRules.getJSONObject("lines").getJSONObject("2003").getJSONObject("modes");
        // 覆盖后的 1.1 数据完整
        Assertions.assertEquals("AUTO", parsedModes.getJSONObject("1.1").getString("source"));
        Assertions.assertEquals("rule-1", parsedModes.getJSONObject("1.1")
                .getJSONArray("ruleGroups").getJSONObject(0).getString("id"));
        // 显式 null 删除的 key 序列化后不存在，存储无 null 残留
        Assertions.assertFalse(parsedModes.containsKey("1.2"));
        Assertions.assertFalse(parsedModes.toJSONString().contains("null"));
    }

    /**
     * 任务1.4：incoming lines 缺失或 data 非 Map 时 no-op，已有数据原样保留
     */
    @Test
    public void shouldNoopWhenIncomingLinesMissing() {
        JSONObject stored = JSON.parseObject("{\"version\":1,\"lines\":{\"2003\":{\"modes\":{"
                + "\"1.1\":{\"source\":\"MANUAL\"}}}}}");

        // lines 缺失
        Assertions.assertEquals(stored, PipelineFlowRulesMerger.merge(stored, incoming(1, null)));
        // data 非 Map
        Assertions.assertEquals(stored, PipelineFlowRulesMerger.merge(stored, "invalid"));
        // data 为 null
        Assertions.assertEquals(stored, PipelineFlowRulesMerger.merge(stored, null));
    }

    /**
     * 任务1.4：存储为 null 时首次保存直接建结构
     */
    @Test
    public void shouldCreateStructureWhenStoredIsNull() {
        Map<String, Object> modes = new HashMap<>();
        modes.put("1.1", rule("MANUAL", "rule-1"));
        Map<String, Object> incomingData = incoming(1, lineOf("2003", line(modes)));

        JSONObject result = PipelineFlowRulesMerger.merge(null, incomingData);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getJSONObject("lines").size());
    }
}
