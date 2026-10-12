package com.persagy.apccontrol.basic.structure.control.enumeration;


import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Getter;

/**
 * 冷机大小机类型(区分冷机大中小)
 *
 * @author yuecaipu
 * @date 2025/3/31 10:13
 * @since v7.250201.00
 */
@Getter
public enum ChillerSize {

    /**
     * 大机
     */
    LARGE("LARGE", "key_275ea4b2fe3c40d1961e535e55e80ff1"),

    /**
     * 中机1
     */
    M1("M1", "key_dd429e2689764f25aea3445bbfc4d07d"),

    /**
     * 中机2
     */
    M2("M2", "key_dd429e2689764f25aea3445bbfc4d07d"),

    /**
     * 中机3
     */
    M3("M3", "key_dd429e2689764f25aea3445bbfc4d07d"),

    /**
     * 小机
     */
    SMALL("SMALL", "key_898f8ac31ed641e8b0b67a28c8f7534f");

    /**
     * code
     */
    private final String code;
    private final String name;
    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }
    
    
    ChillerSize(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ChillerSize getByCode(String code) {
        for (ChillerSize chillerSize : ChillerSize.values()) {
            if (chillerSize.getCode().equalsIgnoreCase(code)) {
                return chillerSize;
            }
        }
        return null;
    }
    
}
