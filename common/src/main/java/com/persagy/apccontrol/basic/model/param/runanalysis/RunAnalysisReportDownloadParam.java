package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 运行分析报告下载参数
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@Schema(description = "运行分析报告下载参数")
public class RunAnalysisReportDownloadParam {

    /**
     * 报告日期
     */
    @NotNull(message = "报告日期不能为空")
    @Schema(description = "报告日期", example = "2026-03-13")
    private LocalDate reportDate;

    /**
     * 报告类型
     */
    @Schema(description = "报告类型", example = "DAILY")
    private RunAnalysisReportTypeEnum reportType;
}
