package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 在线状态枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Getter
@AllArgsConstructor
public enum OnlineStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 在线 1
     */
    ONLINE("ONLINE", "key_3d9cd40c1e7a4bffb610d913a9969cf0"),

    /**
     * 离线 0
     */
    OFFLINE("OFFLINE", "key_bcb07a983fc34e0e8b0878f0668597de");

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
    public static OnlineStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, OnlineStatusEnum.class);
    }
}
