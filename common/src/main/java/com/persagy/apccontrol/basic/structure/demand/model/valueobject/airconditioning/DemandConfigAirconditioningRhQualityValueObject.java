package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调需求配置-湿度品质对象
 *
 * @author JiYinchuan
 * @since 7.251201.0
 */
@Data
public class DemandConfigAirconditioningRhQualityValueObject implements ValueObject {

    @Schema(description = "季节标准品质配置（需要按照夏季顺序返回）")
    private List<DemandConfigAirconditioningRhSeasonValueObject> seasonQuality;

    /**
     * 获取当前空调季下的湿度区间配置数据
     *
     * @param airSeason 空调季
     * @return 湿度区间配置数据
     * @since 7.260401.0
     */
    public DemandConfigAirconditioningRhSeasonValueObject getCurrentSeasonQuality(MidCalendarAirSeasonTypeEnum airSeason) {
        if (airSeason != null && CollectionUtil.isNotEmpty(seasonQuality)) {
            for (DemandConfigAirconditioningRhSeasonValueObject seasonValueObject : seasonQuality) {
                if (airSeason.equals(seasonValueObject.getAirSeason())) {
                    return seasonValueObject;
                }
            }
        }
        return null;
    }
}
