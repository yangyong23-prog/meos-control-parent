package com.persagy.apccontrol.basic.model.vo.sewage;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @description: 污水设备
 * @version: 1.0
 * @author: wxf
 * @create: 2024-12-16 19:10
 **/
@Data
public class SewageEquipVO extends DtpRwdEquipmentVO {
    @Schema(description = "楼层名")
    private String floorName;

    @Schema(description = "建筑名")
    private String buildingName;

    @Schema(description = "空间id")
    private String spaceId;

    @Schema(description = "空间名")
    private String spaceName;

    @Schema(description = "可监可控")
    private String monitoringSurveillanceStatus;

    @Schema(description = "关联的设备")
    private List<DtpRwdEquipmentVO> relEquipment;
}