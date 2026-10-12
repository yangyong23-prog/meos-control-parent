package com.persagy.apccontrol.basic.model.enumtype.calendar;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 工作历时间计划标签类型枚举
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarScheduleTagTypeEnum implements BaseEnum<String>, IEnum<String> {

    // ------------------------- Default -------------------------

    /**
     * 开闭店时间
     */
    OPENING("Opening", "key_bf2862945f714b988e4c7cb262c49cc4");

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
    public static MidCalendarScheduleTagTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, MidCalendarScheduleTagTypeEnum.class);
    }
}
