package com.persagy.apccontrol.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.common.opt.OptHubTwinMapping;
import com.persagy.apccontrol.common.opt.OptHubTwinReader;
import com.persagy.apccontrol.common.opt.OptHubTwinSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 孪生数据源切换：解析 meos-hub 盒内业务 MQTT 出口（hub.exit.mqtt-topic）的原生孪生载荷。
 *
 * <p>一条报文是一个设备的若干点位样本，拆成若干条与 dtp-twin 反馈值同形的 {@link FeedbackInfoMsgMqttVO}。
 * 只有 {@code switch.opt.twinSource=hub} 时这些消息才写实时缓存、走反馈值处理；其余模式只计数
 * （{@code mqtt.recv.hub}），不影响 dtp-twin 数据。
 */
@Slf4j
@Component
public class OptHubTwinMqtt {

    private static final long ERROR_LOG_INTERVAL_MS = 60_000L;

    private final OptHubTwinSettings settings;
    private final AtomicLong lastErrorLog = new AtomicLong();

    public OptHubTwinMqtt(OptHubTwinSettings settings) {
        this.settings = settings;
    }

    /** meos-hub 是否为孪生主数据源（开关为 hub） */
    public static boolean hubPrimary() {
        return OptHubTwinReader.MODE_HUB.equals(OptHubTwinReader.mode());
    }

    /**
     * @return 解析出的反馈值；无法解析时返回空列表（限频打印一条错误日志）
     */
    public List<FeedbackInfoMsgMqttVO> parse(String payload) {
        try {
            JsonNode root = CommonConst.JSON_MAPPER.readTree(payload);
            return OptHubTwinMapping.fromExport(root, settings.getZone(), settings.getDataIndate());
        } catch (Exception e) {
            OptMetrics.incAlways("mqtt.hub.parseError");
            long now = System.currentTimeMillis();
            long last = lastErrorLog.get();
            if (now - last >= ERROR_LOG_INTERVAL_MS && lastErrorLog.compareAndSet(last, now)) {
                log.warn("[opt] meos-hub 孪生报文解析失败: {} payload={}", e.toString(),
                        payload == null || payload.length() <= 300 ? payload : payload.substring(0, 300) + "...");
            }
            return Collections.emptyList();
        }
    }

    /** 该样本是否可以进反馈值处理：值非空、不是离线占位、meos-hub 判定有效 */
    public static boolean usable(FeedbackInfoMsgMqttVO vo) {
        return vo.getDataValue() != null && !"~".equals(vo.getDataValue())
                && (vo.getInvalid() == null || vo.getInvalid() != 1);
    }
}
