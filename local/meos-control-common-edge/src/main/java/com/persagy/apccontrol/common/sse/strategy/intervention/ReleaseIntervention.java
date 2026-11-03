package com.persagy.apccontrol.common.sse.strategy.intervention;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.model.dto.ReleaseInterventionDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 空间状态
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "RELEASE_INTERVENTION")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ReleaseIntervention extends AbstractSseEventStrategyTemplate {
    
    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.RELEASE_INTERVENTION;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ": RELEASE_INTERVENTION";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        return null;
    }

    /**
     * opt B9：与 getSseEmitterObject 的参数校验和对象范围过滤一致。
     */
    @Override
    protected boolean skipBeforeCompute(SseSubscribeParam param, Object condition) {
        if (!(condition instanceof ReleaseInterventionDTO)) {
            return true;
        }
        ReleaseInterventionDTO dto = (ReleaseInterventionDTO) condition;
        return !StrUtil.isAllNotBlank(dto.getObjectId(), dto.getInfoCode())
                || checkClaasCode2objectIds(param, dto.getClassCode(), dto.getObjectId());
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        ReleaseInterventionDTO interventionDTO = condition instanceof ReleaseInterventionDTO ? (ReleaseInterventionDTO) condition : null;
        if (interventionDTO == null || !StrUtil.isAllNotBlank(interventionDTO.getObjectId(),
                interventionDTO.getInfoCode())) {
            return null;
        }

        //过滤设备 ID
        if (checkClaasCode2objectIds(param, interventionDTO.getClassCode(), interventionDTO.getObjectId())) return null;
        
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.RELEASE_INTERVENTION)
                .data(interventionDTO)
                .build();
    }

}
