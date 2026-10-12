package com.persagy.apccontrol.basic.sse.strategy.ai;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import java.util.Objects;
import java.util.Optional;

/**
 * 运行智能体-计算状态推送
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Component("AI_NOTIFY_CALC_STATUS")
public class AiNotifyCalcStatus extends AbstractSseEventStrategyTemplate {

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.AI_NOTIFY_CALC_STATUS;
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        JsonNode body = (JsonNode) condition;
        if (Objects.isNull(body)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_4b45488477204d8fa33f8d9a0fe69a52"));
        }
        String instanceId = param.getInstanceId();
        Optional<JsonNode> systemIdOpt = Optional.ofNullable(body.get("systemId"));
        if (!systemIdOpt.isPresent() || !StringUtils.equals(instanceId, systemIdOpt.get().asText())) {
            body = null;
        }
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(body)
                .build();
    }
}
