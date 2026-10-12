package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 本地远程状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/5/24 14:17
 * @since v5.240502.0
 */
@Getter
@AllArgsConstructor
public enum LocalRemoteEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 本地
     */
    LOCAL("LOCAL", "key_0496c23144324b83b80df317faf857d6"),

    /**
     * 远程
     */
    REMOTE("REMOTE", "key_34bb3f4a33064f4eab818f5b4f438d41");

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
    public static LocalRemoteEnum analyze(String value) {
        return BaseEnum.analyze(value, LocalRemoteEnum.class);
    }

}

