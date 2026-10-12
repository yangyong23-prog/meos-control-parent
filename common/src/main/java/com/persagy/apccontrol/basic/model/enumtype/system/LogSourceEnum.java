package com.persagy.apccontrol.basic.model.enumtype.system;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 日志来源
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum LogSourceEnum implements BaseEnum<String>, IEnum<String> {
    /**
     *
     */
    USER("user", "key_a3a8f63d592e467aa4f7da92ec674d09"),
    SYSTEM("system", "key_ff1b921a39eb4e8ea4edf4195a219db1");

    private final String code;
    private final String desc;

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static LogSourceEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LogSourceEnum i : LogSourceEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.getDesc();
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

}
