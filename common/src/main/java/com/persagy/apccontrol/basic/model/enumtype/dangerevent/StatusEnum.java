package com.persagy.apccontrol.basic.model.enumtype.dangerevent;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wxf
 * @description 风险事件状态
 * @date 18:20 2025/2/12
**/
@Getter
@AllArgsConstructor
public enum StatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 发生
     */
    EXCEPTION("EXCEPTION", "key_172582734d7e475eaf57c555df1bc62f"),

    /**
     * 推测发生
     */
    CONJECTURE_EXCEPTION("CONJECTURE_EXCEPTION", "key_3d42b27784a144aab6c6313d49cf37ba"),

    /**
     * 未知
     */
    UNKNOWN("UNKNOWN", "key_08241d179eeb4c989389940b30544847"),

    /**
     * 大体正常
     */
    GENERAL_NORMAL("GENERAL_NORMAL", "key_461a7ef36c6b40b9bfb92bafc896fdf6"),

    /**
     * 正常
     */
    NORMAL("NORMAL", "key_e9d200d236e64b8584fb926b8aadddc4"),

    /**
     * 潜在风险
     */
    POTENTIAL("POTENTIAL", "key_3181211fbaf64731bfad579d1e6d3e32");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatusEnum analyze(String value) {
        return BaseEnum.analyze(value, StatusEnum.class);
    }
}
