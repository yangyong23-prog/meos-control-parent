package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求配置来源
 *
 * @author yuecaipu
 */
@Getter
@AllArgsConstructor
public enum DemandSourceEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 人工干预需求
     */
    DEMAND_INTERVENTION("DEMAND_INTERVENTION", "key_8436aa8c395240939a68cad35b162743"),

    /**
     * 设备手动控制
     */
    MANUAL_CONTROL("MANUAL_CONTROL", "key_870eafbde5ec457dbe2ce2e6f6c32292"),

    /**
     * 需求配置
     */
    DEMAND_CONFIG("DEMAND_CONFIG", "key_ce4a33050fe14ea6af865b5e81efe28c");

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
