package com.persagy.apccontrol.basic.structure.runanalysis.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "analysis_report", autoResultMap = true)
public class RunAnalysisReport extends BaseAssignIdDO implements Serializable {

    /**
     * 日期
     */
    private LocalDate reportDate;

    /**
     * 类型
     */
    private RunAnalysisReportTypeEnum reportType;

    /**
     * 章节
     */
    private RunAnalysisReportChapterEnum reportChapter;

    /**
     * 状态
     */
    private RunAnalysisReportStatusEnum reportStatus;

    /**
     * 数据
     */
    private String chapterData;

    /**
     * 配置
     */
    private String chapterConfig;

    /**
     * 对象id
     */
    private String objectId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 操作人
     */
    private String operator;

    @TableField(exist = false)
    private String remark;
}