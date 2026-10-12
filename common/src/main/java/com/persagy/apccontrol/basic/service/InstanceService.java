package com.persagy.apccontrol.basic.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.*;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceTreeColdHotSystemVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 实例服务
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public interface InstanceService {

    /**
     * 查询建筑楼层空间树
     *
     * @param param 查询参数
     * @return 建筑楼层空间树
     * @since v5.1.0
     */
    List<DtpRwdBuildingDTO> treeBuildingFloorSpace(@NonNull TreeBuildingFloorSpaceQueryParam param);

    /**
     * 查询建筑楼层空间类型空间树
     *
     * @param param 查询参数
     * @return 建筑楼层空间类型空间树
     * @since 7.250101.0
     */
    List<DtpRwdBuildingDTO> treeBuildingFloorRoomFuncTypeSpace(
            @NonNull @Valid TreeBuildingFloorRoomFuncTypeSpaceQueryParam param);
    
    /**
     * 查询建筑楼层空间类型空间树
     *
     * @param param 查询参数
     */
    List<DtpRwdBuildingDTO> treeBuildingFloorDashboardSpaces(
            @NonNull @Valid TreeBuildingFloorDashboardSpacesQueryParam param);

    /**
     * 查询对象
     *
     * @param param 查询参数
     * @return 对象
     * @since 7.250403.0
     */
    @NonNull
    List<@NonNull InfoCodeRwdObjectDetailDTO> listObjectsByTackValueLogic(@NonNull @Valid ObjectQueryParam param);

    /**
     * 查询建筑
     *
     * @param param 查询参数
     * @return 建筑
     * @since v5.7.0
     */
    List<DtpRwdBuildingDTO> listBuildings(@NonNull BuildingQueryParam param);

    /**
     * 查询楼层
     *
     * @param param 查询参数
     * @return 楼层
     * @since v5.7.0
     */
    List<DtpRwdFloorDTO> listFloors(@NonNull FloorQueryParam param);

    /**
     * 查询空间
     *
     * @param param 查询参数
     * @return 空间
     * @since v5.7.0
     */
    List<DtpRwdSpaceDTO> listSpaces(@NonNull SpaceQueryParam param);

    /**
     * 查询楼层关联空间
     *
     * @param param 查询参数
     * @return 楼层关联空间
     * @since v5.1.0
     */
    List<DtpRwdObjectVO> listFloorRelationSpacesBySystem(@NonNull FloorRelationSpacesBySystemQueryParam param);

    /**
     * 查询空间内的设备
     *
     * @param param 查询参数
     * @return 设备
     * @since 5.241101.0
     */
    List<EquipmentVO> listEquipmentsInBySpaceIds(@NonNull @Valid EquipmentInSpaceQueryParam param);

    List<EquipmentVO> listEquipmentsInBySpaceIdsComputerRoomSecurity(String spaceId);

    /**
     * 查询冷源&热源&冷热源树
     *
     * @param param 查询参数
     * @return 冷源&热源&冷热源树
     * @since 5.241102.0
     */
    List<InstanceTreeColdHotSystemVO> treeColdHotSystem(@NonNull @Valid InstanceTreeColdHotSystemQueryParam param);

    /**
     * 查询配置实例关联的空间ID
     *
     * @param instanceType 实例类型
     * @param instanceId   实例ID, 为空时查询所有
     * @return 空间ID
     * @since 5.241102.0
     */
    Set<String> listSpaceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable String instanceId);

    /**
     * 查询配置实例关联的空间ID
     *
     * @param instanceType 实例类型
     * @param instanceIds  实例ID, 为空时查询所有
     * @return 空间ID
     * @since 5.241102.0
     */
    Set<String> listSpaceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @Nullable Set<String> instanceIds);

    /**
     * 查询配置实例关联的楼层ID
     *
     * @param instanceType 实例类型
     * @param instanceId   实例ID, 为空时查询所有
     * @return 楼层ID
     * @since 5.240402.0
     */
    Set<String> listFloorIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询配置实例关联的楼层ID
     *
     * @param instanceType 实例类型
     * @param instanceIds  实例ID, 为空时查询所有
     * @return 楼层ID
     * @since 5.241102.0
     */
    Set<String> listFloorIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull Set<String> instanceIds);

    /**
     * 查询配置实例关联的建筑ID
     *
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @return 楼层ID
     * @since 5.240402.0
     */
    Set<String> listBuildingIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询配置实例关联的建筑ID
     *
     * @param instanceType 实例类型
     * @param instanceIds  实例ID
     * @return 楼层ID
     * @since 5.241102.0
     */
    Set<String> listBuildingIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull Set<String> instanceIds);

    /**
     * 查询下级所有实例ID
     *
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @return 下级所有实例ID
     * @since 5.240902.0
     */
    Set<String> listChildrenInstanceIdsByInstance(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询配置实例关联的楼层ID
     *
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @return 楼层ID
     * @since 5.240402.0
     */
    @Deprecated
    List<DtpRwdFloorDTO> listFloorsByInstance(
            @NonNull String projectId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询实例ID和所有上级实例映射
     *
     * @param instanceTypeAndIds 实例类型 -> 实例ID
     * @return 实例ID -> 上级实例
     * @since 5.240702.0
     */
    Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParents(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds);

    /**
     * 查询实例ID和所有上级实例映射
     *
     * @param instanceTypeAndIds 实例类型 -> 实例ID
     * @return 实例ID -> 上级实例
     * @since 5.241201.0
     */
    Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParentsWithoutName(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds);

    /**
     * 查询实例ID和所有上级实例映射（直接查询对象方式）
     * <p>
     * 内部逻辑改为直接查询对象，然后取对象中的floorId和buildingId来查询上级
     *
     * @param instanceTypeAndIds 实例类型 -> 实例ID
     * @param queryName          是否查询名称
     * @return 实例ID -> 上级实例
     * @since 7.260302.0
     */
    Map<String, List<DemandInstanceValueObject>> mapInstanceIdAndAllParentsByDirectQuery(
            @NonNull Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds,
            boolean queryName);

    /**
     * 查询孪生控制对象
     *
     * @param param 查询参数
     * @return 孪生控制对象
     * @since 5.240601.0
     */
    Collection<DtpRwdTwinControlObjectDTO> listTwinControlObjects(
            @NonNull @Valid DtpRwdTwinControlObjectQueryParam param);

    /**
     * 查询单楼层下墙和柱子
     *
     * @param groupCode
     * @param projectId
     * @param floorId
     * @return
     */
    JSONObject queryColumnAndWallObjectByFloorId(String groupCode, String projectId, String floorId);

    List<JSONObject> listEquipmentByClassCode(String projectId, String... claasCode);

    /**
     * 查询楼层下的设备
     *
     * @param param 查询参数
     * @return 设备
     * @since 7.250302.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentInByFloorIds(EquipmentInByFloorIdQueryParam param);

    /**
     * 查询对象
     *
     * @param param 查询参数
     * @return 设备
     * @since 7.250302.0
     */
    List<DtpRwdEquipmentDTO> listObject(EquipmentQueryParam param);


}
