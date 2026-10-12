package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 室外天气数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class OutdoorWeatherDataDTO {

    /**
     * 时间点 (格式：YYYY/M/D HH:MM-HH:MM)
     */
    private String timePoint;

    /**
     * 温度
     */
    private BigDecimal temperature;

    /**
     * 湿度
     */
    private BigDecimal humidity;

    /**
     * 湿球温度
     */
    private BigDecimal wetBulbTemp;

    /**
     * 焓值
     */
    private BigDecimal enthalpy;
}
