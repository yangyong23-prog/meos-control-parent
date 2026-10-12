package com.persagy.apccontrol.basic.model.enumtype.weather;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherDayHistoryDataDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 天气服务-查询类型枚举
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Getter
@AllArgsConstructor
public enum WeatherQueryTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 实时数据
     */
    NOW_WEATHER_QUERY("NowWeatherQuery", "实时数据", null),

    /**
     * 小时历史数据
     */
    HOUR_HISTORY_DATA("HourHistoryData", "小时历史数据", null),

    /**
     * 日预报数据
     */
    QUERY_WEATHER_SERVICE("QueryWeatherService", "日预报数据", null),

    /**
     * 24小时预报数据
     */
    HOUR_24_FORECAST("Hour24Forecast", "24小时预报数据", null),

    /**
     * 日历史数据
     */
    DAY_HISTORY_DATA("DayHistoryData", "日历史数据", WeatherDayHistoryDataDTO.class),

    /**
     * 日统计数据
     */
    DAY_STATIC_DATA("DayStaticData", "日统计数据", null),

    /**
     * 灾害预警数据
     */
    QUERY_DISASTER_SERVICE("QueryDisasterService", "灾害预警数据", null),

    /**
     * 日出日落数据
     */
    SUN_RISE_AND_SET("SunRiseAndSet", "日出日落数据", null),

    /**
     * 天气现象编码与文字
     */
    DIC_WEATHER_CODE_NAME("DicWeatherCodeName", "天气现象编码与文字", null);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 响应DTO类型
     */
    private final Class<?> responseClass;

    /**
     * 根据响应数据创建响应DTO
     *
     * @param objectNode 响应数据
     * @param <T>        响应DTO类型
     * @return 响应DTO
     * @since 7.260302.0
     */
    @SuppressWarnings("unchecked")
    public <T> T toResponse(@NonNull ObjectNode objectNode) {
        return (T) ObjectMapperUtils.convertValue(objectNode, this.responseClass);
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
        return this.reasonPhrase;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WeatherQueryTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, WeatherQueryTypeEnum.class);
    }
}
