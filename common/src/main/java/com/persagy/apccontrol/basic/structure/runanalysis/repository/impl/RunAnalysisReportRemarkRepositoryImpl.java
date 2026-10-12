package com.persagy.apccontrol.basic.structure.runanalysis.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportListParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportQueryParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportRemarkParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRemarkRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.mapper.RunAnalysisReportMapper;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.mapper.RunAnalysisReportRemarkMapper;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class RunAnalysisReportRemarkRepositoryImpl extends ServiceImpl<RunAnalysisReportRemarkMapper, RunAnalysisReportRemark> implements RunAnalysisReportRemarkRepository, BizComponent<RunAnalysisReportRemarkRepository> {
    @Override
    public String logTag() {
        return "运行分析-报告备注-仓库";
    }

    @Override
    public List<RunAnalysisReportRemark> getRemarkList(@NonNull List<Long> reportIdList) {
        return this.lambdaQuery()
                .eq(RunAnalysisReportRemark::getProjectId, ContextUtil.getProjectId())
                .in(RunAnalysisReportRemark::getReportId, reportIdList)
                .list();
    }

    @Override
    public RunAnalysisReportRemark getByReport(RunAnalysisReportRemarkParam remark) {
        return this.lambdaQuery()
                .eq(RunAnalysisReportRemark::getProjectId, ContextUtil.getProjectId())
                .eq(RunAnalysisReportRemark::getReportId, remark.getReportId())
                .eq(remark.getObjectId() != null, RunAnalysisReportRemark::getObjectId, remark.getObjectId())
                .last("limit 1")
                .one();
    }
}
