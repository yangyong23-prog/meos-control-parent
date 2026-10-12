package com.persagy.apccontrol.basic.sse.strategy.light;

import com.persagy.apccontrol.basic.model.vo.dashboard.LightingEquipmentCountDataVo;
import com.persagy.apccontrol.basic.service.dashboard.DashboardLightingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 照明DASHBOARD设备统计
 */
@Component("LIGHTING_DASHBOARD_EQUIPMENT_STATISTICS")
public class LightingDashboardEquipmentStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    private DashboardLightingService dashboardLightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHTING_DASHBOARD_EQUIPMENT_STATISTICS;
    }


    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        LightingEquipmentCountDataVo data = dashboardLightingService.dashboardEquipmentStatistics();

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(data)
                .build();
    }

}
