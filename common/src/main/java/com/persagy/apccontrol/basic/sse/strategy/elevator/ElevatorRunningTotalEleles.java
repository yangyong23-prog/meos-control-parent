package com.persagy.apccontrol.basic.sse.strategy.elevator;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.elevator.ElevatorStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.elevator.ElevatorSummaryVO;
import com.persagy.apccontrol.basic.service.elevator.ElevatorService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 查询-全楼、单楼层扶梯数据统计
 *
 * @author yuecaipu
 * @date 2024/8/24 10:22
 * @since v5.240402.00
 */
@Slf4j
@Component("ELEVATOR_RUNNING_TOTAL_ELELES")
public class ElevatorRunningTotalEleles extends AbstractSseEventStrategyTemplate {

    @Resource
    private ElevatorService elevatorService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.ELEVATOR_RUNNING_TOTAL_ELELES;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        //Map<SseEmitterEventEnum, Object> eventParamMap = param.getEventParamMap();
        //if (null == eventParamMap || !eventParamMap.containsKey(getEventType())) {
        //    return null;
        //}

        try {
            //ElevatorStatisticsParam queryParam = (ElevatorStatisticsParam) eventParamMap.get(getEventType());
            ElevatorStatisticsParam queryParam = new ElevatorStatisticsParam();
            queryParam.setInstanceId(param.getInstanceId());
            queryParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
            queryParam.setProjectId(param.getProjectId());
            queryParam.setClassCode(DtpRwdClassCodeEnum.ELELES.getValue());
            ElevatorSummaryVO elevatorSummaryVO = elevatorService.statisticsRunningByClassCode(queryParam);

            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(elevatorSummaryVO)
                    .build();
        } catch (Exception e) {
            log.error("ELEVATOR_RUNNING_TOTAL_ELELES执行异常 ", e);
        }

        return null;
    }
}
