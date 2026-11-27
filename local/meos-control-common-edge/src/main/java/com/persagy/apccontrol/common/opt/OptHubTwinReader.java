package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 孪生数据源切换：当前值查询从 dtp-twin 改读 meos-hub（开关 {@code switch.opt.twinSource}）。
 *
 * <ul>
 *     <li>{@code twin}（默认）：不介入，行为与改造前一致。</li>
 *     <li>{@code verify}：仍返回 dtp-twin 结果，同时查 meos-hub 逐点比对，计数 {@code hub.verify.*}，
 *     不一致样例每分钟最多打一条日志。</li>
 *     <li>{@code hub}：以 meos-hub 为准返回。</li>
 * </ul>
 * 只处理“当前值”查询：指定了 twinTime / unit 或对象为空的请求原样走 dtp-twin。
 * 返回形状与 dtp-twin 一致（dataFlag/dataStatus=1 表示有效），调用方过滤逻辑不变。
 * 与 B2 共享缓存（switch.opt.realtimeCacheRead）可叠加：缓存未命中时的回源换成本类。
 */
@Slf4j
public class OptHubTwinReader {

    public static final String MODE_TWIN = "twin";
    public static final String MODE_VERIFY = "verify";
    public static final String MODE_HUB = "hub";

    /** 单次请求对象数上限，与 B2 回源分片一致；meos-hub 单次点数另有 1 万上限 */
    static final int OBJECT_BATCH = 200;
    private static final long SAMPLE_LOG_INTERVAL_MS = 60_000L;

    private final Function<Map<String, Object>, JsonNode> hub;
    private final String defaultProjectId;
    private final ZoneId zone;
    private final int dataIndate;
    private final AtomicLong lastSampleLog = new AtomicLong();

    /**
     * @param hub              meos-hub getPointTwin 调用（请求体 → 原样响应）
     * @param defaultProjectId 请求对象未带 projectId 时使用
     * @param zone             dataTime 渲染时区
     * @param dataIndate       返回记录的时间常数（分钟），0 为不过期
     */
    public OptHubTwinReader(Function<Map<String, Object>, JsonNode> hub, String defaultProjectId, ZoneId zone, int dataIndate) {
        this.hub = hub;
        this.defaultProjectId = defaultProjectId;
        this.zone = zone;
        this.dataIndate = dataIndate;
    }

    public static String mode() {
        String m = OptSwitch.value(OptSwitch.TWIN_SOURCE, MODE_TWIN);
        return MODE_VERIFY.equals(m) || MODE_HUB.equals(m) ? m : MODE_TWIN;
    }

    /** 当前开关下该请求是否由 meos-hub 参与（verify 或 hub） */
    public boolean handles(QueryTwinDataDTO dto) {
        return !MODE_TWIN.equals(mode()) && isMomentQuery(dto);
    }

    static boolean isMomentQuery(QueryTwinDataDTO dto) {
        return dto != null && dto.getTwinTime() == null && dto.getUnit() == null
                && dto.getObjects() != null && !dto.getObjects().isEmpty();
    }

    /**
     * 按开关取当前值。
     *
     * @param dto  当前值请求
     * @param twin 原 dtp-twin 查询
     */
    public List<DtpTwinIotData> fetch(QueryTwinDataDTO dto, Function<QueryTwinDataDTO, List<DtpTwinIotData>> twin) {
        String mode = mode();
        if (MODE_TWIN.equals(mode) || !isMomentQuery(dto)) {
            return twin.apply(dto);
        }
        if (MODE_HUB.equals(mode)) {
            OptMetrics.inc("hub.read.call");
            return query(dto);
        }
        List<DtpTwinIotData> twinResult = twin.apply(dto);
        verify(dto, twinResult);
        return twinResult;
    }

