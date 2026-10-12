package com.persagy.apccontrol.basic.structure.runanalysis.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportChapterParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportListParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportQueryParam;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;

public interface RunAnalysisReportRepository extends IService<RunAnalysisReport> {
    RunAnalysisReport getByDayAndTypeAndChapter(@NonNull RunAnalysisReport report);
    List<RunAnalysisReport> getList(@NonNull RunAnalysisReportListParam param);
    List<RunAnalysisReport> getChapterList(@NonNull RunAnalysisReportQueryParam param);
    void deleteByDay(@NonNull RunAnalysisReportBuildParam report);

    /**
     * 根据日期和类型获取报告列表（用于ID复用）
     *
     * @param report 查询条件
     * @return 报告列表
     */
    List<RunAnalysisReport> getListByDayAndType(@NonNull RunAnalysisReport report);

    /**
     * 获取指定日期及之前所有章节数据
     *
     * @param date 日期
     * @return 章节数据列表
     */
    List<RunAnalysisReport> getChapterDataListLeDate(LocalDate date, RunAnalysisReportTypeEnum reportType, RunAnalysisReportChapterEnum chapter);
}
