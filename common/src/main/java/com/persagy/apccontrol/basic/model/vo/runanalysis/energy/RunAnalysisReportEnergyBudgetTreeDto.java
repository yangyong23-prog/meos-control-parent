package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
public class RunAnalysisReportEnergyBudgetTreeDto {

    @Schema(description ="能耗分项id")
    private String itemId;

    @Schema(description ="能耗分项名称")
    private String itemName;

    @Schema(description ="本日预算")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayBudget;

    @Schema(description ="日实际能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayRealEnergy;

    @Schema(description ="日能耗占预算比")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayEnergyConsumptionBudgetRatio;

    @Schema(description ="日实际能耗与预算差值")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayRealEnergyDifference;

    @Schema(description ="月累计实际能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal monthCumulativeRealEnergyConsumption;

    @Schema(description ="月总预算")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal monthTotalBudget;

    @Schema(description ="已用月总预算比例")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal monthTotalBudgetRatio;

    @Schema(description ="子节点")
    private List<RunAnalysisReportEnergyBudgetTreeDto> children;


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 16:37
    　　 * @description: 计算指标：日实际能耗与预算差值     计算公式：日能耗占预算比=日实际能耗/本日预算*100%，四舍五入保留一位小数
    */
    public BigDecimal dayEnergyConsumptionBudgetRatioCalculate() {
        //判空、为空、等于0的情况
        if(Objects.isNull(this.dayRealEnergy) || Objects.isNull(this.dayBudget) || this.dayBudget.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return this.dayRealEnergy.divide(this.dayBudget, NEW_SCALE, RoundingMode.HALF_UP);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 17:21
    　　 * @description: 【日实际能耗与预算差值】的指标，日实际能耗与预算差值=日实际能耗-本日预算
    */
    public BigDecimal dayRealEnergyDifferenceCalculate() {
        //判空、为空
        if(Objects.isNull(getDayRealEnergy()) || Objects.isNull(getDayBudget())){
            return null;
        }
        return getDayRealEnergy().subtract(getDayBudget()).setScale(NEW_SCALE, RoundingMode.HALF_UP);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 17:44
    　　 * @description: 已用月总预算比例
    */
    public BigDecimal monthTotalBudgetRatioCalculate() {
        //判空、为空、等于0的情况
        if(Objects.isNull(getMonthCumulativeRealEnergyConsumption()) || Objects.isNull(getMonthTotalBudget()) || getMonthTotalBudget().compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return getMonthCumulativeRealEnergyConsumption().divide(getMonthTotalBudget(), NEW_SCALE, RoundingMode.HALF_UP);
    }
}