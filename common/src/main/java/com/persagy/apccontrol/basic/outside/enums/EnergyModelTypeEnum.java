package com.persagy.apccontrol.basic.outside.enums;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyModelTypeEnum implements BaseEnum<String> {

    GROUP("group", "key_a73b8d8c2d764b568d57aa1ac9e577bb"),
    CUSTOMIZED("customized", "key_4911c9a0220b4e6ba2a78e1788a76ed2");

    private final String value;
    private final String name;


    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(name);
    }

    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }
}
