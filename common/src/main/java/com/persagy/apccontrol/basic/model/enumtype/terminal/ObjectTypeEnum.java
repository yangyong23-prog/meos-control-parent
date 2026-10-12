package com.persagy.apccontrol.basic.model.enumtype.terminal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 查询标签入参类型
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/5/27 19:39
 * @since v5.240502.0
 */
@AllArgsConstructor
public enum ObjectTypeEnum implements BaseEnum<String> {

    /**
     * 空间
     */
    SPACE("space", "key_93930e54e51b4dbda89417183881b172"),

    /**
     * 设备
     */
    EQUIPMENT("equipment", "key_b057517385f34aca901cb69c4f64433d"),

    /**
     * 设备组
     */
    EQUIP_GROUP("equipGroup", "key_cd9c69bfd5174d7bbd6ee06fe4ae011c");


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
    public static ObjectTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, ObjectTypeEnum.class);
    }
}
