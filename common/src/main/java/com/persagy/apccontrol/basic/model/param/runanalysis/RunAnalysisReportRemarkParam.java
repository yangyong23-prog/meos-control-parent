package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class RunAnalysisReportRemarkParam {

    @Schema(description = "报告id")
    @NotNull(message = "reportId can not be null")
    private Long reportId;

    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "自定义键")
    private String customKey;

    @Schema(description = "备注")
    @NotNull(message = "remark can not be null")
    private String remark;

    @Schema(description = "备注id")
    private Long id;
}
