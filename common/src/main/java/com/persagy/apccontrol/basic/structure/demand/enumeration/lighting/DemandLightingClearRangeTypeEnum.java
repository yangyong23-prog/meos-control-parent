package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求管理-照明需求-取消引用枚举
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingClearRangeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 当前对象
     */
    CURRENT("1", "key_a27763f4c690449cbca0950c0ea5ec12"),

    /**
     * 下级对象
     */
    NEXT("2", "key_6dd2d2a873d54463bc4fd09413828b60"),

    /**
     * 当前及下级对象
     */
    ALL("3", "key_9491315558ee42eb8c92e95bc789592c");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandLightingClearRangeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingClearRangeTypeEnum.class);
    }

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
}
