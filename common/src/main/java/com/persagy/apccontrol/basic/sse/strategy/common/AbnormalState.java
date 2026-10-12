package com.persagy.apccontrol.basic.sse.strategy.common;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.AbnormalStateParam;
import com.persagy.apccontrol.basic.service.DashBoardService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 空间设备异常状态
 *
 * @author LiuYang
 * @date 2024年12月10日12:53:28
 * @since v5.241201.00
 */
@Slf4j
@Component("ABNORMAL_STATE")
public class AbnormalState extends AbstractSseEventStrategyTemplate {

    @Resource
    DashBoardService dashBoardService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.ABNORMAL_STATE;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().getValue();
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        try {
            AbnormalStateParam abnormalEventCommonParam = new AbnormalStateParam();
            Map<String, Set<String>> claasCode2objectIds = param.getClaasCode2objectIds();
            abnormalEventCommonParam.setRunningMonitorType(RunningMonitorTypeEnum.of(param.getSubscribeType()));
            Map<String, List<AbnormalStatusTypeEnum>> abnormalStateMap = new HashMap<>();
            if (MapUtils.isNotEmpty(claasCode2objectIds)) {
                String space = DtpRwdClassCodeEnum.SPACE.value();
                abnormalEventCommonParam.setSpaceIds(new LinkedHashSet<>(
                        claasCode2objectIds.getOrDefault(
                                space, Collections.emptySet())));
                Set<String> equipmentIds = claasCode2objectIds.entrySet().stream()
                        .filter(entry -> !space.equals(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .filter(CollectionUtils::isNotEmpty)
                        .flatMap(Collection::stream)
                        .collect(java.util.stream.Collectors.toCollection(
                                LinkedHashSet::new));
                abnormalEventCommonParam.setEquipmentIds(equipmentIds);
                abnormalStateMap = dashBoardService.listAbnormalState(abnormalEventCommonParam);
            }
            return SseEmitterObject.builder()
                    .eventType(getEventType())
                    .data(abnormalStateMap)
                    .build();
        } catch (Exception e) {
            log.error("ABNORMAL_STATE执行异常 ", e);
        }
        return null;
    }

}
