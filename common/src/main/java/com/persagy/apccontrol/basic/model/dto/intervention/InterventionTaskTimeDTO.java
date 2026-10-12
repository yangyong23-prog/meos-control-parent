package com.persagy.apccontrol.basic.model.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Schema(title = "干预时间")
@NoArgsConstructor
@AllArgsConstructor
public class InterventionTaskTimeDTO {
    
    @Schema(description = "开始生效时间")
    private LocalDateTime beginTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
