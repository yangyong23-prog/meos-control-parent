package com.persagy.apccontrol.common.sse.strategy.light;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarDemandChangeVO;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 照明需求变化
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "LIGHT_DEMAND_CHANGE")
public class LightDemandChange extends AbstractSseEventStrategyTemplate {

    @Resource
    AbstractLightingService lightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHT_DEMAND_CHANGE;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":LIGHT_DEMAND_CHANGE";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        LightingInstanceQueryParam lightingInstanceQueryParam = BeanUtil.copyProperties(param, LightingInstanceQueryParam.class);

        LightingSidebarDemandChangeVO demandChange = lightingService.getDemandChange(lightingInstanceQueryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.LIGHT_DEMAND_CHANGE)
                .data(demandChange)
                .build();

    }
}
