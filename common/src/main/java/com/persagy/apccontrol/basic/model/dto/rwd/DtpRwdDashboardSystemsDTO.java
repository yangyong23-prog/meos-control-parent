package com.persagy.apccontrol.basic.model.dto.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

/**
 * 物理世界系统DTO
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界系统DTO")
@FieldNameConstants
public class DtpRwdDashboardSystemsDTO extends DtpRwdObjectDTO {
    
    @Schema(description = "classCode对应名称")
    private String classCodeName;
    
    @Schema(description = "系统下的设备")
    private Set<DtpRwdDashboardElectricityDTO> equipments;
}
