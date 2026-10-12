package com.persagy.apccontrol.basic.model.enumtype.calendar;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 工作历营业时间类型枚举
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@AllArgsConstructor
public enum MidCalendarWorkDayTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 工作日
     */
    WORKDAY("workday", "key_af22aec7b3164acdbb2a47b95919a7fe"),

    /**
     * 休息日
     */
    DAYOFF("dayoff", "key_bb3f02568a6149bfa2eb43759ae56f05");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 转换工作历日期标签值枚举
     *
     * @return 工作历日期标签值枚举
     * @since v5.7.0
     */
    public MidCalendarDateTagValueEnum toDateTagValueEnum() {
        switch (this) {
            case WORKDAY:
                return MidCalendarDateTagValueEnum.WORKDAY;
            case DAYOFF:
                return MidCalendarDateTagValueEnum.DAYOFF;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
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
    public static MidCalendarWorkDayTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, MidCalendarWorkDayTypeEnum.class);
    }
}
