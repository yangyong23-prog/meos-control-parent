package com.persagy.apccontrol.service.impl;

import com.persagy.apccontrol.basic.model.dto.twin.BusinessExternalPushDTO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.common.service.MqttService;
import com.persagy.apccontrol.service.DtpMeosServerEdgeService;
import com.persagy.apccontrol.websocket.event.PushIOTEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MQTT 服务
 *
 * @author yuyao
 */
@Service
@Slf4j
public class MqttServiceImpl implements MqttService {
    
    @Autowired
    DtpMeosServerEdgeService dtpMeosServerEdgeService;
    @Autowired
    PushIOTEventListener pushIOTEventListener;


    @Override
    public void expense(FeedbackInfoMsgMqttVO vo) {
        if (null == vo) {
            return;
        }
        pushIOTEventListener.onApplicationEvent(vo);
    }

    @Override
    public void dtpMeosServerEdge(BusinessExternalPushDTO vo) {
        if (null == vo) {
            return;
        }
        dtpMeosServerEdgeService.dtpMeosServerEdge(vo);
    }

}
