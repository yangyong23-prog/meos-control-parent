package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天气服务-实时数据DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "天气服务-历史数据DTO")
public class WeatherHourHistoryDataDTO {

    @Schema(description = "温度")
    private BigDecimal temperature;

    @Schema(description = "相对湿度")
    private BigDecimal humidity;

    @Schema(description = "湿球温度")
    private BigDecimal wetBulbTemp;

    @Schema(description = "天气现象文字")
    private String text;

    @Schema(description = "小时时间")
    private LocalDateTime hourTime;
    
    @Schema(description = "焓值")
    private BigDecimal enthalpyValue;

}
