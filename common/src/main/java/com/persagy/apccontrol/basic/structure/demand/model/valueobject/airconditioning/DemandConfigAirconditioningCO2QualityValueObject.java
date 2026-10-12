package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空调需求配置--CO2品质对象
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningCO2QualityValueObject implements ValueObject {

    @Schema(description = "二氧化碳舒适区上限")
    private BigDecimal carbonDioxideLimitValue;

    @Schema(description = "二氧化碳过高上限")
    private BigDecimal carbonDioxideUpperLimitValue;

    @Schema(description = "二氧化碳日调度时长")
    private BigDecimal carbonDioxideScheduledHourInDay;

    @Schema(description = "二氧化碳可连续调度时长")
    private BigDecimal carbonDioxideContinuousScheduledHour;

    @Schema(description = "二氧化碳不可调度时段, 前闭后开")
    private List<DemandTimeIntervalValueObject> carbonDioxideUnscheduledTimeRange;

    /**
     * 获取旧版的二氧化碳目标值
     *
     * @return
     */
    public BigDecimal getOldCarbonDioxideTargetValue() {
        if (carbonDioxideLimitValue == null) {
            return null;
        }
        return carbonDioxideLimitValue.subtract(new BigDecimal("100"));
    }

    /**
     * 获取旧版的二氧化碳上限值
     *
     * @return
     */
    public BigDecimal getOldCarbonDioxideLimitValue() {
        if (carbonDioxideUpperLimitValue == null) {
            return null;
        }
        return carbonDioxideUpperLimitValue.subtract(new BigDecimal("100"));
    }

}
