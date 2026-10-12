package com.persagy.apccontrol.controller.system;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.common.model.param.CoolWaterReturnTempCfgParam;
import com.persagy.apccontrol.common.model.param.ExtendInfoCodeValueConfigParam;
import com.persagy.apccontrol.service.ColdCfgService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 冷站配置相关
 */
@Slf4j
@RestController
@Tag(name = "冷站配置相关")
@RequestMapping(value = "/coldCfg")
public class ColdCfgController {

    @Resource
    private ColdCfgService coldService;

    @Operation(summary = "冷却塔出水温度配置", tags = {VersionConstant.V7_250202_0})
    @PostMapping("/coolWaterReturnTemp")
    public R<Boolean> coolWaterReturnTempCfg(@Validated @RequestBody CoolWaterReturnTempCfgParam param) {
        coldService.coolWaterReturnTempCfg(param);
        return R.success(true);
    }
    
    @Operation(summary = "供冷系统扩展信息点值配置", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/extendInfoCodeValueConfig")
    public R<Boolean> extendInfoCodeValueConfig(@Validated @RequestBody ExtendInfoCodeValueConfigParam param) {
        coldService.extendInfoCodeValueConfig(param);
        return R.success(true);
    }

}
