package com.persagy.apccontrol.basic.model.enumtype.security;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 安防设别类型
 */
@Getter
@AllArgsConstructor
public enum SecurityEquipmentTypeEnum  implements BaseEnum<String>, IEnum<String> {
    DOOR("DOOR","key_b24440a0826945cea288dcc8e65715ca"),
    CAMERA("CAMERA","key_5a477826b9e3444799a5968b75470e9f"),
    BURGLAR_ALARM_DEVICE("BURGLARALARMDEVICE","key_1687a857bead42d8809f4cd23155b15c");


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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SecurityEquipmentTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, SecurityEquipmentTypeEnum.class);
    }
}
