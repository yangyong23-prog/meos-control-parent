package com.persagy.apccontrol.basic.controller.airteminal;

import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;
import com.persagy.apccontrol.basic.service.airterminal.AirStatisticsService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_241201_0;

/**
 * 空调末端统计
 */
@RestController
@RequestMapping(value = "/air/statistics")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirStatisticsController {

    @Resource
    AirStatisticsService airStatisticsService;

    @Operation(summary = "查询-首页设备统计", tags = V5_241201_0)
    @PostMapping(value = "/homePageEquipment")
    public R<List<AirHomePageEquipmentVO>> homePageEquipment(@RequestBody @Valid AirHomePageEquipmentParam param) {
        return R.success(airStatisticsService.homePageEquipment(param));
    }
}
