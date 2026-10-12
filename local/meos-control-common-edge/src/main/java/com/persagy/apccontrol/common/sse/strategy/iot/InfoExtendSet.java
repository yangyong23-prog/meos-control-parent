package com.persagy.apccontrol.common.sse.strategy.iot;

import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 信息点扩展配置
 *
 * @author yuecaipu
 * @date 2024/8/26 15:38
 * @since v5.240802.00
 */
@Slf4j
@Component(value = "INFO_EXTEND_SET")
public class InfoExtendSet extends AbstractSseEventStrategyTemplate {

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.INFO_EXTEND_SET;
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        if (condition instanceof FeedbackInfoMsgMqttVO) {
            FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = (FeedbackInfoMsgMqttVO) condition;
            if (!param.getProjectId().equals(feedbackInfoMsgMqttVO.getProjectId())) {
                return null;
            }
            
            //过滤设备 ID
            if (checkClaasCode2objectIds(param, feedbackInfoMsgMqttVO.getClassCode(),
                    feedbackInfoMsgMqttVO.getObjectId())) {
                return null;
            }

            return SseEmitterObject.builder()
                    .projectId(param.getProjectId())
                    .eventType(SseEmitterEventEnum.INFO_EXTEND_SET)
                    .data(feedbackInfoMsgMqttVO)
                    .build();
        }
        return null;
    }

}
