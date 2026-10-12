package com.persagy.apccontrol.basic.outside.vo.riskevent;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SolveProcessEnum implements BaseEnum<String>, IEnum<String> {

    FIND_EXCEPTION("FIND_EXCEPTION", "key_68946ed46ecc443a9327adc36bfe47a4"),
    SOLVING("SOLVING", "key_2b49cea75e3940e7887a521e2fd03c62"),
    VERIFICATION("VERIFICATION", "key_fe9886d37c91440b8f33d1a6509d6461"),
    ;

    private final String value;
    private final String desc;

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.3.0
     */
    @Override
    public String value() {
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.3.0
     */
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SolveProcessEnum analyze(String value) {
        return BaseEnum.analyze(value, SolveProcessEnum.class);
    }
}
