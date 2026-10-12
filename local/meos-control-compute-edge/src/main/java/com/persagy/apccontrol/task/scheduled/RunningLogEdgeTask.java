package com.persagy.apccontrol.task.scheduled;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 边缘端运行日志任务
 */
@Slf4j
@Component
public class RunningLogEdgeTask {
    
    @Resource
    private RunningLogServiceResource runningLogServiceResource;
    
    @Resource
    private ProjectCfg projectCfg;
    
    /**
     * 边缘端日志保留 7 天的
     * 每日 3 点执行
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void synShellConfigToTwin() {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        ContextUtil.setProjectId(projectCfg.getProjectId());
        //删除创建时间为 7 天前的数据
        LocalDateTime time = LocalDateTime.now().minusDays(7);
        runningLogServiceResource.lambdaUpdate().le(RunningLog::getDataTime, time).remove();
        
    }
}
