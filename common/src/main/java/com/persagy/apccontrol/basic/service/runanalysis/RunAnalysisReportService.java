package com.persagy.apccontrol.basic.service.runanalysis;

import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportChapterVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportRemarkVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface RunAnalysisReportService {
    /**
     * 创建报告
     */
    Boolean build(RunAnalysisReportBuildParam param);
    Boolean buildAuto();

    /**
     * 获取报告列表
     */
    List<RunAnalysisReportVO> getList(RunAnalysisReportListParam param);

    /**
     * 获取报告章节树
     */
    List<RunAnalysisReportChapterVO> getChapterTree(RunAnalysisReportQueryParam param);

    /**
     * 获取报告详情
     */
    List<RunAnalysisReportVO> getDetail(List<Long> ids);

    Boolean saveRemark(List<RunAnalysisReportRemarkParam> param);

    List<RunAnalysisReportRemarkVO> listRemark(List<Long> reportIdList);

    /**
     * 下载运行分析报告CSV
     *
     * @param param 报告参数
     * @return CSV内容
     */
    void downloadReport(RunAnalysisReportDownloadParam param, HttpServletResponse response) throws Exception;
}
