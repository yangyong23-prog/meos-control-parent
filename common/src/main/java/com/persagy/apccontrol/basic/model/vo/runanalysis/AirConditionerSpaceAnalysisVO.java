package com.persagy.apccontrol.basic.model.vo.runanalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * 空调空间分析
 */
@Data
@FieldNameConstants
public class AirConditionerSpaceAnalysisVO {

    @Schema(description = "项目达标率")
    private String satisfyPercentage;

    /**
     * 空间分析数据
     */
    @Schema(description = "空间分析数据")
    private List<SpacesAnalysis> spacesAnalysisList;
}

