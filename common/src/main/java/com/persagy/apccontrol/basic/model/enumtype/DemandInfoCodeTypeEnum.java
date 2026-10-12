package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 信息点配置，当前所被管控的类型
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/5/8 14:47
 */
@Getter
@AllArgsConstructor
public enum DemandInfoCodeTypeEnum implements BaseEnum<String>, IEnum<String> {

    MODUAN("MODUAN", "key_56a25250df214dbbb7c1e62c8aae8801"),

    LIGHTING("LIGHTING", "key_1f55dccb06c94d7fa6bb1c4ade132b07");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String desc;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return value;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }
}
