package com.persagy.apccontrol.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupParam;
import com.persagy.apccontrol.basic.model.param.validatedinterface.*;
import com.persagy.apccontrol.basic.service.demand.instancegroup.DemandInstanceGroupService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 需求管理-编组接口
 */
@RestController
@RequestMapping("/demand/instanceGroup")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupCloudController {

    private final DemandInstanceGroupService demandInstanceGroupService;

    @Operation(summary = "新增编组", tags = VersionConstant.V7_250602_0)
    @PostMapping("/add")
    public R<Boolean> addInstanceGroup(@RequestBody @Validated(CreateGroup.class) DemandInstanceGroupParam param) {
        return R.success(demandInstanceGroupService.addInstanceGroup(param));
    }

    @Operation(summary = "编辑编组", tags = VersionConstant.V7_250602_0)
    @PostMapping("/edit")
    public R<Boolean> editInstanceGroup(@RequestBody @Validated(UpdateGroup.class) DemandInstanceGroupParam param) {
        return R.success(demandInstanceGroupService.editInstanceGroup(param));
    }

    @Operation(summary = "删除编组", tags = VersionConstant.V7_250602_0)
    @PostMapping("/del")
    public R<Boolean> deleteInstanceGroup(@RequestBody @Validated(DeleteGroup.class) DemandInstanceGroupParam param) {
        return R.success(demandInstanceGroupService.deleteInstanceGroup(param));
    }

    @Operation(summary = "复制编组", tags = VersionConstant.V7_250602_0)
    @PostMapping("/copy")
    public R<Boolean> copyInstanceGroup(@RequestBody @Validated(CopyGroup.class) DemandInstanceGroupParam param) {
        return R.success(demandInstanceGroupService.copyInstanceGroup(param));
    }

    @Operation(summary = "校验-编组名称是否存在", tags = VersionConstant.V7_250602_0)
    @PostMapping("/checkNameExists")
    public R<Boolean> checkNameExists(@RequestBody @Validated(CheckGroup.class) DemandInstanceGroupParam param) {
        return R.success(demandInstanceGroupService.checkNameExists(param));
    }

}
