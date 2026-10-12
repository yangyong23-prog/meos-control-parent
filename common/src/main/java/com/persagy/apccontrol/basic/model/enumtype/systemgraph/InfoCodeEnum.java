package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 空间iot信息
 *
 * @author LiuYang
 * @date 2024/6/24
 * @since v5.240602.0
 */
@Getter
@AllArgsConstructor
public enum InfoCodeEnum {

    SPACE_TDB("tdb", "key_7274b502eecc4af1ac679f90941dfcbe", "℃"),
    SPACE_CO2("cO2", "key_f4b2a3540e1543c9a24f9c15c564caaa", "ppm"),
    RH("rH", "key_c03fb9cdd9344d3293d66c6382095dba", "%RH"),
    RELATIVE_PRESSURE_DIFF("relativePressureDiffToOtherSpace", "key_pressure_diff_relative_to_other_space", "Pa"),
    PM2D5("pM2d5", "PM2.5", "μg/m³"),
    PM10("pM10", "PM10", "μg/m³"),
    CO("cO", "key_b4ec68b6476c4def9282903a8b358f9b", "μg/m³"),
    VOC("vOC", "key_6381ee9deefd43b2833662417e71388d", "μg/m³"),
    TVOC("tVOC", "key_618a701a2e66457d8739009355b0e39e", "μg/m³"),
    HCHO("hCHO", "key_f4cc76df3e7d4b63b5bd22c317e1df72", "μg/m³"),
    DEFAULT("-", "-", "-");

    private final String code;
    private final String desc;
    private final String unit;

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public static InfoCodeEnum getInfo(String code) {
        for (InfoCodeEnum value : InfoCodeEnum.values()) {
            boolean equals = value.getCode().equals(code);
            if (equals) {
                return value;
            }
        }
        return DEFAULT;
    }

}
