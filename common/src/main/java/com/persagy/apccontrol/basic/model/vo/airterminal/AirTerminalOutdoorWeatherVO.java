package com.persagy.apccontrol.basic.model.vo.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空调末端-室外天气VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-室外天气VO")
public class AirTerminalOutdoorWeatherVO {

    @Schema(description = "天气现象文字")
    private String text;

    @Schema(description = "当前温度")
    private String temperature;

    @Schema(description = "相对湿度")
    private String humidity;

    @Schema(description = "当日最低温度")
    private String minTemperature;

    @Schema(description = "当日最高温度")
    private String maxTemperature;

}
