package com.persagy.apccontrol.common.sse.strategy.light;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarLightLoopTotalVO;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 照明需求满足情况
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "LIGHT_LOOP")
public class LightLoopTotal extends AbstractSseEventStrategyTemplate {

    @Resource
    LightingService lightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHT_LOOP;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":LIGHT_LOOP";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        LightingInstanceQueryParam lightingInstanceQueryParam = BeanUtil.copyProperties(param, LightingInstanceQueryParam.class);
        List<LightingSidebarLightLoopTotalVO> loopTotal = lightingService.listLightLoopTotal(lightingInstanceQueryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.LIGHT_LOOP)
                .data(loopTotal)
                .build();

    }
}
