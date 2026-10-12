package com.persagy.apccontrol.basic.sse.strategy.security;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.vo.security.SecurityDashboardVO;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 【安防】DASH_BOARD 定时刷新
 */
@Component("SECURITY_DASH_BOARD")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SecurityDashBoard extends AbstractSseEventStrategyTemplate {

    private final SecurityService airportService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.SECURITY_DASH_BOARD;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        //查询 DASH_BOARD 接口
        SecurityDashboardQueryParam queryParam = new SecurityDashboardQueryParam();
        queryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.SECURITY_ARMING);
        SecurityDashboardVO dashboard = airportService.dashboard(queryParam);
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(dashboard)
                .build();
    }
}
