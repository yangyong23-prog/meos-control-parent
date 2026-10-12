package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 逐15min冷却塔风机数据DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class CoolingTowerFan15MinDataDTO {

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
     * 运行频率 (Hz)
     */
    private BigDecimal freq;

    /**
     * 运行频率设定 (Hz)
     */
    private BigDecimal equipFreqSet;

    /**
     * 运行档位
     */
    private String fanGear;

    /**
     * 运行档位设定
     */
    private String equipGearSet;
}
