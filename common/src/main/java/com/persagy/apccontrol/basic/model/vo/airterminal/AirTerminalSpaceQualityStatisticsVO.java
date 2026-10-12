package com.persagy.apccontrol.basic.model.vo.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空调末端-空间品质统计VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-空间品质统计VO")
public class AirTerminalSpaceQualityStatisticsVO {

    @Schema(description = "品质-温度统计")
    private AirTerminalQualityStatisticsVO qualityTemperatureStatistics;

    @Schema(description = "品质-二氧化碳统计")
    private AirTerminalQualityStatisticsVO qualityCarbonDioxideStatistics;

    @Schema(description = "调度-温度统计")
    private AirTerminalSchedulingStatisticsVO schedulingTemperatureStatistics;

    @Schema(description = "调度-二氧化碳统计")
    private AirTerminalSchedulingStatisticsVO schedulingCarbonDioxideStatistics;

}
