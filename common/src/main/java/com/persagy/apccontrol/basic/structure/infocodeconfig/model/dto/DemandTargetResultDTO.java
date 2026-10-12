package com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 需求目标值DTO
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandTargetResultDTO {

    /**
     * 需求状态
     */
    private AirTerminalDemandStatusEnum demandStatus;

    /**
     * 需求状态值
     */
    private BigDecimal demandStatusValue;

}
