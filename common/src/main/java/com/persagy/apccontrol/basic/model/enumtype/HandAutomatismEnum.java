package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 手自动状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/5/24 14:17
 * @since v5.240502.0
 */
@Getter
@AllArgsConstructor
public enum HandAutomatismEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 手动
     */
    MANUAL_OPERATION("MANUAL_OPERATION", "key_a2341d9fc6b04ba2a3f5781eb747b14c"),

    /**
     * 自动
     */
    AUTO("AUTO", "key_e31ea221b6e74c5898d5ed6208968a54");

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
    public static HandAutomatismEnum analyze(String value) {
        return BaseEnum.analyze(value, HandAutomatismEnum.class);
    }

}

