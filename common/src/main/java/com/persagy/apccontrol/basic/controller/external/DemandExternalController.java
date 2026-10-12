package com.persagy.apccontrol.basic.controller.external;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandSpaceSequenceExternalQueryParam;
import com.persagy.apccontrol.basic.model.param.external.demand.DemandTreeInstanceExternalQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandValueSelectConfigVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandSpaceSequenceExternalVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.DemandTreeInstanceProjectExternalVO;
import com.persagy.apccontrol.basic.service.demand.DemandExternalService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * 需求管理对外接口
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@RestController
@RequestMapping("/external/demand")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandExternalController {

    private final DemandExternalService demandExternalService;

    @Operation(summary = "查询-需求实例树", tags = {
            VersionConstant.V5_241202_0,
            ExternalServerConstant.ENERGY_DDP_SERVER,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/treeDemandInstance")
    public R<DemandTreeInstanceProjectExternalVO> treeDemandInstance(
            @RequestBody @Valid DemandTreeInstanceExternalQueryParam param) {
        return demandExternalService.treeDemandInstance(param);
    }

    @Operation(summary = "查询-需求空间", tags = {
            VersionConstant.V5_241102_0,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/listDemandSpaces")
    public R<Collection<DemandSpaceExternalVO>> listDemandSpaces(
            @RequestBody @Valid DemandSpaceExternalQueryParam param) {
        return demandExternalService.listDemandInstances(param);
    }

    @Operation(summary = "查询-末端需求配置时序数据(5min)", tags = {
            VersionConstant.V5_241102_0,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/listAirTerminalSpaceSequenceData")
    public R<TreeMap<LocalDate, List<DemandSpaceSequenceExternalVO>>> listSpaceDemandConfigSequenceData(@RequestBody @Valid DemandSpaceSequenceExternalQueryParam param) throws Exception {
        TreeMap<LocalDate, List<DemandSpaceSequenceExternalVO>> demandInstanceExternalVOS = demandExternalService.listSpaceDemandConfigSequenceData(param);
        return R.success(demandInstanceExternalVOS);
    }
    
    @Operation(summary = "查询价值选择每日的配置", tags = VersionConstant.V7_250102_0)
    @PostMapping("/queryEverydayValueSelectionConfig")
    public R<List<DemandValueSelectConfigVO>> queryEverydayValueSelectionConfig(
            @RequestBody @Valid DemandValueSelectConfigQueryParam param) {
        return R.success(demandExternalService.queryEverydayFixDataList(param));
    }

}
