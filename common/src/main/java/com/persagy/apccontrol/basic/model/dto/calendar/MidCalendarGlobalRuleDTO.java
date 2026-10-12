package com.persagy.apccontrol.basic.model.dto.calendar;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.BusinessTimeRangeParam;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

import static com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO.localDateRange;
import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 工作历全局规则DTO
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@FieldNameConstants
public class MidCalendarGlobalRuleDTO {

    // ------------------------- 基础字段 -------------------------

    /**
     * 工作历ID
     */
    private String calendarId;

    // ------------------------- 全局配置 -------------------------

    /**
     * 规则类型 -> 规则ID
     */
    @Nullable
    private Map<MidCalendarDateTagValueEnum, String> globalTypeAndRuleId;

    /**
     * 全局工作日
     */
    @Nullable
    private LinkedHashSet<DayOfWeek> globalWeekDays;

    /**
     * 全局休息日
     */
    @Nullable
    private LinkedHashSet<DayOfWeek> globalNonWeekDays;

    /**
     * 全局店庆日
     */
    @Nullable
    private LinkedHashSet<Range<MonthDay>> globalAnniversary;

    /**
     * 全局空调工况期范围
     */
    @Nullable
    private Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<Range<MonthDay>>> globalSeasonDateRangeMap;

    /**
     * 全局工作日时间范围
     */
    @Nullable
    private LinkedHashSet<Range<LocalTime>> globalBusinessTimeRangeListOnWeekDays;

    /**
     * 全局休息日开闭店时间范围
     */
    @Nullable
    private LinkedHashSet<Range<LocalTime>> globalBusinessTimeRangeListOnNonWeekDays;

    /**
     * 营业日历-星期号
     */
    private LinkedHashSet<BusinessTimeRangeParam.WeekNumberParam> weekNumberParamList;


    /**
     * 默认营业时间 规则
     */
    private LinkedHashSet<Range<LocalTime>> defaultBusinessTimeRangeList;

    /**
     * 作历日期标签类型枚举 DEFAULT全局规则,WORKDAY 公休日
     */
    private MidCalendarDateTagTypeEnum dateTagType;

    /**
     * 空调季期范围
     */
    @Nullable
    private Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> globalAirSeasonRangeDateRangeMap;


    @Schema(description = "节假日配置-通用配置")
    private LinkedHashSet<Range<MonthDay>> holidayGeneralConfiguration;

    @Schema(description = "节假日配置-特殊配置(注意剔除掉不含【今日及未来日】的日期段)")
    private LinkedHashSet<Range<LocalDate>> holidaySpecialConfiguration;

    @Schema(description = "特殊对象配置-特殊配置内容")
    private LinkedHashSet<BusinessTimeRangeParam.SpecialConfigurationContentParam> specialConfigurationContentParamList;

    // -------------------- GlobalAnniversary --------------------

