package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参与调度枚举
 */
@Getter
@AllArgsConstructor
public enum ParticipateInSchedulingEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 部分调度
     */
    PARTIAL_SCHEDULE("PARTIAL_SCHEDULE", "key_4f3880a89b124d8bac8cd5b59c7fc33d"),

    /**
     * 全部调度
     */
    ALL_SCHEDULE("ALL_SCHEDULE", "key_c647191c3e9c4affbcc9b8c2f0079a9b"),

    /**
     * 不参与调度
     */
    NON_SCHEDULE("NON_SCHEDULE", "key_6d5f8e43276e44bf9ecdd55ad91defd3"),


    /**
     * 未配置
     */
    NON_CONFIG("NON_CONFIG", "key_a09d40c0ec674d9583b1c6b8c7f22ee1");

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
    public static ParticipateInSchedulingEnum analyze(String value) {
        return BaseEnum.analyze(value, ParticipateInSchedulingEnum.class);
    }
}
