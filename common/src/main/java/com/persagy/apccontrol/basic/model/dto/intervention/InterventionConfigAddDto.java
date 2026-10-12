package com.persagy.apccontrol.basic.model.dto.intervention;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "干预配置新增-配置DTO")
public class InterventionConfigAddDto {

    private Long id;


    /**
     * 被干预的实例
     */
    private String instanceId;

    private DemandInstanceTypeEnum instanceType;
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
