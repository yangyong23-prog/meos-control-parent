package com.persagy.apccontrol.basic.model.enumtype.instance;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InstanceConfigTypeEnum implements BaseEnum<String>, IEnum<String> {

    //白名单，黑名单
    WHITE_LIST("WHITE_LIST", "key_ed022c7f25404a2eaa63ba309fae75ab"),
    BLACK_LIST("BLACK_LIST", "key_b1f9bd5d18d447c7b482eb5bb436f76e"),
    EQUIPMENT_LIST_HEADER("EQUIPMENT_LIST_HEADER", "key_547a0ec58e6c43c488d002133c552109"),

    ;
    private final String value;
    private final String reasonPhrase;


    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举值
     *
     * @return 枚举值
     * @since 1.3.0
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * 获取枚举信息
     *
     * @return 枚举信息
     * @since 1.3.0
     */
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InstanceConfigTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InstanceConfigTypeEnum.class);
    }
}
