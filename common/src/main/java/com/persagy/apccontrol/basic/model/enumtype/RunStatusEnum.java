package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运行状态枚举
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum RunStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 运行
     */
    OPEN("OPEN", "key_e347f04da74d4b12a28617d8b450cd0c"),

    /**
     * 关闭
     */
    CLOSE("CLOSE", "key_b1620633958840638440dd1b3e10024e");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 转换布尔类型运行状态
     *
     * @return 布尔类型运行状态
     * @since v5.6.0
     */
    public Boolean toBoolean() {
        switch (this) {
            case OPEN:
                return true;
            case CLOSE:
                return false;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    public static RunStatusEnum parseString(String value) {
        return "1".equals(value) ? OPEN : CLOSE;
    }

    /**
     * 转换数字类型运行状态
     *
     * @return 数字类型运行状态
     * @since v5.6.0
     */
    public Integer toInteger() {
        switch (this) {
            case OPEN:
                return 1;
            case CLOSE:
                return 0;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
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
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, RunStatusEnum.class);
    }
}
