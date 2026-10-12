package com.persagy.apccontrol.basic.sse.strategy.airterminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceQualityStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceQualityStatisticsVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 品质统计
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "SPACE_QUALITY_STATISTICS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpaceQualityStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    AirTerminalService airportService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.SPACE_QUALITY_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":SPACE_QUALITY_STATISTICS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        AirTerminalSpaceQualityStatisticsQueryParam queryParam = new AirTerminalSpaceQualityStatisticsQueryParam();
        queryParam.setInstanceId(param.getInstanceId());
        queryParam.setInstanceType(AirTerminalInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        queryParam.setProjectId(param.getProjectId());
        AirTerminalSpaceQualityStatisticsVO qualityStatistics = airportService.getSpaceQualityStatistics(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.SPACE_QUALITY_STATISTICS)
                .data(qualityStatistics)
                .build();
    }

}
