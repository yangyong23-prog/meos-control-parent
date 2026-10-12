package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.service.demand.instancegroup.DemandInstanceGroupMessageService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 云端数据通知
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupMessageServiceImpl implements DemandInstanceGroupMessageService {
    
    private final DemandLightingService demandLightingService;
    
    @Override
    public void sendIdMessage(RunningMonitorSecondTypeEnum type, Long groupId) {
        demandLightingService.resultComputeDemandLightingResult(type, groupId);
    }
}
