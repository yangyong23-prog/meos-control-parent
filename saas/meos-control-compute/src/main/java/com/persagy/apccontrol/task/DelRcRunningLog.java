package com.persagy.apccontrol.task;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.resource.RcRunningLogService;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.engine.AbstractTecXxJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 定期清理运行日志
 */
@Slf4j
@Service
@Component("delRcRunningLogHandler")
public class DelRcRunningLog extends AbstractTecXxJobHandler {
    @Resource
    private RcRunningLogService rcRunningLogService;
    @Resource
    private RunningLogServiceResource runningLogServiceResource;
    @Resource
    protected NacosSwitchConfig nacosSwitchConfig;
    
    public static final String LOG_TAG = LocaleMessageUtil.getMessage("key_f854a3e430b142449b9c567e3df65aa0");
    
    @Override
    public String getHandlerName() {
        return LOG_TAG;
    }
    
    @Override
    public void handleProject(ProjectDTO projectDTO) {
        try {
            runningLogServiceResource.createPartitionAndDeleteHistoryPartition(projectDTO.getProjectId());
        } catch (Exception e) {
            log.error("新版-运行日志历史数据清除失败", e);
        }
        
        try {
            LocalDate localDate = LocalDate.now().minusMonths(nacosSwitchConfig.getRunningDeleteMonthsAgoData());
            log.info("新版-运行日志历史数据清除，时间", localDate);
            LambdaQueryChainWrapper<RcRunningLog> chainWrapper = this.rcRunningLogService
                    .lambdaQuery()
                    .eq(RcRunningLog::getProjectId, projectDTO.getProjectId())
                    .lt(RcRunningLog::getCreateTime, localDate);
            this.rcRunningLogService.remove(chainWrapper.getWrapper());
        } catch (Exception e) {
            log.error("老版-运行日志历史数据清除失败", e);
        }
        
    }
    
}
