package com.persagy.apccontrol.basic.controller.lighting;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandLightingParam;
import com.persagy.apccontrol.basic.model.param.lighting.*;
import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingFuncTypeSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingSpaceFuncTypeVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingTypeGroupTree;
import com.persagy.apccontrol.basic.model.vo.lighting.*;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingHandler;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.*;

/**
 * 照明控制器
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@RestController
@RequestMapping(value = "/lighting")
public class LightingController {

    @Resource
    AbstractLightingService lightingService;
    @Resource
    DemandConfigLightingHandler demandConfigLightingHandler;
    @Resource
    DemandLightingQueryService demandLightingQueryService;
    @Resource
    DemandConfigLightingResultService demandConfigLightingResultService;

    @Operation(summary = "查询-需求目标", tags = {
            V7_260101_0,
            ExternalServerConstant.DTP_MEOS_SERVER_EDGE
    })
    @PostMapping("/getDemandTarget")
    public R<LihgtingDemandTargetVO> getDemandTarget(
            @RequestBody @Valid LightingDemandTargetQueryParam param) {
        return R.success(lightingService.getDemandTarget(param));
    }

    @Operation(summary = "查询-照明回路信息点", tags = V5_2404_2)
    @PostMapping(value = "/getCircuitInfoCode")
    public R<LightingCircuitInfoCodeVO> getCircuitInfoCode(
            @Validated @RequestBody LightingCircuitInfoCodeQueryParam param) {
        return R.success(lightingService.getCircuitInfoCode(param));
    }

    /**
     * 查询-实例对应的照明功能类型树
     *
     * @param param 入参
     * @return {@link R<List<DemandLightingFuncTypeSettingVO>>}
     * @author yuecaipu
     * @date 2024/7/22 14:02
     * @since v5.240702
     */
    @PostMapping(value = "/queryCircuitIdFuncTypeList")
    public R<List<DemandLightingFuncTypeSettingVO>> queryCircuitIdFuncTypeList(@RequestBody @Validated DemandLightingParam param) {
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(param.getProjectId(), null, param.getDemandTemplateType(),
                param.getInstanceId(), param.getInstanceType(), 0);
        return R.success(lightingTypeGroupTree.getFuncTypeSettings());
    }


    @Operation(summary = "侧边栏-查询-需求变化", tags = V5_2404_2)
    @PostMapping("/getDemandChange")
    public R<LightingSidebarDemandChangeVO> getDemandChange(@RequestBody @Valid LightingInstanceQueryParam lightingInstanceQueryParam) {
        return R.success(lightingService.getDemandChange(lightingInstanceQueryParam));
    }

    @Operation(summary = "侧边栏-当前需求满足情况", tags = V5_2404_2)
    @PostMapping("/listLightLoopTotal")
    public R<List<LightingSidebarLightLoopTotalVO>> listLightLoopTotal(@RequestBody @Valid LightingInstanceQueryParam lightingInstanceQueryParam) {
        return R.success(lightingService.listLightLoopTotal(lightingInstanceQueryParam));
    }

    @Operation(summary = "侧边栏-手自动控制", tags = V7_250302_0)
    @PostMapping("/listManualAutomaticControl")
    public R<List<LightingSidebarManualAutomaticControlVO>> listManualAutomaticControl(@RequestBody @Valid LightingInstanceQueryParam lightingInstanceQueryParam) {
        return R.success(lightingService.listManualAutomaticControl(lightingInstanceQueryParam));
    }

    @Operation(summary = "侧边栏-设备状态", tags = V5_2404_2)
    @PostMapping("/countEquipmentStatus")
    public R<LightingSidebarDeviceStateVO> countEquipmentStatus(@RequestBody @Valid LightingInstanceQueryParam lightingInstanceQueryParam) {
        if (StringUtils.isBlank(lightingInstanceQueryParam.getInstanceId())) {
            lightingInstanceQueryParam.setInstanceType(DemandInstanceTypeEnum.PROJECT);
            lightingInstanceQueryParam.setInstanceId(lightingInstanceQueryParam.getProjectId());
        }
        return R.success(lightingService.countEquipmentStatus(lightingInstanceQueryParam));
    }

    @Operation(summary = "查询-空间功能类型列表", tags = VersionConstant.V5_240702_0)
    @PostMapping("/intervention/querySpaceFuncTypeList")
    public R<List<DemandLightingSpaceFuncTypeVO>> querySpaceFuncTypeList(@RequestBody @Validated DemandLightingParam param) {
        return R.success(demandLightingQueryService.listSpaceFuncTypeList(param));
    }


    @Operation(summary = "查询-异常信息列表", tags = V5_2404_2)
    @PostMapping("/getAbnormalDataList")
    public R<List<ExceptionEventBaseInfoVO>> getAbnormalDataList(@RequestBody LightingInstanceQueryParam lightingInstanceQueryParam) {
        return R.success(lightingService.getAbnormalDataList(lightingInstanceQueryParam));
    }

    @Operation(summary = "查询-空间的飞行线(空间关联的空间)", tags = V5_2404_2)
    @PostMapping("/getLineOfFlight")
    public R<List<DtpRwdSpaceDTO>> getLineOfFlight(@RequestBody LightingInstanceQueryParam lightingInstanceQueryParam) {
        return R.success(lightingService.getLineOfFlight(lightingInstanceQueryParam));
    }

    @Operation(summary = "查询-空间信息", tags = V5_2404_2)
    @PostMapping("/getSpaceData")
    public R<LightingHoverSpaceAbnormalDataVO> getSpaceData(@RequestBody @Valid LightingHoverQueryParam lightingHoverQueryParam) {
        return R.success(lightingService.getSpaceOrLampFuncHoverInfo(lightingHoverQueryParam));
    }

    @Operation(summary = "查询-空间带状态", tags = V5_2404_2)
    @PostMapping(value = "/listSpaceWithStatus")
    public R<Collection<LightingSpaceWithStatusVO>> listSpaceWithStatus(
            @RequestBody @Valid LightingSpaceWithStatusQueryParam dto) {
        return R.successMulti(lightingService.listSpaceWithStatus(dto));
    }

    @Operation(summary = "查询-灯具功能类型带状态", tags = V5_2405_1)
    @PostMapping(value = "/getLampFuncTypeWithStatus")
    public R<LightingNightWithStatusVO> getLampFuncTypeWithStatus(
            @RequestBody @Valid LightingLampFuncTypeWithStatusQueryParam param) {
        return R.success(lightingService.getLampFuncTypeWithStatus(param));
    }

    @Operation(summary = "侧边栏-照度监测数据", tags = V5_2404_2)
    @PostMapping("/getIllumInfo")
    public R<LightingSidebarIllumInfoVO> getIllumInfo(@RequestBody @Valid ProjectVO projectInfo) {
        return R.success(lightingService.getIllumInfo(projectInfo));
    }

    @Operation(summary = "查询-照明控制模块详情", tags = V5_2404_2)
    @PostMapping(value = "/getControllerDetail")
    public R<LightingControllerDetailVO> getControllerDetail(
            @RequestBody @Valid LightingControllerDetailQueryParam param) {
        return R.success(lightingService.getControllerDetail(param));
    }

    @Operation(summary = "查询-空间详情", tags = V5_2404_2)
    @PostMapping(value = "/getSpaceDetail")
    public R<LightingSpaceDetailVO> getSpaceDetail(@RequestBody @Valid LightingSpaceDetailQueryParam param) {
        return R.success(lightingService.getSpaceDetail(param));
    }

    @Operation(summary = "查询-照明回路的需求结果", tags = V7_250801_0)
    @PostMapping("/listCircuitConfigResults")
    public R<List<LightingCircuitConfigResultVO>> listCircuitConfigResults(@RequestBody @Valid CircuitConfigResultsQueryParam queryParam) {
        return R.success(demandConfigLightingResultService.listCircuitConfigResults(queryParam));
    }

}
