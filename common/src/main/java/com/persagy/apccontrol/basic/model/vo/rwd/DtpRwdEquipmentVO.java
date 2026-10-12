package com.persagy.apccontrol.basic.model.vo.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界设备对象VO
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界设备对象VO")
public class DtpRwdEquipmentVO extends DtpRwdObjectVO {

    @Schema(description = "floorId")
    private String floorId;

    @Schema(description = "buildingId")
    private String buildingId;

    @Schema(description = "bimId")
    private String bimId;

    @Schema(description = "bimLocation")
    private String bimLocation;

}
