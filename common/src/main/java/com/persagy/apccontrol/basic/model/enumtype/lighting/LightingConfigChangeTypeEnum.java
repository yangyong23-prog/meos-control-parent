package com.persagy.apccontrol.basic.model.enumtype.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 照明-配置变更类型枚举
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Getter
@AllArgsConstructor
public enum LightingConfigChangeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 应用模版变动
     */
    TEMPLATE_APPLY_CHANGED("TEMPLATE_APPLY", "key_a47cbfcb063442b89587022dfb5d6d70"),

    /**
     * 场景配置变动
     */
    SCENE_CONFIG_CHANGED("SCENE_CONFIG_CHANGED", "key_c1fe150ac3da4540b18dc8fa71ee80d5");

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
        return this.value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LightingConfigChangeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, LightingConfigChangeTypeEnum.class);
    }
}
