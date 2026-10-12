package com.persagy.apccontrol.basic.controller.wsd;

import com.persagy.apccontrol.basic.model.param.wsd.QueryFloorSpaceEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.wsd.SpaceEquipmentVO;
import com.persagy.apccontrol.basic.service.wsd.WaterSupplyDrainageService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_241202_0;

/**
 * 给排水
 */
@RestController
@RequestMapping("/waterSupplyDrainage")
public class WaterSupplyDrainageController {


    @Resource
    private WaterSupplyDrainageService waterSupplyDrainageService;


    @Operation(summary = "查询空间设备信息接口", tags = V5_241202_0)
    @PostMapping(value = "/querySpaceEquipmentList")
    public R<List<SpaceEquipmentVO>> querySpaceEquipmentList(
            @RequestBody @Valid QueryFloorSpaceEquipmentParam param) {
        return R.success(waterSupplyDrainageService.querySpaceEquipmentList(param));
    }


}
