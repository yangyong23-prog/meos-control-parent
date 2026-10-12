package com.persagy.apccontrol.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.websocket.WSClient;
import com.persagy.apccontrol.websocket.WSClientCache;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author shaohongbo
 * @date 2023年0I月29日 17:27
 */
@Component
@Slf4j
public class WsMessageServiceImpl implements IWSMessageService {

    @Autowired
    private ProjectCfg projectCfg;

    @Lazy
    @Autowired
    SseHandlerContext sseHandlerContext;

    /**
     * 消息推送
     */
    @Override
    public void pushMessage(FeedbackInfoMsgMqttVO source) {
        source.setWebType(source.getWebType() == null ? WebSocketTypeEnum.PARAM_SET : source.getWebType());
        for (WSClient client : WSClientCache.getProjectClients(projectCfg.getProjectId())) {
            try {
                if (WebSocketTypeEnum.PARAM_SET.equals(source.getWebType()) && client.getFunctionType() != null) {
                    Set<String> filterCodes = LocalCacheConstant.WEBSOCKET_CONFIG.get(client.getFunctionType().getCode());
                    if (CollectionUtil.isNotEmpty(filterCodes) && filterCodes.contains(WebsocketConfig.getUniqueKey(source.getClassCode(), source.getInfoCode()))) {
                        client.send(JSON.toJSONString(source));
                    }
                } else {
                    client.send(JSON.toJSONString(source));
                }
                DailyStringCounter.incrementCount("websocket发送总数" + client.getChannelId());
            } catch (Exception e) {
                log.error("websocket推送客户端数据失败{}.......", e.getMessage());
                DailyStringCounter.incrementCount("websocket发送失败总数" + client.getChannelId());
            }
        }
        // sse 推送
        if (WebSocketTypeEnum.PAGE_BROADCAST.equals(source.getWebType())) {
            // 页面播报
            sseHandlerContext.getStrategy(SseEmitterEventEnum.PAGE_BROADCAST).doOperation(source.getProjectId(), source);
        } else if (WebSocketTypeEnum.INFO_EXTEND_SET.equals(source.getWebType())) {
            // 信息点扩展配置
            sseHandlerContext.getStrategy(SseEmitterEventEnum.INFO_EXTEND_SET).doOperation(source.getProjectId(), source);
        }
    }
    
    @Override
    public void pushMessageWeb(FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO) {
        if (feedbackInfoMsgMqttVO == null) {
            return;
        }
        try {
            String modelType = ContextUtil.getString(CommonConst.MODEL_TYPE_HEADER);
            modelType = StrUtil.isBlank(modelType) || "null".equalsIgnoreCase(modelType) ? null : modelType;
            
            if (StrUtil.isBlank(feedbackInfoMsgMqttVO.getModelType())) {
                feedbackInfoMsgMqttVO.setModelType(modelType);
            }
            if (StrUtil.isBlank(feedbackInfoMsgMqttVO.getSystemId())) {
                feedbackInfoMsgMqttVO.setSystemId(feedbackInfoMsgMqttVO.getModelType());
            }
            if (StrUtil.isBlank(feedbackInfoMsgMqttVO.getSystemId())) {
                feedbackInfoMsgMqttVO.setSystemId(modelType);
            }
            pushMessage(feedbackInfoMsgMqttVO);
        } catch (Exception e) {
            log.error("webSocket pushMessage error:{}", e);
        }
    }
    
    
}
