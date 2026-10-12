package com.persagy.apccontrol.basic.service.runninglog.category;

import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogMeosMsgDTO;

/**
 * 运行日志处理类
 */
public interface RunningLogAddService {
    
    /**
     * 系统内部日志记录
     *
     * @param log
     */
    public void saveByMeosMsg(RunningLogMeosMsgDTO log);
    
    
}
