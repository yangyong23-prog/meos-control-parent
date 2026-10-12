package com.persagy.apccontrol.basic.model.dto.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 安防特殊的字段
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/7 10:13
 * @since v5.240801.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界对象设备DTO")
@FieldNameConstants
public class SecurityDtpRwdEquipmentDTO extends DtpRwdObjectDTO {

    @Nullable
    @Schema(description = "是否有布撤防功能")
    private String hasArmingFunc;

    @Schema(description = "是否可控制, 摄像头专用")
    private String isControl;

    @Nullable
    @Schema(description = "摄像头类型")
    private String cameraType;

    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "bimId")
    private String bimId;

    @Schema(description = "bimLocation")
    private String bimLocation;

    @Schema(description = "所属厂家")
    private String manufacturer;

    /**
     * 空间功能类型
     */
    @Schema(description = "空间功能类型")
    private List<String> roomFuncTypes;

}
