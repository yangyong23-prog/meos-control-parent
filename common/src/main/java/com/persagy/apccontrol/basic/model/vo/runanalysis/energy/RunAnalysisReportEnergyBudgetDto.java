package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy.NEW_SCALE;


/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:47
　　 * @description: 能耗预算数据Vo
*/
@Data
@Schema(title = "能耗预算数据Vo")
public class RunAnalysisReportEnergyBudgetDto extends RunAnalysisReportDataDto {

    @Schema(description = "树列表")
    private List<RunAnalysisReportEnergyBudgetTreeDto> trees;

    @Schema(description = "能耗预算概述")
    private EnergyBudgetOverview energyBudgetOverview;

    @Schema(description ="运行概述-当日预算统计")
    private RunOverviewEnergyBudgetDto runOverviewEnergyBudgetDto;

    @Schema(description ="章节配置(配置中选中的管控制或目标值对应的字段编码)",hidden = true)
    private String benchmark;


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/22 16:12
    　　 * @description: 能耗预算概述
    */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Schema(title = "能耗预算概述")
    public static class EnergyBudgetOverview extends RunOverviewEnergyBudgetDto {

        @Schema(description ="本月累计预算")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal monthCumulativeTotalBudget;

        @Schema(description ="月累计实际能耗")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal monthCumulativeRealEnergyConsumption;

        @Schema(description ="月累计占比")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal monthCumulativeTotalBudgetRatio;

        @Schema(description ="月总预算")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal monthTotalBudget;

        @Schema(description ="已用月总预算比例")
        @NumberToScaleString(scale = NEW_SCALE)
        private BigDecimal monthTotalBudgetRatio;

        /**
         　　* @author weiyizhong
            * @Company yushu.biz
        　　 * @date 2026/1/27 10:49
        　　 * @description: 月累计占比
        */
        public BigDecimal monthCumulativeTotalBudgetRatioCalculate() {
            //判空、为空、等于0的情况
            if(Objects.isNull(getMonthCumulativeRealEnergyConsumption()) || Objects.isNull(getMonthCumulativeTotalBudget()) || getMonthCumulativeTotalBudget().compareTo(BigDecimal.ZERO) == 0){
                return null;
            }
            return getMonthCumulativeRealEnergyConsumption().divide(getMonthCumulativeTotalBudget(), NEW_SCALE, RoundingMode.HALF_UP);
        }

        /**
         　　* @author weiyizhong
            * @Company yushu.biz
        　　 * @date 2026/1/27 10:55
        　　 * @description: 已用月总预算比例  公式：本月已用预算比=本月累计实际能耗/月总预算*100%，四舍五入保留一位小数
        */
        public BigDecimal monthTotalBudgetRatioCalculate() {
            //判空、为空、等于0的情况
            if(Objects.isNull(getMonthCumulativeRealEnergyConsumption()) || Objects.isNull(getMonthTotalBudget()) || getMonthTotalBudget().compareTo(BigDecimal.ZERO) == 0){
                return null;
            }
            return getMonthCumulativeRealEnergyConsumption().divide(getMonthTotalBudget(), NEW_SCALE, RoundingMode.HALF_UP);
        }

    }



}