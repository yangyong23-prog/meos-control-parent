package com.persagy.apccontrol.basic.model.enumtype;

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
public enum EnumCalculateTarget implements BaseEnum<String>, IEnum<String> {

    OPEN("open", "key_d196ab48969847ba99dc9176595c281f"),

    CLOSE("close", "key_49789b1b32324f03a49a9d621a36fcb0"),

    DISPATCH("dispatch", "key_d4b4a4891cbf49caad9fbb100a0f3d79"),

    TDB("tdb", "key_7274b502eecc4af1ac679f90941dfcbe"),

    RH("rH", "key_c03fb9cdd9344d3293d66c6382095dba"),

    CO2("cO2", "key_f4b2a3540e1543c9a24f9c15c564caaa");

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
        return LocaleMessageUtil.getMessage( desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage( desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnumCalculateTarget analyze(String value) {
        return BaseEnum.analyze(value, EnumCalculateTarget.class);
    }

}
