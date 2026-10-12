package com.persagy.apccontrol.basic.sse.strategy.passengerflow;

import com.persagy.apccontrol.basic.model.vo.passengerflow.PassengerFlowHomeStatistics;
import com.persagy.apccontrol.basic.service.passengerflow.PassengerFlowService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客流-出入口人数统计
 *
 * @author yuecaipu
 * @date 2024/8/24 10:22
 * @since v5.240402.00
 */
@Slf4j
@Component("PASSENGER_FLOW_HEAD_TOTAL")
public class PassengerFlowHeadTotal extends AbstractSseEventStrategyTemplate {

    @Resource
    PassengerFlowService passengerFlowService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.PASSENGER_FLOW_HEAD_TOTAL;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        //Map<SseEmitterEventEnum, Object> eventParamMap = param.getEventParamMap();
        //if (null == eventParamMap || !eventParamMap.containsKey(getEventType())) {
        //    return null;
        //}

        try {
            List<PassengerFlowHomeStatistics> passagewayHeadCount = passengerFlowService.getPassagewayHeadCount();
            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(passagewayHeadCount)
                    .build();
        } catch (Exception e) {
            log.error("PASSENGER_FLOW_HEAD_TOTAL执行异常 ", e);
        }

        return null;
    }
}
