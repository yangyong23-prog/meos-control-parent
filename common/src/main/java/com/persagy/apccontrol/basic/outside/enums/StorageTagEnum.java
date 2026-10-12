package com.persagy.apccontrol.basic.outside.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageTagEnum implements BaseEnum<String>, IEnum<String> {
    //0:无，1:蓄冷，2:蓄热
    NONE("0", "key_f81cf7864fa24205a6127b27d636f610"),
    COLD("1", "key_41f807fe8b03416ebb3cdd657094b4d3"),
    HOT("2", "key_d0fe13087f604ea287b5be15e46f1579");

    private final String tag;

    private final String desc;

    @Override
    public String value() {
        return tag;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StorageTagEnum analyze(String value) {
        return BaseEnum.analyze(value, StorageTagEnum.class);
    }

    @Override
    public String getValue() {
        return tag;
    }
}
