package com.persagy.apccontrol.basic.model.entity.intervention;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import lombok.Data;

@Data
public class InterventionInheritConfigDesc {

    /**
     * 所继承对象的实例id
     */
    private String instanceId;
    /**
     * 实例名字
     */
    private String instanceName;

    private InstanceTypeEnum instanceType;
}
