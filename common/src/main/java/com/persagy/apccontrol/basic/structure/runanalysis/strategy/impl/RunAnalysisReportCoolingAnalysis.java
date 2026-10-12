package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 冷量分析策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportCoolingAnalysis extends AbstractRunAnalysisReportStrategy<RunAnalysisReportDataDto> {

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_ANALYSIS;
    }

    @Override
    public String logTag() {
        return "运行分析报告-冷量分析";
    }
}
