package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 物理世界-控制信息点点位类型枚举
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@AllArgsConstructor
public enum DtpRwdControlInfoCodeTypeEnum implements BaseEnum<Integer>, IEnum<Integer> {

    /**
     * 字典信息点(软点)
     */
    DICTIONARY_INFO_CODE(1, "key_f6718c5b0e7d45f5903011ebc49f8fc2"),

    /**
     * 带绑点的字典信息点
     */
    DICTIONARY_EXISTS_RULE_INFO_CODE(2, "key_7554dcf1151f4d3f8dd8d8c96293f7cb");


    /**
     * 枚举值
     */
    private final Integer value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    @Override
    public Integer value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdControlInfoCodeTypeEnum analyze(Integer value) {
        return BaseEnum.analyze(value, DtpRwdControlInfoCodeTypeEnum.class);
    }
}
