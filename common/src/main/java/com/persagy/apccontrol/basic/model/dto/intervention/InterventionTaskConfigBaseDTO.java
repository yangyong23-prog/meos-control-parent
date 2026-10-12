package com.persagy.apccontrol.basic.model.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "干预任务配置-配置DTO")
public class InterventionTaskConfigBaseDTO {
    
    @Schema(description = "任务 ID")
    private Long taskId;
    
}
