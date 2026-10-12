package com.persagy.apccontrol.basic.model.entity.intervention;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class InterventionInfoQueryParamEntity {


    @NotNull
    private String projectId;


    /**
     * 干预起止时间
     */
    @NotNull
    private LocalDateTime beginDateTime;
    /**
     * 干预截至时间
     */
    @NotNull
    private LocalDateTime endDateTime;

    private Set<String> instanceIds;

    @NotNull
    private DemandInstanceTypeEnum instanceType;
    @NotNull
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
}
