package com.persagy.apccontrol.service.impl;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.service.demand.instancegroup.DemandInstanceGroupMessageService;
import org.springframework.stereotype.Service;

/**
 * 边通知
 */
@Service
public class DemandInstanceGroupMessageEdgeServiceImpl implements DemandInstanceGroupMessageService {
    @Override
    public void sendIdMessage(RunningMonitorSecondTypeEnum templateId, Long groupId) {
    
    }
}
