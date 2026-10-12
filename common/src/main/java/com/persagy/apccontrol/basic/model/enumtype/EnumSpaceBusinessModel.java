package com.persagy.apccontrol.basic.model.enumtype;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界系统类编码枚举
 *
 * @author ShaoHongbo
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum EnumSpaceBusinessModel implements BaseEnum<String>, IEnum<String> {

    INTERMITTENT_BUSINESS_MODEL("1", "key_5b3c9ff8116f47d3a1c711abaf52a96e"),
    AIR_CONDITION_BUSINESS_MODEL("2", "key_4a2ebbd5da2b4f9b9eadd72ff9bf4b78"),
    ALL_THE_BUSINESS_MODEL("3", "key_faaa93cb97e74b9aa6e840bbcf15d489");

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
    public static EnumSpaceBusinessModel analyze(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return BaseEnum.analyze(value, EnumSpaceBusinessModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
