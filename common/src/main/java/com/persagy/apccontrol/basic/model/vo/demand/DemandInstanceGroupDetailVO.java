package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class DemandInstanceGroupDetailVO extends DtpRwdEquipmentDTO {

    /**
     * 实例类型
     */
    private InstanceTypeEnum instanceType;
}
