package com.persagy.apccontrol.basic.sse.strategy.airterminal;

import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;
import com.persagy.apccontrol.basic.service.airterminal.AirStatisticsService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设备状态统计
 */
@Component(value = "BDTP_EQUIPMENT_STATUS_STATISTICS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BdtpEquipmentStatusStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    AirStatisticsService airStatisticsService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.BDTP_EQUIPMENT_STATUS_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":BDTP_EQUIPMENT_STATUS_STATISTICS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        AirHomePageEquipmentParam p = new AirHomePageEquipmentParam(param.getInstanceId(), param.getInstanceType());

        List<AirHomePageEquipmentVO> list = airStatisticsService.homePageEquipment(p);
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.BDTP_EQUIPMENT_STATUS_STATISTICS)
                .data(list)
                .build();
    }

}
