package com.persagy.apccontrol.basic.structure.control.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务需求计算类型枚举类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/2/14 10:30
 */
@Getter
@AllArgsConstructor
public enum ComputeTypeEnum implements BaseEnum<String>, IEnum<String> {

    SPACE_STATUS_DRIVE("SPACE_STATUS_DRIVE", "key_7ffe6666c43b462dbf7c1d45ca7a5191"),

    COOL_SUPPLY("COOL_SUPPLY", "key_a3984a2b104841069b726a8139dc4ef1"),

    COOL_OUTPUT("COOL_OUTPUT", "key_a126ed25163049059bc8417d6025100d"),

    COOL_INPUT("COOL_INPUT", "key_41f807fe8b03416ebb3cdd657094b4d3"),

    AIR_TERMINAL_ZONE("AIR_TERMINAL_ZONE", "key_85562193ba3e470cb03c1ce870fcf538");

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