    @JsonSetter(Fields.globalAnniversary)
    public void deserializeGlobalAnniversary(LinkedHashSet<String> globalAnniversary) {
        this.globalAnniversary = RangeUtils.instanceByArray(globalAnniversary, item ->
                MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(Fields.globalAnniversary)
    public LinkedHashSet<String> serializeGlobalAnniversary() {
        return RangeUtils.toString(globalAnniversary, item ->
                item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- globalSeasonDateRangeMap --------------------

    @JsonSetter(Fields.globalSeasonDateRangeMap)
    public void deserializeGlobalSeasonDateRangeMap(
            Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<String>> globalSeasonDateRangeMap) {
        if (Objects.nonNull(globalSeasonDateRangeMap)) {
            Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<Range<MonthDay>>> result = new HashMap<>(16);
            globalSeasonDateRangeMap.forEach((key, value) -> result.put(key, RangeUtils.instanceByArray(value,
                    item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new)));
            this.globalSeasonDateRangeMap = result;
        }
    }

    @JsonGetter(Fields.globalSeasonDateRangeMap)
    public Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<String>> serializeGlobalSeasonDateRangeMap() {
        if (Objects.isNull(globalSeasonDateRangeMap)) {
            return null;
        }
        Map<MidCalendarAcSeasonTypeEnum, LinkedHashSet<String>> result = new HashMap<>(16);
        globalSeasonDateRangeMap.forEach((key, value) -> result.put(key, RangeUtils.toString(value,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new)));
        return result;
    }

    // -------------------- globalBusinessTimeRangeListOnWeekDays --------------------

    @JsonSetter(Fields.globalBusinessTimeRangeListOnWeekDays)
    public void deserializeGlobalBusinessTimeRangeListOnWeekDays(
            LinkedHashSet<String> globalBusinessTimeRangeListOnWeekDays) {
        this.globalBusinessTimeRangeListOnWeekDays = RangeUtils.instanceByArray(
                globalBusinessTimeRangeListOnWeekDays,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.globalBusinessTimeRangeListOnWeekDays)
    public LinkedHashSet<String> serializeGlobalBusinessTimeRangeListOnWeekDays() {
        return RangeUtils.toString(this.globalBusinessTimeRangeListOnWeekDays,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- globalBusinessTimeRangeListOnNonWeekDays --------------------

    @JsonSetter(Fields.globalBusinessTimeRangeListOnNonWeekDays)
    public void deserializeGlobalBusinessTimeRangeListOnNonWeekDays(
            LinkedHashSet<String> globalBusinessTimeRangeListOnNonWeekDays) {
        this.globalBusinessTimeRangeListOnNonWeekDays = RangeUtils.instanceByArray(
                globalBusinessTimeRangeListOnNonWeekDays,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.globalBusinessTimeRangeListOnNonWeekDays)
    public LinkedHashSet<String> serializeGlobalBusinessTimeRangeListOnNonWeekDays() {
        return RangeUtils.toString(this.globalBusinessTimeRangeListOnNonWeekDays,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.defaultBusinessTimeRangeList)
    public void deserializeDefaultBusinessTimeRangeList(
            LinkedHashSet<String> defaultBusinessTimeRangeList) {
        this.defaultBusinessTimeRangeList = RangeUtils.instanceByArray(
                defaultBusinessTimeRangeList,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.defaultBusinessTimeRangeList)
    public LinkedHashSet<String> serializeDefaultBusinessTimeRangeList() {
        return RangeUtils.toString(this.defaultBusinessTimeRangeList,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(Fields.holidayGeneralConfiguration)
    public LinkedHashSet<String> serializeHolidayGeneralConfiguration() {
        return RangeUtils.toString(this.holidayGeneralConfiguration,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }
    @JsonSetter(Fields.holidayGeneralConfiguration)
    public void deserializeHolidayGeneralConfiguration(LinkedHashSet<String> holidayGeneralConfiguration) {
        this.holidayGeneralConfiguration = RangeUtils.instanceByArray(holidayGeneralConfiguration,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonGetter(DemandGlobalRuleVO.Fields.holidaySpecialConfiguration)
    public LinkedHashSet<String> serializeHolidaySpecialConfiguration() {
        if(CollectionUtils.isEmpty(this.holidaySpecialConfiguration)){
            return null;
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Range<LocalDate> localDateRange : this.holidaySpecialConfiguration) {
            result.add(String.join(DEFAULT_RANGE_SPLIT_REGEX, localDateRange.getMinimum().format(DatePattern.NORM_DATE_FORMATTER),localDateRange.getMaximum().format(DatePattern.NORM_DATE_FORMATTER)));
        }
        return result;
    }

    @JsonSetter(DemandGlobalRuleVO.Fields.holidaySpecialConfiguration)
    public void deserializeHolidaySpecialConfiguration(LinkedHashSet<String> holidaySpecialConfiguration) {
        if(CollectionUtils.isEmpty(holidaySpecialConfiguration)){
            return ;
        }
        LinkedHashSet<Range<LocalDate>> linkedHashSet = new LinkedHashSet<>();
        for (String source : holidaySpecialConfiguration) {
            String[] split = source.split(DEFAULT_RANGE_SPLIT_REGEX);
            // 1. 先把两端写成 LocalDate 变量，杜绝任何向上转型
            LocalDate start = LocalDate.parse(split[0], DatePattern.NORM_DATE_FORMATTER);
            LocalDate end   = LocalDate.parse(split[1], DatePattern.NORM_DATE_FORMATTER);
            // 2. 显式指定 <LocalDate>，且实参就是 LocalDate 变量
            linkedHashSet.add(localDateRange(start,end));
        }
        this.holidaySpecialConfiguration = linkedHashSet;
    }

}
