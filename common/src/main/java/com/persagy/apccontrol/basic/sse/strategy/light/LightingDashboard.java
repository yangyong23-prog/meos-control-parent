package com.persagy.apccontrol.basic.sse.strategy.light;

import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 照明DASHBOARD
 */
@Component("LIGHTING_DASHBOARD")
public class LightingDashboard extends AbstractSseEventStrategyTemplate {

    @Resource
    AbstractLightingService lightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHTING_DASHBOARD;
    }


    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {

        List<LightingCountDataVo> dataList = new ArrayList<>();
        LightingCountDataVo data = lightingService.dashboard(new DashBoardQueryCountParam(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING));
        LightingCountDataVo data2 = lightingService.dashboard(new DashBoardQueryCountParam(DemandTemplateTypeEnum.PARKING_LOT_LIGHTING));
        dataList.add(data);
        dataList.add(data2);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(dataList)
                .build();
    }

}
