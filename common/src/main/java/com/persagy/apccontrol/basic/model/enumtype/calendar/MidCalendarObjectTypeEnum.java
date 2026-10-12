package com.persagy.apccontrol.basic.model.enumtype.calendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * 工作历对象类型
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarObjectTypeEnum implements BaseEnum<String> {

    /**
     * 空间
     */
    SPACE("space", "key_93930e54e51b4dbda89417183881b172"),

    /**
     * 楼层
     */
    FLOOR("floor", "key_860f403627e4419496e3711477912a39"),

    /**
     * 建筑
     */
    BUILDING("building", "key_e977b930027f4fcfbf29bdf9ed47663f"),

    /**
     * 项目
     */
    PROJECT("project", "key_c8ff811cb5234f69b587f5dd1fa73141");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 从数据源解析当前枚举
     *
     * @param source 数据源
     * @return 当前枚举
     * @since v5.7.0
     */
    public static MidCalendarObjectTypeEnum from(@NonNull InstanceTypeEnum source) {
        switch (source) {
            case PROJECT:
                return PROJECT;
            case BUILDING:
                return BUILDING;
            case FLOOR:
                return FLOOR;
            case SPACE:
                return SPACE;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(source.getClass().getSimpleName(), source);
        }
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
    public static MidCalendarObjectTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, MidCalendarObjectTypeEnum.class);
    }
}
