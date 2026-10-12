package com.persagy.apccontrol.basic.sse.strategy.light;

import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarIllumInfoVO;
import com.persagy.apccontrol.basic.service.lighting.LightingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 照明监测
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "ILLUM_INFO")
public class IllumInfo extends AbstractSseEventStrategyTemplate {

    @Resource
    LightingService lightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.ILLUM_INFO;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":ILLUM_INFO";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        ProjectVO projectVO = new ProjectVO();
        projectVO.setProjectId(param.getProjectId());
        LightingSidebarIllumInfoVO illumInfo = lightingService.getIllumInfo(projectVO);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.ILLUM_INFO)
                .data(illumInfo)
                .build();

    }
}
