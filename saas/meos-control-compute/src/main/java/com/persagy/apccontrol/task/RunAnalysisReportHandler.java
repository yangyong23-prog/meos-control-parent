package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.service.RunAnalysisReportDomainService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-26 18:06
 **/
@Component("runAnalysisReportHandler")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportHandler implements TecXxJobHandler {
    private final RunAnalysisReportDomainService runAnalysisReportDomainService;

    public static final String LOG_TAG = "运行分析报告生成";
    @Override
    public String getHandlerName() {
        return "runAnalysisReport";
    }

    @Override
    public Future<String> processAsync(String groupCode, ProjectDTO projectDTO) {
        process(groupCode, projectDTO);
        return new AsyncResult<>(this.getHandlerName() + LocaleMessageUtil.getMessage("key_2fa808b2f7ea4112a5ed1750c743f957") + groupCode + ";projectId" + projectDTO.getProjectId());

    }

    @Override
    public void process(String groupCode, ProjectDTO projectDTO) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectDTO.getProjectId());
        try {
            runAnalysisReportDomainService.buildAuto();
        } catch (Exception e) {
            log.error("自动生成运行分析报告生成异常", e);
        }
    }
}