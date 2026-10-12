package com.persagy.apccontrol.basic.structure.calendar.converter;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDateTagRuleUpdateParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarSchedulePlanRuleUpdateParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeProjectResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarResponse;
import com.persagy.apccontrol.basic.outside.feign.service.param.calendar.*;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.pattern.DatePattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.Range;
import org.mapstruct.*;
import org.springframework.lang.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 工作历转换器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {
                Objects.class, Lists.class,
                MidCalendarDateTagTypeEnum.class,
                MidCalendarDateTagValueEnum.class,
                MidCalendarScheduleTagTypeEnum.class
        }
)
public interface MidCalendarConvert {

    /**
     * 日期格式
     */
    DateTimeFormatter GLOBAL_DATE_FORMATTER = DatePattern.NORM_MONTHDAY_FORMATTER;

    /**
     * 营业时间格式
     */
    DateTimeFormatter GLOBAL_OPENING_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // ------------------------- To Dto -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250202.0
     */
    @Mapping(target = "workTimeRanges", ignore = true)
    MidCalendarOpenTimeProjectDTO toOpenTimeProjectDto(MidCalendarOpenTimeProjectResponse source);

    /**
     * 转换模型
     *
     * @param source     数据源
     * @param linkObject 关联对象
     * @return 模型
     * @since v5.7.0
     */
    @Mappings({
            @Mapping(target = "calendarId", source = "source.id"),
            @Mapping(target = "calendarName", source = "source.name"),
            @Mapping(target = "objectId", source = "linkObject.objectId"),
            @Mapping(target = "objectName", source = "linkObject.objectName"),
            @Mapping(target = "objectType", source = "linkObject.objType")
    })
    MidCalendarDTO toDto(MidCalendarResponse source, MidCalendarResponse.LinkObject linkObject);

