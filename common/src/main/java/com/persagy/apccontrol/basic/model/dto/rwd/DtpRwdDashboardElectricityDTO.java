package com.persagy.apccontrol.basic.model.dto.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

/**
 * 物理世界设备DTO
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界设备DTO")
@FieldNameConstants
public class DtpRwdDashboardElectricityDTO extends DtpRwdEquipmentDTO {
    
    @Schema(description = "类型编码名称")
    private String classCodeName;
    
    @Schema(description = "空间 ID集合")
    private Set<String> spaceIds;
}
