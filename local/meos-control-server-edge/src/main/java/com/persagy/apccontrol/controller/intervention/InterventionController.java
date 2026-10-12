package com.persagy.apccontrol.controller.intervention;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigAddDto;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionReleaseParam;
import com.persagy.apccontrol.basic.service.intervention.InterventionConfigQueryService;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.common.service.intervention.InterventionConfigOperationService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 需求干预接口
 *
 * @author yuecaipu
 * @date 2024/7/23 16:01
 * @since v5.240402.00
 */
@Slf4j
@RestController("localInterventionController")
@RequestMapping(value = "/intervention")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionController<T extends InterventionConfigData> {

    private final InterventionConfigQueryService<T> interventionConfigQueryService;
    private final InterventionConfigOperationService configChangeApplicationService;

    @PostMapping("/transferInterventionConfigToCloud")
    public R<Boolean> transferInterventionConfigToCloud() {
        return R.success(configChangeApplicationService.transferInterventionConfigToCloud());
    }

    @Operation(summary = "解除干预", tags = {VersionConstant.V5_240702_0})
    @PostMapping("/release")
    public R<Boolean> release(@RequestBody @Valid InterventionReleaseParam param) {
        return configChangeApplicationService.release(param);
    }

    @Operation(summary = "新增or更新干预配置", tags = {VersionConstant.V5_240702_0})
    @PostMapping("/config/saveOrUpdate")
    public R<Boolean> interventionAddConfig(@Validated @RequestBody InterventionConfigAddDto addDto){
        return R.success(configChangeApplicationService.interventionSaveOrUpdate(addDto));
    }

    @Operation(summary = "校验配置", tags = {VersionConstant.V5_240702_0})
    @PostMapping("/config/valid")
    public R<Boolean> interventionValidConfig(@Validated @RequestBody InterventionConfigAddDto addDto){
        return R.success(interventionConfigQueryService.validatedAddConfigSlot(addDto));
    }
}
