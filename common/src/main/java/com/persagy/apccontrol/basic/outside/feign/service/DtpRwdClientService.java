package com.persagy.apccontrol.basic.outside.feign.service;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.*;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleByIdResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.ObjectInstantiateResponse;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerSize;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.executor.MapExecutor;
import feign.FeignException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询物理世界关系
 *
 * <p>------------------------- 通用查询 -------------------------
 * <ul>
 *     <li><b>getObject</b> - 查询对象实例</li>
 *     <li><b>listObject</b> - 查询对象实例</li>
 *     <li><b>listRelationObject</b> - 查询关联对象实例</li>
 *     <li><b>listBuilding</b> - 查询建筑</li>
 *     <li><b>listFloor</b> - 查询楼层</li>
 *     <li><b>listSpace</b> - 查询空间</li>
 *     <li><b>listCentralCoolingSystem</b> - 查询中央供冷系统</li>
 *     <li><b>listChillerUnit</b> - 查询项目供冷系统下冷机</li>
 *     <li><b>listFreezePumpGroup</b> - 查询冷冻泵组</li>
 *     <li><b>listCircuit</b> - 查询照明回路</li>
 * </ul>
 * <p>------------------------- 持久化查询 -------------------------
 * <ul>
 *     <li><b>getInstantiate</b> - 查询对象持久化规则</li>
 *     <li><b>listTwinObjectRule</b> - 查询孪生对象持久化规则</li>
 * </ul>
 * <p>------------------------- 公共方法 -------------------------
 * <ul>
 *     <li><b>parseInstanceName</b> - 解析实例名称</li>
 *     <li><b>analyzeInfoByString</b> - 解析关联对象静态信息点</li>
 *     <li><b>parseBuildingFloorIdAndSpaces</b> - 从空间中解析建筑楼层ID</li>
 * </ul>
 *
 * @author yuyao
 */
public interface DtpRwdClientService extends BaseFeignClientService {

    // ------------------------- 通用查询 -------------------------

