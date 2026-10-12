package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 需求模版标签枚举
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Getter
@AllArgsConstructor
public enum DemandTemplateTagEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 工作日
     */
    WORKDAY("WORKDAY", "key_af22aec7b3164acdbb2a47b95919a7fe", 5, 1),

    /**
     * 休息日
     */
    DAYOFF("DAYOFF", "key_bb3f02568a6149bfa2eb43759ae56f05", 4, 2),

    /**
     * 节假日
     */
    HOLIDAY("HOLIDAY", "key_9d6d7d03e0514a6fa5807dda289b7348", 3, 3),

    /**
     * 店庆日
     */
    ANNIVERSARY("ANNIVERSARY", "key_6b60d76bb60b4d9a8e8aa987d7fde3c5", 2, 4),

    /**
     * 自定义
     */
    CUSTOM("CUSTOM", "key_4911c9a0220b4e6ba2a78e1788a76ed2", 1, 999);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 优先级
     *
     * <p>数值越小优先级越高, 可能存在相同优先级
     */
    private final int priority;

    /**
     * 排序
     *
     * <p>数值越小顺序越靠前
     */
    private final int ordered;

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since v6.0.0
     */
    @Nullable
    public static DemandTemplateTagEnum create(@Nullable MidCalendarDateTagValueEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        switch (source) {
            case WORKDAY:
                return WORKDAY;
            case DAYOFF:
                return DAYOFF;
            case MINOR_VACATION:
            case NATIONAL_DAY:
            case NEW_YEAR:
                return HOLIDAY;
            case ANNIVERSARY:
                return ANNIVERSARY;
            default:
                return null;
        }
    }

    /**
     * 是否为有效的日期标签值
     *
     * @return 是否为有效的日期标签值
     * @since 7.250101.0
     */
    public boolean validDateTagValue(@NonNull MidCalendarDateTagValueEnum dateTagValue) {
        switch (dateTagValue) {
            case WORKDAY:
                return this == WORKDAY;
            case DAYOFF:
                return this == DAYOFF;
            case MINOR_VACATION:
            case NATIONAL_DAY:
            case NEW_YEAR:
                return this == HOLIDAY;
            case ANNIVERSARY:
                return this == ANNIVERSARY;
            default:
                return true;
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandTemplateTagEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandTemplateTagEnum.class);
    }
}
