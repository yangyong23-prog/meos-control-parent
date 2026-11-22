package com.persagy.apccontrol.opt;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * B1：修 16.9。
 *
 * <p>ComputeIotDataHandler.checkComputeInfoCodeDataCondition 里“同一控制指令的完成帧先到、丢弃后到的进行中帧”的判断，
 * 抛出的异常被同一个 try 吞掉，结果仍返回 false 并继续推送和写缓存，让“进行中”覆盖了“完成”。
 * 这里在进入计算前用相同条件判断，命中则由调用方丢弃该帧（不计算、不写缓存、不推送）。
 *
 * <p>开关 {@code switch.opt.ctrlFrameOrderFix}，关闭时 {@link #shouldDiscard} 恒为 false。
 */
@Slf4j
@Component
public class OptCtrlFrameOrderGuard {

    private final IotDataCacheService iotDataCacheService;

    public OptCtrlFrameOrderGuard(@Lazy IotDataCacheService iotDataCacheService) {
        this.iotDataCacheService = iotDataCacheService;
    }

    public boolean shouldDiscard(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        if (!OptSwitch.on(OptSwitch.CTRL_FRAME_ORDER_FIX)) {
            return false;
        }
        if (!isCandidate(source, infoCodeCfg)) {
            return false;
        }
        FeedbackInfoMsgMqttVO before = iotDataCacheService.queryIotCtrlDataVO(source.getObjectId(), source.getInfoCode(), false);
        if (isCompletedSameCommand(before, source)) {
            OptMetrics.inc("mqtt.ctrlFrameDiscard");
            log.warn("[opt] 同一控制指令已完成，丢弃后到的帧。原消息：{}，丢弃消息：{}", before, source);
            return true;
        }
        return false;
    }

    /**
     * 与原判断范围一致：控制值、配置了二次计算公式。
     */
    static boolean isCandidate(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        return infoCodeCfg != null
                && EnumIotDataType.CTRL.getCode().equals(source.getType())
                && infoCodeCfg.getExtendInfo() != null
                && infoCodeCfg.getExtendInfo().getTriggerFormulaInfo() != null;
    }

    /**
     * 与原判断一致：缓存里的控制值与本帧是同一个 id，且缓存里的状态已是完成（2）。
     */
    static boolean isCompletedSameCommand(FeedbackInfoMsgMqttVO before, FeedbackInfoMsgMqttVO source) {
        return before != null
                && before.getId() != null
                && Objects.equals(before.getId(), source.getId())
                && before.getDataStatus() != null
                && before.getDataStatus() == 2;
    }
}
