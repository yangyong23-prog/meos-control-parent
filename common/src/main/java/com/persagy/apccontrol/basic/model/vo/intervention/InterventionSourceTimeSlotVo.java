package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class InterventionSourceTimeSlotVo {

    private DemandInstanceTypeEnum instanceType;

    private List<TimeSlotEntity> timeSlot;

}
