package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zyc
 * @version 1.0
 * @description: 指令下发的数据来源
 * @date 2023/12/20 17:33
 */
@Getter
@AllArgsConstructor
public enum FunctionTypeEnum implements BaseEnum<String>, IEnum<String> {

    SE("SE", "key_2d0c647119f0427fbf2efca1dfb19431"),
    AC("AC", "key_0c76a5ab003b4931abb309e20aeefc8c"),
    ACCI("ACCI", "key_0c76a5ab003b4931abb309e20aeefc8c"),
    WS("WS", "key_4aa65d96aaa54593a8795abd551b50a7"),
    ACCH("ACCH", "key_fcb8bdc59e5c40a9aea4c0d5c3327baf"),
    FF("FF", "key_4c56acee54324bfb8bc7a2bdd032ae29"),
    ;


    private final String code;

    private final String desc;

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FunctionTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, FunctionTypeEnum.class);
    }

}
