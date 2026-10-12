package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.time.MonthDay;
import java.util.LinkedHashSet;

/**
 * 空调季参数
 */
@Data
@FieldNameConstants
@Schema(title = "营业日历-空调季参数")
public class SeasonRangeParam implements BaseCheckParamProvider {

    @Schema(description = "夏季")
    private LinkedHashSet<Range<MonthDay>> airSummerRangeList;

    @Schema(description = "夏季高峰")
    private LinkedHashSet<Range<MonthDay>> airSummerPeakRangeList;

    @Schema(description = "冬季")
    private LinkedHashSet<Range<MonthDay>> airWinterRangeList;

    @Schema(description = "过渡季（春）")
    private LinkedHashSet<Range<MonthDay>> airSeasonSpringRangeList;

    @Schema(description = "过渡季（秋）")
    private LinkedHashSet<Range<MonthDay>> airSeasonAutumnRangeList;


    @JsonSetter(Fields.airSummerRangeList)
    public void deserializeBusinessTimeRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.airSummerRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.airSummerRangeList)
    public LinkedHashSet<String> serializeBusinessTimeRangeList() {
        return RangeUtils.toString(this.airSummerRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.airSummerPeakRangeList)
    public void deserializeAirSummerPeakRangeList(
            LinkedHashSet<String> airSummerPeakRangeList) {
        this.airSummerPeakRangeList = RangeUtils.instanceByArray(airSummerPeakRangeList,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.airSummerPeakRangeList)
    public LinkedHashSet<String> serializeAirSummerPeakRangeList() {
        return RangeUtils.toString(this.airSummerPeakRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.airWinterRangeList)
    public void deserializeAirWinterRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.airWinterRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.airWinterRangeList)
    public LinkedHashSet<String> serializeAirWinterRangeList() {
        return RangeUtils.toString(this.airWinterRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.airSeasonSpringRangeList)
    public void deserializeAirSeasonSpringRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.airSeasonSpringRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.airSeasonSpringRangeList)
    public LinkedHashSet<String> serializeAirSeasonSpringRangeList() {
        return RangeUtils.toString(this.airSeasonSpringRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.airSeasonAutumnRangeList)
    public void deserializeAirSeasonAutumnRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.airSeasonAutumnRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.airSeasonAutumnRangeList)
    public LinkedHashSet<String> serializeAirSeasonAutumnRangeList() {
        return RangeUtils.toString(this.airSeasonAutumnRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

}
