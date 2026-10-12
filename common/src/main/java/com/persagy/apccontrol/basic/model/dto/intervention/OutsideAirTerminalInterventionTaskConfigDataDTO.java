package com.persagy.apccontrol.basic.model.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 末端干预的配置对象
 * 对外接口返回结果
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OutsideAirTerminalInterventionTaskConfigDataDTO extends InterventionTaskConfigDataDTO {
    
    @Schema(description = "配置数据")
    private AirTerminalInterventionTaskConfigDataDTO config;
    
    @Schema(description = "空间 ID")
    private String spaceId;
    
    @Schema(description = "开始生效时间")
    private LocalDateTime beginTime;
    
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    
}
