package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandAirconditioningShellConfigDataQueryParam;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 需求管理-空调需求-自定义接口
 */
@RestController
@RequestMapping("/demand/airconditioning")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandAirconditioningController {

    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;

    @Deprecated
    @Operation(summary = "查询-空调工况与壳配置综合后的配置", tags = VersionConstant.V5_240902_0)
    @PostMapping("/getWorkConditionByShellConfigData")
    public R<DemandConfigAirconditioningWorkConditionValueObject> treeControlledObject(@RequestBody @Valid DemandAirconditioningShellConfigDataQueryParam queryParam) {
        return R.success(this.demandConfigAirconditioningCustomService.getShellConfigData(queryParam.getProjectId(), queryParam.getTemplateId(), queryParam.getDate()));
    }

    @Operation(summary = "兼容价值选择配置", tags = VersionConstant.V5_240902_0)
    @PostMapping("/fixHistoryValueSelectConfig")
    public R<Boolean> compatibleValueSelectConfig() {
        return R.success(demandConfigAirconditioningCustomService.compatibleValueSelectConfig());
    }

}
