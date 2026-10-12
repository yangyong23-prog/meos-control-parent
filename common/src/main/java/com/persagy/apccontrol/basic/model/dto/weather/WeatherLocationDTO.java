package com.persagy.apccontrol.basic.model.dto.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 天气服务-地理位置DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "天气服务-地理位置DTO")
public class WeatherLocationDTO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "地理位置名称")
    private String name;

    @Schema(description = "国家编码")
    private String country;

    @Schema(description = "地理位置路径")
    private String path;

    @Schema(description = "时区")
    private String timezone;

    @Schema(description = "时区偏移量")
    private String timezoneOffset;

}
