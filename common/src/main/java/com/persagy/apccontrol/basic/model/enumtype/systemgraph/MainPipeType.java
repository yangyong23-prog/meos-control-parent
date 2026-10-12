package com.persagy.apccontrol.basic.model.enumtype.systemgraph;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author houyuanzhi
 * @date 2023/9/21 15:39
 */
@AllArgsConstructor
@Getter
public enum MainPipeType {

    BUSBAR("Bus", "key_a4195526a5264fc283125145ab5bfccf"),
    GUIDELINE("GuideLine", "key_12ccfa078e2a4feaa41e9476ecc77687"),
    PARALLEL_LINE("ParallelLine", "key_b384a6dffabe4f4f80537f8d025bfb77"),
    Loop("Loop", "key_6ce3e078d6e34f12ab122e4f2d0c164a"),
    RiserMainPipe("RiserMainPipe", "key_f8026d7c2876424eac4725404481f542"),
    RiserWhole("RiserWhole", "key_ece06cd0c8db439498dad23329e12c74"),

    ;


    @EnumValue
    private String value;
    private String desc;

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }


    public static MainPipeType getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (MainPipeType mainPipeCompType : values()) {
            if (value.equals(mainPipeCompType.getValue())) {
                return mainPipeCompType;
            }
        }
        return null;
    }
}

