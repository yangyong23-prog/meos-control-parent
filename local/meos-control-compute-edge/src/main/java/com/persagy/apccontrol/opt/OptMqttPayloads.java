package com.persagy.apccontrol.opt;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * B1：Twin MQTT 报文解析与无效值日志。
 *
 * <p>原逻辑同一 payload 经 fastjson 解析两次、序列化两次后再交给 Jackson；这里直接用同一个
 * {@link CommonConst#JSON_MAPPER} 解析一次。Jackson 解析失败（非标准 JSON）时退回原来的 fastjson 规整路径，
 * 保证能解析的报文范围不缩小。
 */
@Slf4j
public final class OptMqttPayloads {

    private static final long INVALID_LOG_INTERVAL_MS = 60_000L;
    private static final AtomicLong LAST_INVALID_LOG = new AtomicLong();
    private static final AtomicLong INVALID_SINCE_LAST_LOG = new AtomicLong();

    private OptMqttPayloads() {
    }

    /**
     * @return 解析结果；payload 为 JSON null 或无法解析时返回 null
     */
    public static FeedbackInfoMsgMqttVO parseTwin(String payload) {
        return parseTwin(payload, CommonConst.JSON_MAPPER);
    }

    static FeedbackInfoMsgMqttVO parseTwin(String payload, ObjectMapper mapper) {
        try {
            return mapper.readValue(payload, FeedbackInfoMsgMqttVO.class);
        } catch (Exception first) {
            try {
                JSONObject obj = JSONObject.parseObject(payload);
                if (obj == null) {
                    return null;
                }
                OptMetrics.inc("mqtt.parseFallback");
                return mapper.readValue(obj.toJSONString(), FeedbackInfoMsgMqttVO.class);
            } catch (Exception e) {
                log.error("MQTT推送数据，数据格式错误:{}", payload, e);
                return null;
            }
        }
    }

    /**
     * "~" 或空值：debug 级别逐条，warn 级别每分钟最多一条（带期间条数）。
     */
    public static void logInvalidValue(String payload) {
        OptMetrics.inc("mqtt.invalidValue");
        long count = INVALID_SINCE_LAST_LOG.incrementAndGet();
        if (log.isDebugEnabled()) {
            log.debug("MQTT推送数据，无效消息值-:{}", payload);
        }
        long now = System.currentTimeMillis();
        long last = LAST_INVALID_LOG.get();
        if (now - last >= INVALID_LOG_INTERVAL_MS && LAST_INVALID_LOG.compareAndSet(last, now)) {
            INVALID_SINCE_LAST_LOG.addAndGet(-count);
            log.warn("MQTT推送数据，近一分钟无效消息值 {} 条，最近一条:{}", count, payload);
        }
    }
}
