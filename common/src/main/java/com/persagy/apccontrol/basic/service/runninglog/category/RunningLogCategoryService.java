package com.persagy.apccontrol.basic.service.runninglog.category;

import com.persagy.apccontrol.basic.model.entity.RunningLog;

/**
 * 运行日志分类服务
 */
public interface RunningLogCategoryService {
    
    
    /**
     * 构建日志的一二级日志分类
     *
     * @param runLog
     */
    public void buildLogCategory(RunningLog runLog);
    
}
