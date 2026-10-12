package com.persagy.apccontrol.basic.model.vo.parking;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ParkingFloorParkingSpaceStatisticsVO {

    @Schema(description = "空间id")
    private String spaceId;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "车位总数")
    private int count;

    @Schema(description = "营业起始时间")
    private LocalDateTime startTime;

    @Schema(description = "营业结束时间")
    private LocalDateTime endTime;

    @Schema(description = "车位统计列表")
    private List<ParkingSpaceStatisticsVO> list;


}
