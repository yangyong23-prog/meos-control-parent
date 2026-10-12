package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeatherDTO {

    @Schema(description = "温度")
    private BigDecimal temperature;

    @Schema(description = "相对湿度")
    private BigDecimal humidity;

    @Schema(description = "湿球温度")
    private BigDecimal wetBulbTemp;

    @Schema(description = "天气现象文字")
    private String text;

    @Schema(description = "时间")
    private LocalDateTime time;

    /**
     * 风向文字
     *
     * <p>example: 南
     */
    @Schema(description = "风向文字")
    private String windDirection;

    /**
     * 风速
     *
     * <p>example: 10
     */
    @Schema(description = "风速")
    private BigDecimal windSpeed;
    
    @Schema(description = "焓值")
    private BigDecimal enthalpyValue;
}
