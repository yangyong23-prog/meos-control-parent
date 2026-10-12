package com.persagy.apccontrol.basic.model.calculate;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价值计算--条件数据集
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Data
public class CalculateCondition {

    // ------------------------- 价值计算对配置解析后的临时存储扩展字段数据 -------------------

    @Schema(description = "满足率配置")
    private List<ValueSelectConfigDto> fillRate;

    @Schema(description = "空调季")
    private MidCalendarAirSeasonTypeEnum airSeason;

    @Schema(description = "蓄冷、蓄热类型，0-蓄冷，1-蓄热")
    private String tag;

    @Schema(description = "提前开机分钟数")
    private Integer startupMinutes;

    @Schema(description = "提前蓄冷、蓄热分钟数")
    private Integer storageMinutes;

    @Schema(description = "提前开机壳配置")
    private Integer openShellConfig;

    @Schema(description = "提前关机壳配置")
    private Integer closeShellConfig;

    @Schema(description = "当前计算的时间点--开关机时间点")
    private String workTime;

    @Schema(description = "提前关机分钟数")
    private Integer advanceMinutes;

    @Schema(description = "蓄冷/蓄热标签")
    private Integer storageTag;

    @Schema(description = "供冷/供热成本及难度系数")
    private JSONObject difficultyExpense;

    @Schema(description = "温度满足率")
    private BigDecimal tempRate;

    @Schema(description = "CO2满足率")
    private BigDecimal carbonRate;

    @Schema(description = "总成本")
    private BigDecimal sumExpense;

    @Schema(description = "单空间面积")
    private BigDecimal spaceArea;

    @Schema(description = "总面积")
    private BigDecimal sumArea;

    @Schema(description = "CO2调度情况")
    private Boolean carbonScheduling;

    @Schema(description = "温度调度情况")
    private Boolean tdbScheduling;

    @Schema(description = "是否被屏蔽")
    private Boolean isShielded;

    @Schema(description = "是否算法自动计算")
    private Boolean isAlgorithmCompute;

    @Schema(description = "是否允许提前关机")
    private Boolean isAllowEarlyShutdown;

    @Schema(description = "是否允许响应调节")
    private Boolean isAllowResponseRegulation;

    @Schema(description = "是否参与调度")
    private Boolean isParticipateInScheduling;

    @Schema(description = "工作日营业时间, 前闭后闭")
    private Object businessTimeRangeList;

    @Schema(description = "空调需求时间, 前闭后闭")
    private Object airConditioningDemandTimeRangeList;

    // ------------------------- 温度标准 -------------------------

    @Schema(description = "单季-舒适区下限值")
    private BigDecimal lowerComfortSeasonLimitValue;

    @Schema(description = "单季-舒适区上限值")
    private BigDecimal upperComfortSeasonLimitValue;

    @Schema(description = "单季-过冷门限")
    private BigDecimal underCoolingSeasonLimitValue;

    @Schema(description = "单季-过热门限")
    private BigDecimal overHeatingSeasonLimitValue;

    @Schema(description = "温度日调度时长")
    private BigDecimal temperatureScheduledHourInDay;

    @Schema(description = "温度可连续调度时长")
    private BigDecimal temperatureContinuousScheduledHour;

    @Schema(description = "温度不可调度时段, 前闭后闭")
    private Object temperatureUnscheduledTimeRange;


    // ------------------------- CO2 标准 -------------------------

    @Schema(description = "二氧化碳舒适区上限")
    private BigDecimal carbonDioxideLimitValue;

    @Schema(description = "二氧化碳过高上限")
    private BigDecimal carbonDioxideUpperLimitValue;

    @Schema(description = "二氧化碳日调度时长")
    private BigDecimal carbonDioxideScheduledHourInDay;

    @Schema(description = "二氧化碳可连续调度时长")
    private BigDecimal carbonDioxideContinuousScheduledHour;

    @Schema(description = "二氧化碳不可调度时段, 前闭后闭")
    private Object carbonDioxideUnscheduledTimeRange;

}
