package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.annotations.ApiModelProperty;
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
　　 * @description: 能耗数据树Vo
*/
@Data
@Schema(title = "能耗数据树Vo")
public class RunAnalysisReportEnergyAnalysisTreeDto {

    @Schema(description = "能耗分项id",hidden = true)
    private String id;

    @Schema(description ="能耗分项编码")
    private String itemCode;

    @Schema(description ="能耗分项名称")
    private String itemName;

    @Schema(description ="日能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayEnergyConsumption;

    @Schema(description = "项目总用电量",hidden = true)
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal projectTotalEnergyConsumption;

    @Schema(description ="日能耗占项目用电比")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal dayEnergyConsumptionProjectRatio;

    @Schema(description ="上周相似日平均能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal weekLastSimilarDayAverageEnergyConsumption;

    @Schema(description ="环比")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal chainRatio;

    @Schema(description ="月累计能耗")
    @NumberToScaleString(scale = NEW_SCALE)
    private BigDecimal monthCumulativeEnergyConsumption;

    @Schema(description ="实例id",hidden = true)
    private String rwdId;

    @Schema(description ="子节点")
    private List<RunAnalysisReportEnergyAnalysisTreeDto> children;


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/30 16:20
    　　 * @description: 环比=（日能耗-上周相似日平均能耗）/上周相似日平均能耗 *100%
     */
    public BigDecimal chainRatioCalculate() {
        if(ObjectUtil.isEmpty(getDayEnergyConsumption()) || ObjectUtil.isEmpty(getWeekLastSimilarDayAverageEnergyConsumption()) || getWeekLastSimilarDayAverageEnergyConsumption().compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        BigDecimal subtract = getDayEnergyConsumption().subtract(getWeekLastSimilarDayAverageEnergyConsumption());
        return  subtract.divide(getWeekLastSimilarDayAverageEnergyConsumption(), NEW_SCALE, RoundingMode.HALF_UP);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/2 10:41
    　　 * @description: 日能耗占项目用电比
    */

    public BigDecimal dayEnergyConsumptionProjectRatioCalc() {
        if(ObjectUtil.isEmpty(getDayEnergyConsumption()) ||  ObjectUtil.isEmpty(getProjectTotalEnergyConsumption()) || getProjectTotalEnergyConsumption().compareTo(BigDecimal.ZERO) == 0){
            return null;
        }
        return getDayEnergyConsumption().divide(getProjectTotalEnergyConsumption(), NEW_SCALE, RoundingMode.HALF_UP);
    }

}