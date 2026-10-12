package com.persagy.apccontrol.basic.sse.strategy.ventilation;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.nacos.api.cmdb.pojo.EntityEventType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.service.ventilation.VentilationIotService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 通风设备统计
 */
@Slf4j
@Component(value = "VENTILATION_EQUIP_STATISTICS")
public class VentilationModelView extends AbstractSseEventStrategyTemplate {

    @Resource
    private VentilationIotService ventilationEdgeService;

    /**
     * 获取异常事件类型
     *
     * @return {@link EntityEventType}
     * @author yuecaipu
     * @date 2024/7/1 16:49
     * @since 24Q1 T3S2-5.18.0
     */
    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.VENTILATION_EQUIP_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":VENTILATION_EQUIP_STATISTICS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        Map<String, Set<String>> eventParam = analyzeEventParam(param, new TypeReference<Map<String, Set<String>>>() {
        });
        if (Objects.isNull(eventParam)) {
            throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_d543521f653240c89a4c07825576070d"), getEventType().getValue()));
        }
        Map<String, KitChenRangeHoodEquipStatisticsVo> equipStatisticsMap =
                ventilationEdgeService.queryEquipStatistics(eventParam);
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                               .eventType(SseEmitterEventEnum.VENTILATION_EQUIP_STATISTICS)
                               .data(equipStatisticsMap)
                .build();
    }

}
