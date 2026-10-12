package com.persagy.apccontrol.basic.model.enumtype.outside.energy;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatisticsTypeEnum implements BaseEnum<String>, IEnum<String> {
    /**
     * 末端设备组统计数据
     **/
    TERMINAL_EQUIP_GROUP_STATISTICS("TERMINAL_EQUIP_GROUP_STATISTICS", "key_ef41afe2ee8844539f5e888460473e2a"),
    /**
     * 末端设备信息点统计
     **/
    TERMINAL_INFO_CODE_STATISTICS("TERMINAL_INFO_CODE_STATISTICS", "key_4df013422af248bbb706e872882f510d"),
    /**
     * 照明设备信息点统计
     **/
    LIGHTING_INFO_CODE_STATISTICS("LIGHTING_INFO_CODE_STATISTICS", "key_d453afc14a564d23bf0c4b79e95bde63"),
    /**
     * 照明空间下照明设备统计
     **/
    LIGHTING_EQUIP_IN_SPACE_STATISTICS("LIGHTING_EQUIP_IN_SPACE_STATISTICS", "key_69dbd47ab2364f7c9a0e4b0b7fa87346");

    private final String value;
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
    public static StatisticsTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, StatisticsTypeEnum.class);
    }
}