    // ------------------------- To Param -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "workId", source = "source.calendarId"),
            @Mapping(target = "dateTagType", expression = "java(MidCalendarDateTagTypeEnum.WORKDAY)"),
            @Mapping(target = "dateTagValue", expression = "java(source.getWorkDayType().toDateTagValueEnum())"),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "periodDate", ignore = true)
    })
    MidCalendarDateTagRuleUpdateParam.Content toWorkDateTagRuleUpdateParamContent(
            MidCalendarGlobalDateWorkDateTagRuleUpdateParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "workId", source = "source.calendarId"),
            @Mapping(target = "dateTagType", expression = "java(MidCalendarDateTagTypeEnum.AC_SEASON)"),
            @Mapping(target = "dateTagValue", expression = "java(source.getAcSeasonType())"),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "periodDate", ignore = true)
    })
    MidCalendarDateTagRuleUpdateParam.Content toAcSeasonTagRuleUpdateParamContent(
            MidCalendarGlobalDateAcSeasonTagRuleUpdateParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    Collection<MidCalendarDateTagRuleUpdateParam.Content> toAcSeasonTagRuleUpdateParamContent(
            Collection<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "workId", source = "source.calendarId"),
            @Mapping(target = "dateTagType", expression = "java(MidCalendarDateTagTypeEnum.AIR_SEASON)"),
            @Mapping(target = "dateTagValue", expression = "java(source.getAcSeasonType())"),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "periodDate", ignore = true)
    })
    MidCalendarDateTagRuleUpdateParam.Content toAirSeasonTagRuleUpdateParamContent(
            MidCalendarGlobalDateAirSeasonTagRuleUpdateParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    Collection<MidCalendarDateTagRuleUpdateParam.Content> toAirSeasonTagRuleUpdateParamContent(
            Collection<MidCalendarGlobalDateAirSeasonTagRuleUpdateParam> source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "workId", source = "source.calendarId"),
            @Mapping(target = "dateTagType", expression = "java(MidCalendarDateTagTypeEnum.ANNIVERSARY)"),
            @Mapping(target = "dateTagValue", expression = "java(MidCalendarDateTagValueEnum.ANNIVERSARY)"),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "periodDate", ignore = true)
    })
    MidCalendarDateTagRuleUpdateParam.Content toAnniversaryRuleUpdateParamContent(
            MidCalendarGlobalAnniversaryRuleUpdateParam source);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/4 14:46
    　　 * @description: 转换数据
    */
    @Mappings({
            @Mapping(target = "workId", source = "source.calendarId"),
            @Mapping(target = "dateTagType", expression = "java(MidCalendarDateTagTypeEnum.HOLIDAY)"),
            @Mapping(target = "dateTagValue", expression = "java(MidCalendarDateTagValueEnum.MINOR_VACATION)"),
            @Mapping(target = "date", ignore = true),
            @Mapping(target = "periodDate", ignore = true)
    })
    MidCalendarDateTagRuleUpdateParam.Content toHolidayGeneralRuleUpdateParamContent(
            MidCalendarGlobalHolidayRuleUpdateParam source);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/4 14:46
    　　 * @description: 转换数据
    */
    default List<MidCalendarDateTagRuleUpdateParam.Content> toHolidaySpecialRuleUpdateParamContent(
            MidCalendarGlobalHolidayRuleUpdateParam source){
        List<MidCalendarDateTagRuleUpdateParam.Content> result = Lists.newArrayList();
        Collection<Range<LocalDate>> periodDate = source.getPeriodDate();
        if(org.springframework.util.CollectionUtils.isEmpty(periodDate)){
            return result;
        }
        for (Range<LocalDate> localDateRange : periodDate) {
            MidCalendarDateTagRuleUpdateParam.Content content = new MidCalendarDateTagRuleUpdateParam.Content();
            content.setWorkId(source.getCalendarId());
            content.setDateTagType(MidCalendarDateTagTypeEnum.HOLIDAY);
            content.setDateTagValue(MidCalendarDateTagValueEnum.MINOR_VACATION);
            content.setPeriodDate(String.join(DEFAULT_RANGE_SPLIT_REGEX, localDateRange.getMinimum().format(DatePattern.NORM_DATE_FORMATTER),localDateRange.getMaximum().format(DatePattern.NORM_DATE_FORMATTER)));
            result.add(content);
        }
        return result;
    }

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "dateTagValue", expression = "java(Objects.nonNull(source.getWorkDayType()) ? source.getWorkDayType().toDateTagValueEnum() : Objects.nonNull(source.getDateTagValueType())  ? source.getDateTagValueType():null )"),
            @Mapping(target = "scheduleTagType", expression = "java(MidCalendarScheduleTagTypeEnum.OPENING)"),
            @Mapping(target = "scheduleContent", expression = "java(toGlobalScheduleContentString(source.getScheduleContent()))"),
            @Mapping(target = "date", ignore = true)
    })
    MidCalendarSchedulePlanRuleUpdateParam.Content toOpeningRuleUpdateParamContent(
            MidCalendarGlobalOpeningRuleUpdateParam source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    Collection<MidCalendarSchedulePlanRuleUpdateParam.Content> toOpeningRuleUpdateParamContent(
            Collection<MidCalendarGlobalOpeningRuleUpdateParam> source);

    /**
     * 转换工作历星期字符串
     *
     * @param dayOfWeek 星期
     * @return 工作历星期字符串
     * @since v5.7.0
     */
    default DayOfWeek toCalendarDayOfWeek(@NonNull String dayOfWeek) {
        switch (dayOfWeek) {
            case "1":
                return DayOfWeek.MONDAY;
            case "2":
                return DayOfWeek.TUESDAY;
            case "3":
                return DayOfWeek.WEDNESDAY;
            case "4":
                return DayOfWeek.THURSDAY;
            case "5":
                return DayOfWeek.FRIDAY;
            case "6":
                return DayOfWeek.SATURDAY;
            case "0":
                return DayOfWeek.SUNDAY;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(dayOfWeek.getClass().getSimpleName(), dayOfWeek);
        }
    }

    /**
     * 转换工作历星期字符串
     *
     * @param dayOfWeek 星期
     * @return 工作历星期字符串
     * @since v5.7.0
     */
    default String toCalendarDayOfWeekString(@NonNull DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "1";
            case TUESDAY:
                return "2";
            case WEDNESDAY:
                return "3";
            case THURSDAY:
                return "4";
            case FRIDAY:
                return "5";
            case SATURDAY:
                return "6";
            case SUNDAY:
                return "0";
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(dayOfWeek.getClass().getSimpleName(), dayOfWeek);
        }
    }

    /**
     * 转换全局日期字符串
     *
     * @param source 数据源
     * @return 字符串
     * @since v5.8.0
     */
    default Set<String> toGlobalDateString(@NonNull Collection<Range<MonthDay>> source) {
        return source.stream().map(item -> String.join(",",
                        item.getMinimum().format(GLOBAL_DATE_FORMATTER),
                        item.getMaximum().format(GLOBAL_DATE_FORMATTER)))
                .collect(Collectors.toSet());
    }

    /**
     * 转换全局时间字符串
     *
     * @param source 数据源
     * @return 字符串
     * @since v6.0.0
     */
    default Collection<String> toGlobalScheduleContentString(@NonNull Collection<Range<LocalTime>> source) {
        return CollectionUtils.isNotEmpty(source)
                ? source.stream().map(item -> String.join(",",
                        item.getMinimum().format(GLOBAL_OPENING_TIME_FORMATTER),
                        item.getMaximum().format(GLOBAL_OPENING_TIME_FORMATTER)))
                .collect(Collectors.toSet()) : Sets.newHashSet();
    }
}
