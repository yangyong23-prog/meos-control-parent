package com.persagy.apccontrol.task.scheduled.sse;

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
 * sse照明定时调度 边缘端
 *
 * @author yuecaipu
 * @date 2024/4/30 15:36
 * @since v5.240402.00
 */
@Slf4j
@Component
public class SseEventScheduledLocalService {

    @Resource
    private SseEmitters sseEmitters;

    @Resource
    SseHandlerContext handlerContext;


    @Value("${statistics.push.switch:true}")
    private Boolean pushSwitch;
    
    
    @Scheduled(cron = "0 0/1 * * * *")
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
            // 照明
            case LIGHT_MONITORING:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.LIGHTING_LAMP_FUNC_TYPE_STATUS,
                        SseEmitterEventEnum.ABNORMAL_STATE,
                        SseEmitterEventEnum.LIGHTING_DASHBOARD_EQUIPMENT_STATISTICS,
                        SseEmitterEventEnum.LIGHTING_DASHBOARD
                };
            // 空调末端
            case AIR_TERMINAL:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.SPACE_QUALITY_STATISTICS,
                        SseEmitterEventEnum.EQUIPMENT_STATUS_STATISTICS,
                        SseEmitterEventEnum.BDTP_EQUIPMENT_STATUS_STATISTICS,
                        SseEmitterEventEnum.OUTDOOR_WEATHER,
                        SseEmitterEventEnum.OBJECT_TAG,
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT,
                        //SseEmitterEventEnum.EQUIPMENT_BUSINESS,
                        SseEmitterEventEnum.SPACE_WITH_STATUS
                };
            // 安防
            case SECURITY:
                return new SseEmitterEventEnum[]{
                        // SseEmitterEventEnum.EQUIPMENT_BUSINESS,
                        SseEmitterEventEnum.SECURITY_EQUIPMENT_STATISTICS,
                        SseEmitterEventEnum.SECURITY_ARMING_SPACE_STATISTICS,
                        SseEmitterEventEnum.SECURITY_ACCESS_SPACE_STATISTICS,
                        //SseEmitterEventEnum.SECURITY_MODEL_VIEW,
                        SseEmitterEventEnum.SECURITY_DASH_BOARD,
                        SseEmitterEventEnum.ABNORMAL_STATE
                };
            // 通风
            case VENTILATION:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.VENTILATION_EQUIP_STATISTICS,
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT,
                };
            // 电梯
            case ELEVATOR:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.EQUIPMENT_BUSINESS,
                        SseEmitterEventEnum.ELEVATOR_RUNNING_TOTAL_ELELEL,
                        SseEmitterEventEnum.ELEVATOR_RUNNING_TOTAL_ELELES
                        //SseEmitterEventEnum.ABNORMAL_DATA_LIST_DIVIDE_SYSTEM
                };
            // 客流
            case PASSENGER_FLOW:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.EQUIPMENT_BUSINESS,
                        //SseEmitterEventEnum.PASSENGER_FLOW_HEAD_TOTAL,
                        //SseEmitterEventEnum.PASSENGER_FLOW_HOME_STATISTICS
                        //SseEmitterEventEnum.ABNORMAL_DATA_LIST_DIVIDE_SYSTEM
                };
            // 停车场
            case PARKING:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.EQUIPMENT_BUSINESS,
                        SseEmitterEventEnum.PARKING_STATISTICS,
                        SseEmitterEventEnum.FLOOR_PARKING_SPACE_STATISTICS,
                        SseEmitterEventEnum.PARKING_EQUIP_STATISTICS,
                        SseEmitterEventEnum.PARKING_SPACE_GIRD_STATISTICS
                };
            // 厨房油烟机
            case KITCHEN_RANGE_HOOD:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT
                };
            // 机房安全
            case COMPUTER_ROOM_SECURITY:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT,
                        SseEmitterEventEnum.DANGER_EVENT_MONITORING_SITUATION
                };
            case FIRE_FIGHTING:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT
                };
            case ACCC:
            case ACCI:
            case ACCH:
            case AIR_CONDITIONING_COLD_SOURCE:
            case PHOTOVOLTAIC:
            case TOILET:
            case ACAH:
            case ACAI:
                return new SseEmitterEventEnum[]{
                        SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT
                };
            default:
                return new SseEmitterEventEnum[]{};
        }
    }
}
