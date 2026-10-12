package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空调需求配置--温度品质对象
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningTdbQualityValueObject implements ValueObject {

    @Schema(description = "季节标准品质配置（需要按照夏季顺序返回）")
    private List<DemandConfigAirconditioningTdbSeasonValueObject> seasonQuality;

    @Schema(description = "温度日调度时长")
    private BigDecimal temperatureScheduledHourInDay;

    @Schema(description = "温度可连续调度时长")
    private BigDecimal temperatureContinuousScheduledHour;

    @Schema(description = "温度不可调度时段, 前闭后开")
    private List<DemandTimeIntervalValueObject> temperatureUnscheduledTimeRange;

    /**
     * 获取当前空调季下的温度区间配置数据
     */
    public DemandConfigAirconditioningTdbSeasonValueObject getCurrentSeasonQuality(MidCalendarAirSeasonTypeEnum airSeason) {
        if (airSeason != null && CollectionUtil.isNotEmpty(seasonQuality)) {
            for (DemandConfigAirconditioningTdbSeasonValueObject seasonValueObject : seasonQuality) {
                if (airSeason.equals(seasonValueObject.getAirSeason())) {
                    return seasonValueObject;
                }
            }
        }
        return null;
    }

}
