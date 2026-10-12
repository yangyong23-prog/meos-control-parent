package com.persagy.apccontrol.common.service;


import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;

public interface IWSMessageService {


    void pushMessage(FeedbackInfoMsgMqttVO source);
    
    /**
     * 迁移接口
     *
     * @param source
     */
    void pushMessageWeb(FeedbackInfoMsgMqttVO source);
}
