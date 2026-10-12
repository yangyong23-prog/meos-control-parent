package com.persagy.apccontrol.basic.model.vo.weakpower;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 运行监测-弱电设备DTO
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RunningMonitorWeakPowerEquipmentDTO extends DtpRwdEquipmentDTO {

    /**
     * 子集
     */
    private List<RunningMonitorWeakPowerEquipmentDTO> children;

}
