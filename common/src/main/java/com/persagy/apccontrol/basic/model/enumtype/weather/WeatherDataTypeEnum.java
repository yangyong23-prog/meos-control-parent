package com.persagy.apccontrol.basic.model.enumtype.weather;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 天气服务-数据类型枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum WeatherDataTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 所有数据
     */
    ALL("All", "key_02e4152a28904377be73614bfd1db874"),

    /**
     * 天气数据
     */
    WEATHER("weather", "key_2597b32dc89a43809f0fa9827ca933a0"),

    /**
     * 空气数据
     */
    AIR("air", "key_4b83df7aebaa400abd6432e51afdb70a");

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
    public static WeatherDataTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, WeatherDataTypeEnum.class);
    }
}
