package com.persagy.apccontrol.basic.model.vo.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FloorParkingSpaceStatisticVO {


    @Schema(description = "楼层id")
    private String floorId;

    @Schema(description = "楼层名")
    private String floorName;

    @Schema(description = "充电车位总数")
    private Integer chargingParkingSpaceCount = 0;

    @Schema(description = "异常总数")
    private Integer exceptionCount = 0 ;


    @Schema(description = "车位总数")
    private Integer parkIngSpaceCount = 0;

    @Schema(description = "空间车位统计")
    private List<SpaceParkIngSpaceStatisticVO> spaceParkIngSpaceStatistics;

    @Schema(description = "空闲充电车位总数")
    private Integer vacantChargingParkingSpace = 0;

    @Schema(description = "空闲车位总数")
    private Integer vacantParkIngSpaceCount = 0;
}
