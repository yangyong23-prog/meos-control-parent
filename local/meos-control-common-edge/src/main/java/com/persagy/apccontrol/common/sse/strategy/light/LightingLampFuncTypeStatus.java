package com.persagy.apccontrol.common.sse.strategy.light;

import com.persagy.apccontrol.basic.service.lighting.LightingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.model.param.lighting.LightingLampFuncTypeWithStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingNightWithStatusVO;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 照明-灯具类型状态
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Slf4j
@Component(value = "LIGHTING_LAMP_FUNC_TYPE_STATUS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingLampFuncTypeStatus extends AbstractSseEventStrategyTemplate implements BizComponent<LightingLampFuncTypeStatus> {

    private final LightingService lightingService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHTING_LAMP_FUNC_TYPE_STATUS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":LIGHTING_LAMP_FUNC_TYPE_STATUS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        if (!param.getDemandTemplateType().isNightLighting()) {
            // 仅支持夜景照明
            return null;
        }
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getDemandTemplateType();

        LightingLampFuncTypeWithStatusQueryParam queryParam = new LightingLampFuncTypeWithStatusQueryParam();
        queryParam.setProjectId(projectId);
        queryParam.setTemplateType(templateType);
        LightingNightWithStatusVO lampFuncTypeWithStatus = lightingService.getLampFuncTypeWithStatus(queryParam);
        if (Objects.isNull(lampFuncTypeWithStatus)) {
            return null;
        }
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.LIGHTING_LAMP_FUNC_TYPE_STATUS)
                .data(lampFuncTypeWithStatus)
                .build();
    }

    @Override
    public String logTag() {
        return "SSE-照明-灯具类型标签推送";
    }
}
