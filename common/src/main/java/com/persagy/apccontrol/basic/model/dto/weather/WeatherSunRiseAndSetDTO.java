package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 天气服务日出日落时间DTO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "天气服务日出日落时间DTO")
public class WeatherSunRiseAndSetDTO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "日出时间")
    private LocalDateTime sunRise;

    @Schema(description = "日落时间")
    private LocalDateTime sunSet;

}
