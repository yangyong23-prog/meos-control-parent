package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Objects;

import static com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO.localDateRange;
import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 营业时间请求参数
 */
@Data
@FieldNameConstants
@Schema(title = "营业日历-营业时间请求参数")
public class BusinessTimeRangeParam implements BaseCheckParamProvider {

    @Schema(description = "实例ID")
    private String instanceId;

    @Schema(description = "是否清除配置")
    private Boolean isDelete;

    @Schema(description = "工作历日期标签类型枚举 DEFAULT全局规则,WORKDAY 公休日,WEEK 星期号")
    private MidCalendarDateTagTypeEnum dateTagType;

    @Schema(description = "全年统一-营业时间段 dateTagType=DEFAULT全局规则")
    private LinkedHashSet<Range<LocalTime>> businessTimeRangeList;

    @Schema(description = "工作日-营业时间段 dateTagType=WORKDAY全局规则")
    private LinkedHashSet<Range<LocalTime>> workdayBusinessTimeRangeList;

    @Schema(description = "休息日-营业时间段 dateTagType=WORKDAY全局规则")
    private LinkedHashSet<Range<LocalTime>> dayOffBusinessTimeRangeList;

    @Schema(description = "营业日历-星期号dateTagType=WEEK全局规则")
    private LinkedHashSet<WeekNumberParam> weekNumberParamList;

    @Schema(description = "特殊对象配置-特殊配置内容")
    private LinkedHashSet<SpecialConfigurationContentParam> specialConfigurationContentParamList;


    @JsonSetter(Fields.businessTimeRangeList)
    public void deserializeBusinessTimeRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.businessTimeRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.businessTimeRangeList)
    public LinkedHashSet<String> serializeBusinessTimeRangeList() {
        return RangeUtils.toString(this.businessTimeRangeList,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.workdayBusinessTimeRangeList)
    public void deserializeWorkdayBusinessTimeRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.workdayBusinessTimeRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.workdayBusinessTimeRangeList)
    public LinkedHashSet<String> serializeWorkdayBusinessTimeRangeList() {
        return RangeUtils.toString(this.workdayBusinessTimeRangeList,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    @JsonSetter(Fields.dayOffBusinessTimeRangeList)
    public void deserializeDayOffBusinessTimeRangeList(
            LinkedHashSet<String> globalAnniversaries) {
        this.dayOffBusinessTimeRangeList = RangeUtils.instanceByArray(globalAnniversaries,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.dayOffBusinessTimeRangeList)
    public LinkedHashSet<String> serializeDayOffBusinessTimeRangeList() {
        return RangeUtils.toString(this.dayOffBusinessTimeRangeList,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    public MidCalendarDateTagTypeEnum getDateTagType() {
        if (dateTagType == null) {
            return MidCalendarDateTagTypeEnum.DEFAULT;
        }

        return dateTagType;
    }


    @Data
    @FieldNameConstants
    @Schema(title = "营业日历-星期号请求求参数")
    public static class WeekNumberParam{

        @Schema(description = "每组选择的星期号")
        private LinkedHashSet<DayOfWeek> weekdays;

        @Schema(description = "星期号-营业时段")
        private LinkedHashSet<Range<LocalTime>> weekNumberBusinessTimeRangeList;

        @JsonSetter(WeekNumberParam.Fields.weekNumberBusinessTimeRangeList)
        public void deserializeWeekNumberBusinessTimeRangeList(LinkedHashSet<String> weekNumberBusinessTimeRangeList) {
            this.weekNumberBusinessTimeRangeList = RangeUtils.instanceByArray(weekNumberBusinessTimeRangeList, item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
        }

        @JsonGetter(WeekNumberParam.Fields.weekNumberBusinessTimeRangeList)
        public LinkedHashSet<String> serializeWeekNumberBusinessTimeRangeList() {
            return RangeUtils.toString(this.weekNumberBusinessTimeRangeList, item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
        }

    }

    @Data
    @FieldNameConstants
    @Schema(title = "特殊对象配置-特殊配置内容求参数")
    public static class SpecialConfigurationContentParam{

        @Schema(description = "特殊配置内容-时间段(注意剔除掉不含【今日及未来日】的日期段)")
        private Range<LocalDate> timePeriod;

        @Schema(description = "特殊配置内容-营业时段")
        private LinkedHashSet<Range<LocalTime>> specialConfigurationContentBusinessTimeRangeList;

        @JsonSetter(SpecialConfigurationContentParam.Fields.timePeriod)
        public void deserializeTimePeriod(String timePeriod) {
            if(!StringUtils.hasLength(timePeriod)){
                return;
            }
            String[] split = timePeriod.split(DEFAULT_RANGE_SPLIT_REGEX);
            // 1. 先把两端写成 LocalDate 变量，杜绝任何向上转型
            LocalDate start = LocalDate.parse(split[0], DatePattern.NORM_DATE_FORMATTER);
            LocalDate end   = LocalDate.parse(split[1], DatePattern.NORM_DATE_FORMATTER);
            // 2. 显式指定 <LocalDate>，且实参就是 LocalDate 变量
            this.timePeriod = localDateRange(start,end);
        }

        @JsonGetter(SpecialConfigurationContentParam.Fields.timePeriod)
        public String serializeTimePeriod() {
            if(Objects.isNull(this.timePeriod)){
                return null;
            }
            return String.join(DEFAULT_RANGE_SPLIT_REGEX, timePeriod.getMinimum().format(DatePattern.NORM_DATE_FORMATTER),timePeriod.getMaximum().format(DatePattern.NORM_DATE_FORMATTER));
        }


        @JsonSetter(SpecialConfigurationContentParam.Fields.specialConfigurationContentBusinessTimeRangeList)
        public void deserializeWeekNumberBusinessTimeRangeList(LinkedHashSet<String> specialConfigurationContentBusinessTimeRangeList) {
            this.specialConfigurationContentBusinessTimeRangeList = RangeUtils.instanceByArray(specialConfigurationContentBusinessTimeRangeList, item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
        }

        @JsonGetter(SpecialConfigurationContentParam.Fields.specialConfigurationContentBusinessTimeRangeList)
        public LinkedHashSet<String> serializeWeekNumberBusinessTimeRangeList() {
            return RangeUtils.toString(this.specialConfigurationContentBusinessTimeRangeList, item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
        }
    }
}
