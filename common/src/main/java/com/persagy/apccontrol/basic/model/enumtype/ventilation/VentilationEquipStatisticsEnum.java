package com.persagy.apccontrol.basic.model.enumtype.ventilation;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VentilationEquipStatisticsEnum implements BaseEnum<String>, IEnum<String> {
    ACVTSF_SEND_FAN("ACVTSF_SEND_FAN", "ACVTSF", "key_cae575a2889d41f7a0d37be27516e18f"),
    FFSCMF_SEND_FAN("FFSCMF_SEND_FAN", "FFSCMF", "key_8cc120ed974043d8872de690b9ab6286"),
    ACVTSF_RECEIVE_FAN("ACVTSF_RECEIVE_FAN", "ACVTSF", "key_a888b765deeb4bdabd1e1c15c463e07d"),
    FFSCAF_RECEIVE_FAN("FFSCAF_RECEIVE_FAN", "ACVTSF", "key_a5f73583fccd4853a7c060e126a03e2c"),
    VENTILATION_FAN_CFCSWN("VENTILATION_FAN_CFCSWN", "CFCSWN", "key_a1b2c3d4e5f6478a0b1c2d3e4f50617"),
    VENTILATION_FAN_CFCSEW("VENTILATION_FAN_CFCSEW", "CFCSEW", "key_b2c3d4e5f6a7489b1c2d3e4f5061728"),
    ;

    final String value;
    final String infoCode;
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
}
