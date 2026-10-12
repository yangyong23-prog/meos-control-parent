package com.persagy.apccontrol.basic.model.enumtype.calendar;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 工作历日期标签类型枚举
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarDateTagValueEnum implements BaseEnum<String>, IEnum<String> {

    // ------------------------- Workday -------------------------

    /**
     * 工作日
     */
    WORKDAY("workday", "key_af22aec7b3164acdbb2a47b95919a7fe"),

    /**
     * 休息日
     */
    DAYOFF("dayoff", "key_bb3f02568a6149bfa2eb43759ae56f05"),

    // ------------------------- Holiday -------------------------

    /**
     * 小长假
     */
    MINOR_VACATION("minorVacation", "key_29d6f409efdd4cf19273e347fed3cf7f"),

    /**
     * 国庆
     */
    NATIONAL_DAY("nationalDay", "key_2beb8450239940e79d169e7c827583e1"),

    /**
     * 春节
     */
    NEW_YEAR("newYear", "key_da3cb120f4ea430aa5cddc4710310fd9"),

    // ------------------------- Anniversary -------------------------

    /**
     * 店庆日
     */
    ANNIVERSARY("anniversary", "key_6b60d76bb60b4d9a8e8aa987d7fde3c5"),

    // ------------------------- ACSeason -------------------------

    /**
     * 供冷季
     */
    SEASON_COOLING("ACSeason-1", "key_bc52fd0bd5b549e2ae716d44f8ec6b60"),

    /**
     * 供暖季
     */
    SEASON_HEATING("ACSeason-2", "key_c58783d3d304414881abd3d00b887940"),

    /**
     * 不供热不供暖
     */
    SEASON_TRANSITION("ACSeason-3", "key_b259340f108e449395c5e5bdfa850ec7"),

    // ------------------------- Season -------------------------

    /**
     * 春
     */
    SPRING("spring", "key_238858ad9d344cf4bf18c82fc1b6744b"),

    /**
     * 夏
     */
    SUMMER("summer", "key_b9de317c3b06464aa6cbc220f66ba6df"),

    /**
     * 秋
     */
    AUTUMN("autumn", "key_d5c402cd0fc44c2c86f60ff85f25d8a0"),

    /**
     * 冬
     */
    WINTER("winter", "key_a1b8e0ad1e744517b27335188498f303"),
    // ------------------------- 空调季 -------------------------


    /**
     * 夏季
     */
    AIR_SUMMER("AirSummer", "key_fa717a2d4456488a9058da5357b94bfc"),
    /**
     * 夏季高峰
     */
    AIR_SUMMER_PEAK("AirSummerPeak", "key_f3090e940dc44f598ac1b2bee2a25b66"),
    /**
     * 冬季
     */
    AIR_WINTER("AirWinter", "key_6778c5ec0c8845ca93f5c62d0c66cb21"),
    /**
     * 过渡季（春）
     */
    AIR_SEASON_SPRING("AirSeasonSpring", "key_70e0f303bd374d56a8b05a82fc8e6fcc"),
    /**
     * 过渡季（秋）
     */
    AIR_SEASON_AUTUMN("AirSeasonAutumn", "key_22bc81be27c043c2adcd6c2159614111"),

    /**
     * 星期号
     */
    WEEK("week", "key_week_number")

    ;
    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 转换工作历工作日枚举
     *
     * @return 工作历工作日枚举
     * @since v5.7.0
     */
    public MidCalendarWorkDayTypeEnum toWorkDayTypeEnum() {
        switch (this) {
            case WORKDAY:
                return MidCalendarWorkDayTypeEnum.WORKDAY;
            case DAYOFF:
                return MidCalendarWorkDayTypeEnum.DAYOFF;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    /**
     * 转换工作历空调季枚举
     *
     * @return 工作历空调季枚举
     * @since v5.7.0
     */
    public MidCalendarAcSeasonTypeEnum toAcSeasonTypeEnum() {
        switch (this) {
            case SEASON_COOLING:
                return MidCalendarAcSeasonTypeEnum.SEASON_COOLING;
            case SEASON_HEATING:
                return MidCalendarAcSeasonTypeEnum.SEASON_HEATING;
            case SEASON_TRANSITION:
                return MidCalendarAcSeasonTypeEnum.SEASON_TRANSITION;
            default:
                return null;
        }
    }

    /**
     * 转换工作历空调季枚举
     *
     * @return 工作历空调季枚举
     * @since v5.7.0
     */
    public static List<MidCalendarAirSeasonTypeEnum> toACSeasonValueNewEnum(List<MidCalendarDateTagValueEnum> calendarTags) {
        List<MidCalendarAirSeasonTypeEnum> resultNewEnum = new ArrayList<>();
        if (CollectionUtils.isEmpty(calendarTags)) {
            return resultNewEnum;
        }
        for (MidCalendarDateTagValueEnum calendarTag : calendarTags) {
            MidCalendarAirSeasonTypeEnum seasonValueEnum = toACSeasonValueNewEnum(calendarTag);
            if (seasonValueEnum != null) {
                resultNewEnum.add(seasonValueEnum);
            }
        }
        return resultNewEnum;
    }

    /**
     * 转换工作历空调季枚举
     *
     * @return 工作历空调季枚举
     * @since v5.7.0
     */
    public static MidCalendarAirSeasonTypeEnum toACSeasonValueNewEnum(MidCalendarDateTagValueEnum calendarTag) {
        if (calendarTag == null) {
            return null;
        }
        switch (calendarTag) {
            case AIR_SUMMER:
                return MidCalendarAirSeasonTypeEnum.AIR_SUMMER;
            case AIR_SUMMER_PEAK:
                return MidCalendarAirSeasonTypeEnum.AIR_SUMMER_PEAK;
            case AIR_WINTER:
                return MidCalendarAirSeasonTypeEnum.AIR_WINTER;
            case AIR_SEASON_SPRING:
                return MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING;
            case AIR_SEASON_AUTUMN:
                return MidCalendarAirSeasonTypeEnum.AIR_SEASON_AUTUMN;
            default:
                return null;
        }
    }
    
    public static MidCalendarDateTagValueEnum getCompareValue(String value) {
        for (MidCalendarDateTagValueEnum item : MidCalendarDateTagValueEnum.values()) {
            if (Objects.equals(item.value(), value)) {
                return item;
            }
        }
        
        return null;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MidCalendarDateTagValueEnum analyze(String value) {
        return BaseEnum.analyze(value, MidCalendarDateTagValueEnum.class);
    }
}
