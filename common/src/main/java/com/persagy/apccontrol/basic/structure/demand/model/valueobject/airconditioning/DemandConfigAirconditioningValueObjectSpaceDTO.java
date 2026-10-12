package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.Range;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 空调㤹配置值对象
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
public class DemandConfigAirconditioningValueObjectSpaceDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "实例id")
    private String instanceId;

    @Schema(description = "是否屏蔽")
    private Boolean shielded;

    @Schema(description = "是否是自定义配置")
    private Boolean customConfig;

    @Schema(description = "是否参与调度（同globalConfig、environmentQuality 中得配置，为了取值方便）")
    private Boolean isParticipateInScheduling;

    @Schema(description = "空调季")
    private MidCalendarAirSeasonTypeEnum airSeason;

    @Schema(description = "空调工况，原空调季")
    private MidCalendarAcSeasonTypeEnum season;

    @Schema(description = "系统ID（为空不参与价值计算，仅限冷站、冷热站、热站）")
    private String systemId;

    @Schema(description = "是否存在温度孪生点位")
    private boolean tdb;

    @Schema(description = "营业时间")
    private List<Range<LocalTime>> businessTimeRangeList;

    @Schema(description = "响应调节时间")
    private List<LocalTime> responseRegulationTimeList;

    @JsonSetter("businessTimeRangeList")
    public void deserializeBusinessTimeRangeListOnWeekdays(LinkedHashSet<String> businessTimeRangeList) {
        this.businessTimeRangeList = RangeUtils.instanceByArray(businessTimeRangeList,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), ArrayList::new);
    }

    @Schema(description = "当前实例配置详情")
    private DemandConfigAirconditioningValueObject globalConfig;

}
