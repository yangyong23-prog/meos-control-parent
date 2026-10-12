package com.persagy.apccontrol.basic.model.vo.instance;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 设备VO
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EquipmentVO extends DtpRwdEquipmentDTO {

    /**
     * 是否在运行监测策略中
     */
    private Boolean inStrategy;

    /**
     * 是否为可运行设备
     */
    private Boolean operableRunning;

    /**
     * 设备类名称
     */
    private String classCodeName;

    /**
     * 专业名称
     */
    private String majorName;

}
