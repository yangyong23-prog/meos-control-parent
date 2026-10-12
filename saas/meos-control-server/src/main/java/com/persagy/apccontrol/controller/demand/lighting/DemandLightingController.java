package com.persagy.apccontrol.controller.demand.lighting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.model.param.demand.DemandAddParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.*;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.*;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingHandler;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 照明需求控制器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@RestController
@RequestMapping("/demand/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandLightingController {

    private final DemandLightingService demandLightingService;
    private final DemandLightingQueryService demandLightingQueryService;
    private final DemandConfigLightingHandler demandConfigLightingHandler;

    @GetMapping("/migrateSceneData")
    @Operation(summary = "迁移-场景数据", tags = VersionConstant.V7_250801_0)
    public R<Boolean> migrateSceneData(@RequestParam(required = false) DemandTemplateTypeEnum templateType) {
        return R.success(demandLightingService.migrateSceneData(templateType));
    }

    @GetMapping("/migrateConfigData")
    @Operation(summary = "迁移-配置数据", tags = VersionConstant.V7_250801_0)
    public R<Boolean> migrateConfigData(
            @RequestParam(required = false) DemandTemplateTypeEnum templateType,
            @RequestParam(required = false) Boolean forceMigrate) {
        return R.success(demandLightingService.migrateConfigData(templateType, forceMigrate));
    }

    @Operation(summary = "查询-照明回路服务的楼层", tags = VersionConstant.V5_18_0)
    @PostMapping("/listFloorWithCircuits")
    public R<List<DemandLightingFloorWithCircuitVO>> listFloorWithCircuits(
            @RequestBody @Valid DemandLightingFloorWithCircuitsQueryParam param) {
        return R.success(demandLightingQueryService.listFloorWithCircuits(param));
    }

    @Operation(summary = "查询-获取存在场景配置的模板列表", tags = VersionConstant.V5_17_0)
    @PostMapping("/listExistsSceneConfigTemplate")
    public R<List<DemandTemplateVO>> listExistsSceneConfigTemplate(@RequestBody @Validated DemandLightingTemplateIdParam templateIdParam) {
        return R.success(demandLightingQueryService.listExistsSceneConfigTemplate(ContextUtil.getGroupCode(), templateIdParam.getProjectId(), templateIdParam.getTemplateId()));
    }

    @Operation(summary = "查询-当前模板下的所有场景", tags = {
            VersionConstant.V5_17_0,
            ExternalServerConstant.CUSTOM_HR
    })
    @PostMapping("/listSceneConfig")
    public R<List<DemandConfigLightingScene>> listSceneConfig(@RequestBody @Validated DemandLightingTemplateIdParam templateIdParam) {
        return R.success(demandLightingQueryService.listSceneConfig(ContextUtil.getGroupCode(), templateIdParam.getProjectId(), templateIdParam.getTemplateId()));
    }

    @Operation(summary = "查询-当前类型下的所有场景", tags = VersionConstant.V7_250601_0)
    @PostMapping("/listAllScene")
    public R<List<DemandLightingAllSceneVO>> listAllScene(@RequestBody @Validated DemandTemplateBatchQueryParam param) {
        return R.success(demandLightingQueryService.listAllScene(param));
    }

    @Operation(summary = "查询-某场景下实例对应的配置信息（不处理引用）", tags = VersionConstant.V5_17_0)
    @PostMapping("/findSceneObjectSettings")
    public R<DemandConfigLighting> findSceneObjectSettings(@RequestBody @Validated DemandLightingInstanceQueryParam param) {
        return R.success(demandLightingQueryService.getSceneObjectSettings(ContextUtil.getGroupCode(), param.getProjectId(), param.getInstanceId(), param.getTemplateId(), param.getSceneId()));
    }

    @Operation(summary = "查询-某场景下实例对应的配置信息", tags = VersionConstant.V5_17_0)
    @PostMapping("/getSceneObjectSettings")
    public R<DemandConfigLightingDataVO> getSceneObjectSettings(@RequestBody @Validated DemandLightingInstanceQueryParam instanceQueryParam) {
        DemandConfigLightingDataVO sceneObjectSettings = demandLightingQueryService.getSceneObjectSettings(instanceQueryParam);
        // 移除灯具类型层级 防止影响前端
        removeLampFuncType(sceneObjectSettings);

        return R.success(sceneObjectSettings);
    }

    private void removeLampFuncType(DemandConfigLightingDataVO sceneObjectSettings) {
        if (null != sceneObjectSettings) {
            DemandConfigLightingGlobalConfigVO global = sceneObjectSettings.getGlobal();
            if (null != global && CollectionUtil.isNotEmpty(global.getFuncTypeSettings())) {
                global.getFuncTypeSettings().forEach(funcTypeSetting -> {
                    funcTypeSetting.setLampFuncTypeSettings(null);
                });
            }
            DemandConfigLightingSpaceConfigVO space = sceneObjectSettings.getSpace();
            if (null != space && CollUtil.isNotEmpty(space.getSettings())) {
                space.getSettings().forEach(setting -> {
                    setting.getFuncTypeSettings().forEach(funcTypeSetting -> {
                        funcTypeSetting.setLampFuncTypeSettings(null);
                    });
                });
            }
        }
    }

    @Operation(summary = "查询-某场景下实例所对应的所有照明功能类型列表", tags = VersionConstant.V5_17_0)
    @PostMapping("/queryCircuitIdFuncTypeList")
    public R<List<DemandLightingFuncTypeSettingVO>> queryCircuitIdFuncTypeList(@RequestBody @Validated DemandLightingInstanceQueryParam instanceQueryParam) {
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(instanceQueryParam.getProjectId(), instanceQueryParam.getTemplateId(), null,
                instanceQueryParam.getInstanceId(), instanceQueryParam.getInstanceType(), 0);
        return R.success(CollectionUtil.isEmpty(lightingTypeGroupTree.getFuncTypeSettings()) ? Lists.newArrayList() : lightingTypeGroupTree.getFuncTypeSettings());
    }

    @Operation(summary = "查询-空间功能类型列表", tags = VersionConstant.V5_17_0)
    @PostMapping("/querySpaceFuncTypeList")
    public R<List<DemandLightingSpaceFuncTypeVO>> querySpaceFuncTypeList(@RequestBody @Validated DemandLightingInstanceQueryParam instanceQueryParam) {
        return R.success(demandLightingQueryService.listSpaceFuncTypeList(ContextUtil.getGroupCode(), instanceQueryParam, BooleanUtil.isTrue(instanceQueryParam.getFilterExists())));
    }

    @Operation(summary = "校验-反馈调节条件多空间一致性", tags = VersionConstant.V7_260801_0)
    @PostMapping("/check/feedbackConfig")
    public R<List<FeedbackConsistencyConflictVO>> feedbackConsistencyCheck(
            @RequestBody @Valid DemandAddParam param) {
        return R.success(demandLightingService.feedbackConsistencyCheck(param));
    }

    @Operation(summary = "查询-某模板下的照明回路功能类型树", tags = {VersionConstant.V5_18_0})
    @PostMapping("/queryTemplateFuncTypeList")
    public R<DemandLightingTemplateTypeVO> queryTemplateFuncTypeList(@RequestBody @Validated DemandLightingTemplateIdParam templateIdParam) {
        return R.success(demandLightingQueryService.getTemplateFuncTypeList(ContextUtil.getGroupCode(), templateIdParam.getProjectId(), templateIdParam.getTemplateId(), null));
    }

    @Operation(summary = "查询-某场景下的照明回路功能类型树", tags = {VersionConstant.V5_18_0})
    @PostMapping("/querySceneFuncTypeList")
    public R<DemandLightingTemplateTypeVO> querySceneFuncTypeList(@RequestBody @Validated DemandLightingTemplateSceneParam sceneParam) {
        return R.success(demandLightingQueryService.getTemplateFuncTypeList(ContextUtil.getGroupCode(), sceneParam.getProjectId(), sceneParam.getTemplateId(), sceneParam.getSceneId()));
    }

    @Operation(summary = "查询-某场景下的照明回路功能类型树", tags = {
            VersionConstant.V5_18_0,
            ExternalServerConstant.CUSTOM_HR
    })
    @PostMapping("/queryTemplateCircuitView")
    public R<DemandLightingCircuitView> queryTemplateCircuitView(@RequestBody @Validated DemandLightingCircuitViewParam viewParam) {
        DemandLightingCircuitView circuitView = demandLightingQueryService.getTemplateCircuitView(ContextUtil.getGroupCode(), viewParam);
        R<DemandLightingCircuitView> success = R.success(circuitView);
        success.setTotal((long) circuitView.getSummary().getTotal());
        success.setCount(circuitView.getCircuitSettings().size());
        return success;
    }

    @Operation(summary = "查询-管控对象树和空间配置信息", tags = VersionConstant.V5_17_0)
    @PostMapping("/query/treeControlledObject")
    public R<DemandLightingTreeObjectVO> queryTreeControlledObject(@RequestBody @Valid DemandTreeQueryParam param) {
        param.check();
        return R.success(demandLightingQueryService.treeObject(param));
    }

    @Operation(summary = "查询-回路统计", tags = VersionConstant.V5_17_0)
    @PostMapping("/query/circuitObjCfgStatistics")
    public R<CircuitObjCfgStatisticsVO> queryCircuitObjCfgStatistics(@RequestBody @Valid CircuitObjCfgStatisticsQueryParam param) {
        param.check();
        return R.success(demandLightingQueryService.countCircuitObjCfgStatistics(param));
    }

    @Operation(summary = "查询-空间回路状态统计", tags = VersionConstant.V5_17_0)
    @PostMapping("/query/spaceCfgStatistics")
    public R<List<DemandLightingSpaceCircuitVO>> spaceCfgStatistics(@RequestBody @Valid CircuitObjCfgStatisticsQueryParam param) {
        param.check();
        return R.success(demandLightingQueryService.listSpaceCfgStatistics(param));
    }


    @Operation(summary = "查询-空间是否允许反馈调节", tags = VersionConstant.V5_241202_0)
    @PostMapping("/query/spaceFeedbackAdjustSettingsCheck")
    public R<Boolean> spaceFeedbackAdjustSettingsCheck(@RequestBody @Valid SpaceFeedbackAdjustSettingsCheckParam param) {
        return R.success(demandLightingQueryService.spaceFeedbackAdjustSettingsCheck(param));
    }
}
