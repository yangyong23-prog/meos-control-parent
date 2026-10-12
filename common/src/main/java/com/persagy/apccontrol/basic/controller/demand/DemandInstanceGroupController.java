package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryByInstanceIdParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandTreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceGroupVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeObjectVO;
import com.persagy.apccontrol.basic.service.demand.instancegroup.DemandInstanceGroupService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 需求管理-编组接口
 */
@RestController
@RequestMapping("/demand/instanceGroup")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupController {

    private final DemandInstanceGroupService demandInstanceGroupService;

    @Operation(summary = "查询-实例树", tags = VersionConstant.V7_250202_0)
    @PostMapping("/treeBuildingFloorInstances")
    public R<DemandTreeObjectVO> listConfigGroups(@RequestBody @Valid DemandTreeBuildingFloorSpaceQueryParam param) {
        return R.success(demandInstanceGroupService.treeBuildingFloorInstances(param));
    }

    @Operation(summary = "查询-实例编组列表", tags = VersionConstant.V7_250602_0)
    @PostMapping("/list")
    public R<List<DemandInstanceGroupVO>> list(@RequestBody @Valid DemandInstanceGroupQueryParam param) {
        return R.success(demandInstanceGroupService.listInstanceGroup(param));
    }

    @Operation(summary = "查询-实例编组列表", tags = VersionConstant.V7_250602_0)
    @PostMapping("/listByInstanceIds")
    public R<List<DemandInstanceGroupVO>> list(@RequestBody @Valid DemandInstanceGroupQueryByInstanceIdParam param) {
        return R.success(demandInstanceGroupService.listInstanceGroupByInstanceId(param));
    }

}
