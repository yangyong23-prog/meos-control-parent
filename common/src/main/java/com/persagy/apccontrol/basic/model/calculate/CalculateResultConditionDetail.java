package com.persagy.apccontrol.basic.model.calculate;

import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价值计算--综合结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Data
public class CalculateResultConditionDetail {

    // ------------------------- 价值计算 每个时间点的依赖数据 -------------------

    /**
     * 季节 ACSeason-1：供冷季   ACSeason-2：供暖季    ACSeason-3：过渡季
     */
    private String season;

    /**
     * 当前时间点对应的营业时间的时间模式
     */
    private String businessModel;

    /**
     * 价值计算方向
     *
     *     VALUE_SELECT_LOWEST_CAST("value_select_lowest_cast", "费用最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_CARBON_EMISSIONS("value_select_Lowest_carbon_emissions", "碳排放最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_ENERGY("value_select_lowest_energy", "能耗最低", "项目纬度"),
     *     VALUE_SELECT_QUALITY_FIRST("value_select_quality_first", "品质优先", "项目纬度"),
     */
    private String calculateType;

    @Schema(description = "满足率配置")
    private List<ValueSelectConfigDto> fillRate;

    // ------------------------- 温度标准 -------------------------

    @Schema(description = "供冷季目标值")
    private BigDecimal coolingSeasonTargetValue;

    @Schema(description = "供冷季上限值")
    private BigDecimal coolingSeasonUpperLimitValue;

    @Schema(description = "供冷季蓄冷值")
    private BigDecimal coolingSeasonColdStorageValue;

    @Schema(description = "采暖季目标值")
    private BigDecimal heatingSeasonTargetValue;

    @Schema(description = "采暖季下限值")
    private BigDecimal heatingSeasonLowerLimitValue;

    @Schema(description = "采暖季蓄热值")
    private BigDecimal heatingSeasonHeatStorageValue;

    @Schema(description = "温度可连续调度时长")
    private BigDecimal temperatureContinuousScheduledHour;

    // ------------------------- CO2 标准 -------------------------

    @Schema(description = "二氧化碳目标值")
    private BigDecimal carbonDioxideTargetValue;

    @Schema(description = "二氧化碳上限值")
    private BigDecimal carbonDioxideUpperLimitValue;

    @Schema(description = "二氧化碳可连续调度时长")
    private BigDecimal carbonDioxideContinuousScheduledHour;

}
