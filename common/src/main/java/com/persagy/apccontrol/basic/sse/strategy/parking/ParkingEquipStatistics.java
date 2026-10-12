package com.persagy.apccontrol.basic.sse.strategy.parking;

import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingEquipStatisticsVO;
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

import static com.persagy.apccontrol.basic.sse.SseEmitterEventEnum.PARKING_EQUIP_STATISTICS;

/**
 * 停车位设备统计
 */

@Slf4j
@Component(value = "PARKING_EQUIP_STATISTICS")
public class ParkingEquipStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    ParkingService parkingService;
    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        try {
            ParkingSpaceStatisticsQueryParam parkingSpaceStatisticsQueryParam = new ParkingSpaceStatisticsQueryParam();
            parkingSpaceStatisticsQueryParam.setInstanceId(param.getInstanceId());
            parkingSpaceStatisticsQueryParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
            List<ParkingEquipStatisticsVO> result = parkingService.getParkingEquipStatistics(parkingSpaceStatisticsQueryParam);
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
        return PARKING_EQUIP_STATISTICS;
    }
    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }
}
