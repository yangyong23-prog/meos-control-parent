package com.persagy.apccontrol.basic.model.enumtype.airterminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 空调末端-实例类型枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum AirTerminalInstanceTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 项目
     */
    PROJECT("PROJECT", "key_c8ff811cb5234f69b587f5dd1fa73141"),

    /**
     * 建筑
     */
    BUILDING("BUILDING", "key_e977b930027f4fcfbf29bdf9ed47663f"),

    /**
     * 楼层
     */
    FLOOR("FLOOR", "key_860f403627e4419496e3711477912a39");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 转换需求实例类型枚举
     *
     * @return 需求实例类型枚举
     * @since 5.240502.0
     */
    public DemandInstanceTypeEnum toDemandInstanceTypeEnum() {
        switch (this) {
            case PROJECT:
                return DemandInstanceTypeEnum.PROJECT;
            case BUILDING:
                return DemandInstanceTypeEnum.BUILDING;
            case FLOOR:  return DemandInstanceTypeEnum.FLOOR;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

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
    public static AirTerminalInstanceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, AirTerminalInstanceTypeEnum.class);
    }
}
