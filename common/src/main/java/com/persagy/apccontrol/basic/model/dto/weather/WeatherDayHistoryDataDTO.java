package com.persagy.apccontrol.basic.model.dto.weather;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天气服务 - 日历史数据 DTO
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
public class WeatherDayHistoryDataDTO {

    /**
     * 降水量 (mm)
     */
    private BigDecimal rainfall;

    /**
     * 白天天气现象编码
     */
    private String codeDay;

    /**
     * 日期时间
     */
    private LocalDateTime dayTime;

    /**
     * 风向角度
     */
    private BigDecimal windDirectionDegree;

    /**
     * 最高温度 (℃)
     */
    private BigDecimal high;

    /**
     * 白天天气现象文字
     */
    private String textDay;

    /**
     * 降水量 (mm)
     */
    private BigDecimal precip;

    /**
     * 夜间天气现象编码
     */
    private String codeNight;

    /**
     * 最低温度 (℃)
     */
    private BigDecimal low;

    /**
     * 夜间天气现象文字
     */
    private String textNight;

    /**
     * 相对湿度 (%)
     */
    private BigDecimal humidity;

    /**
     * 风向
     */
    private String windDirection;

    /**
     * 风速 (km/h)
     */
    private BigDecimal windSpeed;

    /**
     * 风力等级
     */
    private String windScale;

    /**
     * 获取天气现象文字
     *
     * @return 天气现象文字 Pair，如果白天或夜间天气现象文字为空则返回 null
     * @since 7.260302.0
     */
    public Pair<String, String> getTextPair() {
        return Pair.of(this.textDay, this.textNight);
    }
}
