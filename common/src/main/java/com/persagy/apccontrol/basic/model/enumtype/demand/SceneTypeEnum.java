package com.persagy.apccontrol.basic.model.enumtype.demand;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 场景类型
 *
 * @author yuecaipu
 * @since v5.240702
 */
@Getter
@AllArgsConstructor
public enum SceneTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 需求干预
     */
    INTERVENTION("INTERVENTION", "key_e3277b402bff46139ffd08467f50f9d0"),

    /**
     * 自动需求
     */
    AUTO("AUTO", "key_440b15c72841457b80750144424519b6");

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
    public static SceneTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, SceneTypeEnum.class);
    }
}
