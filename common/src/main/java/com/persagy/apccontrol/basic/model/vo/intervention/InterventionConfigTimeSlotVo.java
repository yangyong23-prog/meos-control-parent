package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import lombok.Data;

import java.util.List;

@Data
public class InterventionConfigTimeSlotVo {

    private List<TimeSlotEntity>aggregation ;

    private List<TimeSlotEntity> demand;
    /**

     */
    List<InterventionSourceTimeSlotVo> source;
}
