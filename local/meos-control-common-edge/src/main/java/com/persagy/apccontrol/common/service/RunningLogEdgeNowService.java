package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.common.outside.dtpmeos.BusinessRunningLogPushDTO;


/**
 * 新版运行日志边缘端服务
 */
public interface RunningLogEdgeNowService {
    
    /**
     * 通过 bdtp发送的控制消息保存日志
     *
     * @param log
     */
    public void saveByBdtpMsg(BusinessRunningLogPushDTO log);
    
    /**
     * 通过twin设定参数消息更新日志信息
     *
     * @param vo
     */
    public void updateByTwinMsg(FeedbackInfoMsgMqttVO vo);
    
    
    /**
     * 保存日志
     *
     * @param runLog
     */
    public void save(RunningLog runLog);
}

