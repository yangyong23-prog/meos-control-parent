package com.persagy.apccontrol.basic.structure.runanalysis.service;

import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportChapterDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RunAnalysisReportDomainService extends BizComponent<RunAnalysisReportDomainService> {
    /**
     * 手动创建报告
     */
    Boolean build(RunAnalysisReportBuildParam param);

    /**
     * 自动创建报告
     */
    Boolean buildAuto();

    /**
     * 获取报告列表
     */
    List<RunAnalysisReport> getList(RunAnalysisReportListParam param);
    List<RunAnalysisReport> getList(List<Long> idList);

    /**
     * 获取报告章节目录
     */
    List<RunAnalysisReportChapterDto> getChapterList(RunAnalysisReportQueryParam param);

    /**
     * 获取报告详情
     */
    List<RunAnalysisReport> getDetail(RunAnalysisReportQueryParam param);

    List<RunAnalysisReport> getDetail(List<Long> idList);


    /**
     * 保存报告备注
     */
    Boolean saveRemark(List<RunAnalysisReportRemarkParam> param);

    List<RunAnalysisReportRemark> getRemarkList(List<Long> reportIdList);

    /**
     * 下载运行分析报告CSV
     *
     * @param param 报告日期
     * @return CSV内容
     */
    void downloadReport(RunAnalysisReportDownloadParam param, HttpServletResponse response) throws Exception;
}
