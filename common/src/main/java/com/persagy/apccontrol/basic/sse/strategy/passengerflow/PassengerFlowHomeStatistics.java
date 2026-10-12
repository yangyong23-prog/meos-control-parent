package com.persagy.apccontrol.basic.sse.strategy.passengerflow;

import com.persagy.apccontrol.basic.model.vo.passengerflow.ClientServerHomeStatistics;
import com.persagy.apccontrol.basic.service.passengerflow.PassengerFlowService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 客流-出入口人数统计
 *
 * @author LiuYang
 * @date 2024/9/13 10:22
 * @since v5.240902.0
 */
@Slf4j
@Component("PASSENGER_FLOW_HOME_STATISTICS")
public class PassengerFlowHomeStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    PassengerFlowService passengerFlowService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.PASSENGER_FLOW_HOME_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        try {
            ClientServerHomeStatistics passagewayHeadCount = passengerFlowService.homeStatistics();
            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(passagewayHeadCount)
                    .build();
        } catch (Exception e) {
            log.error("PASSENGER_FLOW_HOME_STATISTICS执行异常 ", e);
        }

        return null;
    }
}
