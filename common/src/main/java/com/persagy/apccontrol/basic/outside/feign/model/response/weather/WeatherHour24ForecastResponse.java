package com.persagy.apccontrol.basic.outside.feign.model.response.weather;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 天气服务-24小时预报数据响应
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WeatherHour24ForecastResponse extends BaseResponse {

    /**
     * 地理位置
     */
    private Location location;

    /**
     * 小时数据
     */
    private List<Hourly> hourly;

    /**
     * 地理位置
     *
     * @author JiYinchuan
     * @since 5.240502.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class Location extends BaseResponse {

        /**
         * ID
         */
        private String id;

        /**
         * 地理位置名称
         */
        private String name;

        /**
         * 国家编码
         */
        private String country;

        /**
         * 地理位置路径
         */
        private String path;

        /**
         * 时区
         */
        private String timezone;

        /**
         * 时区偏移量
         */
        @JsonAlias("timezone_offset")
        private String timezoneOffset;

    }

    /**
     * 小数数据
     *
     * @author JiYinchuan
     * @since 5.240502.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class Hourly extends BaseResponse {

        /**
         * 时间
         *
         * <p>example: 2021-01-21 17:00:00
         */
        private String time;

        /**
         * 天气现象文字
         *
         * <p>example: 晴
         */
        private String text;

        /**
         * 天气现象编码
         *
         * <p>example: 0
         */
        private String code;

        /**
         * 温度
         *
         * <p>example: 4
         */
        private String temperature;

        /**
         * 风向文字
         *
         * <p>example: 南
         */
        @JsonAlias("wind_direction")
        private String windDirection;

        /**
         * 风速
         *
         * <p>example: 10
         */
        @JsonAlias("wind_speed")
        private String windSpeed;

        /**
         * 湿球温度
         */
        @JsonAlias("wet_bulb_temp")
        private BigDecimal wetBulbTemp;
        /**
         * 湿度
         */
        private BigDecimal humidity;
        /**
         * 焓值
         */
        @JsonAlias("enthalpy_value")
        private BigDecimal enthalpyValue;

    }
}
