package com.persagy.apccontrol.basic.model.enumtype.dutyHome;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 燃烧状态
 *
 * @author LiuYang
 * @since 5.241102.0
 */
@Getter
@AllArgsConstructor
public enum CombustionStateEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 大火
     */
    BIG_FIRE("1", "key_80fd46f374494d61868238e130e6f12d"),

    /**
     * 小火
     */
    SMALL_FIRE("2", "key_15e4b1c06d984494b8e874379973d926"),

    /**
     * 无火
     */
    NO_FIRE("3", "key_9bcb5db7e9d147c0a487c322c34776b5");

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

    /**
     * 根据iot数值转换为燃烧状态枚举
     *
     * @param value 数值
     * @return 燃烧状态
     */
    public static CombustionStateEnum getCombustionStateEnum(Object value) {
        if ("1".equals(value)) {
            return BIG_FIRE;
        } else if ("2".equals(value)) {
            return SMALL_FIRE;
        } else if ("3".equals(value)) {
            return NO_FIRE;
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CombustionStateEnum analyze(String value) {
        return BaseEnum.analyze(value, CombustionStateEnum.class);
    }
}
