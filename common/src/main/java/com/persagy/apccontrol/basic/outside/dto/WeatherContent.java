package com.persagy.apccontrol.basic.outside.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 天气服务返回的数据
 *
 * @author yx
 * @date 2023/3/29
 */
@Data
public class WeatherContent {

    @Schema(description = "天气服务配置")
    private String config;

    @Schema(description = "干球温度")
    private String temperature;

    @Schema(description = "相对湿度")
    private String humidity;

    @Schema(description = "空气湿球温度")
    private String wet_bulb_temp;

    @Schema(description = "空气比焓")
    private String h;

    @Schema(description = "白日气象编码")
    private String codeDay ;

    @Schema(description = "白日气象文字")
    private String textDay;

    @Schema(description = "夜晚气象编码")
    private String codeNight;

    @Schema(description = "夜晚气象文字")
    private String textNight;

    @Schema(description = "最高温")
    private String maxTemperature;

    @Schema(description = "最低温")
    private String minTemperature;

    @Schema(description = "白日图标-字节数组")
    private byte[] iconDay;

}
