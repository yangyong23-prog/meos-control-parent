package com.persagy.apccontrol.basic.sse.strategy.airterminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalEquipmentStatusStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalFloorEquipmentTypeStatisticsVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设备状态统计
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Component(value = "EQUIPMENT_STATUS_STATISTICS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EquipmentStatusStatistics extends AbstractSseEventStrategyTemplate {

    @Resource
    AirTerminalService airportService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.EQUIPMENT_STATUS_STATISTICS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":EQUIPMENT_STATUS_STATISTICS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        AirTerminalEquipmentStatusStatisticsQueryParam queryParam = new AirTerminalEquipmentStatusStatisticsQueryParam();
        queryParam.setInstanceId(param.getInstanceId());
        queryParam.setInstanceType(AirTerminalInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        queryParam.setProjectId(param.getProjectId());
        List<AirTerminalFloorEquipmentTypeStatisticsVO> list = airportService.listEquipmentStatusStatistics(queryParam);

        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.EQUIPMENT_STATUS_STATISTICS)
                .data(list)
                .build();
    }

}
