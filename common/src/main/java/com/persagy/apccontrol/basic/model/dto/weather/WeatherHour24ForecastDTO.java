package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.TreeSet;

/**
 * 天气服务-24小时预报数据DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "天气服务-24小时预报数据DTO")
public class WeatherHour24ForecastDTO {

    @Schema(description = "地理位置")
    private WeatherLocationDTO location;

    @Schema(description = "小时天气数据")
    private TreeSet<WeatherHourlyDTO> hourlies;

}
