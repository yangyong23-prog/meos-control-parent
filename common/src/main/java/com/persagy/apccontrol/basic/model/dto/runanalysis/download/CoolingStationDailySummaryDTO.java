package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 冷站日汇总数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class CoolingStationDailySummaryDTO {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 温度满足率 (%)
     */
    private BigDecimal temperatureSatisfactionRate;

    /**
     * 实际能耗 (kWh)
     */
    private BigDecimal actualEnergyConsumption;

    /**
     * 参考能耗-基准年同日实际能耗 (kWh)
     */
    private BigDecimal referenceEnergyConsumption;

    /**
     * 参考能耗-基准年同日日期 (格式：YYYY/M/D)
     */
    private String referenceDate;
}
