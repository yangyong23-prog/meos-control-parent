package com.persagy.apccontrol.basic.sse.strategy.kitchen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.kitchen.KitchenRangeHoodService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component(value = "KITCHEN_RANGE_HOOD_EQUIP_STATISTICS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KitChenRangeHoodEquipStatistics extends AbstractSseEventStrategyTemplate implements BizComponent<KitChenRangeHoodEquipStatistics> {

    private final KitchenRangeHoodService kitchenRangeHoodService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.KITCHEN_RANGE_HOOD_EQUIP_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":KITCHEN_RANGE_HOOD_EQUIP_STATISTICS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        Map<String, Set<String>> paramMap = analyzeEventParam(param, new TypeReference<Map<String, Set<String>>>() {
        });
        if (Objects.isNull(paramMap)) {
            paramMap = kitchenRangeHoodService.handlerStaticAggregateToMap(param.getGroupCode(), param.getProjectId());
        }
        Map<String, KitChenRangeHoodEquipStatisticsVo> equipStatisticsVoMap = kitchenRangeHoodService.queryEquipStatistics(paramMap);
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(equipStatisticsVoMap)
                .build();
    }

    @Override
    public String logTag() {
        return "SSE-厨房油烟机-设备统计";
    }
}
