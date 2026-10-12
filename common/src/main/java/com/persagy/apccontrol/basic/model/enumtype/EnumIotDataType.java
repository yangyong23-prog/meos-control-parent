package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumIotDataType implements BaseEnum<Integer>, IEnum<Integer> {
    /**
     * iot数据类型
     */
    CTRL(1, "key_3e45c8839c2a448da3049c327ad14bff"),
    FEEDBACK(0, "key_a279514e7ff14ba3954390fe0016f75d"),
    ;

    private final Integer code;
    private final String desc;


    @Override
    public Integer getValue() {
        return code;
    }

    @Override
    public Integer value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage( desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage( desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnumIotDataType resolve(Integer value) {
        return BaseEnum.analyze(value, EnumIotDataType.class);
    }
}
