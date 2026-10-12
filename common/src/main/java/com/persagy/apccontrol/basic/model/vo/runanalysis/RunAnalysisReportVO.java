package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-19 17:37
 **/
@Data
public class RunAnalysisReportVO {

    private String id;
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
    private JSONObject chapterData;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private transient JSONObject chapterConfig;
}