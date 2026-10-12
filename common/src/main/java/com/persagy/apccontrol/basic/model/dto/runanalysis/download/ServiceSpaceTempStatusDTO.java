package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 逐15min冷站服务空间的温度状态DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class ServiceSpaceTempStatusDTO {

    /**
     * 时间点 (格式：YYYY/M/D HH:MM-HH:MM)
     */
    private String timePoint;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 温度满足率 (%)
     */
    private BigDecimal temperatureSatisfactionRate;

    /**
     * 过冷空间数量 (个)
     */
    private Integer tooColdSpaceCount;

    /**
     * 偏冷空间数量 (个)
     */
    private Integer slightlyColdSpaceCount;

    /**
     * 舒适空间数量 (个)
     */
    private Integer comfortableSpaceCount;

    /**
     * 偏热空间数量 (个)
     */
    private Integer slightlyHotSpaceCount;

    /**
     * 过热空间数量 (个)
     */
    private Integer tooHotSpaceCount;

    /**
     * 无数据空间数量 (个)
     */
    private Integer noDataSpaceCount;

    /**
     * 总空间数量 (个)
     */
    private Integer totalSpaceCount;

    /**
     * 过冷空间数量占比 (%)
     */
    private BigDecimal tooColdSpacePercentage;

    /**
     * 偏冷空间数量占比 (%)
     */
    private BigDecimal slightlyColdSpacePercentage;

    /**
     * 舒适空间数量占比 (%)
     */
    private BigDecimal comfortableSpacePercentage;

    /**
     * 偏热空间数量占比 (%)
     */
    private BigDecimal slightlyHotSpacePercentage;

    /**
     * 过热空间数量占比 (%)
     */
    private BigDecimal tooHotSpacePercentage;

    /**
     * 无数据空间数量占比 (%)
     */
    private BigDecimal noDataSpacePercentage;
}
