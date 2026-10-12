package com.persagy.apccontrol.basic.structure.runanalysis.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportListParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportQueryParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportRemarkParam;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import org.springframework.lang.NonNull;

import java.util.List;

public interface RunAnalysisReportRemarkRepository extends IService<RunAnalysisReportRemark> {
    List<RunAnalysisReportRemark> getRemarkList(@NonNull List<Long> reportIdList);

    /**
     * 根据报告ID和对象ID查询备注记录
     */
    RunAnalysisReportRemark getByReport(RunAnalysisReportRemarkParam remark);

}
