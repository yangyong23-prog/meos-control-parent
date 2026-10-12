package com.persagy.apccontrol.basic.model.vo.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界空间对象VO
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界空间对象VO")
public class DtpRwdSpaceVO extends DtpRwdObjectVO {

    @Schema(description = "roomFuncType")
    private String roomFuncType;
    
    @Schema(description = "建筑ID")
    private String buildingId;
    
    @Schema(description = "floorId")
    private String floorId;

    @Schema(description = "bimId")
    private String bimId;

    @Schema(description = "bimLocation")
    private String bimLocation;

    @Schema(description = "outline")
    private String outline;

}
