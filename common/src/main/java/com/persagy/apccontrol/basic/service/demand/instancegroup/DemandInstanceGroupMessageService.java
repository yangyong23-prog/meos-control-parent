package com.persagy.apccontrol.basic.service.demand.instancegroup;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;

/**
 * 实例组消息服务
 */
public interface DemandInstanceGroupMessageService {
    
    /**
     * 实例消息发送
     *
     * @param type 类型
     */
    public void sendIdMessage(RunningMonitorSecondTypeEnum type, Long groupId);
    
}
