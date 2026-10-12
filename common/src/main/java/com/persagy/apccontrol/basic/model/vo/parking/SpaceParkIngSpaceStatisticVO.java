package com.persagy.apccontrol.basic.model.vo.parking;

import com.persagy.apccontrol.basic.model.enumtype.parking.ParkingSpaceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class SpaceParkIngSpaceStatisticVO {


    @Schema(description = "空间对象id")
    private String spaceId;

    @Schema(description = "空间名")
    private String spaceName;

    @Schema(description = "营业时间 10:00-20:00")
    private List<String> workTime;

    @Schema(description = "车位统计")
    private Map<String, Integer> parkIngSpaceStatistics = new LinkedHashMap<>();

    public void initData() {
        for (ParkingSpaceTypeEnum parkingSpaceTypeEnum : ParkingSpaceTypeEnum.values()) {
            parkIngSpaceStatistics.put(parkingSpaceTypeEnum.value, 0);
        }
    }

    public int sum() {
        return parkIngSpaceStatistics.getOrDefault(ParkingSpaceTypeEnum.PARKING_SPACE.value(), 0);
    }
}
