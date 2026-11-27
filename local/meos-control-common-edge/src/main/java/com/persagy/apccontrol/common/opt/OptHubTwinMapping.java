package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 孪生数据源切换（switch.opt.twinSource）：把 meos-hub 的两种孪生形状转成 meos-control 现有的形状。
 *
 * <ul>
 *     <li>{@code POST /edge2cloud/point/getPointTwin} 的旧 CloudObjInfoCodeStatus 行 → {@link DtpTwinIotData}
 *     （与 dtp-twin {@code /data/queryTwinDataOnMoment} 同形，调用方过滤逻辑不变）。</li>
 *     <li>盒内业务 MQTT 出口（hub.exit.mqtt-topic）的原生载荷 {@code {projectId, objectId, classCode, reportTime,
 *     samples:[{infoCode, val, time, validity, ...}]}} → 若干条 {@link FeedbackInfoMsgMqttVO}
 *     （与 dtp-twin 反馈值主题同形，进现有实时缓存与消费链）。</li>
 * </ul>
 *
 * <p>时间：meos-hub 以 UTC 渲染（ISO 字符串或毫秒），这里转成 {@code zone} 下的 {@code yyyyMMddHHmmss}，
 * 与 dtp-twin 的 dataTime 口径一致。meos-hub 没有“时间常数”，dataIndate 用配置值（默认 0 = 不过期），
 * 也没有强制状态，forced 留空。纯函数、无状态。
 */
public final class OptHubTwinMapping {

    static final DateTimeFormatter DATA_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** meos-hub 数据标记中表示正常的取值（TagType.NORMAL） */
    static final String TAG_NORMAL = "normal";
    /** meos-hub 在离线中表示离线的取值 */
    static final String OFFLINE = "offline";
    /** meos-hub 孪生有效性中表示有效的取值（HubRuntime.Validity.VALID） */
    static final String VALID = "VALID";
    /** dtp-twin 的离线占位值 */
    static final String TWIN_OFFLINE_VALUE = "~";

    private OptHubTwinMapping() {
    }

    /**
     * getPointTwin 一行 → DtpTwinIotData。
     * <ul>
     *     <li>dataFlag：值为空、为 "~" 或数据标记不是 normal（超限、格式错误等）时为 2（无效），否则 1。</li>
     *     <li>dataStatus：onlineStatus=offline 时为 2（异常），否则 1。</li>
     * </ul>
     *
     * @return objectId 或 infoCode 缺失时返回 null
     */
    public static DtpTwinIotData fromPointTwin(JsonNode row, ZoneId zone, int dataIndate) {
        if (row == null || !row.isObject()) {
            return null;
        }
        String objectId = text(row, "objectId");
        String infoCode = text(row, "infoCode");
        if (isBlank(objectId) || isBlank(infoCode)) {
            return null;
        }
        String val = text(row, "val");
        String tag = text(row, "transfiniteType");
        boolean validValue = !isBlank(val) && !TWIN_OFFLINE_VALUE.equals(val)
                && (isBlank(tag) || TAG_NORMAL.equalsIgnoreCase(tag));

        DtpTwinIotData d = new DtpTwinIotData();
        d.setObjectId(objectId);
        d.setClassCode(text(row, "classCode"));
        d.setInfoCode(infoCode);
        d.setDataValue(val);
        d.setDataTime(isoToDataTime(text(row, "time"), zone));
        d.setDataIndate(String.valueOf(dataIndate));
        d.setDataFlag(validValue ? 1 : 2);
        d.setDataStatus(OFFLINE.equalsIgnoreCase(text(row, "onlineStatus")) ? 2 : 1);
        return d;
    }

    /**
     * 盒内业务 MQTT 原生载荷 → 反馈值消息列表（每个 sample 一条）。
     * validity 不是 VALID 时 invalid=1、dataStatus=2，实时缓存按异常写入，读方不会当作有效值。
     *
     * @return 无法识别的载荷返回空列表
     */
    public static List<FeedbackInfoMsgMqttVO> fromExport(JsonNode root, ZoneId zone, int dataIndate) {
        if (root == null || !root.isObject()) {
            return Collections.emptyList();
        }
        String objectId = text(root, "objectId");
        JsonNode samples = root.get("samples");
        if (isBlank(objectId) || samples == null || !samples.isArray()) {
            return Collections.emptyList();
        }
        String classCode = text(root, "classCode");
        List<FeedbackInfoMsgMqttVO> out = new ArrayList<>(samples.size());
        for (JsonNode s : samples) {
            String infoCode = text(s, "infoCode");
            if (isBlank(infoCode)) {
                continue;
            }
            boolean valid = VALID.equalsIgnoreCase(text(s, "validity"));
            FeedbackInfoMsgMqttVO vo = new FeedbackInfoMsgMqttVO();
            vo.setObjectId(objectId);
            vo.setClassCode(classCode);
            vo.setInfoCode(infoCode);
            vo.setDataValue(text(s, "val"));
            vo.setDataTime(millisToDataTime(s.get("time"), zone));
            vo.setDataIndate(dataIndate);
            vo.setDataStatus(valid ? 1 : 2);
            vo.setInvalid(valid ? 0 : 1);
            out.add(vo);
        }
        return out;
    }

    /** "2026-11-27T04:59:48(.SSS)(Z)"（UTC）→ zone 下的 yyyyMMddHHmmss；解析不了返回 null */
    static String isoToDataTime(String iso, ZoneId zone) {
        if (isBlank(iso)) {
            return null;
        }
        try {
            String s = iso.endsWith("Z") ? iso.substring(0, iso.length() - 1) : iso;
            LocalDateTime utc = LocalDateTime.parse(s);
            return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(zone).format(DATA_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** 毫秒（数字或数字字符串）→ zone 下的 yyyyMMddHHmmss；不是正数返回 null */
    static String millisToDataTime(JsonNode node, ZoneId zone) {
        if (node == null || node.isNull()) {
            return null;
        }
        long millis;
        if (node.isNumber()) {
            millis = node.asLong();
        } else {
            try {
                millis = Long.parseLong(node.asText().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (millis <= 0) {
            return null;
        }
        return Instant.ofEpochMilli(millis).atZone(zone).format(DATA_TIME);
    }

    static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
