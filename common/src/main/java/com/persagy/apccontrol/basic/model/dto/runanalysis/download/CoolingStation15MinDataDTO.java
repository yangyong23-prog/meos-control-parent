package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 逐15min冷站数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class CoolingStation15MinDataDTO {

    /**
     * 时间点 (格式：YYYY/M/D HH:MM-HH:MM)
     */
    private String timePoint;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 冷冻水供供回水温差设定
     */
    private BigDecimal chillWaterDeltaTempSet;

    /**
     * 冷冻水供回水温差
     */
    private BigDecimal chillWaterDeltaTemp;

    /**
     * 冷冻水一次侧供水干管温度壳配置下限值
     */
    private BigDecimal primaryChillWaterSupplyTempLowerLimit;

    /**
     * 冷冻水一次侧供水干管温度
     */
    private BigDecimal primaryChillWaterMainSupplyTemp;

    /**
     * 冷冻水一次侧回水干管温度
     */
    private BigDecimal primaryChillWaterMainReturnTemp;

    /**
     * 冷却塔出水温度设定
     */
    private BigDecimal coolingTowerOutTempSet;

    /**
     * 冷机冷却水进水干管温度
     */
    private BigDecimal coolWaterMainReturnTemp;

    /**
     * 冷机冷却水出水干管温度
     */
    private BigDecimal coolWaterMainSupplyTemp;

    /**
     * 冷却水进出水温差设定
     */
    private BigDecimal coolWaterDeltaTempSet;

    /**
     * 冷却水进出水温差
     */
    private BigDecimal coolWaterDeltaTemp;

    /**
     * 免费冷开关设定
     */
    private String freeCoolingSwitchSet;

    /**
     * 供冷开关设定
     */
    private String coolingSupplySwitchSet;
}
