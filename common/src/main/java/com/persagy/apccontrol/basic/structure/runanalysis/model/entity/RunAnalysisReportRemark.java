package com.persagy.apccontrol.basic.structure.runanalysis.model.entity;

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
@TableName(value = "analysis_report_remark", autoResultMap = true)
public class RunAnalysisReportRemark extends BaseAssignIdDO implements Serializable {

    /**
     * 报告ID
     */
    protected Long reportId;
    /**
     * 对象id
     */
    private String objectId;

    /**
     * 键
     */
    private String customKey;
    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 备注
     */
    private String remark;
}