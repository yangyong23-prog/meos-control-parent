package com.persagy.apccontrol.basic.model.vo.demand.openingcalendar;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.BusinessTimeRangeParam;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.SeasonRangeParam;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashSet;

import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 需求管理-营业日历-全局规则VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@FieldNameConstants
@Schema(title = "需求管理-营业日历-全局规则VO")
public class DemandGlobalRuleVO {

    @Schema(description = "工作日")
    private LinkedHashSet<DayOfWeek> weekdays;

    @Schema(description = "供冷季日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> coolingSeasonDateRangeList;

    @Schema(description = "采暖季开日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> heatingSeasonDateRangeList;

    @Schema(description = "不供热不供暖开日期, 前闭后闭")
    private LinkedHashSet<Range<MonthDay>> notCoolingAndHeatingSeasonDateRangeList;

    @Schema(description = "全局店庆日")
    private LinkedHashSet<Range<MonthDay>> anniversary;

    @Schema(description = "营业时间")
    private BusinessTimeRangeParam businessTimeRange;

    @Schema(description = "空调季参数")
    private SeasonRangeParam seasonRangeParam;

    @Schema(description = "节假日配置-通用配置")
    private LinkedHashSet<Range<MonthDay>> holidayGeneralConfiguration;

    @Schema(description = "节假日配置-特殊配置(注意剔除掉不含【今日及未来日】的日期段)")
    private LinkedHashSet<Range<LocalDate>> holidaySpecialConfiguration;

    // -------------------- CoolingSeasonDateRangeList --------------------

    @JsonGetter(Fields.coolingSeasonDateRangeList)
    public LinkedHashSet<String> serializeCoolingSeasonDateRangeList() {
        return RangeUtils.toString(this.coolingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.coolingSeasonDateRangeList)
    public void deserializeCoolingSeasonDateRangeList(LinkedHashSet<String> coolingSeasonDateRangeList) {
        this.coolingSeasonDateRangeList = RangeUtils.instanceByArray(coolingSeasonDateRangeList,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

    // -------------------- HeatingSeasonDateRangeList --------------------

    @JsonGetter(Fields.heatingSeasonDateRangeList)
    public LinkedHashSet<String> serializeHeatingSeasonDateRangeList() {
        return RangeUtils.toString(this.heatingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }




    @JsonSetter(Fields.heatingSeasonDateRangeList)
    public void deserializeHeatingSeasonDateRangeList(LinkedHashSet<String> heatingSeasonDateRangeList) {
        this.heatingSeasonDateRangeList = RangeUtils.instanceByArray(heatingSeasonDateRangeList,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
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

    @JsonGetter(Fields.holidaySpecialConfiguration)
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

    @JsonSetter(Fields.holidaySpecialConfiguration)
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

    public static Range<LocalDate> localDateRange(LocalDate start, LocalDate end) {
        // 先按 ChronoLocalDate 建实例，再强转
        return (Range) Range.between(start, end);
    }


    // -------------------- Anniversaries --------------------

    @JsonSetter(Fields.anniversary)
    public void deserializeGlobalAnniversary(LinkedHashSet<String> globalAnniversaries) {
        this.anniversary = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.anniversary)
    public LinkedHashSet<String> serializeGlobalAnniversaries() {
        return RangeUtils.toString(this.anniversary,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }


    @JsonSetter(Fields.notCoolingAndHeatingSeasonDateRangeList)
    public void deserializeNotCoolingAndHeatingSeasonDateRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.notCoolingAndHeatingSeasonDateRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> MonthDay.parse(item, DatePattern.NORM_MONTHDAY_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.notCoolingAndHeatingSeasonDateRangeList)
    public LinkedHashSet<String> serializeNotCoolingAndHeatingSeasonDateRangeList() {
        return RangeUtils.toString(this.notCoolingAndHeatingSeasonDateRangeList,
                item -> item.format(DatePattern.NORM_MONTHDAY_FORMATTER), LinkedHashSet::new);
    }

}
