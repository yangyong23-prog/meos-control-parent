package com.persagy.apccontrol.basic.structure.runanalysis.model.param;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import lombok.Data;

import java.time.LocalDate;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-22 16:23
 **/
@Data
public class RunAnalysisReportBuildParam {
    /**
     * 日期
     */
    private LocalDate day;

    /**
     * 类型
     */
    private RunAnalysisReportTypeEnum type;
}