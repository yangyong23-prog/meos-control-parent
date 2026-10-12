package com.persagy.apccontrol.basic.structure.runanalysis.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.RunningLogMapper;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportChapterParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportListParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportQueryParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.repository.InfoCodeRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.mapper.RunAnalysisReportMapper;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RunAnalysisReportRepositoryImpl extends ServiceImpl<RunAnalysisReportMapper, RunAnalysisReport> implements RunAnalysisReportRepository, BizComponent<RunAnalysisReportRepository> {

    public RunAnalysisReport getByDayAndTypeAndChapter(@NonNull RunAnalysisReport report) {
        return this.lambdaQuery()
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReport::getReportDate, report.getReportDate())
                .eq(RunAnalysisReport::getReportType, report.getReportType())
                .eq(RunAnalysisReport::getReportChapter, report.getReportChapter())
                .eq(report.getObjectId() != null, RunAnalysisReport::getObjectId, report.getObjectId())
                .isNull(report.getObjectId() == null, RunAnalysisReport::getObjectId)
                .last("limit 1")
                .one();
    }

    public List<RunAnalysisReport> getList(RunAnalysisReportListParam param) {
        //不要data字段
        return this.lambdaQuery()
                .select(RunAnalysisReport::getId, RunAnalysisReport::getReportDate, RunAnalysisReport::getReportType,RunAnalysisReport::getReportStatus,
                        RunAnalysisReport::getOperator, RunAnalysisReport::getCreateTime, RunAnalysisReport::getUpdateTime)
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .ge(RunAnalysisReport::getReportDate, param.getStartDate())
                .le(RunAnalysisReport::getReportDate, param.getEndDate())
                .eq(RunAnalysisReport::getReportType, param.getReportType())
                //.orderByDesc(RunAnalysisReport::getReportDate)
                .list();
    }

    @Override
    public List<RunAnalysisReport> getChapterList(RunAnalysisReportQueryParam param) {
        return this.lambdaQuery()
                .select(RunAnalysisReport::getId,RunAnalysisReport::getReportChapter,
                        RunAnalysisReport::getChapterConfig)
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReport::getReportDate, param.getReportDate())
                .eq(RunAnalysisReport::getReportType, param.getReportType())
                .list();
    }

    @Override
    public void deleteByDay(RunAnalysisReportBuildParam report) {
        this.lambdaUpdate()
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReport::getReportDate, report.getReportDate())
                .eq(RunAnalysisReport::getReportType, report.getReportType())
                .remove();
    }

    @Override
    public List<RunAnalysisReport> getListByDayAndType(@NonNull RunAnalysisReport report) {
        return this.lambdaQuery()
                .select(RunAnalysisReport::getId, RunAnalysisReport::getReportChapter, RunAnalysisReport::getObjectId)
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReport::getReportDate, report.getReportDate())
                .eq(RunAnalysisReport::getReportType, report.getReportType())
                .list();
    }


    @Override
    public List<RunAnalysisReport> getChapterDataListLeDate(LocalDate date, RunAnalysisReportTypeEnum reportType, RunAnalysisReportChapterEnum chapter) {

        //获取报告id
        List<Long> runAnalysisReportIds = this.lambdaQuery()
                .select(RunAnalysisReport::getId)
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReport::getReportChapter, chapter)
                .eq(RunAnalysisReport::getReportType, reportType)
                .le(RunAnalysisReport::getReportDate, date)
                .list().stream().map(RunAnalysisReport::getId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(runAnalysisReportIds)){
            return Collections.emptyList();
        }
        //查询数据
        return this.lambdaQuery()
                .select(RunAnalysisReport::getId, RunAnalysisReport::getReportDate,RunAnalysisReport::getChapterData)
                .eq(RunAnalysisReport::getProjectId, ContextUtil.getProjectId())
                .in(RunAnalysisReport::getId, runAnalysisReportIds)
                .list();
    }


    @Override
    public String logTag() {
        return "运行分析-报告-仓库";
    }
}