    /**
     * 查 meos-hub，按 ≤200 对象分片；任一分片失败整体抛出（与 dtp-twin 路径一致，不返回残缺结果）。
     */
    public List<DtpTwinIotData> query(QueryTwinDataDTO dto) {
        Map<String, List<Map<String, Object>>> byProject = new LinkedHashMap<>();
        for (QueryTwinDataOnMomentDTO o : dto.getObjects()) {
            if (o == null || o.getObjectId() == null) {
                continue;
            }
            if (o.getInfoCodes() == null || o.getInfoCodes().isEmpty()) {
                // meos-hub 空集合只回直采绑点，与 dtp-twin “全部信息点”口径不同，计数便于排查
                OptMetrics.inc("hub.read.emptyInfoCodes");
            }
            Map<String, Object> point = new HashMap<>(4);
            point.put("objectId", o.getObjectId());
            point.put("infoCodeSet", o.getInfoCodes() == null ? new LinkedHashSet<String>() : o.getInfoCodes());
            String projectId = o.getProjectId() == null || o.getProjectId().isEmpty() ? defaultProjectId : o.getProjectId();
            byProject.computeIfAbsent(projectId, k -> new ArrayList<>()).add(point);
        }
        List<DtpTwinIotData> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : byProject.entrySet()) {
            List<Map<String, Object>> points = e.getValue();
            for (int from = 0; from < points.size(); from += OBJECT_BATCH) {
                Map<String, Object> body = new HashMap<>(4);
                body.put("projectId", e.getKey());
                body.put("pointSet", new ArrayList<>(points.subList(from, Math.min(points.size(), from + OBJECT_BATCH))));
                result.addAll(parse(callHub(body)));
            }
        }
        return result;
    }

    private JsonNode callHub(Map<String, Object> body) {
        try {
            return hub.apply(body);
        } catch (RuntimeException e) {
            OptMetrics.incAlways("hub.read.fail");
            throw e;
        }
    }

    /** 响应取 data（兼容 content / result）数组；找不到数组视为失败 */
    List<DtpTwinIotData> parse(JsonNode resp) {
        JsonNode rows = rows(resp);
        if (rows == null) {
            OptMetrics.incAlways("hub.read.badResponse");
            throw new IllegalStateException("meos-hub getPointTwin 响应无数据数组: " + abbreviate(resp));
        }
        List<DtpTwinIotData> out = new ArrayList<>(rows.size());
        for (JsonNode row : rows) {
            DtpTwinIotData d = OptHubTwinMapping.fromPointTwin(row, zone, dataIndate);
            if (d != null) {
                out.add(d);
            }
        }
        return out;
    }

    static JsonNode rows(JsonNode resp) {
        if (resp == null) {
            return null;
        }
        if (resp.isArray()) {
            return resp;
        }
        for (String f : new String[]{"data", "content", "result"}) {
            JsonNode n = resp.get(f);
            if (n != null && n.isArray()) {
                return n;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ verify

    enum Outcome { match, bothEmpty, hubMiss, twinMiss, mismatch }

    void verify(QueryTwinDataDTO dto, List<DtpTwinIotData> twinResult) {
        try {
            Map<String, String> hubValues = validValues(query(dto));
            Map<String, String> twinValues = validValues(twinResult);
            for (String key : requestedKeys(dto)) {
                Outcome o = compare(twinValues.get(key), hubValues.get(key));
                OptMetrics.incAlways("hub.verify." + o.name());
                if (o == Outcome.mismatch || o == Outcome.hubMiss) {
                    sampleLog(key, o, twinValues.get(key), hubValues.get(key));
                }
            }
        } catch (Exception e) {
            OptMetrics.incAlways("hub.verify.error");
            log.warn("[opt] meos-hub 孪生双读校验异常: {}", e.toString());
        }
    }

    static Outcome compare(String twinValue, String hubValue) {
        if (twinValue == null && hubValue == null) {
            return Outcome.bothEmpty;
        }
        if (hubValue == null) {
            return Outcome.hubMiss;
        }
        if (twinValue == null) {
            return Outcome.twinMiss;
        }
        return OptRealtimeTwinReader.sameValue(twinValue, hubValue) ? Outcome.match : Outcome.mismatch;
    }

    private static Set<String> requestedKeys(QueryTwinDataDTO dto) {
        Set<String> keys = new LinkedHashSet<>();
        for (QueryTwinDataOnMomentDTO o : dto.getObjects()) {
            if (o == null || o.getObjectId() == null || o.getInfoCodes() == null) {
                continue;
            }
            for (String info : o.getInfoCodes()) {
                keys.add(key(o.getObjectId(), info));
            }
        }
        return keys;
    }

    private static Map<String, String> validValues(List<DtpTwinIotData> list) {
        Map<String, String> m = new HashMap<>();
        if (list == null) {
            return m;
        }
        for (DtpTwinIotData d : list) {
            if (d != null && Objects.equals(d.getDataFlag(), 1) && Objects.equals(d.getDataStatus(), 1)) {
                m.put(key(d.getObjectId(), d.getInfoCode()), d.getDataValue());
            }
        }
        return m;
    }

    private static String key(String objectId, String infoCode) {
        return objectId + ":" + infoCode;
    }

    private void sampleLog(String key, Outcome o, String twinValue, String hubValue) {
        long now = System.currentTimeMillis();
        long last = lastSampleLog.get();
        if (now - last >= SAMPLE_LOG_INTERVAL_MS && lastSampleLog.compareAndSet(last, now)) {
            log.info("[opt] meos-hub 孪生双读不一致样例 {} {} twin={} hub={}", o, key, twinValue, hubValue);
        }
    }

    private static String abbreviate(JsonNode resp) {
        String s = String.valueOf(resp);
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
