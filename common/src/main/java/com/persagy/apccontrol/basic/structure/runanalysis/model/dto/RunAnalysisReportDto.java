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
public class RunAnalysisReportDto {
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
     * 创建时间
     */
    public LocalDate createTime;

    /**
     * 更新时间
     */
    public LocalDate updateTime;

    /**
     * 操作人
     */
    public String operator;
}