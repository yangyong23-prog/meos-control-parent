package com.persagy.apccontrol.basic.model.dto.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

/**
 * 物理世界对象DTO
 *
 * @author LiuYang
 * @since v5.240601.0
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界对象设备DTO")
@FieldNameConstants
public class DtpRwdEquipmentDTO extends DtpRwdObjectDTO {

    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "bimId")
    private String bimId;

    @Schema(description = "bimLocation")
    private String bimLocation;

}
