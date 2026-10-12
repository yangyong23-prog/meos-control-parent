package com.persagy.apccontrol.basic.structure.runanalysis.model.dto;

import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
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
public class RunAnalysisReportBuildDto {
    /**
     * 日期
     */
    public LocalDate day;

    /**
     * 类型
     */
    public RunAnalysisReportTypeEnum type;

    /**
     * 章节
     */
    public RunAnalysisReportChapterEnum chapter;

    /**
     * 存储数据
     */
    public RunAnalysisReportDataDto runAnalysisReportDataDto;

    /**
     * 备注
     */
    public String remark;
}