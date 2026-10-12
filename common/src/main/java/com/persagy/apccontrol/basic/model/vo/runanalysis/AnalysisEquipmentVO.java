package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnalysisEquipmentVO extends DtpRwdEquipmentVO {

    /**
     * 楼层名称
     */
    private String floorName;
    /**
     * 建筑id
     */
    private String buildingId;
    /**
     * 建筑物名称
     */
    private String buildingName;

    /**
     * 所关联的空间id
     */
    private Set<String> relObjIds;
}