    /**
     * 查询对象数量
     *
     * @param param 查询参数
     * @return 对象数量
     * @throws FeignException RPC调用异常
     * @since 5.241102.0.0
     */
    int countObject(@NonNull DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询对象实例
     *
     * @param param 查询参数
     * @return 对象实例
     * @throws FeignException RPC调用异常
     * @since 5.240901.0
     */
    default Optional<DtpRwdObjectDTO> getObjectOpt(@NonNull DtpRwdObjectQueryParam param) throws FeignException {
        return Optional.ofNullable(getObject(false, false, ContextUtil.getProjectId(), param));
    }

    /**
     * 查询对象实例
     *
     * @param param 查询参数
     * @return 对象实例
     * @throws FeignException RPC调用异常
     * @since 5.241201.0
     */
    @Nullable
    default DtpRwdObjectDTO getObject(@NonNull DtpRwdObjectQueryParam param) throws FeignException {
        return getObject(false, false, ContextUtil.getProjectId(), param);
    }

    /**
     * 查询对象实例
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 对象实例
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到对象
     * @since v5.1.0
     * @deprecated {@link #getObjectOpt(DtpRwdObjectQueryParam)}
     */
    @Deprecated
    default DtpRwdObjectDTO getObject(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdObjectQueryParam param) throws FeignException, BusinessException {
        String errorMessage = LocaleMessageUtil.getMessage("key_041002fa55684d85a2e0fac32d2c7b91") + parseInstanceName(param.getClassCode()) + LocaleMessageUtil.getMessage("key_c927a53cbc1e4e76900dc749776db1f2");
        return getOne(isThrows, listObject(queryCache, isThrows, projectId, param), errorMessage);
    }

    /**
     * 查询对象实例
     *
     * @param param 查询参数
     * @return 对象实例
     * @throws FeignException RPC调用异常
     * @since 5.240901.0
     */
    List<DtpRwdObjectDTO> listObject(@NonNull DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询对象实例
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 对象实例
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到对象
     * @since v5.1.0
     * @deprecated {@link #listObject(DtpRwdObjectQueryParam)}
     */
    @Deprecated
    List<DtpRwdObjectDTO> listObject(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询对象ID和本地名称映射
     *
     * @param objectIds 对象ID
     * @return 对象ID -> 本地名称
     * @since 7.250801.0
     */
    Map<String, String> mapObjectIdAndLocalName(@NonNull Collection<String> objectIds);

    /**
     * 查询对象ID和本地名称映射
     *
     * @param isThrows  是否在查询失败|数据为空时抛出异常
     * @param objectIds 对象ID
     * @return 对象ID -> 本地名称
     * @since 5.240702.0
     */
    Map<String, String> mapObjectIdAndLocalName(
            boolean isThrows, @NonNull Collection<String> objectIds);

    /**
     * 查询关联对象
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 关联对象
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到关联对象
     * @since v5.1.0
     */
    @Deprecated
    List<DtpRwdRelationResponse> listRelationObject(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdRelationQueryParam param) throws FeignException, BusinessException;

    // ------------------------- 建筑查询 -------------------------

    /**
     * 查询建筑
     *
     * @param param 查询参数
     * @return 项目下建筑
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    List<DtpRwdBuildingDTO> listBuilding(
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询建筑
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下建筑
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到建筑
     * @since v5.1.0
     */
    List<DtpRwdBuildingDTO> listBuilding(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询楼层所在建筑
     *
     * @param floorIds 楼层ID, 为空时查询所有
     * @param param    查询参数
     * @return 项目下建筑
     * @throws FeignException RPC调用异常
     * @since v5.1.0
     */
    List<DtpRwdBuildingDTO> listBuildingByFloorId(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询空间所在建筑
     *
     * @param spaceIds 空间ID, 为空时查询所有
     * @param param    查询参数
     * @return 项目下建筑
     * @throws FeignException RPC调用异常
     * @since 5.240702.0
     */
    List<DtpRwdBuildingDTO> listBuildingBySpaceId(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备所在建筑
     *
     * @param equipmentId 设备ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 5.241001.0
     */
    List<DtpRwdBuildingDTO> listBuildingInByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    // ------------------------- 楼层查询 -------------------------

    /**
     * 查询楼层
     *
     * @param param 查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    List<DtpRwdFloorDTO> listFloor(@Nullable DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询楼层
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下楼层
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since v5.1.0
     */
    List<DtpRwdFloorDTO> listFloor(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询建筑下楼层
     *
     * @param buildingIds 建筑ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdFloorDTO> listFloorByBuildingId(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询空间所在楼层
     *
     * @param spaceIds 空间ID, 为空时查询所有
     * @param param    查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 241102.0
     */
    List<DtpRwdFloorDTO> listFloorBySpaceId(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询空间所在楼层
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param spaceIds   空间ID, 为空时查询所有
     * @param param      查询参数
     * @return 项目下楼层
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since 24Q1 T3S2-5.18.0
     * @deprecated {@link #listFloorBySpaceId(Collection, DtpRwdObjectQueryParam)}
     */
    @Deprecated
    List<DtpRwdFloorDTO> listFloorBySpaceId(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> spaceIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询设备所在楼层
     *
     * @param equipmentId 设备ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 5.240801.0
     */
    List<DtpRwdFloorDTO> listFloorInByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备服务楼层
     *
     * @param equipmentId 设备ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下楼层
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    List<DtpRwdFloorDTO> listFloorServiceByEquipmentId(
            @Nullable Collection<String> equipmentId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备服务楼层
     *
     * @param queryCache  是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows    是否在查询失败|数据为空时抛出异常
     * @param projectId   项目ID
     * @param equipmentId 设备ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下楼层
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since 24Q1 T3S2-5.18.0
     * @deprecated {@link #listFloorServiceByEquipmentId(Collection, DtpRwdObjectQueryParam)}
     */
    @Deprecated
    List<DtpRwdFloorDTO> listFloorServiceByEquipmentId(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentId, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    // ------------------------- 空间查询 -------------------------

    /**
     * 查询空间
     *
     * @param param 查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    List<DtpRwdSpaceDTO> listSpace(@Nullable DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询空调控制分区空间
     *
     * @param param 查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 7.250402.0
     */
    List<DtpRwdSpaceDTO> listACSpace(@Nullable DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询空间
     *
     * @param param 查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.241102.0
     */
    @Nullable
    default DtpRwdSpaceDTO getSpace(@Nullable DtpRwdObjectQueryParam param) throws FeignException {
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = listSpace(param);
        if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
            return null;
        }
        return dtpRwdSpaceDTOS.get(0);
    }

    /**
     * 查询空间
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下空间
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since v5.1.0
     * @deprecated {@link #listSpace(DtpRwdObjectQueryParam)}
     */
    @Deprecated
    List<DtpRwdSpaceDTO> listSpace(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 根据空间功能类型查询空间
     *
     * @param param 查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.240702.0
     */
    List<DtpRwdSpaceDTO> listSpaceByRoomFuncType(
            @NonNull @Valid DtpRwdSpaceByRoomFuncTypeQueryParam param)
            throws FeignException;

    /**
     * 查询项目楼层下的空间
     *
     * @param floorIds 楼层ID, 为空时查询所有
     * @param param    查询参数
     * @return 项目下空间
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdSpaceDTO> listSpaceByFloorId(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询一个小时内的最新数据
     *
     * @param objectIds 对象id
     * @param infoCodes 信息点
     * @return 一个小时内的最新数据
     */
    List<EnergyDataResponse> queryIpDataOnMomentInOneHour(Set<String> objectIds, Set<String> infoCodes);

    /**
     * 查询云上最新的数据
     * 自定义传入 时间区间和精度
     *
     * @param objectIds 对象id
     * @param infoCodes 信息点
     * @return 最新数据
     */
    List<EnergyDataResponse> queryIpDataOnMoment(
            @NonNull Set<String> objectIds, @NonNull Set<String> infoCodes,
            @NonNull DtpRwdPersistenceTimeTypeEnum ipType,
            @Nullable LocalDateTime startTime, @Nullable LocalDateTime endTime);

    /**
     * @param queryCache             是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows               是否在查询失败|数据为空时抛出异常
     * @param projectId              项目ID
     * @param targetRwdObjectTypeIds DtpRwdObjectTypeEnum对应的实例ids
     * @param targetRwdObjectType    DtpRwdClassCodeEnum 类型
     * @param filterClassCode        rwd对应的classCode 根据对应的类型实例过滤空间
     * @return
     */
    List<DtpRwdSpaceDTO> queryInterventionSpace(boolean queryCache, boolean isThrows, @NonNull String projectId, @NonNull List<String> targetRwdObjectTypeIds, @NonNull DtpRwdObjectTypeEnum targetRwdObjectType, @Nullable DtpRwdClassCodeEnum filterClassCode);

    /**
     * 查询项目建筑下的空间
     *
     * @param buildingIds 建筑ID, 为空时查询所有
     * @param param       查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.240702.0
     */
    List<DtpRwdSpaceDTO> listSpaceByBuildingId(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备服务空间
     *
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdSpaceDTO> listSpaceServiceByEquipmentId(
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备服务空间
     *
     * @param queryCache   是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.241201.0
     */
    List<DtpRwdSpaceDTO> listSpaceServiceByEquipmentId(
            boolean queryCache,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备所在空间
     *
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 项目下空间
     * @throws FeignException RPC调用异常
     * @since 5.240402.0
     */
    List<DtpRwdSpaceDTO> listSpaceInByEquipmentId(
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询设备所在空间
     *
     * @param queryCache   是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param projectId    项目ID
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 项目下空间
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since 5.240402.0
     */
    List<DtpRwdSpaceDTO> listSpaceInByEquipmentId(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    // ------------------------- 设备查询 -------------------------

    /**
     * 查询设备
     *
     * @param param 查询参数
     * @return 服务空间的设备
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since 5.240502.0
     */
    List<DtpRwdEquipmentDTO> listEquipment(
            @Nullable DtpRwdObjectQueryParam param) throws FeignException;

    /**
     * 查询服务空间的设备
     *
     * @param spaceIds 空间ID, 为空时查询所有
     * @param param    查询参数
     * @return 服务空间的设备
     * @throws FeignException RPC调用异常
     * @since 5.240502.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentServiceBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询服务空间的传感器信息
     *
     * @param spaceIds 空间ID, 为空时查询所有
     * @param param    查询参数
     * @return 服务空间的设备
     * @throws FeignException RPC调用异常
     * @since 5.240502.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentSensorBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询所在空间的设备
     *
     * @param spaceIds 空间ID, 为空时查询所有
     * @param param    查询参数
     * @return 空间下的设备
     * @throws FeignException RPC调用异常
     * @since 5.240601.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentInBySpaceIds(
            @Nullable Collection<String> spaceIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询安防所有的设备
     *
     * @param isThrows  是否在查询失败|数据为空时抛出异常
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 空间下的设备
     * @throws FeignException RPC调用异常
     * @since 5.240601.0
     */
    List<SecurityDtpRwdEquipmentDTO> listSecurityEquipment(
            boolean isThrows, @NonNull String projectId,
            @NonNull DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询楼层下的设备
     *
     * @param floorIds 楼层ID, 为空时查询所有
     * @param param    查询参数
     * @return 楼层下的设备
     * @throws FeignException RPC调用异常
     * @since 5.240502.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentInByFloorIds(
            @Nullable Collection<String> floorIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询建筑下的设备
     *
     * @param buildingIds 建筑ID, 为空时查询所有
     * @param param       查询参数
     * @return 建筑下的设备
     * @throws FeignException RPC调用异常
     * @since 5.240502.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentInByBuildingIds(
            @Nullable Collection<String> buildingIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    /**
     * 查询系统下的设备
     *
     * @param systemIds 系统ID, 为空时查询所有
     * @param param     查询参数
     * @return 建筑下的设备
     * @throws FeignException RPC调用异常
     * @since 7.250403.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentInBySystemIds(
            @Nullable Collection<String> systemIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    // ------------------------- 设备组查询 -------------------------

    /**
     * 查询系统下的设备组
     *
     * @param systemIds 系统ID, 为空时查询所有
     * @param param     查询参数
     * @return 系统下的设备组
     * @throws FeignException RPC调用异常
     * @since 7.251002.0
     */
    List<DtpRwdEquipmentDTO> listEquipmentGroupInBySystemIds(
            @Nullable Collection<String> systemIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException;

    // ------------------------- 中央系统查询 -------------------------

    /**
     * 查询中央系统
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 中央系统列表
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到中央供冷系统
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdObjectDTO> listCentralSystem(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    // ------------------------- 中央供冷系统查询 -------------------------

    /**
     * 查询中央供冷系统
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下中央供冷系统
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到中央供冷系统
     * @since v5.1.0
     */
    List<DtpRwdObjectDTO> listCentralCoolingSystem(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    // ------------------------- 中央冷热源系统查询 -------------------------

    /**
     * 查询中央冷热源系统
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下冷热源系统
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到中央冷热源系统
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdObjectDTO> listCentralCoolingHeatingSystem(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    // ------------------------- 冷机查询 -------------------------

    /**
     * 查询项目供冷系统下冷机
     *
     * @param queryCache   是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param projectId    项目ID
     * @param coldSystemId 中央供冷系统ID
     * @param param        查询参数
     * @return 项目下末级冷冻水泵控制组
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到冷冻水泵控制组
     * @since v5.6.0
     */
    List<DtpRwdRelationResponse> listChillerUnit(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull String coldSystemId, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 根据系统ID查询冷机分组
     *
     * @param systemId 系统ID
     * @return 冷机类型 -> 冷机大小 -> 冷机ID
     * @since 1.0.0
     */
    Map<ChillerTypeSub, Map<ChillerSize, List<String>>> queryChillerType2Size2Ids(@NonNull String systemId);

    // ------------------------- 冷冻水泵控制组查询 -------------------------

    /**
     * 查询冷冻水泵控制组
     *
     * @param queryCache    是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param projectId     项目ID
     * @param acccSystemIds 中央供冷系统ID
     * @param acatSystemIds 空调末端系统ID
     * @param param         查询参数
     * @param realLocalName 是否是自身的本地名称，true-不拼接末端系统名称，false-拼接
     * @return 项目下末级冷冻水泵控制组,
     * 拓展字段: relationAcccId, relationAcatId
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到冷冻水泵控制组
     * @since v5.6.0
     */
    List<DtpRwdFreezePumpDTO> listFreezePumpGroup(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> acccSystemIds,
            @Nullable Collection<String> acatSystemIds,
            @Nullable DtpRwdObjectQueryParam param,
            boolean realLocalName)
            throws FeignException, BusinessException;

    // ------------------------- 照明回路查询 -------------------------

    /**
     * 查询照明回路
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since v5.8.0
     */
    List<DtpRwdCircuitDTO> listCircuit(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询空间下的照明回路
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param spaceIds   空间ID, 为空时查询所有
     * @param param      查询参数
     * @return 空间下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since v5.8.0
     */
    List<DtpRwdCircuitDTO> listCircuitBySpaceId(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> spaceIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询照明控制模块关联照明回路
     *
     * @param queryCache    是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param projectId     项目ID
     * @param controllerIds 控制模块ID, 为空时查询所有
     * @param param         查询参数
     * @return 项目下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since v5.8.0
     */
    List<DtpRwdCircuitDTO> listCircuitByControllerId(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> controllerIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    // ------------------------- 照明控制模块查询 -------------------------

    /**
     * 查询照明控制模块
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 项目下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listCircuitController(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询照明回路关联控制模块
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param spaceIds   空间ID, 为空时查询所有
     * @param param      查询参数
     * @return 项目下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listCircuitControllerInBySpaceIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> spaceIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询照明回路关联控制模块
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param circuitIds 照明回路ID, 为空时查询所有
     * @param param      查询参数
     * @return 项目下照明回路
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listCircuitControllerByCircuitIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> circuitIds, @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    // ------------------------- 低压末端配电柜查询 -------------------------

    /**
     * 查询低压末端配电柜
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param param      查询参数
     * @return 低压末端配电柜
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listDistributionCabinet(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询设备关联低压末端配电柜
     *
     * @param queryCache   是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param projectId    项目ID
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 低压末端配电柜
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listDistributionCabinetByEquipmentIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询设备下的设备
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param projectId    项目ID
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 设备
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到照明回路
     */
    List<DtpRwdEquipmentDTO> listLocationInsideByEquipmentIds(
            boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    /**
     * 查询污水设备下的潜污泵
     * <p>
     * 优先查询 {@code MechSubset/Eq2Eq} 关系，无结果时回退到 {@code MechLocation/Inside} 关系。
     * </p>
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param projectId    项目ID
     * @param equipmentIds 设备ID, 为空时查询所有
     * @param param        查询参数
     * @return 潜污泵设备
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到设备
     * @since 7.260702.0
     */
    List<DtpRwdEquipmentDTO> listSubmersibleSewagePumpByEquipmentIds(
            boolean isThrows, @NonNull String projectId,
            @Nullable Collection<String> equipmentIds,
            @Nullable DtpRwdObjectQueryParam param)
            throws FeignException, BusinessException;

    // ------------------------- 持久化查询 -------------------------

    /**
     * 查询信息点精度
     *
     * @param objectIds 对象ID集合
     * @return 持久化规则
     * @since v7.250102.0
     */
    List<DtpRwdQueryPointListDTO> queryPointList(@NonNull Set<String> objectIds,
                                                 @NonNull Set<String> infoCodes);

    /**
     * 查询信息点精度
     *
     * @param objectIds 对象ID集合
     * @return 持久化规则
     * @since v7.250102.0
     */
    List<DtpRwdFractionalPrecisionRuleDTO> queryInfoPointPeroid(@NonNull Set<String> objectIds);

    /**
     * 查询持久化规则
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param objectId   对象ID
     * @param infoCode   信息点
     * @return 持久化规则
     * @see #listExistsInstantiateObjectIds(boolean, boolean, String, Collection)
     * @since v5.7.0
     */
    ObjectInstantiateResponse getInstantiate(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull String objectId, @NonNull String infoCode);

    /**
     * 查询持久化规则是否存在
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param params     查询参数
     * @return 存在实例化规则的对象ID列表
     * @see #getInstantiate(boolean, boolean, String, String, String)
     * @since v6.0.0
     */
    List<String> listExistsInstantiateObjectIds(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull @Valid Collection<ObjectInstantiateQueryParam> params);

    /**
     * 查询对象ID和持久化信息点映射
     *
     * @param projectId 项目ID
     * @param params    查询参数
     * @return 对象ID -> 实例化信息点编码
     * @since 5.240701.0
     */
    Map<String, Set<String>> mapObjectIdAndInstantiateInfoCodes(
            @NonNull String projectId, @NonNull Collection<ObjectInstantiateQueryParam> params);

    List<ObjectsExistRule> listObjectsOfExistRuleByClassCodeToTerminal(Collection<DtpRwdObjectsOfExistRuleQueryParam> param, TerminalEnum terminal);

    /**
     * 根据classCode查询持久化规则
     *
     * @param param     查询参数
     * @return 存在实例化规则的对象List
     * @since 7.250802.0
     */
    List<ObjectsExistRule> listObjectsOfExistRuleByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param);
    /**
     * 根据classCode查询孪生规则
     *
     * @param param     查询参数
     * @return 存在实例化规则的对象List
     * @since 7.250802.0
     */
    List<ObjectsExistRule> listTwinObjectsOfExistRuleByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param);

    /**
     * 根据classCode查询对象ID规则Map
     *
     * @param param     查询参数
     * @return 孪生对象规则,key:classCode,key:objectId,value:infoCodes
     * @since7.250802.0
     */
    Map<String, Map<String, Set<String>>> mapClassCodeObjectIdInfoCodesByClassCode(Collection<DtpRwdObjectsOfExistRuleQueryParam> param);

    /**
     * 孪生对象规则查询
     *
     * @param param 查询参数
     * @return 孪生对象规则
     * @since 7.250102.0
     */
    List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            @NonNull DtpRwdTwinObjectRuleQueryParam param);

    /**
     * 孪生对象规则查询
     *
     * @param param 查询参数
     * @return 孪生对象规则
     * @since 7.250602.0
     */
    List<DtpRwdTwinObjectRuleByIdResponse> listTwinObjectRuleById(
            @NonNull DtpRwdTwinObjectRuleByIdQueryParam param);

    /**
     * 孪生对象规则查询
     *
     * @param params 查询参数
     * @return 孪生对象规则
     * @since 7.250102.0
     */
    List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params);

    /**
     * 孪生对象规则查询
     *
     * @param isThrows  是否在查询失败|数据为空时抛出异常
     * @param projectId 项目ID
     * @param params    查询参数
     * @return 孪生对象规则
     * @since v5.6.0
     * @deprecated {@link #listTwinObjectRule(DtpRwdTwinObjectRuleQueryParam)} or {@link #listTwinObjectRule(Collection)}
     */
    @Deprecated
    List<DtpRwdTwinObjectRuleResponse> listTwinObjectRule(
            boolean isThrows, @NonNull String projectId,
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params);

    /**
     * 查询对象ID和孪生规则信息点映射
     *
     * @param param 查询参数
     * @return 对象ID -> 孪生信息点编码
     * @since 7.250102.0
     */
    Map<String, Set<String>> mapObjectIdAndTwinObjectRuleInfoCodes(
            @NonNull DtpRwdTwinObjectRuleQueryParam param);

    /**
     * 查询对象ID和孪生规则信息点映射
     *
     * @param params 查询参数
     * @return 对象ID -> 孪生信息点编码
     * @since 7.250102.0
     */
    Map<String, Set<String>> mapObjectIdAndTwinObjectRuleInfoCodes(
            @NonNull Collection<DtpRwdTwinObjectRuleQueryParam> params);

    /**
     * 孪生控制对象查询
     *
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param classCodes 对象类编码
     * @return 孪生控制对象
     * @since 5.240601.0
     */
    List<DtpRwdTwinControlObjectDTO> listTwinControlObjects(
            boolean isThrows, @NonNull String projectId,
            @NonNull Collection<String> classCodes);

    /**
     * 查询最新的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCodes
     * @return
     */
    List<EnergyDataResponse> queryIpDataOnMoment(String groupCode, String projectId, List<String> objectIds, List<String> infoCodes);

    /**
     * 查询某一时刻的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCodes
     * @param startDateTime
     * @param endDateTime
     * @return
     */
    List<EnergyDataResponse> queryIPDataInPeriod(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes, LocalDateTime startDateTime, LocalDateTime endDateTime, String ipType);

    /**
     * @return 厨房排烟信息信息
     */
    List<KitchenExhaustResponse> queryKitchenExhaust();

    // ------------------------- 公共方法 -------------------------

    /**
     * 解析实例名称
     *
     * @param classCodes 类型编码
     * @return 实例名称
     * @since v5.1.0
     */
    default String parseInstanceName(@Nullable Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return LocaleMessageUtil.getMessage("key_683a9ce893c54c73ad68e143024bb27b");
        }
        return String.join("|", classCodes);
    }

    /**
     * 解析建筑楼层ID和空间数据
     *
     * @param spaces 空间列表
     * @return 建筑ID-楼层ID -> 空间数据
     * @since v5.7.0
     */
    default MultiKeyMap<String, List<DtpRwdSpaceDTO>> parseBuildingFloorIdAndSpaces(
            @Nullable Collection<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(spaces)) {
            return new MultiKeyMap<>();
        }
        MultiKeyMap<String, List<DtpRwdSpaceDTO>> buildingFloorIdAndControlSpaces = new MultiKeyMap<>();
        for (DtpRwdSpaceDTO space : spaces) {
            String buildingId = space.getBuildingId();
            String floorId = space.getFloorId();
            if (StringUtils.isAnyBlank(buildingId, floorId)) {
                continue;
            }
            MultiKey<String> multiKey = new MultiKey<>(buildingId, floorId);
            List<DtpRwdSpaceDTO> innerSpaces = Optional.ofNullable(
                    buildingFloorIdAndControlSpaces.get(multiKey)).orElseGet(ArrayList::new);
            innerSpaces.add(space);
            buildingFloorIdAndControlSpaces.put(multiKey, innerSpaces);
        }
        return buildingFloorIdAndControlSpaces;
    }

    /**
     * 获取对象ID和关联对象映射, 反向解析关联对象中 {@code relObjIds} 字段和对象自身
     *
     * @param objectsWithRelObjectIds 带关联对象的对象列表
     * @param <T>                     对象类型
     * @return 关联对象ID -> 对象
     * @since 5.241202.0
     */
    default <T extends DtpRwdObjectDTO> Map<String, List<T>> mapObjectIdAndRelObjects(
            @Nullable Collection<T> objectsWithRelObjectIds) {
        if (CollectionUtils.isEmpty(objectsWithRelObjectIds)) {
            return Maps.newHashMap();
        }
        Map<String, List<T>> result = new LinkedHashMap<>();
        for (T object : objectsWithRelObjectIds) {
            Set<String> relObjIds = object.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relObjIds)) {
                relObjIds.forEach(item -> MapExecutor.dynamicPut(result, item, object, ArrayList::new));
            }
        }
        return result;
    }

    default <T extends DtpRwdObjectDTO> Map<String, List<T>> mapObjectIdAndRelObjects(
            @Nullable Collection<T> objectsWithRelObjectIds,List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objectsWithRelObjectIds)) {
            return Maps.newHashMap();
        }
        //查询冷却塔控制组的总数量时：需要加限定条件，from冷却塔控制组，to冷却塔风机，而不是查询所有的设备类），字典定义了四类关系，但仅统计风机的数量
        Set<String> coolingTowerFanIds= objects.stream().filter(e -> DtpRwdClassCodeEnum.ACOTC_GROUP.getValue().equals(e.getClassCode()))
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, List<T>> result = new LinkedHashMap<>();
        for (T object : objectsWithRelObjectIds) {
            Set<String> relObjIds = object.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relObjIds)) {
                relObjIds.forEach(item -> {
                    if (coolingTowerFanIds.contains(item)
                            && !DtpRwdClassCodeEnum.ACCCOF.getValue().equals(object.getClassCode())) {
                        // item 是冷却塔控制组，但 object 不是冷却塔风机，跳过
                        return;
                    }
                    MapExecutor.dynamicPut(result, item, object, ArrayList::new);
                });

            }
        }
        return result;
    }

    Map<ChillerTypeSub, Map<String, List<String>>> queryChillerGroupById(
            String groupCode, String projectId, String objectId, String classCode);

    Map<String, Map<ChillerSize, List<String>>> queryPumpGroups(Set<String> pumpIds);

}
