package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/22 10:51
 */
@Getter
@AllArgsConstructor
public enum EnumCalculateValueType implements BaseEnum<String>, IEnum<String> {

    TARGET_VALUE("10", "key_ec213f2de1a249ffa0ab3fcb3fde5a53"),
    UPPER_VALUE("11", "key_21130ae39e5149ae99d36f5ed902bc30"),
    COLD_STORAGE_VALUE("12", "key_46b36f9eeb134d289aed546794fcb80d"),
    LIMIT_VALUE("13", "key_d75f557d7e4146cbbb5fc9ee6a2de575"),
    HEAT_STORAGE_VALUE("14", "key_edaf71231069441689b29cf6493d3e89");

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
    public static EnumCalculateValueType analyze(String value) {
        return BaseEnum.analyze(value, EnumCalculateValueType.class);
    }

}
