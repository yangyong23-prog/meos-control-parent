package com.persagy.apccontrol.basic.model.dto.intervention;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Schema(title = "干预需求详配置详情-配置DTO")
public class InterventionInfoQueryListParamDto {


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

    @NotNull
    private Set<String> instanceIds;

    @NotNull
    private DemandInstanceTypeEnum instanceType;
    @NotNull
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
}
