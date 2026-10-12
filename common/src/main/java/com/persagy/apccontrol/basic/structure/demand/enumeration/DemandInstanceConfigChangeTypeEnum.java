package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求实例配置变更类型枚举
 *
 * @author JiYinchuan
 * @since 7.250331.0
 */
@Getter
@AllArgsConstructor
public enum DemandInstanceConfigChangeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 黑名单变更
     */
    BLACKLIST("BLACKLIST", "黑名单变更"),

    /**
     * 屏蔽空间变更
     */
    SHIELDED("SHIELDED", "屏蔽空间变更"),
    ;

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
    public static DemandInstanceConfigChangeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandInstanceConfigChangeTypeEnum.class);
    }
}
