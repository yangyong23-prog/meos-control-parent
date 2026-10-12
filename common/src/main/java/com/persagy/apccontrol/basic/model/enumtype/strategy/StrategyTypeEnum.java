package com.persagy.apccontrol.basic.model.enumtype.strategy;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 策略类型枚举
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum StrategyTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 固定策略
     */
    FIXED_STRATEGY("FIXED_STRATEGY", "key_651c44d3dd0b44e29b1d6e111e0f7ef9"),

    /**
     * 临时策略
     */
    TEMP_STRATEGY("TEMP_STRATEGY", "key_2ec135a4cf024a9abc7b55658b3803e5");

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StrategyTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, StrategyTypeEnum.class);
    }
}
