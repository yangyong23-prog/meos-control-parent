package com.persagy.apccontrol.basic.model.vo.external.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TreeMap;

/**
 * 需求空间查询参数
 *
 * @author zhangqiankun
 * @since 5.241102.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求空间配置时序数据查询参数")
public class DemandSpaceSequenceExternalVO {

    @Schema(description = "当前日期")
    private LocalDate currentDate;

    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "时序数据")
    private TreeMap<LocalTime, SpaceSequenceData> sequenceData;

    @Getter
    @Setter
    @ToString
    public static class SpaceSequenceData {

        @Schema(description = "是否有需求")
        private Boolean enable;

        @Schema(description = "温度目标值")
        private BigDecimal tempSeasonTargetValue;

        @Schema(description = "温度上限值")
        private BigDecimal tempSeasonUpperValue;

        @Schema(description = "温度下限值")
        private BigDecimal tempSeasonLowerValue;

        @Schema(description = "CO2目标值")
        private BigDecimal carbonDioxideTargetValue;

        @Schema(description = "CO2上限值")
        private BigDecimal carbonDioxideLimitValue;

        public SpaceSequenceData(Boolean enable, BigDecimal tempSeasonTargetValue, BigDecimal tempSeasonUpperValue, BigDecimal tempSeasonLowerValue, BigDecimal carbonDioxideTargetValue, BigDecimal carbonDioxideLimitValue) {
            this.enable = enable;
            this.tempSeasonTargetValue = tempSeasonTargetValue;
            this.tempSeasonUpperValue = tempSeasonUpperValue;
            this.tempSeasonLowerValue = tempSeasonLowerValue;
            this.carbonDioxideTargetValue = carbonDioxideTargetValue;
            this.carbonDioxideLimitValue = carbonDioxideLimitValue;
        }

    }

}
