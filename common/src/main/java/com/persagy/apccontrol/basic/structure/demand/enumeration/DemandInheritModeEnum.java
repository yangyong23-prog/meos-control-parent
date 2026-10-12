package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求继承模式类型
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Getter
@AllArgsConstructor
public enum DemandInheritModeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 空调需求
     */
    GLOBAL("GLOBAL", "key_8bfa8caa2bb04b339abbd5434091243d"),

    /**
     * 空间功能类型
     */
    ROOM_FUN_TYPE("ROOM_FUN_TYPE", "key_c8e9a828419e4e2f81da655dd97ee277");

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

        @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandInheritModeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandInheritModeEnum.class);
    }
}
