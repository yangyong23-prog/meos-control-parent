package com.persagy.apccontrol.basic.outside.enums;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyModelTagEnum implements BaseEnum<String> {

    COOL("COOL", "key_energy_model_tag_cool", "key_e925f3cd20df43ddb616f387c33c638c"),
    HEAT("HEAT", "key_energy_model_tag_heat", "key_0affaf81b05e4c00ad7b231b1f005241"),
    COOL_HEAT("COOL_HEAT", "key_energy_model_tag_cool_heat", "key_1e2f55cb96de4ee3940cba885950b7e3"),
    AIR_TERMINAL("AIR_TERMINAL", "key_energy_model_tag_air_terminal", "key_6e3184deda8f40afbf68ee767834bcb0"),
    TOTAL("TOTAL", "key_437957876bc14ea286f4ae197682d9af", "key_e6942aacbd834cd0b4f6d5ef2ac7d9b9");


    private final String code;
    /**
     * 标签名称：与能源调研服务 energy_model_item_tag 字典名称对齐，
     * 使用本枚举独立 i18n key（勿与运行日志分类等共享 key），展示翻译见 energyName
     */
    private final String name;
    private final String energyName;

    @Override
    public String value() {
        return name;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(name);
    }

    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }

    public String getEnergyName() {
        return LocaleMessageUtil.getMessage(energyName);
    }
}
