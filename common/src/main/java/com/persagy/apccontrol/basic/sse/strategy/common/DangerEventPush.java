package com.persagy.apccontrol.basic.sse.strategy.common;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO;
import com.persagy.apccontrol.basic.outside.dto.dangerevent.DangerEventMsgDTO;
import com.persagy.apccontrol.basic.service.dangerevent.DangerEventService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 异常事件
 *
 * @author LiuYang
 * @date 2024/10/25 15:38
 * @since v5.241002.0
 */
@Slf4j
@Component(value = "DANGER_EVENT")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventPush extends AbstractSseEventStrategyTemplate {

    private final DangerEventService dangerEventService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.DANGER_EVENT;
    }

    @Override
    protected String getRedisLockKey(String eventKey, Object condition) {
        if (condition instanceof DangerEventMsgDTO) {
            return String.format("DANGER_EVENT:%s", condition);
        }
        return null;
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        if (condition instanceof DangerEventMsgDTO) {
            DangerEventMsgDTO dangerEvent = (DangerEventMsgDTO) condition;
            List<DangerEventMsgDTO.InstanceTreeStatusDiffs> instanceTreeStatusDiffs = dangerEvent.getInstanceTreeStatusDiffs();
            // 创建一个Map，key为对象id，值为List<DangerEventVO>，同一个实例的多个风险事件
            List<String> dangerEventIds = messageProcessing(instanceTreeStatusDiffs);

            if (CollectionUtils.isEmpty(dangerEventIds)) {
                return null;
            }

            List<DangerEventVO> dangerEvents = dangerEventService.listByDangerEvent(dangerEventIds);
            if (CollectionUtils.isEmpty(dangerEvents)) {
                return null;
            }

            // 数据库更新，消息不区分，推送过滤对象id 更新暂停 2025-02-17
            /*List<DangerEvent> dangerEventList = Lists.newArrayList();

            dangerEvents.forEach(dangerEventVO -> {
                DangerEvent saveDangerEvent = dangerEventConverter.toDangerEvent(dangerEventVO);
                dangerEventList.add(saveDangerEvent);
            });

            dangerEventResourceService.batchSave(dangerEventList);
            */


            // 从参数中获取类别代码到对象ID集合的映射
            Map<String, Set<String>> claasCode2objectIds = param.getClaasCode2objectIds();
            // 如果映射为空，则返回null
            if (MapUtils.isEmpty(claasCode2objectIds)) {
                return null;
            }
            // 将映射中的所有对象ID集合合并成一个列表
            List<String> objectIds = claasCode2objectIds.values().stream()
                    .flatMap(Set::stream) // 将每个Set转换为Stream并合并
                    .collect(Collectors.toList());
            // 创建一个列表用于存储筛选后的危险事件
            List<DangerEventVO> dangerEventResultList = Lists.newArrayList();

            dangerEvents.forEach(x -> {
                // 如果当前风险事件的对象ID在之前合并的对象ID列表中
                if (objectIds.contains(x.getObjectId())) {
                    dangerEventResultList.add(x);
                }
            });

            // 如果筛选后的危险事件列表为空，则返回null
            if (CollectionUtils.isEmpty(dangerEventResultList)) {
                return null;
            }

            return SseEmitterObject.builder()
                    .projectId(param.getProjectId())
                    .eventType(SseEmitterEventEnum.DANGER_EVENT)
                    .data(dangerEventResultList)
                    .build();
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
