package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import cn.hutool.core.util.ObjectUtil;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy.NEW_SCALE;


/**
 * @Author weiyizhong
 * @Date 2026/1/30 14:03
 * @Company yushu.biz
 * @Description: 总览-能耗分析
 */
@Data
@Schema(title = "能耗分析数据Vo")
public class RunOverviewEnergyAnalysisDto {

    @Schema(description ="上周相似日平均能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal weekLastSimilarDayAverageEnergyConsumption;

    @Schema(description ="项目总用电量")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal projectTotalEnergyConsumption;

    @Schema(description ="项目月总用电量")
    @NumberToScaleString(scale = NEW_SCALE)
    private transient BigDecimal projectMonthTotalEnergyConsumption;

    @Schema(description ="环比")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal chainRatio;


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/30 16:20
    　　 * @description: 环比=（项目总用电-上周相似日平均能耗）/上周相似日平均能耗 *100%
     */
    public BigDecimal chainRatioCalculate() {
        if(ObjectUtil.isEmpty(getProjectTotalEnergyConsumption()) || ObjectUtil.isEmpty(getWeekLastSimilarDayAverageEnergyConsumption()) || getWeekLastSimilarDayAverageEnergyConsumption().compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        BigDecimal subtract = getProjectTotalEnergyConsumption().subtract(getWeekLastSimilarDayAverageEnergyConsumption());
        return  subtract.divide(getWeekLastSimilarDayAverageEnergyConsumption(), NEW_SCALE, RoundingMode.HALF_UP);
    }
}
