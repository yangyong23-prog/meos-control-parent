package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 18:33
 */
@Getter
@AllArgsConstructor
public enum TerminalEnum implements BaseEnum<String>, IEnum<String> {

    CLOUD("CLOUD", "key_7d8de2e9f4614e68a6d2861e08ed59bd"),

    EDGE("EDGE", "key_302fa7276ba84b76a792b32ce3357187");

    private String code;

    private String desc;

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return code;
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TerminalEnum analyze(String value) {
        try {
            return BaseEnum.analyze(value, TerminalEnum.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
