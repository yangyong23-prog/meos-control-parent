package com.persagy.apccontrol.basic.model.enumtype.runanalysis;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnalysisTypeEnum implements BaseEnum<String> {
    //冷冻侧 / 冷却侧 / 末级泵组
    FREEZE_SIDE("FREEZE_SIDE", "key_11090ecef96e4018b2fb13e6a385bfa9", 1),
    COOLING_SIDE("COOLING_SIDE", "key_af7cc5e0263649f6929e11942879e90e", 2),
    END_PUMP_GROUP("END_PUMP_GROUP", "key_8e30db9140a3437498dbe88c41ada654", 4),
    COLD_STORE_SIDE("COLD_STORE_SIDE", "key_8e3528ae1288427684b1c463e747504f", 3);

    private final String value;
    private final String name;
    private final Integer order;


    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(name);
    }

    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }
}
