package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 物理世界设备DTO
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界设备DTO")
@FieldNameConstants
public class VentilationDtpRwdEquipmentVO extends DtpRwdEquipmentDTO {
    
    
    @Schema(description = "funcType")
    private String funcType;
    
    @Schema(description = "监测监控状态")
    private String monitoringSurveillanceStatus;
    
}
