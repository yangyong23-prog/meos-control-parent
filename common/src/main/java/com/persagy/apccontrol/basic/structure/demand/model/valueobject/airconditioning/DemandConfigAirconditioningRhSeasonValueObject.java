package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 空调需求配置-湿度品质对象-季节配置
 *
 * @author JiYinchuan
 * @since 7.251201.0
 */
@Data
public class DemandConfigAirconditioningRhSeasonValueObject implements ValueObject {

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

}
