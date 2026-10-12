package com.persagy.apccontrol.basic.model.entity.intervention;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class InterventionConfigEntity {

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型
     */
    private InstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 干预数据
     */
    private JsonNode configData;

    private String reason;

    /**
     * 干预状态
     */
    private InterventionStatusEnum interventionStatus;

    /**
     * 继承列表
     */
    private List<InterventionInheritConfigDesc> inherit;

}
