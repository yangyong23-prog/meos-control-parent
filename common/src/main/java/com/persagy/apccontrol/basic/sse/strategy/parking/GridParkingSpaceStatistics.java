package com.persagy.apccontrol.basic.sse.strategy.parking;

import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingSpaceGridStatisticsVO;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 网格车位统计
 */
@Slf4j
@Component(value = "PARKING_SPACE_GIRD_STATISTICS")
public class GridParkingSpaceStatistics extends AbstractSseEventStrategyTemplate {


    @Resource
    ParkingService parkingService;

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        try {
            ParkingSpaceStatisticsQueryParam parkingSpaceStatisticsQueryParam = new ParkingSpaceStatisticsQueryParam();
            parkingSpaceStatisticsQueryParam.setInstanceId(param.getInstanceId());
            parkingSpaceStatisticsQueryParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
            List<ParkingSpaceGridStatisticsVO> result = parkingService.getParkingSpaceGridStatistics(parkingSpaceStatisticsQueryParam, true);
            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("{} 执行异常 ", getEventType(), e);
        }

        return null;
    }

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.PARKING_SPACE_GIRD_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

}
