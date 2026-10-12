package com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 信息点模版对象类型枚举
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Getter
@AllArgsConstructor
public enum InfoCodeObjectTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 设备
     */
    EQUIPMENT("EQUIPMENT", "key_b057517385f34aca901cb69c4f64433d"),

    /**
     * 空间
     */
    SPACE("SPACE", "key_93930e54e51b4dbda89417183881b172");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InfoCodeObjectTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InfoCodeObjectTypeEnum.class);
    }
}
