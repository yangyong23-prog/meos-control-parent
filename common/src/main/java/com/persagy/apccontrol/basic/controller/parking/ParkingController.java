package com.persagy.apccontrol.basic.controller.parking;

import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingEquipStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingFloorParkingSpaceStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingSpaceGridStatisticsVO;
import com.persagy.apccontrol.basic.service.parking.ParkingService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240901_0;

@RestController
@RequestMapping(value = "/parking")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(value = "停车场", tags = V5_240901_0)
public class ParkingController {

    private final ParkingService parkingService;

    @Operation(summary = "项目车位网格图统计", tags = V5_240901_0)
    @PostMapping("/parkingSpace/grid/statistics")
    public R<List<ParkingSpaceGridStatisticsVO>> getParkingSpaceGridStatistics(@RequestBody @Validated ParkingSpaceStatisticsQueryParam param) {
        return R.success(parkingService.getParkingSpaceGridStatistics(param,true));
    }

    @Operation(summary = "停车场设备统计", tags = V5_240901_0)
    @PostMapping("/equipment/statistics")
    public R<List<ParkingEquipStatisticsVO>> getParkingEquipStatistics(@RequestBody @Validated ParkingSpaceStatisticsQueryParam param) {
        return R.success( parkingService.getParkingEquipStatistics(param));
    }

    @Operation(summary = "楼层的车位统计", tags = V5_240901_0)
    @PostMapping("/floor/parkingSpace/statistics")
    public R<List<ParkingFloorParkingSpaceStatisticsVO>> getFloorParkingSpaceStatistics(@RequestBody @Validated ParkingSpaceStatisticsQueryParam param) {
       return R.success(parkingService.getFloorParkingSpaceStatistics(param));
    }

    @Operation(summary = "车位统计", tags = V5_240901_0)
    @PostMapping("/parkingSpace/statistics")
    public R<Map<String, Integer>> getParkingSpaceStatistics(@RequestBody @Validated ParkingSpaceStatisticsQueryParam param) {
        return R.success(parkingService.getParkingSpaceStatistics(param));
    }
}
