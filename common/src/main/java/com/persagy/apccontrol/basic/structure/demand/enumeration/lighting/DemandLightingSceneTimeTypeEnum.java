package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求管理-照明需求-场景时间类型
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingSceneTimeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 日出时间
     */
    SUNRISE("SUNRISE", "key_0b7fd3318ea443b287faa48eb0196711"),

    /**
     * 日落时间
     */
    SUNSET("SUNSET", "key_382160afedd04842a4d7e00eaf3ce511"),

    /**
     * 绝对时间
     */
    ABSOLUTE("ABSOLUTE", "key_33ccec4d579f4fffb31ba8be25d78799");

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
    public static DemandLightingSceneTimeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingSceneTimeTypeEnum.class);
    }
}
