package com.persagy.apccontrol.basic.service.systemgraph;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 管线流向规则合并器
 *
 * <p>处理前端通过 {@code editSystemGraph}（type=CUSTOMIZE，code=pipelineFlowRules）提交的
 * 管线流向规则数据，以 PATCH 语义深合并到存储数据：请求中出现的 key 才被触碰，
 * 未出现的 key 保持不变；显式 null 表示删除（lines[lineId] 为 null 删除整条线，
 * modes[modeKey] 为 null 删除该模式）；合并后 modes 为空的线自动移除条目，
 * 全部删空后不再保留 pipelineFlowRules。
 *
 * <p>硬性约束：全程只走 Map API 区分显式 null 与 key 不存在，严禁对请求数据做
 * fastjson 序列化往返（toJSONString 默认丢弃 null 值，会令删除协议静默失效）。
 *
 * @author weiyizhong
 * @since 7.260902.0
 */
public final class PipelineFlowRulesMerger {

    /**
     * customizeContent 中管线流向规则数据的固定 key
     */
    public static final String PIPELINE_FLOW_RULES_KEY = "pipelineFlowRules";

    /**
     * lines 层 key
     */
    private static final String LINES_KEY = "lines";

    /**
     * modes 层 key
     */
    private static final String MODES_KEY = "modes";

    /**
     * version 字段 key
     */
    private static final String VERSION_KEY = "version";

    private PipelineFlowRulesMerger() {
    }

    /**
     * 将请求中的管线流向规则数据深合并到存储数据
     *
     * @param stored   存储中的 pipelineFlowRules，可为 null（尚无数据）
     * @param incoming 请求数据，HTTP 反序列化得到的 Map（显式 null 保留）或 JSONObject
     * @return 合并后的 pipelineFlowRules；规则被删空或无可保留数据时返回 null，调用方应移除该 key
     */
    @SuppressWarnings("unchecked")
    public static JSONObject merge(JSONObject stored, Object incoming) {
        JSONObject result = stored != null ? stored : new JSONObject();
        if (incoming instanceof Map) {
            Map<String, Object> incomingMap = (Map<String, Object>) incoming;
            Object incomingVersion = incomingMap.get(VERSION_KEY);
            if (incomingVersion != null) {
                result.put(VERSION_KEY, incomingVersion);
            }
            Object incomingLines = incomingMap.get(LINES_KEY);
            if (incomingLines instanceof Map) {
                JSONObject lines = result.getJSONObject(LINES_KEY);
                if (lines == null) {
                    lines = new JSONObject();
                }
                mergeLines(lines, (Map<String, Object>) incomingLines);
                if (lines.isEmpty()) {
                    result.remove(LINES_KEY);
                } else {
                    result.put(LINES_KEY, lines);
                }
            }
        }
        JSONObject finalLines = result.getJSONObject(LINES_KEY);
        if (finalLines == null || finalLines.isEmpty()) {
            return null;
        }
        return result;
    }

    /**
     * 合并 lines 层：lineId 值为 null 时删除整条线，非 null 时进入 modes 层合并
     *
     * @param lines         存储中的 lines（就地修改）
     * @param incomingLines 请求中的 lines
     */
    @SuppressWarnings("unchecked")
    private static void mergeLines(JSONObject lines, Map<String, Object> incomingLines) {
        for (Map.Entry<String, Object> lineEntry : incomingLines.entrySet()) {
            String lineId = lineEntry.getKey();
            Object lineValue = lineEntry.getValue();
            if (lineValue == null) {
                lines.remove(lineId);
                continue;
            }
            if (!(lineValue instanceof Map)) {
                continue;
            }
            Object incomingModes = ((Map<String, Object>) lineValue).get(MODES_KEY);
            JSONObject line = lines.getJSONObject(lineId);
            if (line == null) {
                line = new JSONObject();
            }
            if (incomingModes instanceof Map) {
                JSONObject modes = line.getJSONObject(MODES_KEY);
                if (modes == null) {
                    modes = new JSONObject();
                }
                mergeModes(modes, (Map<String, Object>) incomingModes);
                line.put(MODES_KEY, modes);
            }
            JSONObject finalModes = line.getJSONObject(MODES_KEY);
            if (finalModes == null || finalModes.isEmpty()) {
                lines.remove(lineId);
            } else {
                lines.put(lineId, line);
            }
        }
    }

    /**
     * 合并 modes 层：modeKey 值为 null 时删除该模式，非 null 时整体覆盖该模式
     *
     * @param modes         存储中的 modes（就地修改）
     * @param incomingModes 请求中的 modes
     */
    private static void mergeModes(JSONObject modes, Map<String, Object> incomingModes) {
        for (Map.Entry<String, Object> modeEntry : incomingModes.entrySet()) {
            String modeKey = modeEntry.getKey();
            Object modeValue = modeEntry.getValue();
            if (modeValue == null) {
                modes.remove(modeKey);
            } else {
                modes.put(modeKey, modeValue);
            }
        }
    }
}
