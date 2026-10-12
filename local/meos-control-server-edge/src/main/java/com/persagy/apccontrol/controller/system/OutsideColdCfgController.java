package com.persagy.apccontrol.controller.system;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.common.model.param.OutsideCoolWaterReturnTempCfgParam;
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
 * 外部接口
 */
@Slf4j
@RestController
@Tag(name = "冷站配置相关-外部接口")
@RequestMapping(value = "/outside/coldCfg")
public class OutsideColdCfgController {
    
    @Resource
    private ColdCfgService coldService;
    
    @Operation(summary = "外部查询-冷却塔出水温度配置", tags = {
            VersionConstant.V7_250901_0,
            ExternalServerConstant.HR_MEOS_AUTOCONTROL_WEB_EDGE
    })
    @PostMapping("/coolWaterReturnTemp")
    public R<Boolean> outsideCoolWaterReturnTempCfg(@Validated @RequestBody OutsideCoolWaterReturnTempCfgParam param) {
        coldService.outsideCoolWaterReturnTempCfg(param);
        return R.success(true);
    }
    
    
}
