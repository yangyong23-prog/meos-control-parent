package com.persagy.apccontrol.basic.structure.control.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 控制台类型
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/4/03 10:30
 */
@Getter
@AllArgsConstructor
public enum ConsoleTypeEnum implements BaseEnum<String>, IEnum<String> {

    COOL_SUPPLY("COOL_SUPPLY", "key_61bcb8647d02408e9c32fe41ce9f2e94"),

    COOL_INPUT("COOL_INPUT", "key_2083dda5db7940adac75f12280ef0121"),

    AIR_TERMINAL_ZONE("AIR_TERMINAL_ZONE", "key_309b38e12acf4c7a8a8fc60e2e755ecb");

    private String value;

    private String reasonPhrase;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

}
