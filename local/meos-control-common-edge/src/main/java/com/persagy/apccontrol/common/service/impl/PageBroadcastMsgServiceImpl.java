package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.service.common.PageBroadcastMsgQueryService;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.common.service.PageBroadcastMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class PageBroadcastMsgServiceImpl implements PageBroadcastMsgService {
    
    @Resource
    private PageBroadcastMsgQueryService pageBroadcastMsgQueryService;
    @Autowired
    private IWSMessageService iWSMessageService;
    /**
     * 前端页面播报
     */
    @Override
    public void pushMessageToWeb(String projectId, String systemId, String modelType) {
        List<FeedbackInfoMsgMqttVO.PageBroadcastContent> contents =
                pageBroadcastMsgQueryService.listPageBroadcastMsg(new PageBroadcastQueryParam(projectId, systemId));
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        feedbackInfoMsgMqttVO.setProjectId(projectId);
        feedbackInfoMsgMqttVO.setSystemId(systemId);
        feedbackInfoMsgMqttVO.setPageBroadcastContents(contents);
        feedbackInfoMsgMqttVO.setWebType(WebSocketTypeEnum.PAGE_BROADCAST);
        feedbackInfoMsgMqttVO.setModelType(modelType);
        
        iWSMessageService.pushMessageWeb(feedbackInfoMsgMqttVO);
    }
}
