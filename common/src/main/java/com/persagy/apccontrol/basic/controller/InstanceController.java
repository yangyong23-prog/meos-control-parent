package com.persagy.apccontrol.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SingleFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSystemQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.*;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceTreeColdHotSystemVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 实例管理
 *
 * <p>------------------------- 树结构 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>treeBuildingFloorSpace</b> -
 *         [查询] 建筑楼层空间树，返回建筑-楼层-空间三级树结构，缓存策略：Redis Key=`treeBuildingFloorSpace`
 *     </li>
 *     <li>
 *         <b>treeBuildingFloorRoomFuncTypeSpace</b> -
 *         [查询] 建筑楼层空间功能类型空间树
 *     </li>
 *     <li>
 *         <b>treeColdHotSystem</b> -
 *         [查询] 冷热源系统树，缓存策略：Redis Key=`treeColdHotSystem`
 *     </li>
 * </ul>
 *
 * <p>------------------------- 建筑 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>listBuildings</b> -
 *         [查询] 建筑列表，缓存策略：Redis Key=`listBuildings`
 *     </li>
 * </ul>
 *
 * <p>------------------------- 楼层 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>listFloors</b> -
 *         [查询] 楼层列表，缓存策略：Redis Key=`listFloors`
 *     </li>
 * </ul>
 *
 * <p>------------------------- 空间 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>listSpaces</b> -
 *         [查询] 空间列表，缓存策略：Redis Key=`listSpaces`
 *     </li>
 *     <li>
 *         <b>listFloorRelationSpacesBySystem</b> -
 *         [查询] 系统下楼层关联空间，缓存策略：Redis Key=`listFloorRelationSpacesBySystem`
 *     </li>
 * </ul>
 *
 * <p>------------------------- 设备 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>listEquipmentsInBySpaceIds</b> -
 *         [查询] 所在空间的设备，缓存策略：Redis Key=`listEquipmentsInBySpaceIds`
 *     </li>
 *     <li>
 *         <b>listEquipmentInByFloorIds</b> -
 *         [查询] 楼层下的设备
 *     </li>
 *     <li>
 *         <b>listEquipments</b> -
 *         [查询] 设备
 *     </li>
 * </ul>
 *
 * <p>------------------------- 其他 -------------------------</p>
 * <ul>
 *     <li>
 *         <b>listTwinControlObjects</b> -
 *         [查询] 孪生控制对象
 *     </li>
 *     <li>
 *         <b>queryColumnAndWallObjectByFloorId</b> -
 *         [查询] 单楼层下墙和柱子
 *     </li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Tag(name = "实例管理")
