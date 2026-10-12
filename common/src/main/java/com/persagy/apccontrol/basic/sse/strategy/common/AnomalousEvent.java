package com.persagy.apccontrol.basic.sse.strategy.common;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventMsgDTO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.event.converter.AbnormalEventConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService.processAbnormalEventInfo;


/**
 * 异常事件
 *
 * @author LiuYang
 * @date 2024/10/25 15:38
 * @since v5.241002.0
 */
@Slf4j
@Component(value = "ABNORMAL_EVENT")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AnomalousEvent extends AbstractSseEventStrategyTemplate {

    private final AbnormalEventConverter abnormalEventConverter;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.ABNORMAL_EVENT;
    }

    @Override
    protected String getRedisLockKey(String eventKey, Object condition) {
        if (condition instanceof List) {
            return String.format("ANOMALOUS_EVENT:%s", condition);
        }
        return null;
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        if (condition instanceof List) {
            Collection<ExceptionEventMsgDTO> anomalousEventMsgs = CollectionUtils.collect((List<?>) condition, input -> (ExceptionEventMsgDTO) input);
            List<ExceptionEventBaseInfoVO> arrayConversion = anomalousEventMsgs.stream()
                    .map(abnormalEventConverter::msgDtoToVo)
                    .collect(Collectors.toList());
            RunningMonitorTypeEnum runningMonitorTypeEnum = RunningMonitorTypeEnum.of(param.getSubscribeType());

            Map<String, Set<String>> claasCode2objectIds = param.getClaasCode2objectIds();
            if (MapUtils.isEmpty(claasCode2objectIds)) {
                return null;
            }
            // 订阅的空间ID
            Set<String> subscribeSpaceIds = claasCode2objectIds.get(DtpRwdClassCodeEnum.SPACE.value());
            List<String> objectIds = claasCode2objectIds.values().stream().flatMap(Set::stream).collect(Collectors.toList());
            List<ExceptionEventBaseInfoVO> resultList = new ArrayList<>();
            for (ExceptionEventBaseInfoVO exceptionEventBaseInfoVO : arrayConversion) {
                for (ExceptionEventBaseInfoVO.ExceptionInstance objectInfo : exceptionEventBaseInfoVO.getInstances()) {
                    String objectId = objectInfo.getInstanceId();
                    Set<String> inSpaceIds = objectInfo.getInSpaceIds();

                    boolean toBeAdded = false;
                    if (runningMonitorTypeEnum == RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY) {
                        // 为机房安全时, 如果推送的对象ID为订阅的空间ID, 或者设备的所在空间ID在订阅的空间ID中, 均允许推送
                        if (subscribeSpaceIds.contains(objectId) || (CollectionUtils.isNotEmpty(inSpaceIds)
                                && CollectionUtils.containsAny(subscribeSpaceIds, inSpaceIds))) {
                            toBeAdded = true;
                        }
                    } else if (objectIds.contains(objectId)) {
                        toBeAdded = true;
                    }
                    String objectType = objectInfo.getObjectType();
                    if (toBeAdded && StringUtils.isNotBlank(objectType)) {
                        exceptionEventBaseInfoVO.setId(objectId);
                        exceptionEventBaseInfoVO.setLocalName(objectInfo.getInstanceName());
                        exceptionEventBaseInfoVO.setObjectType(objectType);
                        exceptionEventBaseInfoVO.setInSpaceIds(inSpaceIds);
                        processAbnormalEventInfo(exceptionEventBaseInfoVO);
                        resultList.add(exceptionEventBaseInfoVO);
                    }
                }
            }
            // 过滤不满足条件的异常事件
            Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition = null;
            if (Objects.nonNull(runningMonitorTypeEnum)) {
                exceptionFilterCondition = runningMonitorTypeEnum.getRunningMonitorStrategy().exceptionFilterCondition();
            }
            if (Objects.nonNull(exceptionFilterCondition)) {
                resultList = exceptionFilterCondition.apply(resultList);
            }

            if (CollectionUtils.isEmpty(resultList)) {
                return null;
            }
            return SseEmitterObject.builder()
                    .projectId(param.getProjectId())
                    .eventType(SseEmitterEventEnum.ABNORMAL_EVENT)
                    .data(resultList)
                    .build();
        }
        return null;
    }

}
