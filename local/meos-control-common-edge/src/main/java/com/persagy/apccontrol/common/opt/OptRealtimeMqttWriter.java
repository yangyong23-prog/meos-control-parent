package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * B2 写路径：MQTT 收到 Twin 反馈值后第一步写共享缓存（开关 {@code switch.opt.realtimeCacheWrite}）。
 *
 * <p>必须在任何换算（ComputeIotDataHandler 原地乘系数）之前调用，写入的是 Twin 原始值。
 * "~" 写离线标记。rcRunStatus 是本服务计算出的点，已由 IotDataCacheServiceImpl.insert 写同名 key（带 TTL），这里跳过。
 * 写失败只计数和告警，不影响后续消费。
 */
@Slf4j
@Component
public class OptRealtimeMqttWriter {

    private final OptRealtimeCacheCore core;

    public OptRealtimeMqttWriter(OptRealtimeCacheCore core) {
        this.core = core;
    }

    public void onTwinFeedback(FeedbackInfoMsgMqttVO raw) {
        if (raw == null || !OptSwitch.on(OptSwitch.REALTIME_CACHE_WRITE)) {
            return;
        }
        if (InfoCodeEnum.RC_RUN_STATUS.getCode().equals(raw.getInfoCode())) {
            return;
        }
        try {
            core.write(from(raw));
        } catch (Exception e) {
            OptMetrics.incAlways("rt.write.error");
            log.warn("[opt] 实时值写缓存失败 {}:{} {}", raw.getObjectId(), raw.getInfoCode(), e.toString());
        }
    }

    static OptRealtimeValue from(FeedbackInfoMsgMqttVO raw) {
        OptRealtimeValue v = new OptRealtimeValue();
        v.setObjectId(raw.getObjectId());
        v.setClassCode(raw.getClassCode());
        v.setInfoCode(raw.getInfoCode());
        v.setDataValue(raw.getDataValue() == null ? OptRealtimeValue.OFFLINE_VALUE : raw.getDataValue());
        v.setDataTime(raw.getDataTime());
        v.setDataIndate(raw.getDataIndate());
        // invalid=1 表示 Twin 推送时已判定超时无效，按异常写入，读方不会当作有效值
        v.setDataStatus(raw.getInvalid() != null && raw.getInvalid() == 1 ? Integer.valueOf(2) : raw.getDataStatus());
        v.setSource(OptRealtimeValue.SOURCE_MQTT);
        return v;
    }
}
