package com.persagy.apccontrol.basic.model.enumtype.intervention;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 干预执行类型枚举
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Getter
@AllArgsConstructor
public enum InterventionExecuteTypeEnum implements BaseEnum<String> {

    /**
     * 单次执行
     */
    ONCE("once", "Once"),

    /**
     * 多次执行
     */
    MULTI("multi", "Multi"),

    /**
     * 手动执行
     */
    MANUAL("manual", "Manual"),

    /**
     * 立即执行
     */
    IMMEDIATELY("immediately", "Immediately");

    private final String value;
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InterventionExecuteTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InterventionExecuteTypeEnum.class);
    }
}
