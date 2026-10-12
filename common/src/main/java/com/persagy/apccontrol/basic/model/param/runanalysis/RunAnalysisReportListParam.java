package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class RunAnalysisReportListParam {

    @Schema(description = "报告开始日期")
    @NotNull(message = "date can not be null")
    private LocalDate startDate;

    @Schema(description = "报告结束日期")
    @NotNull(message = "endDate can not be null")
    private LocalDate endDate;

    @Schema(description = "报告类型")
    @NotNull(message = "reportType can not be null")
    private RunAnalysisReportTypeEnum reportType;

    @Schema(description = "页码")
    private Integer pageNo;

    @Schema(description = "每页大小")
    private Integer pageSize;
}
