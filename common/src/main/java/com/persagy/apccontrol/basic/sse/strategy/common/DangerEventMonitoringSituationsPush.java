package com.persagy.apccontrol.basic.sse.strategy.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.outside.dto.dangerevent.DangerEventMsgDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.InstanceTreeMonitoringSituationChangeMsgDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 异常事件
 *
 * @author LiuYang
 * @date 2024/10/25 15:38
 * @since v5.241002.0
 */
@Slf4j
@Component(value = "DANGER_EVENT_MONITORING_SITUATION")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventMonitoringSituationsPush extends AbstractSseEventStrategyTemplate {

    private final ObjectMapper objectMapper;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.DANGER_EVENT_MONITORING_SITUATION;
    }

    @Override
    protected String getRedisLockKey(String eventKey, Object condition) {
        return String.format("DANGER_EVENT_MONITORING_SITUATION:%s", condition);
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        // 风险事件监控情况推送
        if (condition instanceof InstanceTreeMonitoringSituationChangeMsgDTO) {
            Map<SseEmitterEventEnum, Object> eventParamMap = param.getEventParamMap();
            if (MapUtils.isEmpty(eventParamMap) || !eventParamMap.containsKey(getEventType())) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_d52365bcc194407cabe816747b2bb8d1"));
            }
            try {
                Object object = eventParamMap.get(getEventType());
                InstanceTreeMonitoringSituationChangeMsgDTO dto = (InstanceTreeMonitoringSituationChangeMsgDTO) condition;
                List<InstanceTreeMonitoringSituationChangeMsgDTO> data = dto.getData();
                if (CollectionUtils.isEmpty(data)) {
                    return null;
                }
                List<String> instanceTreeIds = objectMapper.readValue(objectMapper.writeValueAsString(object),
                        ArrayList.class);
                List<InstanceTreeMonitoringSituationChangeMsgDTO> pushData = data.stream().filter(instanceTreeStatus -> instanceTreeIds.contains(instanceTreeStatus.getInstanceTreeId()))
                        .collect(Collectors.toList());
                return SseEmitterObject.builder()
                        .projectId(param.getProjectId())
                        .eventType(getEventType())
                        .data(pushData)
                        .build();
            } catch (Exception e) {
                log.error("", e);
            }

        }
        return null;
    }

    /**
     * 消息内容判断逻辑
     *
     * @param instanceTreeStatusDiffs 实例树状态变化
     * @return <对象id集合>
     */
    private List<String> messageProcessing(List<DangerEventMsgDTO.InstanceTreeStatusDiffs> instanceTreeStatusDiffs) {
        List<String> dangerEventIds = Lists.newArrayList();
        for (DangerEventMsgDTO.InstanceTreeStatusDiffs instanceTreeStatusDiff : instanceTreeStatusDiffs) {
            DangerEventMsgDTO.InstanceTreeStatus instanceTreeStatus = instanceTreeStatusDiff.getInstanceTreeStatus();
            // 风险事件信息非空
            if (Objects.isNull(instanceTreeStatus) || CollectionUtils.isEmpty(instanceTreeStatusDiffs)) {
                continue;
            }
            // 变化信息非空
            DangerEventMsgDTO.DiffDetail diffDetail = instanceTreeStatusDiff.getDiffDetail();
            if (Objects.isNull(diffDetail)) {
                continue;
            }
            // 当前时间和状态都为空则跳过
            DangerEventMsgDTO.DiffItem currentStatusDiff = diffDetail.getCurrentStatusDiff();
            DangerEventMsgDTO.DiffItem updateTimeDiff = diffDetail.getStatusUpdateTimeDiff();
            if (Objects.isNull(currentStatusDiff) || Objects.isNull(updateTimeDiff)) {
                continue;
            }
            // 时间和状态变化都非空且都为非变化状态跳过
            Boolean statusDiffChanged = currentStatusDiff.getChanged();
            Boolean timeDiffChanged = updateTimeDiff.getChanged();
            if (Objects.isNull(statusDiffChanged) || !statusDiffChanged || Objects.isNull(timeDiffChanged) || !timeDiffChanged) {
                continue;
            }
            // 时间和状态变化都不相等
            if (currentStatusDiff.getOldValue().equals(currentStatusDiff.getNewValue())
                    || updateTimeDiff.getOldValue().equals(updateTimeDiff.getNewValue())) {
                continue;
            }

            // 组装格式存入库，且发送消息
            String dangerEventId = instanceTreeStatus.getId();

            dangerEventIds.add(dangerEventId);
        }
        return dangerEventIds;
    }

}
