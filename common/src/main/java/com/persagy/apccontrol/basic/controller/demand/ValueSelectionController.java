package com.persagy.apccontrol.basic.controller.demand;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandValueSelectTypeQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.ValueSelectQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectTypeConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandValueSelectTypeVO;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectInsertParam;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/valueSelection")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ValueSelectionController {

    private final ValueSelectionService valueSelectionService;

    @Operation(summary = "查询价值选择类型", tags = {
            VersionConstant.V6_0_0,
            ExternalServerConstant.AI_AUTORUN_AGENT,
            ExternalServerConstant.CUSTOM_YX
    })
    @PostMapping("/listValueSelectType")
    public R<List<DemandValueSelectTypeVO>> listValueSelectType(@RequestBody @Valid DemandValueSelectTypeQueryParam param) {
        return R.success(this.valueSelectionService.listValueSelectType(param));
    }

    @Operation(summary = "查询价值选择类型", tags = {
            VersionConstant.V7_250102_0,
            ExternalServerConstant.AI_AUTORUN_AGENT
    })
    @PostMapping("/listConfigByType")
    public R<Collection<ValueSelectConfigDto>> listConfigsByType(
            @RequestBody @Valid DemandValueSelectTypeConfigQueryParam param) {
        return R.successMulti(valueSelectionService.listConfigsByType(param));
    }

    @Operation(summary = "查询当前温度、CO2满足率", tags = VersionConstant.V5_7_0)
    @PostMapping("/currentTempAndCarbonRate")
    public R<JSONObject> currentTempAndCarbonRate(@RequestBody @Valid ValueSelectQueryParam param) {
        return R.success(this.valueSelectionService.currentTempAndCarbonRate(param));
    }

    @Operation(summary = "查询当前应用的配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/currentApplyConfig")
    public R<RcBasicsConfig> currentApplyConfig(@RequestBody @Valid ValueSelectQueryParam param) {
        return R.success(valueSelectionService.currentApplyConfig(param));
    }

    @Operation(summary = "保存配置", tags = VersionConstant.V5_7_0)
    @PostMapping("/saveConfig")
    public R<Boolean> listConfig(@RequestBody @Valid ValueSelectInsertParam param) {
        return R.success(valueSelectionService.saveConfig(param));
    }

    @Operation(summary = "查询电价数据", tags = VersionConstant.V5_7_0)
    @PostMapping("/listElectricityData")
    public R<ValueSelectionTrendTableDto> listElectricityData(@RequestBody @Valid ValueSelectElecQueryParam param) {
        return R.success(valueSelectionService.listElectricityData(param));
    }

    @Operation(summary = "查询碳因子数据", tags = VersionConstant.V5_7_0)
    @PostMapping("/listCarbonData")
    public R<ValueSelectionTrendTableDto> listCarbonData(@RequestBody @Valid ValueSelectElecQueryParam param) {
        return R.success(valueSelectionService.listCarbonData(param));
    }

}
