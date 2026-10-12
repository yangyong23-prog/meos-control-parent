package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-01-19 17:37
 **/
@Data
public class RunAnalysisReportRemarkVO {

    private String id;

    @Schema(description = "报告id")
    private String reportId;

    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "自定义键")
    private String customKey;

    @Schema(description = "备注")
    private String remark;
}