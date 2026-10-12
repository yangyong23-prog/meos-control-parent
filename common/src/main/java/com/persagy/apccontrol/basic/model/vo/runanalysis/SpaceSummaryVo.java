package com.persagy.apccontrol.basic.model.vo.runanalysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @description: 空间整体概述
 * @author: wxf
 * @create: 2025-10-16 14:51
 **/
@Data
public class SpaceSummaryVo {
    @Schema(description = "达标率")
    private String satisfyPercentage;

    @Schema(description = "标签统计")
    private Map<String, Integer> tagCount;

    @Schema(description = "空间分析")
    private Map<String, SpaceDetailVO> spaceAnalysis;
}