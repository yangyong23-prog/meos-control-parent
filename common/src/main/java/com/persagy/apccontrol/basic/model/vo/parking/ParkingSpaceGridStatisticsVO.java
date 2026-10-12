package com.persagy.apccontrol.basic.model.vo.parking;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ParkingSpaceGridStatisticsVO {


    @Schema(description = "建筑id")
    private String buildingId;

    @Schema(description = "建筑名")
    private String buildingName;

    @Schema(description = "楼层车位统计")
    private List<FloorParkingSpaceStatisticVO> floorParkingSpaceStatistics;

}
