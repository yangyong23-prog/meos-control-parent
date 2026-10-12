package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogMeosMsgDTO;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.service.runninglog.category.RunningLogAddService;
import com.persagy.apccontrol.common.service.RunningLogEdgeNowService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * 内部服务边缘端日志保存
 */
@Service
@RefreshScope
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningLogAddServiceEdge implements RunningLogAddService {
    
    private final RunningLogEdgeNowService runningLogEdgeNowService;
    
    @Override
    public void saveByMeosMsg(RunningLogMeosMsgDTO log) {
        if (log == null) {
            return;
        }
        //数据转换
        RunningLog runLog = new RunningLog();
        runLog.setProjectId(ContextUtil.getProjectId());
        runLog.setType(log.getType());
        runLog.setSourceType(log.getSourceType());
        runLog.setState(log.getState());
        runLog.setDataTime(LocalDateTime.now());
        runLog.setCreateTime(LocalDateTime.now());
        runLog.setClassCode(log.getClassCode());
        
        runLog.setInfoCode(log.getInfoCode());
        runLog.setInfoValue(log.getInfoValue());
        runLog.setCreateUser(ContextUtil.getUserName());
        runLog.setContent(log.getContent());
        
        runLog.setKeyWord(log.getKeyWord());
        
        runLog.setObjectId(log.getObjectId());
        runLog.setObjectName(log.getObjectName());
        
        runLog.setSystemId(log.getSystemId());
        runLog.setSystemName(log.getSystemName());
        runLog.setSystemClassCode(log.getSystemClassCode());
        
        runLog.setFirstLevelCategory(log.getFirstLevelCategory());
        runLog.setSecondLevelCategory(log.getSecondLevelCategory());
        runningLogEdgeNowService.save(runLog);
    }
}
