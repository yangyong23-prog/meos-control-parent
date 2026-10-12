package com.persagy.apccontrol.basic.model.vo.parking;

import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ParkingSpaceStatisticsVO {

    @Schema(description = "车位数量")
    private int count;

    @Schema(description = "车位类型编码")
    private ParkingSpaceTypeEnum parkSpaceType;
}
