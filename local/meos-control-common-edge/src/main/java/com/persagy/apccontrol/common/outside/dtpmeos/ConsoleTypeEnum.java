package com.persagy.apccontrol.common.outside.dtpmeos;

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
    
    COOL_SUPPLY("COOL_SUPPLY", "key_a3984a2b104841069b726a8139dc4ef1"),
    
    COOL_INPUT("COOL_INPUT", "key_41f807fe8b03416ebb3cdd657094b4d3"),

    HEAT_SUPPL("HEAT_SUPPLY", "key_a3984a2b104841069b726a8139dc4ef2"),

    WSDH_SYSTEM("WSDH_SYSTEM", "key_f995455bf333452f980cca5fd9f94cb1"),

    AIR_TERMINAL_ZONE("AIR_TERMINAL_ZONE", "key_872853171d364737bc1a9563f0935084");
    
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
