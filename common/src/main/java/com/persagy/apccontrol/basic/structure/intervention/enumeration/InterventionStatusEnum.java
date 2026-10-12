package com.persagy.apccontrol.basic.structure.intervention.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 干预状态枚举
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Getter
@AllArgsConstructor
public enum InterventionStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 生效中
     */
    EFFECTING("EFFECTING", "key_0d4b1f27ff5f49a092b786d910fe238b"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "key_d08ab06130b64044a256177e05943fa3");

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
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InterventionStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, InterventionStatusEnum.class);
    }
}
