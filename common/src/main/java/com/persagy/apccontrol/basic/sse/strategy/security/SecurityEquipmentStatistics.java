package com.persagy.apccontrol.basic.sse.strategy.security;

import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.security.SecurityEquipmentTypeStatisticsVo;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 【安防】设备统计
 *
 * @author yuecaipu
 * @date 2024/8/9 11:22
 * @since v5.240801.00
 */
@Component("SECURITY_EQUIPMENT_STATISTICS")
public class SecurityEquipmentStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    SecurityService airportService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.SECURITY_EQUIPMENT_STATISTICS;
    }


    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        SecurityModelViewParam queryParam = new SecurityModelViewParam();
        queryParam.setInstanceId(param.getInstanceId());
        queryParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));

        List<SecurityEquipmentTypeStatisticsVo> equipmentTypeStatistics = airportService.getEquipmentTypeStatistics(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.SECURITY_EQUIPMENT_STATISTICS)
                .data(equipmentTypeStatistics)
                .build();
    }

}
