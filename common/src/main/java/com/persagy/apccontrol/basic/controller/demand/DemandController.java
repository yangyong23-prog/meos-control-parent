package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.model.vo.demand.*;
import com.persagy.apccontrol.basic.service.demand.DemandQueryService;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 需求管理-基础接口
 */
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandController {

    private final DemandQueryService demandQueryService;

    /**
     * 查询需求空间树.
     *
     * @param param 查询参数
     * @return 需求空间树
     */
    @Deprecated
    @Operation(summary = "查询-需求空间树", tags = VersionConstant.V5_240902_0)
    @PostMapping("/treeControlledObject")
    public R<DemandTreeControlledObjectVO> treeControlledObject(@RequestBody @Valid DemandTreeQueryParam param) {
        return R.success(this.demandQueryService.demandTree(param));
    }

    /**
     * 查询需求对象列表.
     *
     * @param param 查询参数
     * @return 需求对象列表
     */
    @Deprecated
    @Operation(summary = "查询-需求对象列表", tags = VersionConstant.V5_240902_0)
    @PostMapping("/listControlledObject")
    public R<List<DtpRwdObjectDTO>> listControlledObject(@RequestBody @Valid DemandObjectQueryParam param) {
        return R.success(this.demandQueryService.listControlledObject(param));
    }

    /**
     * 查询屏蔽空间树.
     *
     * @param param 查询参数
     * @return 屏蔽空间树
     */
    @Deprecated
    @Operation(summary = "查询-屏蔽空间树", tags = VersionConstant.V5_240902_0)
    @PostMapping("/treeShieldedObject")
    public R<DemandTreeShieldedObjectVO> treeShieldedObject(
            @RequestBody @Valid DemandTreeShieldedObjectQueryParam param) {
        return R.success(this.demandQueryService.treeShieldedObject(param));
    }

    /**
     * 查询需求实例树.
     *
     * @param param 查询参数
     * @return 需求实例树
     */
    @Operation(summary = "查询-需求实例树", tags = VersionConstant.V5_241102_0)
    @PostMapping("/treeDemandInstance")
    public R<DemandTreeInstanceProjectVO> treeDemandInstance(
            @RequestBody @Valid DemandTreeInstanceQueryParam param) {
        return this.demandQueryService.treeDemandInstance(param);
    }

    /**
     * 查询实例配置.
     *
     * @param param 查询参数
     * @return 实例配置
     */
    @Operation(summary = "查询-实例配置", tags = VersionConstant.V5_241102_0)
    @PostMapping("/listInstanceConfig")
    public R<Collection<DemandInstanceConfigProjectVO>> listInstanceConfig(
            @RequestBody @Valid DemandInstanceConfigViewQueryParam param) {
        return this.demandQueryService.listInstanceConfig(param);
    }

    /**
     * 查询楼层下需求空间.
     *
     * @param param 查询参数
     * @return 楼层下需求空间
     */
    @Operation(summary = "查询-楼层下需求空间", tags = VersionConstant.V5_241102_0)
    @PostMapping("/listDemandSpaceInFloor")
    public R<Collection<DemandSpaceInFloorVO>> listDemandSpaceInFloor(
            @RequestBody @Valid DemandSpaceInFloorQueryParam param) {
        return this.demandQueryService.listDemandSpaceInFloor(param);
    }

    /**
     * 查询自定义配置实例.
     *
     * @param param 查询参数
     * @return 自定义配置实例
     */
    @Operation(summary = "查询-自定义配置实例", tags = VersionConstant.V5_241102_0)
    @PostMapping("/listCustomizedConfigInstance")
    public R<Collection<DemandCustomizedConfigInstanceVO>> listCustomizedConfigInstance(
            @RequestBody @Valid DemandCustomizedConfigInstanceQueryParam param) {
        return this.demandQueryService.listCustomizedConfigInstance(param);
    }

    /**
     * 查询楼层下引用上级的需求空间.
     *
     * @param param 查询参数
     * @return 楼层下引用上级的需求空间
     */
    @Operation(summary = "查询-楼层下引用上级的需求空间", tags = VersionConstant.V5_241102_0)
    @PostMapping("/listDemandReferenceParentSpaceByInFloor")
    public R<Collection<DemandReferenceParentSpaceInFloorVO>> listDemandReferenceParentSpaceByInFloor(
            @RequestBody @Valid DemandReferenceParentSpaceInFloorQueryParam param) {
        return this.demandQueryService.listDemandReferenceParentSpaceByInFloor(param);
    }

    /**
     * 查询实例下空间功能类型.
     *
     * @param param 查询参数
     * @return 实例下空间功能类型
     */
    @Operation(summary = "[查询] 实例下空间功能类型")
    @PostMapping("/listChildrenSpaceFuncTypes")
    public R<List<DemandInstanceChildrenSpaceFuncTypeVO>> listChildrenSpaceFuncTypes(
            @RequestBody @Valid DemandInstanceChildrenSpaceFuncTypeQueryParam param) {
        return R.success(this.demandQueryService.listChildrenSpaceFuncTypes(param));
    }

    /**
     * 查询需求配置.
     *
     * @param param 查询参数
     * @return 需求配置
     */
    @Operation(summary = "查询-需求配置", tags = {
            VersionConstant.V5_240902_0,
            ExternalServerConstant.AI_AU_SERVER,
            ExternalServerConstant.AI_AUTORUN_AGENT,
            ExternalServerConstant.CUSTOM_HR
    })
    @PostMapping("/config/listConfigsByInstance")
    public R<Collection<DemandConfigVO<BaseDemandConfig>>> queryDemandConfig(
            @RequestBody @Valid DemandConfigQueryParam param) {
        return R.successMulti(this.demandQueryService.queryDemandConfig(param), -1L);
    }

    /**
     * 查询需求管理建筑楼层空间树.
     *
     * @param param 查询参数
     * @return 需求管理建筑楼层空间树
     */
    @Operation(summary = "查询-需求管理建筑楼层空间树", tags = VersionConstant.V7_250202_0)
    @PostMapping("/treeBuildingFloorSpace")
    public R<Collection<DemandTreeBuildingFloorSpaceVO>> treeBuildingFloorSpace(
            @RequestBody @Valid DemandTreeBuildingFloorSpaceQueryParam param) {
        return R.successMulti(this.demandQueryService.treeBuildingFloorSpace(param));
    }

    /**
     * 查询需求管理空间功能类型树.
     *
     * @param param 查询参数
     * @return 需求管理空间功能类型树
     */
    @Operation(summary = "查询-需求管理空间功能类型树", tags = VersionConstant.V7_250202_0)
    @PostMapping("/treeSpaceRoomFuncTypes")
    public R<List<DemandTreeSpaceRoomFuncTypeVO>> treeSpaceRoomFuncTypes(
            @RequestBody @Valid DemandTreeSpaceRoomFuncTypeQueryParam param) {
        return R.success(this.demandQueryService.treeSpaceRoomFuncTypes(param));
    }

    /**
     * 查询自定义配置实例接口.
     *
     * @param param 查询参数
     * @return 自定义配置实例
     */
    @Operation(summary = "查询-自定义配置实例接口", tags = VersionConstant.V7_250202_0)
    @PostMapping("/config/listConfigGroups")
    public R<List<DemandConfigGroupVO>> listConfigGroups(
            @RequestBody @Valid DemandTemplateSceneParam param) {
        return R.success(this.demandQueryService.listConfigGroups(param));
    }

    /**
     * 查询需求配置预计算结果.
     *
     * @param param 查询参数
     * @return 需求配置预计算结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @Operation(summary = "查询-需求配置预计算结果", tags = VersionConstant.V7_260701_0)
    @PostMapping("/config/listConfigResultsByInstances")
    public @NonNull R<List<DemandConfigResultVO<BaseDemandConfig>>> listConfigResultsByInstances(
            @RequestBody @NonNull @Valid DemandConfigResultBatchQueryParam param) {
        return R.success(this.demandQueryService.listConfigResultsByInstances(param));
    }
}
