package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求配置清除类型枚举
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Getter
@AllArgsConstructor
public enum DemandConfigClearTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 当前对象
     */
    CURRENT("CURRENT", "key_a27763f4c690449cbca0950c0ea5ec12"),

    /**
     * 下级对象
     */
    CHILDREN("CHILDREN", "key_6dd2d2a873d54463bc4fd09413828b60"),

    /**
     * 当前及下级对象
     */
    CURRENT_AND_CHILDREN("CURRENT_AND_CHILDREN", "key_9491315558ee42eb8c92e95bc789592c");

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
    public static DemandConfigClearTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandConfigClearTypeEnum.class);
    }
}
