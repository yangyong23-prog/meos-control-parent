package com.persagy.apccontrol.basic.model.enumtype.security;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecurityEquipmentStatusEnum implements BaseEnum<String>, IEnum<String> {
    AMAZING("amazing", "key_a798cfa32a78488a97800c237a370b1f"),
    DISAMAZING("disamazing", "key_5f966a12d1fa40f3ad40585cb398dcc3"),
    OPEN("open", "key_5f6a4957123248279ddf22f6cfa74063"),
    CLOSE("close", "key_b1620633958840638440dd1b3e10024e"),

    EXCEPTION("exception", "key_5bf0b8410a1f4947ab5c4d9c580e3db4"),
    OFFLINE("offline", "key_bcb07a983fc34e0e8b0878f0668597de"),
    ONLINE("online", "key_3d9cd40c1e7a4bffb610d913a9969cf0"),
    EMPTY(null, "key_5fb8c0e8570249be858b27a54d361648"),


    ;


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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SecurityEquipmentStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, SecurityEquipmentStatusEnum.class);
    }

    public static SecurityEquipmentStatusEnum getOpenStatus(Object value) {
        return "1".equals(value) ? OPEN : CLOSE;
    }

    public static SecurityEquipmentStatusEnum getOnlineStatus(Object value) {
        return "0".equals(value) ? OFFLINE : ONLINE;
    }

    public static SecurityEquipmentStatusEnum getAmazingStatus(Object value) {
        return "0".equals(value) ? DISAMAZING : AMAZING;
    }

}
