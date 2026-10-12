package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 空调需求配置--温度品质对象--季节配置
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningTdbSeasonValueObject implements ValueObject {

    @Schema(description = "单季-空调季")
    private MidCalendarAirSeasonTypeEnum airSeason;

    @Schema(description = "单季-舒适区下限值")
    private BigDecimal lowerComfortSeasonLimitValue;

    @Schema(description = "单季-舒适区上限值")
    private BigDecimal upperComfortSeasonLimitValue;

    @Schema(description = "单季-过冷门限")
    private BigDecimal underCoolingSeasonLimitValue;

    @Schema(description = "单季-过热门限")
    private BigDecimal overHeatingSeasonLimitValue;

    /**
     * 获取旧版的温度目标值
     *
     * @return
     */
    public BigDecimal getOldTempSeasonTargetValue() {
        if (upperComfortSeasonLimitValue == null || lowerComfortSeasonLimitValue == null) {
            return null;
        }
        BigDecimal decimal = upperComfortSeasonLimitValue.add(lowerComfortSeasonLimitValue);
        return decimal.divide(new BigDecimal("2"), 1, RoundingMode.HALF_UP);
    }

    /**
     * 获取旧版的温度上限值
     *
     * @return
     */
    public BigDecimal getOldTempSeasonUpperValue() {
        if (overHeatingSeasonLimitValue == null) {
            return null;
        }
        return overHeatingSeasonLimitValue.subtract(new BigDecimal("0.5"));
    }

    /**
     * 获取旧版的温度下限值
     *
     * @return
     */
    public BigDecimal getOldTempSeasonLowerValue() {
        if (underCoolingSeasonLimitValue == null) {
            return null;
        }
        return underCoolingSeasonLimitValue.add(new BigDecimal("0.5"));
    }

}
