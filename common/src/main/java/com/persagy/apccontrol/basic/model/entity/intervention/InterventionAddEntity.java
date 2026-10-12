package com.persagy.apccontrol.basic.model.entity.intervention;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * @author macbook
 * @title: InterventionAddEntity
 * @projectName meos-control-parent
 * @date 2024/7/2221:38
 */
@Data
public class InterventionAddEntity {

    private Long id;


    private String projectId;


    /**
     * 被干预的实例
     */
    private String instanceId;

    private DemandInstanceTypeEnum instanceType;

    private InterventionStatusEnum interventionStatus;
    /**
     * 具体的干预配置
     */
    private JsonNode interventionConfig;
    /**
     * 干预原因
     */
    private String reason;
    /**
     * 干预类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    private List<TimeSlotEntity> timeSlot;
}