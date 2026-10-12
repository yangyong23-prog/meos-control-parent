package com.persagy.apccontrol.task.sse;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitters;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * sse照明定时调度 云端
 *
 * @author yuecaipu
 * @date 2024/4/30 15:36
 * @since v5.240402.00
 */
@Slf4j
@Component
public class SseEventScheduledServiceFor15Min {

    @Resource
    private SseEmitters sseEmitters;

    @Resource
    SseHandlerContext handlerContext;

    @Value("${statistics.push.switch:true}")
    private Boolean pushSwitch;

    @Scheduled(cron = "0 0/15 * * * *")
    public void scheduled() {
        if (!pushSwitch) {
            return;
        }
        log.info("=====>>>>>使用cron  {}", System.currentTimeMillis());

        for (SubscribeTypeEnum subscribeTypeEnum : SubscribeTypeEnum.values()) {
            SseEmitterEventEnum[] scheduleEventTypes = getScheduleEventTypes(subscribeTypeEnum);
            if (null == scheduleEventTypes || scheduleEventTypes.length == 0) {
                continue;
            }
            for (SseEmitterEventEnum eventType : scheduleEventTypes) {
                executeStrategyForEventType(subscribeTypeEnum, eventType);
            }
        }
    }

    private void executeStrategyForEventType(
            SubscribeTypeEnum subscribeTypeEnum,
            SseEmitterEventEnum eventType) {
        Map<String, SseSubscribeParam> eventKeys = sseEmitters.getEventKeys(subscribeTypeEnum);
        if (CollUtil.isEmpty(eventKeys)) {
            return;
        }
        eventKeys.forEach((eventKey, sseSubscribeParam) -> {
            ContextUtil.setGroupCode(sseSubscribeParam.getGroupCode());
            ContextUtil.setTenant(sseSubscribeParam.getGroupCode());
            ContextUtil.setProjectId(sseSubscribeParam.getProjectId());
            log.info("执行定时任务：subscribeTypeEnum：{}eventType：{}", subscribeTypeEnum, eventType);
            if (handlerContext.getStrategy(eventType) == null) {
                return;
            }
            try {
                handlerContext.getStrategy(eventType)
                        .doOperation(eventKey, sseSubscribeParam, sseEmitters.getSseEmittersByEventKey(eventKey));
            } catch (Exception e) {
                log.error("执行异常", e);
            } finally {
                ContextUtil.remove();
            }
        });
    }


    /**
     * 获取定时推送的事件类型
     *
     * @param subscribeType sse订阅类型
     * @return {@link SseEmitterEventEnum}
     * @author yuecaipu
     * @date 2024/8/9 11:45
     * @since v5.240801.0
     */
    public SseEmitterEventEnum[] getScheduleEventTypes(SubscribeTypeEnum subscribeType) {
        switch (subscribeType) {
            // 客流
            case PASSENGER_FLOW:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.PASSENGER_FLOW_HEAD_TOTAL
                };
            default:
                return new SseEmitterEventEnum[]{};
        }
    }
}
