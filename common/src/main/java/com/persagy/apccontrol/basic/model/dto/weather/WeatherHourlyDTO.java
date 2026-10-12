package com.persagy.apccontrol.basic.model.dto.weather;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 天气服务-小时天气数据
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "天气服务-小时天气数据")
public class WeatherHourlyDTO implements Comparable<WeatherHourlyDTO> {

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 天气现象文字
     *
     * <p>example: 晴
     */
    private String text;

    /**
     * 天气现象编码
     *
     * <p>example: 0
     */
    private String code;

    /**
     * 温度
     */
    private BigDecimal temperature;

    /**
     * 风向文字
     *
     * <p>example: 南
     */
    private String windDirection;

    /**
     * 风速
     *
     * <p>example: 10
     */
    private BigDecimal windSpeed;

    /**
     * 湿球温度
     */
    @JsonAlias("wet_bulb_temp")
    private BigDecimal wetBulbTemp;
    /**
     * 湿度
     */
    private BigDecimal humidity;
    
    private BigDecimal enthalpyValue;

    @Override
    public int compareTo(@NonNull WeatherHourlyDTO o) {
        return Objects.isNull(time) ? 1 : time.compareTo(o.getTime());
    }
}
