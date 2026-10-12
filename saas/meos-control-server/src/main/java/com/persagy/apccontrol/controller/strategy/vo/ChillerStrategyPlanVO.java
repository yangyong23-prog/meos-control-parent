package com.persagy.apccontrol.controller.strategy.vo;

import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 冷机策略计划VO
 *
 * @since v5.6.0
 */
@Data
@Schema(title = "冷机固定策略计划VO")
public class ChillerStrategyPlanVO {

    @Schema(description = "策略ID")
    private String strategyId;

    @Schema(description = "策略计划ID")
    private Long strategyPlanId;

    @Schema(description = "策略时间")
    protected LocalTime strategyTime;

    @Schema(description = "策略日期")
    protected LocalDate date;

    @Schema(description = "运行状态")
    private RunStatusEnum runStatus;

    @Schema(description = "大机台数")
    private Integer chillerLNum;

    @Schema(description = "小机台数")
    private Integer chillerSNum;

    @Schema(description = "冷机出水温度设定值")
    @NumberToScaleString
    private BigDecimal chillWaterOutTempSet;

}
