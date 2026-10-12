package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class RunAnalysisReportQueryParam {

    @Schema(description = "报告日期")
    @NotNull(message = "date can not be null")
    private LocalDate reportDate;

    @Schema(description = "报告类型")
    @NotNull(message = "reportType can not be null")
    private RunAnalysisReportTypeEnum reportType;

    @Schema(description = "报告章节")
    private List<RunAnalysisReportChapterParam> reportChapters;
}
