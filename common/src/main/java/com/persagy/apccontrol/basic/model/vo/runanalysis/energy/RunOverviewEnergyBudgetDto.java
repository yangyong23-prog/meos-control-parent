package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy.NEW_SCALE;


/**
 * @Author weiyizhong
 * @Date 2026/2/2 14:17
 * @Company yushu.biz
 * @Description: TODO
 */
@Schema(title = "概览-能耗预算")
@Data
public class RunOverviewEnergyBudgetDto {

    @Schema(description ="本日预算")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayBudget;

    @Schema(description ="本日日实际能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayRealEnergy;

    @Schema(description ="本日日能耗占预算比")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayEnergyConsumptionBudgetRatio;

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 10:29
    　　 * @description: 日能耗占比   公式：日能耗占预算比=日实际能耗/本日预算*100%，四舍五入保留一位小数
     */
    public BigDecimal dayEnergyConsumptionBudgetRatioCalculate() {
        //判空、为空、等于0的情况
        if(Objects.isNull(getDayRealEnergy()) || Objects.isNull(getDayBudget()) || getDayBudget().compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return getDayRealEnergy().divide(getDayBudget(), NEW_SCALE, RoundingMode.HALF_UP);
    }

}
