package com.persagy.apccontrol.basic.structure.cache.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 缓存-IOT规则类型枚举
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Getter
@AllArgsConstructor
public enum CacheIotRuleTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 持久化
     */
    PERSISTENCE("PERSISTENCE", "key_cache_iot_rule_type_persistence"),

    /**
     * 孪生
     */
    TWIN("TWIN", "key_cache_iot_rule_type_twin");;

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

    /**
     * 根据终端类型获取缓存-IOT规则类型
     *
     * @param terminalType 终端类型
     * @return 缓存-IOT规则类型
     * @since 7.260302.0
     */
    public static CacheIotRuleTypeEnum from(@NonNull TerminalEnum terminalType) {
        return terminalType == TerminalEnum.EDGE ? TWIN : PERSISTENCE;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CacheIotRuleTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, CacheIotRuleTypeEnum.class);
    }
}
