package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author MuAnFu
 */
@Getter
@AllArgsConstructor
public enum AbnormalEquipmentEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 控反不一致
     */
    CONTROL_REVERSE_INCONSISTENT("CONTROL_REVERSE_INCONSISTENT", "key_f0a572661bea44208463591020227964"),
    /**
     * 离线
     */
    OFFLINE("OFFLINE", "key_bcb07a983fc34e0e8b0878f0668597de"),
    /**
     * 异常
     */
    ABNORMAL("ABNORMAL", "key_5bf0b8410a1f4947ab5c4d9c580e3db4");

    private String value;
    private String reasonPhrase;

    @Override
    public String value() {
        return value;
    }
    @Override
    public String getReasonPhrase() {return LocaleMessageUtil.getMessage(reasonPhrase);
    }
}
