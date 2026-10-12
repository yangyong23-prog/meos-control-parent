package com.persagy.apccontrol.basic.model.dto.intervention;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(title = "查询干预时间段-配置DTO")
public class InterventionQueryTimeSlotDto {

    @NotNull
    private LocalDateTime beginDateTime;

    @NotNull
    private LocalDateTime endDateTime;
    /**
     * 干预对象id
     */
    @NotNull
    private String instanceId;

    @NotNull
    private DemandInstanceTypeEnum instanceType;

    @NotNull
    private RunningMonitorSecondTypeEnum runMonitorSecondType;
}
