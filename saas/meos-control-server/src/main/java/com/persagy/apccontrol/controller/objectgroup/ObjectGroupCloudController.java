package com.persagy.apccontrol.controller.objectgroup;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.objectgroup.ObjectGroupParam;
import com.persagy.apccontrol.basic.service.objectgroup.ObjectGroupService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * 设备编组控制器
 *
 * @author MuAnFu
 * @date 2025-04-18
 */
@RestController
@RequestMapping(value = "/objectGroup")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObjectGroupCloudController {

    private final ObjectGroupService objectGroupService;


    @Operation(summary = "编辑编组", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/editGroup")
    public R<Boolean> editGroup(@Validated @RequestBody ObjectGroupParam param) {
        return R.success(objectGroupService.editGroup(param, false));
    }

    @Operation(summary = "删除编组", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/deleteGroup/{id}")
    public R<Boolean> deleteGroup(@PathVariable @NotBlank Long id) {
        return R.success(objectGroupService.deleteGroup(id, false));
    }

}
