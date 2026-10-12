package com.persagy.apccontrol.basic.outside.feign.model.response.weather;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 天气服务-实时数据查询
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WeatherNowWeatherResponse extends BaseResponse {

    /**
     * 天气现象编码
     */
    private String code;

    /**
     * 臭氧
     */
    private String o3;

    /**
     * 能见度
     */
    private String visibility;

    /**
     * 可吸入颗粒物
     */
    private String pm10;

    /**
     * 风向文字
     */
    @JsonAlias("wind_direction")
    private String windDirection;

    /**
     * 气压值
     */
    private String pressure;

    /**
     * 云量
     */
    private String clouds;

    /**
     * 一氧化碳
     */
    private String co;

    /**
     * 体感温度
     */
    @Schema(description = "体感温度")
    @JsonAlias("feels_like")
    private String feelsLike;

    /**
     * 空气质量文字
     */
    @Schema(description = "空气质量文字")
    private String quality;

    /**
     * 二氧化碳
     */
    @Schema(description = "二氧化碳")
    private String no2;

    /**
     * PM2.5
     */
    @Schema(description = "PM2.5")
    private String pm25;

    /**
     * 二氧化硫
     */
    @Schema(description = "二氧化硫")
    private String so2;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    private String lastUpdate;

    /**
     * 风力等级
     */
    @JsonAlias("wind_scale")
    private String windScale;

    /**
     * 空气指数
     */
    @Schema(description = "空气指数")
    private String aqi;

    /**
     * 首要污染物
     */
    @JsonAlias("primary_pollutant")
    private String primaryPollutant;

    /**
     * 温度
     */
    private String temperature;

    /**
     * 相对湿度
     */
    private String humidity;

    /**
     * 风向角
     */
    @JsonAlias("wind_direction_degree")
    private String windDirectionDegree;

    /**
     * 风速
     */
    @JsonAlias("wind_speed")
    private String windSpeed;

    /**
     * 天气现象文字
     */
    private String text;

    /**
     * 空气湿球温度
     */
    @JsonAlias("wet_bulb_temp")
    private String wetBulbTemp;

    /**
     * 小时时间
     */
    private LocalDateTime hourTime;

    /**
     * 焓值
     */
    @JsonAlias("enthalpy_value")
    private String enthalpyValue;

}
