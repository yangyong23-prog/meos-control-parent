package com.persagy.apccontrol.basic.model.enumtype.runanalysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupTypeEnum implements BaseEnum<String> {

    GROUP("GROUP", "key_bdcf4bb034434e989c21a24c83733b59"),
    EQUIP_IN_GROUP("EQUIP_IN_GROUP", "key_cea72b69123d4ef7a495057b03405509"),
    ;

    private final String name;
    private final String reasonPhrase;

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.3.0
     */
    @Override
    public String value() {
        return name;
    }

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.3.0
     */
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GroupTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, GroupTypeEnum.class);
    }
}
