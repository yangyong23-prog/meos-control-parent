package com.persagy.apccontrol.basic.controller.objectgroup;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupNameParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupParam;
import com.persagy.apccontrol.basic.model.vo.objectgroup.ObjectGroupVO;
import com.persagy.apccontrol.basic.service.objectgroup.ObjectGroupService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备编组控制器
 *
 * @author MuAnFu
 * @date 2025-04-18
 */
@RestController
@RequestMapping(value = "/objectGroup")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObjectGroupController {

    private final ObjectGroupService objectGroupService;

    @Operation(summary = "查询编组", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/queryGroup")
    public R<List<ObjectGroupVO>> queryGroup(@RequestBody @Validated QueryObjectGroupParam groupType) {
        return R.success(objectGroupService.queryGroup(groupType));
    }


    @Operation(summary = "验证编组名称是否存在", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/hasExistsGroupName")
    public R<Boolean> hasExistsGroupName(@RequestBody @Validated QueryObjectGroupNameParam param) {
        return R.success(objectGroupService.hasExistsGroupName(param));
    }
}
