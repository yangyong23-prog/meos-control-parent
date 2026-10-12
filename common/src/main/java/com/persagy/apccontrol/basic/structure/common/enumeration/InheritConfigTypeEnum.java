package com.persagy.apccontrol.basic.structure.common.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 继承类型枚举
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Getter
@AllArgsConstructor
public enum InheritConfigTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 对象继承
     */
    OBJECT("OBJECT", "key_01427e89d9b5444c83e8a625785480f4"),

    /**
     * 字段继承
     */
    FIELD("FIELD", "key_15ee316dcb344d0e9c42976b7e6dd626");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InheritConfigTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InheritConfigTypeEnum.class);
    }
}
