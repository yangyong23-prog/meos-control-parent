package com.persagy.apccontrol.basic.model.enumtype.calendar;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 工作历日期标签类型枚举
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarDateTagTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 默认
     */
    DEFAULT("Default", "key_1f44623b840d44b199b70a6239a60063"),

    /**
     * 供冷供暖季
     */
    AC_SEASON("ACSeason", "key_d9932c582b3247a19f96654a3a3b1fd9"),

    /**
     * 工休日
     */
    WORKDAY("Workday", "key_12e35e12fa3f41ba86080ec1224426a7"),

    /**
     * 工休日
     */
    HOLIDAY("Holiday", "key_404a0131d6544ee7b2698c21eb0fe9b4"),

    /**
     * 店庆日
     */
    ANNIVERSARY("Anniversary", "key_6b60d76bb60b4d9a8e8aa987d7fde3c5"),

    /**
     * 季节配置
     */
    SEASON("Season", "key_8e8ec964456146beb8c0297c233bf85e"),

    /**
     * 空调季
     */
    AIR_SEASON("AirSeason", "key_ff97d83e44e24d139cf203eeb5c8b0a8"),


    /**
     * 星期号
     */
    WEEK("Week", "key_week_number");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

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
    public static MidCalendarDateTagTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, MidCalendarDateTagTypeEnum.class);
    }
}
