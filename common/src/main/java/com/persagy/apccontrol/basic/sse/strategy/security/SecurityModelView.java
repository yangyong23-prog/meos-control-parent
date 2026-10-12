package com.persagy.apccontrol.basic.sse.strategy.security;

import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.security.SecurityModelViewVO;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 【安防】设备统计
 *
 * @author yuecaipu
 * @date 2024/8/9 11:22
 * @since v5.240801.00
 */
@Component("SECURITY_MODEL_VIEW")
public class SecurityModelView extends AbstractSseEventStrategyTemplate {

    @Resource
    SecurityService airportService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.SECURITY_MODEL_VIEW;
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

        SecurityModelViewVO securityModelView = airportService.querySecurityModelView(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.SECURITY_MODEL_VIEW)
                .data(securityModelView)
                .build();
    }

}
