package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 天气服务-实时数据DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "天气服务-实时数据DTO")
public class WeatherNowWeatherDTO {

    @Schema(description = "温度")
    private BigDecimal temperature;

    @Schema(description = "相对湿度")
    private BigDecimal humidity;

    @Schema(description = "天气现象文字")
    private String text;

}
