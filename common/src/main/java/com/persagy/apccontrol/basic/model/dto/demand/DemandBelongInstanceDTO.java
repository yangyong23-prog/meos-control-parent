package com.persagy.apccontrol.basic.model.dto.demand;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import lombok.Data;

@Data
public class DemandBelongInstanceDTO {

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 实例类型
     */
    private InstanceTypeEnum instanceType;

    /** 序列 1:项目，2:建筑，3:楼层*/
    private Integer sequence;

    /** 楼层序列 */
    private Integer floorSequence;



}
