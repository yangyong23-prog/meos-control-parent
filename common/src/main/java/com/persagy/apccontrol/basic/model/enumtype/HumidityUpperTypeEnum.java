package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

/**
 * 湿度上限类型
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Getter
@AllArgsConstructor
public enum HumidityUpperTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 强力除湿
     */
    STRONG_DEHUMIDIFICATION("STRONG_DEHUMIDIFICATION", "key_58c602eeb2d943a0b46dd738e104996d", BigDecimal.valueOf(60)),

    /**
     * 一般除湿
     */
    GENERAL_DEHUMIDIFICATION("GENERAL_DEHUMIDIFICATION", "key_c872a2eb21314968b3505aeea960bf31", BigDecimal.valueOf(70)),

    /**
     * 弱除湿
     */
    WEAK_DEHUMIDIFICATION("WEAK_DEHUMIDIFICATION", "key_9fc7ad2335e4414ca49b4231416abda6", BigDecimal.valueOf(80)),

    /**
     * 自定义
     */
    CUSTOM("CUSTOM", "key_4911c9a0220b4e6ba2a78e1788a76ed2", BigDecimal.valueOf(-1));

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 湿度上限值
     */
    private final BigDecimal upperNumber;

    @Override
    @NonNull
    public String value() {
        return this.value;
    }

    @Override
    @NonNull
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HumidityUpperTypeEnum resolve(String value) {
        return BaseEnum.analyze(value, HumidityUpperTypeEnum.class);
    }
}
