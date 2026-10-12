package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class RunAnalysisReportBuildParam {

    @Schema(description = "报告日期")
    @NotNull(message = "date can not be null")
    private LocalDate reportDate;

    @Schema(description = "报告类型")
    @NotNull(message = "reportType can not be null")
    private RunAnalysisReportTypeEnum reportType;

    @Schema(description = "报告状态")
    private RunAnalysisReportStatusEnum reportStatus;

    @Schema(description = "操作人")
    @NotNull(message = "operator can not be null")
    private String operator;

    @Schema(description = "对象id")
    private String objectId;

    private List<RunAnalysisReportChapterEnum> chapterList;
}
