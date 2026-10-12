package com.persagy.apccontrol.basic.sse.strategy.common;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventCommonParam;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.abnormal.AbnormalEventService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 按系统划分异常事件
 *
 * @author yuecaipu
 * @date 2024年9月9日15:24:46
 * @since v5.240901.00
 */
@Slf4j
@Component("ABNORMAL_DATA_LIST_DIVIDE_SYSTEM")
public class AbnormalDataListDivideSystem extends AbstractSseEventStrategyTemplate {

    @Resource
    AbnormalEventService abnormalEventService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.ABNORMAL_DATA_LIST_DIVIDE_SYSTEM;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        AbnormalEventCommonParam abnormalEventCommonParam = new AbnormalEventCommonParam();
        abnormalEventCommonParam.setInstanceId(param.getInstanceId());
        abnormalEventCommonParam.setInstanceType(DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        abnormalEventCommonParam.setRunningMonitorType(RunningMonitorTypeEnum.analyze(param.getSubscribeType().getValue()));
        try {
            List<ExceptionEventBaseInfoVO> result = abnormalEventService.getAbnormalDataListDivideSystem(abnormalEventCommonParam);

            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("ABNORMAL_DATA_LIST_DIVIDE_SYSTEM执行异常 ", e);
        }

        return null;
    }
}
