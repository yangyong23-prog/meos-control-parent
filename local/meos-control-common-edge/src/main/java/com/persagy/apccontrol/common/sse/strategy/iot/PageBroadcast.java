package com.persagy.apccontrol.common.sse.strategy.iot;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.Map;


/**
 * 页面播报
 *
 * @author yuecaipu
 * @date 2024/8/26 15:38
 * @since v5.240802.00
 */
@Slf4j
@Component(value = "PAGE_BROADCAST")
public class PageBroadcast extends AbstractSseEventStrategyTemplate {

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.PAGE_BROADCAST;
    }
    
    @Resource
    private ObjectMapper objectMapper;
    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param, Object condition) {
        if (condition instanceof FeedbackInfoMsgMqttVO) {
            FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = (FeedbackInfoMsgMqttVO) condition;
            if (!param.getProjectId().equals(feedbackInfoMsgMqttVO.getProjectId())) {
                return null;
            }
            // 不进行跨模块推送
            if (!param.getSubscribeType().getValue().equals(feedbackInfoMsgMqttVO.getModelType())) {
                return null;
            }

            return SseEmitterObject.builder()
                    .projectId(param.getProjectId())
                    .eventType(SseEmitterEventEnum.PAGE_BROADCAST)
                    .data(feedbackInfoMsgMqttVO)
                    .build();
        }
        if (condition instanceof RunningLogVO) {
            RunningLogVO runningLogVO = (RunningLogVO) condition;
            
            Map<SseEmitterEventEnum, Object> eventParamMap = param.getEventParamMap();
            if (MapUtils.isEmpty(eventParamMap) || !eventParamMap.containsKey(getEventType())) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_d52365bcc194407cabe816747b2bb8d1"));
            }
            Object object = eventParamMap.get(getEventType());
            try {
                EventParam eventParam = objectMapper.readValue(objectMapper.writeValueAsString(object),
                        EventParam.class);
                
                // 二级分类 订阅就匹配二级分类是否匹配
                if (eventParam.getSecondLevelCategory() != null &&
                        !StringUtils.equals(eventParam.getSecondLevelCategory(), runningLogVO.getSecondLevelCategory())) {
                    return null;
                }
                //一级分类是必须的 二级分类匹配上后匹配一级分类
                if (runningLogVO.getFirstLevelCategory().equals(eventParam.getFirstLevelCategory())) {
                    return SseEmitterObject.builder()
                                           .projectId(param.getProjectId())
                                           .eventType(SseEmitterEventEnum.PAGE_BROADCAST)
                                           .data(runningLogVO)
                                           .build();
                }
            } catch (Exception e) {
                log.error("解析订阅参数异常", e);
            }
        }
        return null;
    }
    
    
    /**
     * 事件参数
     */
    @Data
    @FieldNameConstants
    public static class EventParam implements BaseCheckParamProvider {
        
        @Schema(description = "日志一级分类")
        private NowRunningLogCategory firstLevelCategory;
        
        @Schema(description = "日志二级分类")
        private String secondLevelCategory;
        
        public void setSystemId(String systemId) {
            this.secondLevelCategory = systemId;
        }
    }
}
