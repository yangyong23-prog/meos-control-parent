package com.persagy.apccontrol.controller;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.common.service.command.InitManualAutoMaticService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/9 7:33 PM
 */
@Tag(name = "系统运行看板")
@Validated
@RestController
@RequestMapping("/autoMatic")
@Slf4j
public class AutoMaticController {

    @Autowired
    private InitManualAutoMaticService initManualAutoMaticService;

    @Operation(summary = "根据系统、末级泵组去redis库里面，删除自动模式", tags = VersionConstant.V5_9_0)
    @GetMapping("/dataAutomatic")
    public R<JSONArray> dataAutomatic() {
        try {
            initManualAutoMaticService.setColdSourceManualAutoMatic();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success(null);
    }
}