@Validated
@RestController
@RequestMapping("/instance")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InstanceController {

    private final InstanceService instanceService;
    private final RedisCacheTemplate redisCacheTemplate;

    @Operation(summary = "[查询] 建筑楼层空间树")
    @PostMapping("/treeBuildingFloorSpace")
    public R<List<DtpRwdBuildingDTO>> treeBuildingFloorSpace(
            @RequestBody @Valid TreeBuildingFloorSpaceQueryParam param) {
        return R.success(instanceService.treeBuildingFloorSpace(param));
    }

    @Operation(summary = "[查询] 建筑楼层空间功能类型空间树")
    @PostMapping("/treeBuildingFloorRoomFuncTypeSpace")
    public R<List<DtpRwdBuildingDTO>> treeBuildingFloorRoomFuncTypeSpace(
            @RequestBody @Valid TreeBuildingFloorRoomFuncTypeSpaceQueryParam param) {
        return R.success(instanceService.treeBuildingFloorRoomFuncTypeSpace(param));
    }

    @Operation(summary = "[查询] 建筑楼层空间Dashboard空间树")
    @PostMapping("/treeBuildingFloorDashboardSpaces")
    public R<List<DtpRwdBuildingDTO>> treeBuildingFloorDashboardSpaces(
            @RequestBody @Valid TreeBuildingFloorDashboardSpacesQueryParam param) {
        return R.success(instanceService.treeBuildingFloorDashboardSpaces(param));
    }


    @Operation(summary = "[查询] 冷源&热源&冷热源树")
    @PostMapping("/treeColdHotSystem")
    public R<List<InstanceTreeColdHotSystemVO>> treeColdHotSystem(
            @RequestBody @Valid InstanceTreeColdHotSystemQueryParam param) {
        RedisCacheTemplate.MethodCall<List<InstanceTreeColdHotSystemVO>> methodToCache = () -> instanceService.treeColdHotSystem(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("treeColdHotSystem", param, methodToCache));
    }

    @Operation(summary = "[查询] 根据取值逻辑查询对象", tags = VersionConstant.V7_250403_0)
    @PostMapping("/listObjectsByTackValueLogic")
    public R<Collection<InfoCodeRwdObjectDetailDTO>> listObjectsByTackValueLogic(
            @RequestBody @Valid ObjectQueryParam param) {
        return R.successMulti(instanceService.listObjectsByTackValueLogic(param));
    }

    @Operation(summary = "[查询] 建筑")
    @PostMapping("/listBuildings")
    public R<List<DtpRwdBuildingDTO>> listBuildings(
            @RequestBody @Valid BuildingQueryParam param) {
        RedisCacheTemplate.MethodCall<List<DtpRwdBuildingDTO>> methodToCache = () -> instanceService.listBuildings(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listBuildings", param, methodToCache));
    }

    @Operation(summary = "[查询] 楼层")
    @PostMapping("/listFloors")
    public R<List<DtpRwdFloorDTO>> listFloors(
            @RequestBody @Valid FloorQueryParam param) {
        RedisCacheTemplate.MethodCall<List<DtpRwdFloorDTO>> methodToCache = () -> instanceService.listFloors(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listFloors", param, methodToCache));
    }

    @Operation(summary = "[查询] 空间")
    @PostMapping("/listSpaces")
    public R<List<DtpRwdSpaceDTO>> listSpaces(
            @RequestBody @Valid SpaceQueryParam param) {
        RedisCacheTemplate.MethodCall<List<DtpRwdSpaceDTO>> methodToCache = () -> instanceService.listSpaces(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listSpaces", param, methodToCache));
    }

    @Operation(summary = "[查询] 感知空间列表")
    @PostMapping("/listSpacesByRunningMonitor/{runningMonitorType}")
    public R<List<DtpRwdSpaceDTO>> listSpacesByRunningMonitor(
            @RequestBody @Valid RunningMonitorSpaceQueryParam param,
            @PathVariable("runningMonitorType") RunningMonitorTypeEnum runningMonitorType) {
        return R.success(runningMonitorType.getRunningMonitorStrategy().listSpaces(param));
    }

    @Operation(summary = "[查询] Dashboard空间列表")
    @PostMapping("/listDashboardSpaces/{runningMonitorType}")
    public R<List<? extends DtpRwdSpaceVO>> listDashboardSpaces(
            @RequestBody @Valid RunningMonitorSpaceQueryParam param,
            @PathVariable("runningMonitorType") RunningMonitorTypeEnum runningMonitorType) {
        return R.success(runningMonitorType.getRunningMonitorStrategy().listDashboardSpaces(param));
    }

    @Operation(summary = "[查询] Dashboard系统列表")
    @PostMapping("/listDashboardSystems/{runningMonitorType}")
    public R<List<? extends DtpRwdObjectDTO>> listDashboardSystems(
            @RequestBody @Valid RunningMonitorSystemQueryParam param,
            @PathVariable("runningMonitorType") RunningMonitorTypeEnum runningMonitorType) {
        RedisCacheTemplate.MethodCall<List<? extends DtpRwdObjectDTO>> methodToCache =
                () -> runningMonitorType.getRunningMonitorStrategy().listDashboardSystems(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listDashboardSystems" + runningMonitorType, param,
                methodToCache));
    }

    @Operation(summary = "[查询] 系统下楼层关联空间")
    @PostMapping("/listFloorRelationSpacesBySystem")
    public R<List<DtpRwdObjectVO>> listFloorRelationSpacesBySystem(
            @RequestBody @Valid FloorRelationSpacesBySystemQueryParam param) {
        RedisCacheTemplate.MethodCall<List<DtpRwdObjectVO>> methodToCache = () -> instanceService.listFloorRelationSpacesBySystem(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listFloorRelationSpacesBySystem", param, methodToCache));
    }

    @Operation(summary = "[查询] 所在空间的设备", tags = VersionConstant.V5_241101_0)
    @PostMapping("/listEquipmentsInBySpaceIds")
    public R<List<EquipmentVO>> listEquipmentsInBySpaceIds(
            @RequestBody @Valid EquipmentInSpaceQueryParam param) {
        RedisCacheTemplate.MethodCall<List<EquipmentVO>> methodToCache = () -> instanceService.listEquipmentsInBySpaceIds(param);
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listEquipmentsInBySpaceIds", param, methodToCache));
    }
    @Operation(summary = "定制接口所在机房空间的设备",tags = VersionConstant.V7_260802_0)
    @GetMapping("/listEquipmentsInBySpaceIds/computerRoomSecurity")
    public R<List<EquipmentVO>> listEquipmentsInBySpaceIds(@RequestParam String spaceId) {
        return R.success(instanceService.listEquipmentsInBySpaceIdsComputerRoomSecurity(spaceId));
//        RedisCacheTemplate.MethodCall<List<EquipmentVO>> methodToCache = () -> instanceService.listEquipmentsInBySpaceIdsComputerRoomSecurity(spaceId);
//        return R.success(redisCacheTemplate.asyncRefreshTemplate("computerRoomSecurityListEquipmentsInBySpaceIds", spaceId, methodToCache));
    }

    @Operation(summary = "查询-楼层下的设备", tags = {VersionConstant.V7_250302_0})
    @PostMapping("/listEquipmentInByFloorIds")
    public R<List<DtpRwdEquipmentDTO>> listEquipmentInByFloorIds(
            @RequestBody @Valid EquipmentInByFloorIdQueryParam param) {
        return R.success(this.instanceService.listEquipmentInByFloorIds(param));
    }

    @Operation(summary = "[查询] 设备")
    @PostMapping("/listEquipments/{runningMonitorType}")
    public R<List<? extends DtpRwdObjectDTO>> listDashboardSystems(
            @PathVariable("runningMonitorType") RunningMonitorTypeEnum runningMonitorType,
            @RequestBody @Valid RunningMonitorEquipmentQueryParam param) {
        if (this.allowMysqlCache(runningMonitorType)) {
            param.setMysqlCache(true);
            return R.success(runningMonitorType.getRunningMonitorStrategy().listEquipments(param));
        }
        return R.success(param.canCache()
                ? redisCacheTemplate.asyncRefreshTemplate("listEquipments" + runningMonitorType, param,
                () -> runningMonitorType.getRunningMonitorStrategy().listEquipments(param))
                : runningMonitorType.getRunningMonitorStrategy().listEquipments(param));
    }

    @Operation(summary = "定制接口进入楼层图的设备",tags = VersionConstant.V7_260802_0)
    @GetMapping("/listSpaceAndEquipmentInByFloorId/computerRoomSecurity")
    public R<List<? extends DtpRwdObjectDTO>> listDashboardSystemsComputerRoomSecurity(@RequestParam String floorId) {
        return R.success( ((RunningMonitorComputerRoomSecurityStrategy)RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY.getRunningMonitorStrategy()).listEquipmentsFromRwdFlor(floorId));
    }

    @Operation(summary = "[查询] 孪生控制对象", tags = {VersionConstant.V5_240601_0})
    @PostMapping("/listTwinControlObjects")
    public R<Collection<DtpRwdTwinControlObjectDTO>> listTwinControlObjects(
            @RequestBody @Validated DtpRwdTwinControlObjectQueryParam param) {
        return R.successMulti(instanceService.listTwinControlObjects(param));
    }

    @Operation(summary = "查询单楼层下墙和柱子", tags = {VersionConstant.V5_8_0})
    @PostMapping("/queryColumnAndWallObjectByFloorId")
    public R<JSONObject> queryColumnAndWallObjectByFloorId(@RequestBody @Validated SingleFloorDTO floorDTO) {
        return R.success(this.instanceService.queryColumnAndWallObjectByFloorId(ContextUtil.getGroupCode(), floorDTO.getProjectId(), floorDTO.getFloorId()));
    }

    @Operation(summary = "查询对象", tags = {VersionConstant.V7_260102_0})
    @PostMapping("/listObject")
    public R<List<DtpRwdEquipmentDTO>> listObject(@RequestBody @Validated EquipmentQueryParam param) {
        return R.success(this.instanceService.listObject(param));
    }

    /**
     * 是否使用 Mysql 缓存
     *
     * @param runningMonitorType 运行监测类型
     * @return 是否使用 Mysql 缓存
     * @since 7.260401.0
     */
    private boolean allowMysqlCache(@NonNull RunningMonitorTypeEnum runningMonitorType) {
        return runningMonitorType == RunningMonitorTypeEnum.LIGHTING;
    }
}
