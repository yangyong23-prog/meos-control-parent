package com.persagy.apccontrol.basic.model.vo.parking;

import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ParkingEquipStatusStatisticsVO {


    @Schema(description = "对象数量")
    private int count;

    @Schema(description = "状态编码")
    private SecurityEquipmentStatusEnum objectCode;

    @Schema(description = "状态名")
    private String objectName;

}
