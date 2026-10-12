package com.persagy.apccontrol.basic.structure.intervention.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 干预操作类型枚举
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Getter
@AllArgsConstructor
public enum InterventionOperationTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 新增干预
     */
    ADD("ADD", "key_aabed013990c460c932868ae7c828a20"),

    /**
     * EDIT
     */
    EDIT("EDIT", "key_4c86fd1d413e4d73b1d2ed270cc72bf9"),

    /**
     * 解除干预
     */
    RELEASE("RELEASE", "key_2da8fed128f34402ac30f3531b25cb7f");

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
    public static InterventionOperationTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InterventionOperationTypeEnum.class);
    }
}
