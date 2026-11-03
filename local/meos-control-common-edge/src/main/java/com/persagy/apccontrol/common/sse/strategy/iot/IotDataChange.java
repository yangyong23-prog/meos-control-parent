package com.persagy.apccontrol.common.sse.strategy.iot;

import cn.hutool.core.bean.BeanUtil;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.dto.ObjectIotData;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.service.elevator.ElevatorService;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.apccontrol.basic.service.ventilation.VentilationService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


/**
 * iot数据变更
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240602.00
 */
@Slf4j
@Component(value = "IOT_DATA_CHANGE")
public class IotDataChange extends AbstractSseEventStrategyTemplate {
    @Resource
    AirTerminalService airTerminalService;
    @Resource
    VentilationService ventilationService;
    @Resource
    SecurityService securityService;
    @Resource
    LightingService lightingService;
    @Resource
    ElevatorService elevatorService;
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.IOT_DATA_CHANGE;
    }

    @Override
    protected String getRedisLockKey(String eventKey, Object condition) {
        if (condition instanceof FeedbackInfoMsgMqttVO) {
            return String.format("IOT_DATA_CHANGE:%s:%s",
                    ((FeedbackInfoMsgMqttVO) condition).getObjectId(),
                    ((FeedbackInfoMsgMqttVO) condition).getInfoCode());
        }
        return null;
    }

    private String getLockKey(SubscribeTypeEnum subscribeType, String instanceId) {
        return String.format("IOT_DATA_CHANGE:%s:%s", subscribeType.getValue(), instanceId);
    }


    /**
     * opt B9：与 getSseEmitterObject 的项目、对象范围过滤一致；推送范围尚未计算时不过滤（保留原来的懒加载）。
     */
    @Override
    protected boolean skipBeforeCompute(SseSubscribeParam param, Object condition) {
        if (!(condition instanceof FeedbackInfoMsgMqttVO)) {
            return true;
        }
        FeedbackInfoMsgMqttVO vo = (FeedbackInfoMsgMqttVO) condition;
        if (!param.getProjectId().equals(vo.getProjectId())) {
            return true;
        }
        return param.getClaasCode2objectIds() != null
                && checkClaasCode2objectIds(param, vo.getClassCode(), vo.getObjectId());
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        if (condition instanceof FeedbackInfoMsgMqttVO) {
            FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = (FeedbackInfoMsgMqttVO) condition;
            if (!param.getProjectId().equals(feedbackInfoMsgMqttVO.getProjectId())) {
                return null;
            }
            // 获取推送范围
            if (param.getClaasCode2objectIds() == null) {
                String lockKey = getLockKey(param.getSubscribeType(), param.getInstanceId());
                if (!tryAcquireLock(lockKey, false)) {
                    return null;
                }
                try {
                    if (param.getClaasCode2objectIds() == null) {
                        param.setClaasCode2objectIds(mapClaasCode2objectIds(param));
                    }
                } catch (Exception e) {
                    log.error("mapClaasCode2objectIds异常", e);
                } finally {
                    unlock(lockKey);
                }
            }

            //过滤设备 ID
            if (checkClaasCode2objectIds(param, feedbackInfoMsgMqttVO.getClassCode(), feedbackInfoMsgMqttVO.getObjectId()))
                return null;


            ObjectIotData objectIotData = BeanUtil.copyProperties(condition, ObjectIotData.class);
            
            //填充单位和名称
            RcInfoCodeCfg infoCodeCfg =
                    this.localRedisCacheHandler.getInfoCodeConfig(feedbackInfoMsgMqttVO.getClassCode(),
                    feedbackInfoMsgMqttVO.getInfoCode());
            
            if (infoCodeCfg != null) {
                objectIotData.setUnit(infoCodeCfg.getUnit());
                objectIotData.setDataValueName(infoCodeCfg.getDataValueName(objectIotData.getDataValue()));
            } else {
                objectIotData.setDataValueName(objectIotData.getDataValue());
            }
            
            return SseEmitterObject.builder()
                    .projectId(param.getProjectId())
                    .eventType(SseEmitterEventEnum.IOT_DATA_CHANGE)
                    .data(objectIotData)
                    .build();
        }
        return null;
    }


    /**
     * 跟进订阅类型获取订阅对应的对象类和对象Id集合映射
     * @param param 订阅参数
     * @return {@link Map<String,Set<String>>} key:类编码,value:对象Id集合
     * @author yuecaipu
     * @date 2024/8/17 17:39
     * @since v5.240801
     */
    public Map<String, Set<String>> mapClaasCode2objectIds(SseSubscribeParam param) {
        if (param.getSubscribeType().equals(SubscribeTypeEnum.AIR_TERMINAL)) {
            return airTerminalService.mapClaasCode2objectIds(param.getProjectId(), param.getInstanceId(), AirTerminalInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        } else if (param.getSubscribeType().equals(SubscribeTypeEnum.VENTILATION)) {
            return ventilationService.mapClaasCode2objectIds(param.getProjectId(), param.getInstanceId(), DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        } else if (param.getSubscribeType().equals(SubscribeTypeEnum.SECURITY)) {
            return securityService.mapClaasCode2objectIds(param.getProjectId(), param.getInstanceId(), DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        } else if (param.getSubscribeType().equals(SubscribeTypeEnum.LIGHT_MONITORING)) {
            return lightingService.mapClaasCode2objectIds(param.getProjectId(), param.getDemandTemplateType(), param.getInstanceId(), DemandInstanceTypeEnum.analyze(param.getInstanceType().getValue()));
        } else if (param.getSubscribeType().equals(SubscribeTypeEnum.ELEVATOR)){
            return elevatorService.mapClaasCode2objectIds(param.getProjectId());
        }
        return Collections.emptyMap();
    }

}
