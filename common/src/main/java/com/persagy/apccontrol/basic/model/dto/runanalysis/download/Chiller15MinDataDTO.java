package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 逐15min冷机数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class Chiller15MinDataDTO {

    /**
     * 时间点 (格式：YYYY/M/D HH:MM-HH:MM)
     */
    private String timePoint;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 运行状态
     */
    private String runStatus;

    /**
     * 运行负载率 (%)
     */
    private BigDecimal iPLR;

    /**
     * 设备大小机类型 (大机/小机/中1/中2等)
     */
    private String equipmentSizeType;
}
