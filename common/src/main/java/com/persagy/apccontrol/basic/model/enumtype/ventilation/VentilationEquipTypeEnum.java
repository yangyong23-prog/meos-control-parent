package com.persagy.apccontrol.basic.model.enumtype.ventilation;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VentilationEquipTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 送风机
     */
    SEND_FAN("SEND_FAN", "key_3c618424c3f14678a04424fff7cf5d51"),
    /**
     * 排风机
     */
    RECEIVE_FAN("RECEIVE_FAN", "key_6c58612bf37843e6a201a8704dc53e14"),
    /**
     * 自然通风
     */
    NATURAL_VENTILATION("NATURAL_VENTILATION", "key_c3d4e5f6a7b8490c2d3e4f506172839");

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
}
