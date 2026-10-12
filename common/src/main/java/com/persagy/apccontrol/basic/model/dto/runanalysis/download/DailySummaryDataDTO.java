package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 日汇总数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class DailySummaryDataDTO {

    /**
     * 日期 (格式：YYYY/M/D)
     */
    private String date;

    /**
     * 营业时间段 (格式：HH:MM-HH:MM)
     */
    private String businessTimeRange;

    /**
     * 全天-室外平均温度
     */
    private BigDecimal allDayAvgTemp;

    /**
     * 全天-室外最高温度
     */
    private BigDecimal allDayMaxTemp;

    /**
     * 全天-室外最低温度
     */
    private BigDecimal allDayMinTemp;

    /**
     * 全天-室外平均湿度
     */
    private BigDecimal allDayAvgHumidity;

    /**
     * 全天-室外最高湿度
     */
    private BigDecimal allDayMaxHumidity;

    /**
     * 全天-室外最低湿度
     */
    private BigDecimal allDayMinHumidity;

    /**
     * 全天-室外平均湿球温度
     */
    private BigDecimal allDayAvgWetBulbTemp;

    /**
     * 全天-室外平均焓值
     */
    private BigDecimal allDayAvgEnthalpy;

    /**
     * 营业时段内-室外平均温度
     */
    private BigDecimal businessHoursAvgTemp;

    /**
     * 营业时段内-室外温度最高值
     */
    private BigDecimal businessHoursMaxTemp;

    /**
     * 营业时段内-室外温度最低值
     */
    private BigDecimal businessHoursMinTemp;

    /**
     * 营业时段内-室外平均湿度
     */
    private BigDecimal businessHoursAvgHumidity;

    /**
     * 营业时段内-室外湿度最高值
     */
    private BigDecimal businessHoursMaxHumidity;

    /**
     * 营业时段内-室外湿度最低值
     */
    private BigDecimal businessHoursMinHumidity;

    /**
     * 营业时段内-室外平均湿球温度
     */
    private BigDecimal businessHoursAvgWetBulbTemp;

    /**
     * 营业时段内-室外平均焓值
     */
    private BigDecimal businessHoursAvgEnthalpy;
}
