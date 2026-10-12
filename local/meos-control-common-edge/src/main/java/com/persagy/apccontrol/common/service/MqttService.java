package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.dto.twin.BusinessExternalPushDTO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;

/**
 * MQTT 服务
 *
 * @author yuyao
 */
public interface MqttService {

    /**
     * twin 服务发送的数据
     * 消费 MQTT 消息进行处理
     *
     * @param vo
     */
    public void expense(FeedbackInfoMsgMqttVO vo);


    /**
     * dtp-meos-server-edge 上报数据处理
     *
     * @param vo
     */
    public void dtpMeosServerEdge(BusinessExternalPushDTO vo);






}
