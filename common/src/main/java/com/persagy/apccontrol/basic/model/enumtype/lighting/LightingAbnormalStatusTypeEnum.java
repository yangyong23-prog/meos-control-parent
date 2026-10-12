package com.persagy.apccontrol.basic.model.enumtype.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常状态
 *
 * @author MAF
 * @date 2024/04/29
 */
@Getter
@AllArgsConstructor
public enum LightingAbnormalStatusTypeEnum implements BaseEnum<String>, IEnum<String> {
    /*"DEMAND_DEVIATION": "key_2caa72357b5b40baae2eac0540a55787",
            "ABNORMAL_EVENT": "key_4e717c8ec9a34332a4a5ef73d80922d8",
            "EQUIPMENT_STATUS_ABNORMAL": "key_70a5b3ac22bc45e8a940112e5cb5558d"*/
    DEMAND_DEVIATION("DEMAND_DEVIATION", "key_2caa72357b5b40baae2eac0540a55787"),
    ABNORMAL_EVENT("ABNORMAL_EVENT", "key_4e717c8ec9a34332a4a5ef73d80922d8"),
    EQUIPMENT_STATUS_ABNORMAL("EQUIPMENT_STATUS_ABNORMAL", "key_70a5b3ac22bc45e8a940112e5cb5558d");

    final String value;
    final String reasonPhrase;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LightingAbnormalStatusTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, LightingAbnormalStatusTypeEnum.class);
    }
}
