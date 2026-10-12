package com.persagy.apccontrol.basic.structure.demand.strategy.lighting;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 需求管理-照明需求策略接口
 *
 * <p>------------------------- 外部方法-照明回路 -------------------------
 * <ul>
 *     <li><b>listCircuitServiceAreaTypes</b> - 查询照明回路服务区域类型</li>
 *     <li><b>listCircuits</b> - 查询照明回路</li>
 *     <li><b>listCircuitsByInstance</b> - 查询配置实例关联的照明回路</li>
 *     <li><b>listCircuitsBySpaceIds</b> - 查询空间下的照明回路</li>
 *     <li><b>listCircuitsByController</b> - 查询照明控制模块关联的照明回路</li>
 *     <li><b>mapCircuitInfoCodeAndName</b> - 查询照明回路信息点映射</li>
 *     <li><b>mapCircuitIdAndData</b> - 查询照明回路ID和照明回路映射</li>
 *     <li><b>mapCircuitIdAndSpace</b> - 查询照明回路ID和空间映射</li>
 *     <li><b>mapCircuitIdAndSpaceFuncTypes</b> - 查询照明回路ID和空间功能类型映射</li>
 *     <li><b>mapCircuitIdAndControllers</b> - 查询照明回路ID和控制模块映射</li>
 *     <li><b>mapCircuitIdAndDistributionCabinet</b> - 查询照明回路ID和低压末端配电柜映射</li>
 * </ul>
 * <p>------------------------- 外部方法-照明控制模块 -------------------------
 * <ul>
 *     <li><b>listControllers</b> - 查询照明控制模块</li>
 *     <li><b>listControllersByInstance</b> - 查询配置实例关联的照明控制模块</li>
 *     <li><b>listControllersBySpaceIds</b> - 查询空间下的照明控制模块</li>
 *     <li><b>listControllersByCircuit</b> - 查询照明回路关到的照明控制模块</li>
 *     <li><b>mapControllerIdAndData</b> - 查询照明控制模ID和照明控制模块映射</li>
 *     <li><b>mapControllerIdAndSpace</b> - 查询照明控制模块ID和空间映射</li>
 *     <li><b>mapControllerIdAndCircuits</b> - 查询照明控制模ID和照明回路映射</li>
 *     <li><b>mapControllerIdAndDistributionCabinet</b> - 查询照明控制模块ID和低压末端配电柜映射</li>
 * </ul>
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listSpaceIdsByInstance</b> - 查询配置实例关联的空间ID</li>
 *     <li><b>listSpacesByCircuit</b> - 查询照明回路服务空间</li>
 *     <li><b>listSpacesByController</b> - 查询照明控制模块所在空间</li>
 *     <li><b>listFloorsByCircuit</b> - 查询照明回路服务楼层</li>
 *     <li><b>queryBuildingFloorSpaceObject</b> - 查询建筑楼层空间树</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandLightingStrategy extends DemandStrategy {

    /**
     * 默认照明回路Map大小
     */
    int DEFAULT_CIRCUIT_MAP_SIZE = 1024;

    /**
     * 默认照明控制模块Map大小
     */
    int DEFAULT_CONTROLLER_MAP_SIZE = DEFAULT_CIRCUIT_MAP_SIZE / 2;

    /**
     * 物理世界是否查询缓存
     */
    boolean RWD_QUERY_CACHE = true;

    /**
     * 特殊夜景照明灯具功能类型
     *
     * <ul>
     *     <li><b>8</b> - 洗墙灯</li>
     *     <li><b>10</b> - 投光灯</li>
     * </ul>
     */
    Set<String> SPECIAL_NIGHT_LIGHTING_LAMP_FUNC_TYPES = Sets.newHashSet("8", "10");

    /**
     * 查询照明回路服务区域类型
     *
     * @param templateType 照明模版类型
     * @return 照明回路服务区域类型
     * @since 24Q1 T3S1-5.17.0
     */
    Set<String> listCircuitServiceAreaTypes(@NonNull DemandTemplateTypeEnum templateType);

    /**
     * 查询照明回路
     *
     * @param templateTypes     模版类型
     * @param circuitQueryParam 照明回路查询参数
     * @param spaceQueryParam   非夜景照明回路服务空间查询参数
     * @return 照明回路
     * @since 7.250801.0
     */
    List<DtpRwdCircuitDTO> listCircuits(
            @NonNull Collection<DemandTemplateTypeEnum> templateTypes,
            @Nullable EquipmentQueryParam circuitQueryParam,
            @Nullable SpaceQueryParam spaceQueryParam);

    /**
     * 查询缓存对象模型构建所需的照明回路。
     *
     * <p>查询范围与 {@link #listCircuits(Collection, EquipmentQueryParam, SpaceQueryParam)} 一致，
     * 但回路、服务空间和服务楼层分别使用各自设备类的字段集合。</p>
     *
     * @param templateTypes     模板类型
     * @param circuitQueryParam 照明回路查询参数
     * @param spaceQueryParam   非夜景照明回路服务空间查询参数
     * @param floorWithColumns  夜景照明回路服务楼层查询字段
     * @return 查询条件对应的照明回路
     * @since v7.260701.0
     */
    @NonNull List<DtpRwdCircuitDTO> listCircuitsForCacheBuild(
            @NonNull Collection<DemandTemplateTypeEnum> templateTypes,
            @Nullable EquipmentQueryParam circuitQueryParam,
            @Nullable SpaceQueryParam spaceQueryParam,
            @NonNull Set<String> floorWithColumns);

    /**
     * 查询照明回路
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @return 照明回路
     * @see #listCircuits(Collection, EquipmentQueryParam, SpaceQueryParam)
     * @since 24Q1 T3S1-5.17.0
     * @deprecated 7.250801.0
     */
    List<DtpRwdCircuitDTO> listCircuits(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询配置实例关联的照明回路
     *
     * @param projectId    项目ID
     * @param templateType 照明模版类型
     * @param instanceId   实例ID
     * @param instanceType 实例类型
     * @return 照明回路关联所有配置实例
     * @see #listCircuits(Collection, EquipmentQueryParam, SpaceQueryParam)
     * @since 24Q1 T3S1-5.17.0
     * @deprecated 7.250801.0
     */
    List<DtpRwdCircuitDTO> listCircuitsByInstance(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询空间下的照明回路
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterSpaceIds   过滤空间ID, 为空时查询所有
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @param queryCache       是否查询缓存 null就走通用配置开关
     * @return 空间下的照明回路
     * @see #listCircuits(Collection, EquipmentQueryParam, SpaceQueryParam)
     * @since 24Q1 T3S1-5.17.0
     * @deprecated 7.250801.0
     */
    List<DtpRwdCircuitDTO> listCircuitsBySpaceIds(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Collection<String> filterCircuitIds,
            Boolean queryCache,
            Collection<String> withColumns);

    /**
     * 查询照明控制模块关联的照明回路
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @param filterCircuitIds    过滤照明回路ID, 为空时查询所有
     * @return 照明回路
     * @since 5.240402.0
     */
    List<DtpRwdCircuitDTO> listCircuitsByController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询照明控制模块
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @return 照明控制模块
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listControllers(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds);

    /**
     * 查询配置实例关联的照明控制模块
     *
     * @param projectId    项目ID
     * @param templateType 照明模版类型
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @return 照明控制模块
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listControllersByInstance(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId);

    /**
     * 查询空间下的照明控制模块
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterSpaceIds      过滤空间ID, 为空时查询所有
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @return 空间下的照明控制模块
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listControllersBySpaceIds(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Collection<String> filterControllerIds);

    /**
     * 查询照明回路关到的照明控制模块
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterCircuitIds    过滤照明回路ID, 为空时查询所有
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @return 照明控制模块
     * @since 5.240402.0
     */
    List<DtpRwdObjectDTO> listControllersByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterControllerIds);

    /**
     * 查询照明回路服务空间
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @param filterSpaceIds   过滤空间ID, 为空时查询所有
     * @return 空间实例, {@code relObjIds} 字段为关联照明回路ID
     * @since 24Q1 T3S1-5.17.0
     */
    List<DtpRwdSpaceDTO> listSpacesByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterSpaceIds);

    /**
     * 查询照明控制模块所在空间
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @param filterSpaceIds      过滤空间ID, 为空时查询所有
     * @return 空间实例, {@code relObjIds} 字段为关联照明控制模块ID
     * @since 5.240402.0
     */
    List<DtpRwdSpaceDTO> listSpacesByController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterSpaceIds);

    /**
     * 查询照明回路服务楼层
     *
     * @param projectId    项目ID
     * @param templateType 照明模版类型
     * @param circuitIds   照明回路ID, 为空时查询所有
     * @param floorIds     楼层ID, 为空时查询所有
     * @return 空间实例, {@code relObjIds} 字段为关联照明回路ID
     * @since 24Q1 T3S2-5.18.0
     */
    List<DtpRwdFloorDTO> listFloorsByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> circuitIds,
            @Nullable Collection<String> floorIds);

    /**
     * 查询当前时间所在场景
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @return 当前时间所在场景
     * @since 5.240402.0
     * @deprecated {@link com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway#getValidSceneByNow(DemandTemplateTypeEnum)}
     */
    @Deprecated
    @Nullable
    LightingSceneDTO getSceneByNow(@NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType);

    /**
     * 根据日期查询照明场景列表
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param date         日期
     * @return 照明场景列表
     * @since 5.240402.0
     * @deprecated {@link com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway#listValidScenesByDate(DemandTemplateTypeEnum, LocalDate)}
     */
    @Deprecated
    List<LightingSceneDTO> listScenesByTime(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date);

    /**
     * 根据日期查询所所有时间段照明场景列表
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param date         日期
     * @return 照明场景列表
     * @since 5.240402.0
     * @deprecated {@link com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway#listValidScenesByAllDate(DemandTemplateTypeEnum, LocalDate)}
     */
    @Deprecated
    List<LightingSceneDTO> listScenesByAllTime(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date);

    /**
     * 查询照明回路信息点映射
     *
     * @param infoCodeName 信息点名称
     * @return 信息点Key -> 信息点名称
     * @since 24Q1 T3S2-5.18.0
     */
    Map<String, String> mapCircuitInfoCodeAndName(@NonNull String infoCodeName);

    /**
     * 查询照明回路ID和空间功能类型映射
     *
     * @param projectId    项目ID
     * @param templateType 照明模版类型
     * @return 照明回路ID -> 空间功能类型
     * @since 24Q1 T3S1-5.17.0
     */
    Map<String, List<String>> mapCircuitIdAndSpaceFuncTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType);

    /**
     * 查询照明回路ID和照明回路映射
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @return 照明回路ID -> 照明回路
     * @since 5.240402.0
     */
    Map<String, DtpRwdCircuitDTO> mapCircuitIdAndData(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询照明回路ID和空间映射
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @param filterSpaceIds   过滤空间ID, 为空时查询所有
     * @return 照明回路ID -> 空间
     * @since 5.240402.0
     */
    Map<String, DtpRwdSpaceDTO> mapCircuitIdAndSpace(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterSpaceIds);

    /**
     * 查询照明回路ID和空间功能类型映射
     *
     * @param projectId        项目ID
     * @param templateType     照明模版类型
     * @param filterCircuitIds 过滤回路ID集合, 为空时擦洗所有
     * @return 照明回路ID -> 空间功能类型
     * @author yuecaipu
     * @since 24Q1 T3S2-5.18.0
     */
    Map<String, List<String>> mapCircuitIdAndSpaceFuncTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询照明回路ID和控制模块映射
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterCircuitIds    过滤照明回路ID, 为空时擦洗所有
     * @param filterControllerIds 过滤控制模块ID, 为空时擦洗所有
     * @return 照明回路ID -> 关联控制模块
     * @since 5.240402.0
     */
    Map<String, DtpRwdObjectDTO> mapCircuitIdAndController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterControllerIds);

    /**
     * 查询照明回路ID和低压末端配电柜映射
     *
     * @param projectId        项目ID
     * @param filterCircuitIds 过滤照明回路ID, 为空时查询所有
     * @return 照明回路ID -> 关联低压末端配电柜
     * @since 5.240402.0
     */
    Map<String, DtpRwdObjectDTO> mapCircuitIdAndDistributionCabinet(
            @NonNull String projectId, @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询照明控制模ID和照明控制模块映射
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @return 照明控制模块ID -> 照明控制模块
     * @since 5.240402.0
     */
    Map<String, DtpRwdObjectDTO> mapControllerIdAndData(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds);

    /**
     * 查询照明控制模块ID和空间映射
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @param filterSpaceIds      过滤空间ID, 为空时查询所有
     * @return 照明控制模块ID -> 空间
     * @since 5.240402.0
     */
    Map<String, DtpRwdSpaceDTO> mapControllerIdAndSpace(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterSpaceIds);

    /**
     * 查询照明控制模ID和照明回路映射
     *
     * @param projectId           项目ID
     * @param templateType        照明模版类型
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @param filterCircuitIds    过滤照明回路ID, 为空时查询所有
     * @return 照明控制模块ID -> 关联照明回路
     * @since 5.240402.0
     */
    Map<String, Set<DtpRwdCircuitDTO>> mapControllerIdAndCircuits(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterCircuitIds);

    /**
     * 查询照明控制模块ID和低压末端配电柜映射
     *
     * @param projectId           项目ID
     * @param filterControllerIds 过滤照明控制模块ID, 为空时查询所有
     * @return 照明控制模块ID -> 关联低压末端配电柜
     * @since 5.240402.0
     */
    Map<String, DtpRwdObjectDTO> mapControllerIdAndDistributionCabinet(
            @NonNull String projectId, @Nullable Collection<String> filterControllerIds);

    /**
     * 查询项目下照明业务相关的建筑楼层空间对象
     *
     * @param projectId
     * @param type
     * @return DtpRwdTreeObjectDTO 父子结构
     * 第一层 建筑 第二层楼层 第三层空间
     */
    List<DtpRwdTreeLightingObjectDTO> queryBuildingFloorSpaceObject(@NonNull String projectId,
                                                                    @NonNull DemandTemplateTypeEnum type);


    /**
     * 根据回路查询服务的空间
     *
     * @param projectId      项目ID
     * @param circuitIds     回路列表
     * @param filterSpaceIds 过滤空间ID
     * @param withColumns    需要查询字段 为空查询所有
     * @return {@link List<DtpRwdSpaceDTO>}
     * @author yuecaipu
     * @date 2024/7/5 20:23
     * @since v5.240502
     */
    List<DtpRwdSpaceDTO> querySpaceByCircuit(
            @NonNull String projectId,
            @NonNull Collection<String> circuitIds,
            @Nullable Collection<String> filterSpaceIds,
            Collection<String> withColumns);

    /**
     * 组装空间对应的实际最终配置
     * spaceId -> 空间对应的配置
     *
     * @param projectId              项目ID
     * @param demandTemplateType     模板类型
     * @param spaceId2originalConfig 空间ID -> 空间对应的原始配置(自定义配置或者引用的上级配置)
     * @return {@link Map<String,List<DemandLightingFuncTypeSettingDTO>>} spaceId -> 空间对应的配置
     * @author yuecaipu
     * @date 2024/8/12 19:38
     * @since v5.240702
     */
    Map<String, List<DemandLightingFuncTypeSettingDTO>> spaceSettingsBuilding(String projectId, DemandTemplateTypeEnum demandTemplateType, Map<String, DemandConfigLightingDataDTO> spaceId2originalConfig);
}
