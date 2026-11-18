package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 运行标准过渡 B4：监测页展示的标准值改由 dtp-meos 的运行标准解析结果提供（经我方后端转发）。
 *
 * <p>只转发白名单字段，projectId 以请求上下文为准。{@code switch.opt.standardsDisplay} 关（默认）时
 * 不调用 dtp-meos，直接抛 {@link IllegalStateException}，由 Controller 返回“未开启”。
 * Blake 2026-11-18 定：不依赖 mpc-common-edge。
 */
public class OptStandardsDisplay {

    static final Set<String> RESULT_FIELDS = new HashSet<>(Arrays.asList(
            "instanceIds", "instanceId", "instanceGroup", "instanceType", "instanceCode",
            "standardDate", "beginDate", "endDate", "standardTarget"));
    static final Set<String> DETAIL_FIELDS = new HashSet<>(Arrays.asList(
            "instanceIds", "instanceId", "instanceGroup", "instanceType", "instanceCode",
            "standardTime", "beginTime", "endTime", "standardType"));

    /** 远程调用：(projectId, body) → dtp-meos 原始响应 */
    public interface Remote {
        JsonNode call(String projectId, Map<String, Object> body);
    }

    private final Remote resultRemote;
    private final Remote detailRemote;

    public OptStandardsDisplay(Remote resultRemote, Remote detailRemote) {
        this.resultRemote = resultRemote;
        this.detailRemote = detailRemote;
    }

    public JsonNode listResult(String projectId, Map<String, Object> param) {
        return forward("result", projectId, param, RESULT_FIELDS, resultRemote);
    }

    public JsonNode listDetail(String projectId, Map<String, Object> param) {
        return forward("detail", projectId, param, DETAIL_FIELDS, detailRemote);
    }

    private static JsonNode forward(String name, String projectId, Map<String, Object> param, Set<String> fields, Remote remote) {
        if (!OptSwitch.on(OptSwitch.STANDARDS_DISPLAY)) {
            throw new IllegalStateException("switch.opt.standardsDisplay 未开启");
        }
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少 projectId");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (param != null) {
            param.forEach((k, v) -> {
                if (fields.contains(k) && v != null) {
                    body.put(k, v);
                }
            });
        }
        body.put("projectId", projectId);
        OptMetrics.inc("standards.display." + name);
        JsonNode response = remote.call(projectId, body);
        return unwrap(response);
    }

    /**
     * dtp-meos 返回框架 R：取 data；没有 data 字段时原样返回。业务失败（有 code 且非成功码）抛异常。
     */
    static JsonNode unwrap(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }
        if (!response.isObject() || !response.has("data")) {
            return response;
        }
        JsonNode code = response.get("code");
        JsonNode success = response.get("success");
        boolean ok = success != null && success.isBoolean() ? success.asBoolean()
                : code == null || code.isNull() || isSuccessCode(code.asText());
        if (!ok) {
            JsonNode msg = response.has("message") ? response.get("message") : response.get("msg");
            throw new IllegalStateException("dtp-meos 返回失败：" + (code == null ? "" : code.asText()) + " "
                    + (msg == null ? "" : msg.asText()));
        }
        return response.get("data");
    }

    private static boolean isSuccessCode(String code) {
        return "00000".equals(code) || "0".equals(code) || "200".equals(code) || "success".equalsIgnoreCase(code);
    }
}
