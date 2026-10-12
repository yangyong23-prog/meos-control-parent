package com.persagy.apccontrol.basic.sse.strategy.parking;

import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 所有停车场车位类型统计
 *
 * @author yuecaipu
 * @date 2024/9/9 15:45
 * @since v5.240901.00
 */
@Slf4j
@Component(value = "PARKING_STATISTICS")
public class ParkingStatistics extends AbstractSseEventStrategyTemplate {
    @Resource
    ParkingService parkingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.PARKING_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        try {
            ParkingSpaceStatisticsQueryParam parkingSpaceStatisticsQueryParam = new ParkingSpaceStatisticsQueryParam();
            parkingSpaceStatisticsQueryParam.setInstanceId(param.getInstanceId());
            parkingSpaceStatisticsQueryParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
            Map<String, Integer> result = parkingService.getParkingSpaceStatistics(parkingSpaceStatisticsQueryParam);
            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("{} 执行异常 ", getEventType(), e);
        }

        return null;
    }

}
