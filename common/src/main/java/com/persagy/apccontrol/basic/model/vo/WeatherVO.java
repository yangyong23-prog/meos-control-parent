package com.persagy.apccontrol.basic.model.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 天气数据
 *
 * @author yx
 * @date 2023/3/28
 */
@Data
@Schema(title = "室内温湿度空间目标")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVO {

    @Schema(description = "天气服务配置")
    private String config;

    @JsonAlias({"temperature"})
    @Schema(description = "干球温度")
    private Double dryBulb;

    @Schema(description = "湿球温度")
    private Double wetBulb;

    @JsonAlias({"humidity"})
    @Schema(description = "相对湿度")
    private Double relativeHumidity ;

    @Schema(description = "焓值")
    private Double alkane ;

    @Schema(description = "白日气象编码")
    private String codeDay ;

    @Schema(description = "白日气象文字")
    private String textDay ;

    @Schema(description = "夜晚气象编码")
    private String codeNight ;

    @Schema(description = "夜晚气象文字")
    private String textNight ;

    @Schema(description = "最高温")
    private String maxTemperature ;

    @Schema(description = "最低温")
    private String minTemperature ;

    public WeatherVO(Double dryBulb, Double wetBulb, Double relativeHumidity, Double alkane) {
        this.dryBulb = dryBulb;
        this.wetBulb = wetBulb;
        this.relativeHumidity = relativeHumidity;
        this.alkane = alkane;
    }

}
