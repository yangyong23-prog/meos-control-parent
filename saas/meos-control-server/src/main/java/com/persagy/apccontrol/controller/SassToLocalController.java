package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.model.vo.LocalityProjectCfgVO;
import com.persagy.apccontrol.service.ISaasToLocalService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Tag(name = "云端与本地端")
@Validated
@RestController
@RequestMapping("/saas/local")
@Slf4j
public class SassToLocalController {

    @Resource
    private ISaasToLocalService iSaasToLocalService;


    @Operation(summary = "查询边缘端ip:端口号")
    @GetMapping("/getLocalBaseUrl/{projectId}")
    public R<LocalityProjectCfgVO> getLocalBaseUrl(@PathVariable String projectId) {
        return R.success(iSaasToLocalService.getLocalBaseUrl(projectId));
    }


    @Operation(summary = "心跳")
    @PostMapping("/sendHeart")
    public R<String> sendHeart(@Valid @RequestParam(value = "heartPackage") String heartPackage) throws Exception {
        return R.success(iSaasToLocalService.handleHeart(heartPackage));
    }


    @Operation(summary = "同步账号")
    @PostMapping("/synuser")
    public R<String> synUser(@Valid @RequestParam(value = "groupCode") String groupCode, @Valid @RequestParam(value = "projectId") String projectId) throws Exception {
        iSaasToLocalService.synUser(groupCode, projectId);
        return R.success("ok");
    }

}
