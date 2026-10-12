package com.persagy.apccontrol.basic.model.enumtype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 同步状态枚举
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@AllArgsConstructor
public enum SyncStatusEnum implements BaseEnum<String> {

    /**
     * 未同步
     */
    NONE("NONE", "key_d257f0112b3f44bcbe923533f8722af5"),

    /**
     * 同步失败
     */
    FAIL("FAIL", "key_4e4e26a30a154dae84e3be008c36fdaf"),

    /**
     * 同步等待中
     */
    WAITING("WAITING", "key_ffe3252d9a1f4b67a2286a6b84820061"),

    /**
     * 同步进行中
     */
    PROCESSING("PROCESSING", "key_a3d0586a7ea344bd894c781cf3a13818"),

    /**
     * 同步成功
     */
    SUCCEED("SUCCEED", "key_afd9ee7921de4d2ea00e0ad9b9e7087a");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SyncStatusEnum resolve(String value) {
        return BaseEnum.analyze(value, SyncStatusEnum.class);
    }
}
