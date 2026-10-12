package com.persagy.apccontrol.basic.model.enumtype.intervention;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 干预实例类型枚举
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Getter
@AllArgsConstructor
public enum InterventionInstanceTypeEnum implements BaseEnum<String> {

    /**
     * 项目
     */
    PROJECT("PROJECT", "Project"),

    /**
     * 建筑
     */
    BUILDING("BUILDING", "Building"),

    /**
     * 楼层
     */
    FLOOR("FLOOR", "Floor"),

    /**
     * 空间
     */
    SPACE("SPACE", "Space"),

    /**
     * 照明回路分组
     */
    CIRCUIT_GROUP("CIRCUIT_GROUP", "CircuitGroup");

    @EnumValue
    private final String value;
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InterventionInstanceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InterventionInstanceTypeEnum.class);
    }
}
