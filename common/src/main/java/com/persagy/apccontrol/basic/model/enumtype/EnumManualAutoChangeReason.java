package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.TreeMap;

/**
 * 手自动切换原因
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/25 15:06
 */
@Getter
@AllArgsConstructor
public enum EnumManualAutoChangeReason implements BaseEnum<String>, IEnum<String> {

    RUNNING_CHANGE_REASON_URGENCY("ACCC", "00", "key_35467067fbb14929967637819356aa3f"),
    RUNNING_CHANGE_REASON_WB("ACCC", "10", "key_7e76a6d17e3a4dad98db76f3606bbdf7"),
    RUNNING_CHANGE_REASON_FAULT("ACCC", "20", "key_4ba124fd89e24741aea24dadb152a910"),
    RUNNING_CHANGE_REASON_COOL("ACCC", "30", "key_58008c5710d349daad3448af24cc2ce7"),
    RUNNING_CHANGE_REASON_EQUIP_ARGS("ACCC", "40", "key_2191a0074b694c47b528a410bb69b7e2"),
    RUNNING_CHANGE_REASON_OTHER("ACCC", "50", "key_7d6652fc005d47b3854cdc48b0b5d4f7"),

    ACCH_RUNNING_CHANGE_REASON_URGENCY("ACCH", "00", "key_35467067fbb14929967637819356aa3f"),
    ACCH_RUNNING_CHANGE_REASON_WB("ACCH", "10", "key_7e76a6d17e3a4dad98db76f3606bbdf7"),
    ACCH_RUNNING_CHANGE_REASON_FAULT("ACCH", "20", "key_4ba124fd89e24741aea24dadb152a910"),
    ACCH_RUNNING_CHANGE_REASON_COOL("ACCH", "30", "key_9e989a81f7774296915c87024d2f636e"),
    ACCH_RUNNING_CHANGE_REASON_OTHER("ACCH", "50", "key_7d6652fc005d47b3854cdc48b0b5d4f7"),

    ACAH_RUNNING_CHANGE_REASON_URGENCY("ACAH", "00", "key_35467067fbb14929967637819356aa3f"),
    ACAH_RUNNING_CHANGE_REASON_WB("ACAH", "10", "key_7e76a6d17e3a4dad98db76f3606bbdf7"),
    ACAH_RUNNING_CHANGE_REASON_FAULT("ACAH", "20", "key_4ba124fd89e24741aea24dadb152a910"),
    ACAH_RUNNING_CHANGE_REASON_COOL("ACAH", "30", "key_58008c5710d349daad3448af24cc2ce8"),
    ACAH_RUNNING_CHANGE_REASON_EQUIP_ARGS("ACAH", "40", "key_2191a0074b694c47b528a410bb69b7e2"),
    ACAH_RUNNING_CHANGE_REASON_OTHER("ACAH", "50", "key_7d6652fc005d47b3854cdc48b0b5d4f7"),

    ACAI_RUNNING_CHANGE_REASON_URGENCY("ACAI", "00", "key_35467067fbb14929967637819356aa3f"),
    ACAI_RUNNING_CHANGE_REASON_WB("ACAI", "10", "key_7e76a6d17e3a4dad98db76f3606bbdf7"),
    ACAI_RUNNING_CHANGE_REASON_FAULT("ACAI", "20", "key_4ba124fd89e24741aea24dadb152a910"),
    ACAI_RUNNING_CHANGE_REASON_DEMAND_CHANGE("ACAI", "30", "key_9e989a81f7774296915c87024d2f636e"),
    ACAI_RUNNING_CHANGE_REASON_OTHER("ACAI", "50", "key_7d6652fc005d47b3854cdc48b0b5d4f7");

    private final String type;
    private final String code;
    private final String desc;

    public static TreeMap<String, String> getEnumManualAutoChangeReasonMap(String systemClassCode) {
        TreeMap<String, String> map = new TreeMap<>();
        EnumManualAutoChangeReason[] types = EnumManualAutoChangeReason.values();
        for (EnumManualAutoChangeReason type : types) {
            if (DtpRwdSystemClassCodeEnum.ACCH.getValue().equals(systemClassCode)) {
                if (DtpRwdSystemClassCodeEnum.ACCH.getValue().equals(type.getType())) {
                    map.put(type.getCode(), type.getDesc());
                }
                continue;
            }
            if (DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(systemClassCode)
                    && systemClassCode.equals(type.getType())) {
                map.put(type.getCode(), type.getDesc());
                continue;
            }
            if (DtpRwdSystemClassCodeEnum.ACAI.getValue().equals(systemClassCode)
                    && systemClassCode.equals(type.getType())) {
                map.put(type.getCode(), type.getDesc());
                continue;
            }
            if (DtpRwdSystemClassCodeEnum.ACCC.getValue().equals(type.getType())) {
                map.put(type.getCode(), type.getDesc());
            }
        }
        return map;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    @Override
    public String getReasonPhrase() {
        return this.getDesc();
    }

}