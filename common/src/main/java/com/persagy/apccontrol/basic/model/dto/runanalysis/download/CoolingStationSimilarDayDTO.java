package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 冷站参考相似日能耗数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class CoolingStationSimilarDayDTO {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 日期 (格式：YYYY/M/D)
     */
    private String date;

    /**
     * 参考能耗值 (kWh)
     */
    private BigDecimal referenceEnergyValue;

    /**
     * 全天-室外平均温度
     */
    private BigDecimal allDayAvgTemp;

    /**
     * 全天-室外最高温度
     */
    private BigDecimal allDayMaxTemp;

    /**
     * 全天-室外平均湿度
     */
    private BigDecimal allDayAvgHumidity;
}
