package com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界照明回路自动控制模式枚举
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdCircuitAutoCtrlTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 声控
     */
    VOICE_CONTROL("1", "key_430a4fcc17a644a696e693a6465aad8d"),

    /**
     * 人员探测控制
     */
    PERSONNEL_DETECTION_CONTROL("2", "key_7ca1742a36884912a0b5b8eef9b2fac5"),

    /**
     * 定时控制
     */
    TIMING_CONTROL("3", "key_1ea23d4372fd4785ba1276cd6755bad2"),

    /**
     * 其他
     */
    OTHER("99", "key_7d6652fc005d47b3854cdc48b0b5d4f7");

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

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdCircuitAutoCtrlTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCircuitAutoCtrlTypeEnum.class);
    }
}
