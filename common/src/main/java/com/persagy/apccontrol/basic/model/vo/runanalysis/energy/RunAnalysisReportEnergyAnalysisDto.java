package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy.NEW_SCALE;


/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:47
　　 * @description: 能耗分析数据Vo
*/
@Data
@Schema(title = "能耗分析数据Vo")
public class RunAnalysisReportEnergyAnalysisDto extends RunAnalysisReportDataDto {

    @Schema(description ="树列表")
    private List<RunAnalysisReportEnergyAnalysisTreeDto> trees;

    @Schema(description ="能耗分析概述")
    private EnergyConsumptionOverview energyConsumptionOverview;

    @Schema(description ="总览-能耗分析",hidden = true)
    private transient RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisDto;

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/22 16:03
    　　 * @description: 能耗分析概述
     */
    @Data
    public static class EnergyConsumptionOverview {

        @Schema(description ="本日项目总用电量")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal projectTotalEnergyConsumption;

        @Schema(description ="项目月总用电量")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal projectMonthTotalEnergyConsumption;
    }

}