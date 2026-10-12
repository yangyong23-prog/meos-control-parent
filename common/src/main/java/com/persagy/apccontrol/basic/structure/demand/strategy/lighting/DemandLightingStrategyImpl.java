package com.persagy.apccontrol.basic.structure.demand.strategy.lighting;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.lighting.DemandLightingProperties;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultLightingVO;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingSceneService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import com.persagy.apccontrol.basic.structure.demand.converter.lighting.DemandLightingConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandTemplateChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.strategy.AbstractDemandStrategyProcessor;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.PriorityOrdered;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求策略接口实现
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
 * <p>------------------------- 内部方法 -------------------------
 * <ul>
 *     <li><b>filterCircuits</b> - 过滤照明回路</li>
 *     <li><b>querySpaces</b> - 查询空间</li>
 *     <li><b>querySpaceByCircuit</b> - 根据照明回路查询空间列表</li>
 *     <li><b>queryFloorByCircuit</b> - 根据照明回路查询楼层列表</li>
 *     <li><b>listInfoCodeAndDataSources</b> - 查询信息点编码和数据字典定义</li>
 *     <li><b>mapEquipmentIdAndDistributionCabinet</b> - 查询设备ID和低压末端配电柜映射</li>
 *     <li><b>publishConfigChangeMessage</b> - 发布配置变动消息</li>
 *     <li><b>buildTreeObject</b> - 构建建筑楼层空间树</li>
 *     <li><b>buildLampFuncTypeAndFuncType</b> - 构建灯具类型和功能类型</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Slf4j
@Service(DemandLightingStrategyImpl.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandLightingStrategyImpl
        extends AbstractDemandStrategyProcessor
        implements DemandLightingStrategy, BizComponent<DemandLightingStrategy>, PriorityOrdered {

    public static final String BEAN_NAME = "demandLightingStrategyImpl";
    private static final String LOG_TAG = "需求管理-照明需求策略";

    private final DtpRwdConvert dtpRwdConvert;
    private final DemandLightingProperties demandLightingProperties;
    private final DemandLightingConverter demandLightingConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final InstanceService instanceService;
    private final DemandConfigLightingService demandConfigLightingService;
    private final DemandConfigLightingSceneService demandConfigLightingSceneService;
    private final DemandConfigLightingResultService demandConfigLightingResultService;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final WeatherClientService weatherClientService;
    private final DemandSceneGateway demandSceneGateway;

    @Override
    public Set<String> listCircuitServiceAreaTypes(@NonNull DemandTemplateTypeEnum templateType) {
        Map<DemandTemplateTypeEnum, Set<String>> serviceAreaType = demandLightingProperties.getServiceAreaType();
        Set<String> serviceAreaTypes = MapUtils.getObject(serviceAreaType, templateType);
        if (CollectionUtils.isEmpty(serviceAreaTypes)) {
            logProjectInstance().warn("无服务区域类型",
                    Collections.singletonList(Pair.of("templateType", templateType)));
            return Sets.newHashSet();
        }
        return serviceAreaTypes;
    }

    @Override
    public List<DtpRwdCircuitDTO> listCircuits(
            @NonNull Collection<DemandTemplateTypeEnum> templateTypes,
            @Nullable EquipmentQueryParam circuitQueryParam,
            @Nullable SpaceQueryParam spaceQueryParam) {
        return this.listCircuitsInternal(
                templateTypes, circuitQueryParam, spaceQueryParam, null);
    }

    @Override
    public @NonNull List<DtpRwdCircuitDTO> listCircuitsForCacheBuild(
            @NonNull Collection<DemandTemplateTypeEnum> templateTypes,
            @Nullable EquipmentQueryParam circuitQueryParam,
            @Nullable SpaceQueryParam spaceQueryParam,
            @NonNull Set<String> floorWithColumns) {
        return this.listCircuitsInternal(
                templateTypes, circuitQueryParam, spaceQueryParam,
                floorWithColumns);
    }

    /**
     * 查询并补全照明回路。
     *
     * <p>过滤、服务空间和服务楼层的业务规则保持一份实现；缓存构建可单独指定楼层字段，
     * 避免把回路字段带入楼层查询。</p>
     *
     * @param templateTypes     模板类型
     * @param circuitQueryParam 照明回路查询参数
     * @param spaceQueryParam   非夜景照明回路服务空间查询参数
     * @param floorWithColumns  缓存构建的服务楼层查询字段；{@code null} 时沿用回路字段
     * @return 查询条件对应的照明回路
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdCircuitDTO> listCircuitsInternal(
            @NonNull Collection<DemandTemplateTypeEnum> templateTypes,
            @Nullable EquipmentQueryParam circuitQueryParam,
            @Nullable SpaceQueryParam spaceQueryParam,
            @Nullable Set<String> floorWithColumns) {
        Set<String> serviceAreaTypes = templateTypes.stream()
                .map(this::listCircuitServiceAreaTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(serviceAreaTypes)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT)
                .addInfoCode(DtpRwdCircuitDTO.Fields.serviceAreaType, serviceAreaTypes)
                .addWithColumn(DtpRwdCircuitDTO.Fields.serviceAreaType, DtpRwdCircuitDTO.Fields.funcType)
                .addWithColumnByMonitoringSurveillanceStatus();
        circuitQueryParam = Optional.ofNullable(circuitQueryParam).orElseGet(EquipmentQueryParam::new);
        List<DtpRwdCircuitDTO> circuits = circuitQueryParam.queryObject(queryParam).stream()
                .map(dtpRwdConvert::toCircuitDto)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        circuits.removeIf(item -> {
            if (StringUtils.isBlank(item.getServiceAreaType()) || StringUtils.isBlank(item.getFuncType())) {
                return true;
            }
            DemandTemplateTypeEnum demandTemplateType = item.toDemandTemplateType();
            if (Objects.isNull(demandTemplateType)) {
                return true;
            }
            // 过滤可监可控状态
            return !item.isControlled();
        });
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        // 过滤并填充非夜景照明回路服务空间
        this.filterAndFillServiceSpaces(circuits, spaceQueryParam);
        // 缓存构建按 FLOOR 单独传字段，避免回路与楼层请求互相夹带对方设备类的信息点。
        Set<String> serviceFloorWithColumns = floorWithColumns == null
                ? circuitQueryParam.getWithColumns()
                : floorWithColumns;
        this.fillServiceFloors(circuits, serviceFloorWithColumns);
        return circuits;
    }

    /**
     * 填充服务楼层
     *
     * @param circuits 回路
     * @param withColumns 查询字段
     * @since 7.250801.0
     */
    private void fillServiceFloors(
            @NonNull List<DtpRwdCircuitDTO> circuits,
            @Nullable Set<String> withColumns) {
        if (CollectionUtils.isEmpty(circuits)) {
            return;
        }
        Set<String> nightCircuitIds = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            DemandTemplateTypeEnum demandTemplateType = circuit.toDemandTemplateType();
            if (Objects.isNull(demandTemplateType)) {
                continue;
            }
            // 夜景照明
            if (demandTemplateType.isNightLighting()) {
                nightCircuitIds.add(circuit.getId());
            }
        }
        if (CollectionUtils.isEmpty(nightCircuitIds)) {
            return;
        }
        DtpRwdObjectQueryParam objectQueryParam = new DtpRwdObjectQueryParam();
        if (CollectionUtils.isNotEmpty(withColumns)) {
            objectQueryParam.setWithColumns(withColumns);
        }
        objectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdFloorDTO> floors = this.dtpRwdClientService
                .listFloorServiceByEquipmentId(nightCircuitIds, objectQueryParam);
        Map<String, List<DtpRwdFloorDTO>> circuitIdAndServiceFloors = DtpRwdFloorDTO.mapRelObjIdAndData(floors);
        Iterator<DtpRwdCircuitDTO> iterator = circuits.iterator();
        while (iterator.hasNext()) {
            DtpRwdCircuitDTO circuit = iterator.next();
            String circuitId = circuit.getId();
            if (!nightCircuitIds.contains(circuitId)) {
                continue;
            }
            List<DtpRwdFloorDTO> serviceFloors = circuitIdAndServiceFloors.get(circuit.getId());
            if (CollectionUtils.isEmpty(serviceFloors)) {
                continue;
            }
            // 清空关联对象ID, 避免返回数据大
            serviceFloors.forEach(item -> item.setRelObjIds(null));
            circuit.setServiceFloors(serviceFloors);
        }

    }

    /**
     * 过滤并填充非夜景照明回路服务空间
     *
     * @param circuits 照明回路
     * @param spaceQueryParam 空间查询参数
     * @since 7.250801.0
     */
    private void filterAndFillServiceSpaces(
            @NonNull List<DtpRwdCircuitDTO> circuits,
            @Nullable SpaceQueryParam spaceQueryParam) {
        if (CollectionUtils.isEmpty(circuits)) {
            return;
        }
        Set<String> publicCircuitIds = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            DemandTemplateTypeEnum demandTemplateType = circuit.toDemandTemplateType();
            if (Objects.isNull(demandTemplateType)) {
                continue;
            }
            if (!demandTemplateType.isNightLighting()) {
                publicCircuitIds.add(circuit.getId());
            }
        }
        if (CollectionUtils.isEmpty(publicCircuitIds)) {
            return;
        }
        DtpRwdObjectQueryParam relSpaceQueryParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                .setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(publicCircuitIds))
                .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
        spaceQueryParam = Optional.ofNullable(spaceQueryParam).orElseGet(SpaceQueryParam::new);
        List<DtpRwdSpaceDTO> spaces = spaceQueryParam.queryObject(relSpaceQueryParam);
        Map<String, List<DtpRwdSpaceDTO>> circuitIdAndServiceSpaces = DtpRwdSpaceDTO.mapRelObjIdAndData(spaces);
        Iterator<DtpRwdCircuitDTO> iterator = circuits.iterator();
        while (iterator.hasNext()) {
            DtpRwdCircuitDTO circuit = iterator.next();
            String circuitId = circuit.getId();
            if (!publicCircuitIds.contains(circuitId)) {
                continue;
            }
            List<DtpRwdSpaceDTO> serviceSpaces = circuitIdAndServiceSpaces.get(circuit.getId());
            if (CollectionUtils.isEmpty(serviceSpaces)) {
                iterator.remove();
                continue;
            }
            // 清空关联对象ID, 避免返回数据大
            serviceSpaces.forEach(item -> item.setRelObjIds(null));
            circuit.setServiceSpaces(serviceSpaces);
        }
    }

    @Override
    public List<DtpRwdCircuitDTO> listCircuits(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds) {
        Set<String> serviceAreaTypes = listCircuitServiceAreaTypes(templateType);
        if (CollectionUtils.isEmpty(serviceAreaTypes)) {
            return Lists.newArrayList();
        }
        // 回路需要过滤可见可控
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam()
                .addInfoCode(DtpRwdCircuitDTO.Fields.serviceAreaType, serviceAreaTypes);
        // 不等于夜景照明才过滤
        if (!templateType.isNightLighting()) {
            param.filterMonitoringSurveillanceStatus();
        }
        return queryCircuits(RWD_QUERY_CACHE, projectId, templateType, filterCircuitIds, param);
    }

    /**
     * 查询照明回路
     *
     * @param queryCache
     * @param projectId
     * @param templateType
     * @param filterCircuitIds
     * @param param
     * @return
     */
    private List<DtpRwdCircuitDTO> queryCircuits(boolean queryCache, String projectId,
                                                 DemandTemplateTypeEnum templateType,
                                                 Collection<String> filterCircuitIds,
                                                 DtpRwdObjectQueryParam param) {
        List<DtpRwdCircuitDTO> circuits = dtpRwdClientService.listCircuit(
                queryCache, false, projectId, param);
        circuits = filterCircuits(circuits, filterCircuitIds);
        if (CollectionUtils.isEmpty(circuits)) {
            return Lists.newArrayList();
        }
        Set<String> allCircuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        switch (templateType) {
            case PUBLIC_AREA_LIGHTING:
            case PARKING_LOT_LIGHTING:
                List<DtpRwdSpaceDTO> spaces = querySpaceByCircuit(projectId, allCircuitIds, null, null);
                if (CollectionUtils.isEmpty(spaces)) {
                    return Lists.newArrayList();
                }
                Set<String> validCircuitIds = spaces.stream()
                        .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                        .flatMap(item -> item.getRelObjIds().stream()).collect(Collectors.toSet());
                circuits.removeIf(item -> !validCircuitIds.contains(item.getId()));
                break;
            case BUILDING_LIGHTING:
            case LANDSCAPE_LIGHTING:
            case SIGN_LIGHTING:
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(templateType.getClass().getSimpleName(), templateType);
        }
        return circuits;
    }

    @Override
    public List<DtpRwdCircuitDTO> listCircuitsByInstance(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        if (templateType.isNightLighting()) {
            // 夜景照明回路不需要根据空间关系过滤
            return listCircuits(projectId, templateType, null);
        }
        Set<String> spaceIds = instanceType != DemandInstanceTypeEnum.SPACE
                ? instanceService.listSpaceIdsByInstance(instanceType, instanceId)
                : Sets.newHashSet(instanceId);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        // 根据空间获取所有的回路信息
        return listCircuitsBySpaceIds(projectId, templateType, spaceIds, null, null, null);
    }

    @Override
    public List<DtpRwdCircuitDTO> listCircuitsBySpaceIds(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Collection<String> filterCircuitIds,
            Boolean queryCache, Collection<String> withColumns) {
        if (queryCache == null) {
            queryCache = RWD_QUERY_CACHE;
        }
        // 照明模版类型对应的回路区域类型
        Set<String> serviceAreaTypes = listCircuitServiceAreaTypes(templateType);
        if (CollectionUtils.isEmpty(serviceAreaTypes)) {
            return Lists.newArrayList();
        }
        // 非夜景照明 更新服务空间过滤
        if (!templateType.isNightLighting() && CollUtil.isEmpty(filterSpaceIds)) {
            List<DtpRwdSpaceDTO> spaces = querySpaces(projectId, filterSpaceIds);
            if (CollectionUtils.isEmpty(spaces)) {
                return Lists.newArrayList();
            }
            filterSpaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam()
                .addInfoCode(DtpRwdCircuitDTO.Fields.serviceAreaType, serviceAreaTypes);
        if (withColumns != null) {
            param.addWithColumnWithDefault(withColumns);
            param.addWithColumn(DtpRwdCircuitDTO.Fields.funcType, DtpRwdCircuitDTO.Fields.lampFuncType,
                    DtpRwdCircuitDTO.Fields.serviceAreaType);
        }
        // 不等于夜景照明才过滤
        if (!templateType.isNightLighting()) {
            param.filterMonitoringSurveillanceStatus();
        }
        List<DtpRwdCircuitDTO> circuits = dtpRwdClientService.listCircuitBySpaceId(
                queryCache, false, projectId, filterSpaceIds, param
        );
        filterCircuits(circuits, filterCircuitIds);
        if (CollectionUtils.isEmpty(circuits)) {
            logProjectInstance().warn("空间下无有效的照明回路", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterCircuitIds", filterCircuitIds)
            ));
            return Lists.newArrayList();
        }
        return circuits;
    }

    @Override
    public List<DtpRwdCircuitDTO> listCircuitsByController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterCircuitIds) {
        if (CollectionUtils.isEmpty(filterControllerIds)) {
            List<DtpRwdObjectDTO> controllers = listControllers(projectId, templateType, filterControllerIds);
            if (CollectionUtils.isEmpty(controllers)) {
                return Lists.newArrayList();
            }
            filterControllerIds = controllers.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        }
        Set<String> circuitIds = listCircuits(projectId, templateType, filterCircuitIds).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Lists.newArrayList();
        }
        List<DtpRwdCircuitDTO> circuits = dtpRwdClientService.listCircuitByControllerId(
                RWD_QUERY_CACHE, false, projectId, filterControllerIds,
                new DtpRwdObjectQueryParam().setId(circuitIds));
        if (CollectionUtils.isEmpty(circuits)) {
            logProjectInstance().warn("无有效照明控制模块关联照明回路", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterControllerIds", filterControllerIds),
                    Pair.of("filterCircuitIds", filterCircuitIds)
            ));
            return Lists.newArrayList();
        }
        return circuits;
    }

    @Override
    public List<DtpRwdObjectDTO> listControllers(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds) {
        List<DtpRwdObjectDTO> controllers = listControllersByCircuit(
                projectId, templateType, null, filterControllerIds);
        if (CollectionUtils.isEmpty(controllers)) {
            logProjectInstance().warn("无有效照明控制模块", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterControllerIds", filterControllerIds)
            ));
            return Lists.newArrayList();
        }
        return controllers;
    }

    @Override
    public List<DtpRwdObjectDTO> listControllersByInstance(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId) {
        Set<String> spaceIds = instanceService.listSpaceIdsByInstance(instanceType, instanceId);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        return listControllersBySpaceIds(projectId, templateType, spaceIds, null);
    }

    @Override
    public List<DtpRwdObjectDTO> listControllersBySpaceIds(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Collection<String> filterControllerIds) {
        filterSpaceIds = CollectionUtils.isEmpty(filterSpaceIds)
                ? querySpaces(projectId, filterSpaceIds).stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet())
                : filterSpaceIds;
        if (CollectionUtils.isEmpty(filterSpaceIds)) {
            return Lists.newArrayList();
        }

        Set<String> controllerIds = listControllers(projectId, templateType, filterControllerIds).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(controllerIds)) {
            logProjectInstance().warn("模版下无有效的照明控制模块", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterSpaceIds", filterSpaceIds),
                    Pair.of("filterControllerIds", filterControllerIds)
            ));
            return Lists.newArrayList();
        }
        List<DtpRwdObjectDTO> controllers = dtpRwdClientService.listCircuitControllerInBySpaceIds(
                RWD_QUERY_CACHE, false, projectId, filterSpaceIds,
                new DtpRwdObjectQueryParam().setId(controllerIds));
        if (CollectionUtils.isNotEmpty(filterControllerIds)) {
            controllers.removeIf(item -> !filterControllerIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(controllers)) {
            logProjectInstance().warn("空间下无有效的照明控制模块", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterSpaceIds", filterSpaceIds),
                    Pair.of("filterControllerIds", filterControllerIds)
            ));
            return Lists.newArrayList();
        }
        return controllers;
    }

    @Override
    public List<DtpRwdObjectDTO> listControllersByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterControllerIds) {
        if (CollectionUtils.isEmpty(filterCircuitIds)) {
            List<DtpRwdCircuitDTO> circuits = listCircuits(projectId, templateType, filterCircuitIds);
            if (CollectionUtils.isEmpty(circuits)) {
                return Lists.newArrayList();
            }
            filterCircuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        }
        List<DtpRwdObjectDTO> controllers = dtpRwdClientService.listCircuitControllerByCircuitIds(
                RWD_QUERY_CACHE, false, projectId, filterCircuitIds, new DtpRwdObjectQueryParam());
        if (CollectionUtils.isNotEmpty(filterControllerIds)) {
            controllers.removeIf(item -> !filterControllerIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(controllers)) {
            logProjectInstance().warn("无有效照明回路关联照明控制模块", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("filterCircuitIds", filterCircuitIds),
                    Pair.of("filterControllerIds", filterControllerIds)
            ));
            return Lists.newArrayList();
        }
        return controllers;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpacesByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterSpaceIds) {
        if (CollUtil.isEmpty(filterCircuitIds)) {
            List<DtpRwdCircuitDTO> circuits = listCircuits(projectId, templateType, filterCircuitIds);
            if (CollectionUtils.isEmpty(circuits)) {
                return Lists.newArrayList();
            }
            filterCircuitIds = circuits.stream()
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toSet());
        }
        return querySpaceByCircuit(projectId, filterCircuitIds, filterSpaceIds, null);
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpacesByController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> controllerIds,
            @Nullable Collection<String> filterSpaceIds) {
        List<DtpRwdObjectDTO> controllers = listControllers(projectId, templateType, controllerIds);
        if (CollectionUtils.isEmpty(controllers)) {
            return Lists.newArrayList();
        }
        Set<String> filterControllerIds = controllers.stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceInByEquipmentId(
                RWD_QUERY_CACHE, false, projectId, filterControllerIds, new DtpRwdObjectQueryParam());
        if (CollectionUtils.isNotEmpty(filterSpaceIds)) {
            spaces.removeIf(item -> !filterSpaceIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(spaces)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "无有效照明控制模块所在空间", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("controllerIds", controllerIds),
                    Pair.of("filterSpaceIds", filterSpaceIds)
            ));
        }
        return spaces;
    }

    @Override
    public List<DtpRwdFloorDTO> listFloorsByCircuit(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> circuitIds,
            @Nullable Collection<String> floorIds) {
        if (!templateType.isNightLighting()) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_d243ec4145c04bacae327b14294064fb"));
        }
        List<DtpRwdCircuitDTO> circuits = listCircuits(projectId, templateType, circuitIds);
        circuits.removeIf(item -> {
            String lampFuncType = item.getLampFuncType();
            // 夜景照明中仅特殊灯具类型有和楼层的关系
            return StringUtils.isBlank(lampFuncType) || !SPECIAL_NIGHT_LIGHTING_LAMP_FUNC_TYPES.contains(lampFuncType);
        });
        if (CollectionUtils.isEmpty(circuits)) {
            return Lists.newArrayList();
        }
        return queryFloorByCircuit(projectId, floorIds, circuits.stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
    }

    @Override
    @Nullable
    public LightingSceneDTO getSceneByNow(@NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType) {
        List<LightingSceneDTO> lightingScenes = listScenesByTime(projectId, templateType, DateUtils.now().toLocalDate());
        Optional<LightingSceneDTO> result = lightingScenes.stream()
                .filter(item -> BooleanUtils.isTrue(item.getIsCurrentScene()))
                .findFirst();
        if (!result.isPresent()) {
            LogExecutor.project(projectId).warn(log(), logTag(), "当前时间无场景", Pair.of("templateType", templateType));
            return null;
        }
        return result.get();
    }

    @Override
    public List<LightingSceneDTO> listScenesByTime(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        Pair<DemandTemplateDateSettingVO, List<LightingSceneDTO>> dateSettingAndLightingScenes =
                queryDateSettingAndLightingScenes(projectId, templateType, date);
        if (Objects.isNull(dateSettingAndLightingScenes)) {
            return Lists.newArrayList();
        }
        return dateSettingAndLightingScenes.getRight();
    }

    @Override
    public List<LightingSceneDTO> listScenesByAllTime(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        Pair<DemandTemplateDateSettingVO, List<LightingSceneDTO>> dateSettingAndLightingScenes =
                queryDateSettingAndLightingScenes(projectId, templateType, date);
        LocalDateTime eachBeginDateTime = date.atStartOfDay();
        LocalDateTime eachEndDateTime = eachBeginDateTime.plusDays(1);
        DemandTemplateVO relTemplate = null;
        List<LightingSceneDTO> lightingScenes = null;
        if (Objects.nonNull(dateSettingAndLightingScenes)) {
            relTemplate = dateSettingAndLightingScenes.getLeft().getRelTemplate();
            lightingScenes = dateSettingAndLightingScenes.getRight();
        }
        if (Objects.isNull(relTemplate) || CollectionUtils.isEmpty(lightingScenes)) {
            // 当天模版或场景时, 添加一个覆盖全天的临时场景
            return Lists.newArrayList(LightingSceneDTO.instanceWithoutScene(
                    relTemplate, eachBeginDateTime, eachEndDateTime));
        }
        TemporalUnit sceneTimeTemporalUnit = LightingSceneDTO.SCENE_TIME_TYPE.convertTemporalUnit();
        int sceneTimeAmount = LightingSceneDTO.SCENE_TIME_AMOUNT;
        LinkedList<LightingSceneDTO> result = new LinkedList<>();
        LocalDateTime tempBeginDateTime = null;
        do {
            if (Objects.isNull(tempBeginDateTime)) {
                tempBeginDateTime = eachBeginDateTime;
            }

            Iterator<LightingSceneDTO> sceneIterator = lightingScenes.iterator();
            while (sceneIterator.hasNext()) {
                LightingSceneDTO lightingScene = sceneIterator.next();
                LocalDateTime sceneBeginDateTime = lightingScene.getBeginDateTime();
                LocalDateTime sceneEndDateTime = lightingScene.getEndDateTime();
                Range<ChronoLocalDateTime<?>> timeRange = Range.between(sceneBeginDateTime,
                        sceneEndDateTime.minus(sceneTimeAmount, sceneTimeTemporalUnit));
                if (timeRange.contains(eachBeginDateTime)) {
                    // 当结果集中无场景或最后一个场景结束时间和当前场景开始时间不相等时, 将空场景添加至结果集中
                    if (CollectionUtils.isNotEmpty(result)) {
                        LocalDateTime lastEndDateTime = result.getLast().getEndDateTime();
                        if (!lastEndDateTime.isEqual(sceneBeginDateTime)) {
                            result.add(LightingSceneDTO.instanceWithoutScene(relTemplate,
                                    tempBeginDateTime, sceneBeginDateTime));
                        }
                    }
                    // 当场景时段覆盖遍历时间时, 将当前场景添加至结果集中
                    result.add(lightingScene);
                    // 因已经将当前场景添加至结果集中, 所以将遍历时间设置为当前场景的结束时间, 即跳过了所有当前场景覆盖的时间
                    eachBeginDateTime = sceneEndDateTime;
                    tempBeginDateTime = eachBeginDateTime;
                    // 避免后续重复添加, 将当前场景从列表中移除
                    sceneIterator.remove();
                    break;
                }
            }
            eachBeginDateTime = eachBeginDateTime.plus(sceneTimeAmount, sceneTimeTemporalUnit);
        } while (eachBeginDateTime.isBefore(eachEndDateTime));
        if (CollectionUtils.isEmpty(result)) {
            // 当结果集中不存在场景时, 添加覆盖全天的临时场景
            result.add(LightingSceneDTO.instanceWithoutScene(relTemplate,
                    date.atStartOfDay(), eachEndDateTime));
        } else {
            LightingSceneDTO firstScene = result.getFirst();
            LightingSceneDTO lastScene = result.getLast();
            LocalDateTime dayBeginTime = date.atStartOfDay();
            if (!firstScene.getBeginDateTime().isEqual(dayBeginTime)) {
                // 当第一个场景的开始时间不是当日开始时间时, 补充临时场景至第一个时段
                result.addFirst(LightingSceneDTO.instanceWithoutScene(relTemplate,
                        dayBeginTime, firstScene.getBeginDateTime()));
            }
            if (!lastScene.getEndDateTime().isEqual(eachEndDateTime)) {
                // 当最后一个场景的结束时间不是当日结束时间时, 补充临时场景至最后一个时段
                result.addLast(LightingSceneDTO.instanceWithoutScene(relTemplate,
                        lastScene.getEndDateTime(), eachEndDateTime));
            }
        }
        return result;
    }

    @Override
    public Map<String, String> mapCircuitInfoCodeAndName(@NonNull String infoCodeName) {
        return mapCircuitInfoCodeAndDefined().get(infoCodeName);
    }

    @Override
    public Map<String, List<String>> mapCircuitIdAndSpaceFuncTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType) {
        return mapCircuitIdAndSpaceFuncTypes(projectId, templateType, null);
    }

    @Override
    public Map<String, DtpRwdCircuitDTO> mapCircuitIdAndData(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds) {
        return listCircuits(projectId, templateType, filterCircuitIds).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Map<String, DtpRwdSpaceDTO> mapCircuitIdAndSpace(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterSpaceIds) {
        List<DtpRwdSpaceDTO> spaces = listSpacesByCircuit(projectId, templateType, filterCircuitIds, filterSpaceIds);
        if (CollectionUtils.isEmpty(spaces)) {
            return Maps.newHashMap();
        }
        Map<String, DtpRwdSpaceDTO> result = new HashMap<>(DEFAULT_CIRCUIT_MAP_SIZE);
        for (DtpRwdSpaceDTO space : spaces) {
            Set<String> relCircuitIds = space.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relCircuitIds)) {
                relCircuitIds.forEach(relCircuitId -> result.put(relCircuitId, space));
            }
        }
        return result;
    }

    @Override
    public Map<String, List<String>> mapCircuitIdAndSpaceFuncTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds) {
        List<DtpRwdSpaceDTO> spaces = listSpacesByCircuit(projectId, templateType, filterCircuitIds, null);
        if (CollectionUtils.isEmpty(spaces)) {
            return Maps.newHashMap();
        }
        Map<String, List<String>> circuitIdAndSpaceFuncTypes = new HashMap<>(512);
        for (DtpRwdSpaceDTO space : spaces) {
            String funcType = space.getRoomFuncType();
            filterCircuitIds = space.getRelObjIds();
            if (StringUtils.isBlank(funcType) || CollectionUtils.isEmpty(filterCircuitIds)) {
                continue;
            }
            filterCircuitIds.forEach(item -> MapExecutor.dynamicPut(
                    circuitIdAndSpaceFuncTypes, item, funcType, ArrayList::new));
        }
        return circuitIdAndSpaceFuncTypes;
    }

    @Override
    public Map<String, DtpRwdObjectDTO> mapCircuitIdAndController(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterCircuitIds,
            @Nullable Collection<String> filterControllerIds) {
        List<DtpRwdObjectDTO> controllers = listControllersByCircuit(projectId,
                templateType, filterCircuitIds, filterControllerIds);
        if (CollectionUtils.isEmpty(controllers)) {
            return Maps.newHashMap();
        }
        Map<String, DtpRwdObjectDTO> result = new HashMap<>(DEFAULT_CIRCUIT_MAP_SIZE);
        for (DtpRwdObjectDTO controller : controllers) {
            Set<String> relCircuitIds = controller.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relCircuitIds)) {
                relCircuitIds.forEach(relCircuitId -> result.put(relCircuitId, controller));
            }
        }
        return result;
    }

    @Override
    public Map<String, DtpRwdObjectDTO> mapCircuitIdAndDistributionCabinet(
            @NonNull String projectId, @Nullable Collection<String> filterCircuitIds) {
        return mapEquipmentIdAndDistributionCabinet(projectId, filterCircuitIds);
    }

    @Override
    public Map<String, DtpRwdObjectDTO> mapControllerIdAndData(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds) {
        return listControllers(projectId, templateType, filterControllerIds).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Map<String, DtpRwdSpaceDTO> mapControllerIdAndSpace(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterSpaceIds) {
        List<DtpRwdSpaceDTO> spaces = listSpacesByController(projectId,
                templateType, filterControllerIds, filterSpaceIds);
        if (CollectionUtils.isEmpty(spaces)) {
            return Maps.newHashMap();
        }
        Map<String, DtpRwdSpaceDTO> result = new HashMap<>(DEFAULT_CONTROLLER_MAP_SIZE);
        for (DtpRwdSpaceDTO space : spaces) {
            Set<String> relControllerIds = space.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relControllerIds)) {
                relControllerIds.forEach(relControllerId -> result.put(relControllerId, space));
            }
        }
        return result;
    }

    @Override
    public Map<String, Set<DtpRwdCircuitDTO>> mapControllerIdAndCircuits(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<String> filterControllerIds,
            @Nullable Collection<String> filterCircuitIds) {
        List<DtpRwdCircuitDTO> circuits = listCircuitsByController(projectId,
                templateType, filterControllerIds, filterCircuitIds);
        if (CollectionUtils.isEmpty(circuits)) {
            return Maps.newHashMap();
        }
        Map<String, Set<DtpRwdCircuitDTO>> result = new HashMap<>(DEFAULT_CONTROLLER_MAP_SIZE);
        for (DtpRwdCircuitDTO circuit : circuits) {
            Set<String> relControllerIds = circuit.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relControllerIds)) {
                relControllerIds.forEach(relControllerId -> MapExecutor.dynamicPut(result,
                        relControllerId, circuit, LinkedHashSet::new));
            }
        }
        return result;
    }

    @Override
    public Map<String, DtpRwdObjectDTO> mapControllerIdAndDistributionCabinet(
            @NonNull String projectId, @Nullable Collection<String> filterControllerIds) {
        return mapEquipmentIdAndDistributionCabinet(projectId, filterControllerIds);
    }

    @Override
    public List<DtpRwdTreeLightingObjectDTO> queryBuildingFloorSpaceObject(@NonNull String projectId, @NonNull DemandTemplateTypeEnum type) {

        List<DtpRwdTreeLightingObjectDTO> list = new ArrayList<>();

        // 查询项目下的 建筑信息
        List<DtpRwdBuildingDTO> buildingList = dtpRwdClientService.listBuilding(RWD_QUERY_CACHE, false, projectId, null);

        // 查询项目下的 楼层信息
        List<DtpRwdFloorDTO> floorList = dtpRwdClientService.listFloor(RWD_QUERY_CACHE, false, projectId, null);

        // 查询项目下的 空间信息 空间有楼层和建筑信息
        List<DtpRwdSpaceDTO> spaceList = listSpacesByCircuit(projectId, type, null, null);

        if (CollectionUtils.isEmpty(buildingList) || CollectionUtils.isEmpty(floorList) || CollectionUtils.isEmpty(spaceList)) {
            return list;
        }

        // 构建灯具类型和功能类型 和模型扩展信息
        Map<String, DtpRwdFloorDTO> floorMap = floorList.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity()));
        List<DtpRwdTreeLightingObjectDTO> spaceVoList = buildLampFuncTypeAndFuncType(projectId, type, spaceList, floorMap);

        // 建筑楼层关系
        Map<String, Set<String>> buildingMap = new HashMap<>(16);
        Map<String, List<DtpRwdFloorDTO>> buildingObjMap = new HashMap<>(16);
        // 楼层房间关系
        Map<String, List<DtpRwdObjectDTO>> floorObjMap = new HashMap<>(16);
        // 关系整理
        spaceVoList.forEach(s -> {
            if (floorObjMap.containsKey(s.getFloorId())) {
                floorObjMap.get(s.getFloorId()).add(s);
            } else {
                List<DtpRwdObjectDTO> l = new ArrayList<>();
                l.add(s);
                floorObjMap.put(s.getFloorId(), l);
            }

            if (buildingMap.containsKey(s.getBuildingId())) {
                buildingMap.get(s.getBuildingId()).add(s.getFloorId());
            } else {
                Set<String> l = new HashSet<>();
                l.add(s.getFloorId());
                buildingMap.put(s.getBuildingId(), l);
            }
        });
        // 建筑楼层关系转换为对象关系
        Map<String, DtpRwdFloorDTO> map = floorList.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity(), (o1, o2) -> o2));
        buildingMap.forEach((k, v) -> {
            List<DtpRwdFloorDTO> bl = new ArrayList<>();
            v.forEach(b -> bl.add(map.get(b)));
            buildingObjMap.put(k, bl);
        });
        // 遍历出父子结构
        return buildTreeObject(buildingList, buildingObjMap, floorObjMap);
    }

    @Override
    public List<DemandTemplateTypeEnum> listTemplateTypes() {
        List<DemandTemplateTypeEnum> templateTypes = demandType().toDemandTemplateTypes();
        List<DtpRwdCircuitDTO> circuits = listCircuits(templateTypes, null, null);
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }
        Set<String> existsServiceAreaTypes = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            String serviceAreaType = circuit.getServiceAreaType();
            if (StringUtils.isBlank(serviceAreaType)) {
                continue;
            }
            existsServiceAreaTypes.add(serviceAreaType);
        }
        if (CollectionUtils.isEmpty(existsServiceAreaTypes)) {
            return Collections.emptyList();
        }
        List<DemandTemplateTypeEnum> result = new ArrayList<>();
        for (DemandTemplateTypeEnum templateType : demandType().toDemandTemplateTypes()) {
            Set<String> serviceAreaTypes = listCircuitServiceAreaTypes(templateType);
            if (CollectionUtils.isEmpty(serviceAreaTypes)) {
                continue;
            }
            if (serviceAreaTypes.stream().anyMatch(existsServiceAreaTypes::contains)) {
                result.add(templateType);
            }
        }
        return result;
    }

    @Override
    public List<DtpRwdSpaceDTO> listDemandSpace(@NonNull DemandSpacesQueryParam param) {
        RunningMonitorLightingStrategy lightingStrategy = SpringUtil.getBean(RunningMonitorLightingStrategy.class);
        return lightingStrategy.listSpaces(param.toRunningMonitorSpaceQueryParam());
    }

    @EventListener(DemandTemplateChangeEvent.class)
    public void onDemandTemplateChangeEvent(DemandTemplateChangeEvent event) {
        DemandTemplateChangeEvent.Message message = (DemandTemplateChangeEvent.Message) event.getSource();
        DemandTemplateChangeTypeEnum changeType = message.getDataType();
        String projectId = ContextUtil.getProjectId();
        DemandTemplate oldTemplate = message.getOldTemplate();

        Long oldTemplateId;
        switch (changeType) {
            case REMOVE:
                oldTemplateId = oldTemplate.getId();
                demandConfigLightingService.lambdaUpdate().eq(DemandConfigLighting::getRelDemandTemplateId, oldTemplateId).remove();
                demandConfigLightingSceneService.lambdaUpdate().eq(DemandConfigLightingScene::getRelDemandTemplateId, oldTemplateId).remove();
                demandConfigLightingResultService.deleteByProjectIdAndTemplateIdAndSceneId(projectId, oldTemplateId, null);
                break;
            case INSERT:
            case APPLIED:
            case UPDATE:
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(changeType.getClass().getSimpleName(), changeType);
        }
    }

    @Override
    public DemandTypeEnum demandType() {
        return DemandTypeEnum.LIGHTING;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }

    /**
     * 查询日期标签和照明场景
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param date         日期
     * @return 日期标签和照明场景
     * @since 5.240402.0
     * @deprecated see com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGatewayImpl#queryDateSettingAndValidScenes(DemandTemplateTypeEnum, LocalDate)
     */
    @Nullable
    @Deprecated
    private Pair<DemandTemplateDateSettingVO, List<LightingSceneDTO>> queryDateSettingAndLightingScenes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date) {
        DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(templateType, date);
        if (Objects.isNull(dateSetting)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "当前日期未配置模版", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("date", date))
            );
            return null;
        }
        Long templateId = dateSetting.getRelTemplate().getTemplateId();
        List<DemandSceneEntity> lightingScenes = demandSceneGateway.listScenesByTemplateId(Sets.newHashSet(templateId));
        lightingScenes.removeIf(DemandSceneEntity::isInvalid);
        if (CollectionUtils.isEmpty(lightingScenes)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "当前日期模版类型下无场景配置", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("date", date))
            );
            return Pair.of(dateSetting, Lists.newArrayList());
        }
        WeatherSunRiseAndSetDTO sunRiseAndSet = weatherClientService.getSunRiseAndSetCache(projectId, date);
        if (Objects.isNull(sunRiseAndSet)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "当前日期无日出日落时间", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("date", date))
            );
            return Pair.of(dateSetting, Lists.newArrayList());
        }
        return Pair.of(dateSetting, toLightingScenes(projectId, lightingScenes, sunRiseAndSet));
    }

    /**
     * 转换照明场景DTO
     *
     * @param projectId      项目ID
     * @param lightingScenes 照明场景配置
     * @param sunRiseAndSet  日出日落时间
     * @return 照明场景DTO
     * @since 5.240402.0
     */
    private List<LightingSceneDTO> toLightingScenes(
            @NonNull String projectId,
            @NonNull List<DemandSceneEntity> lightingScenes,
            @NonNull WeatherSunRiseAndSetDTO sunRiseAndSet) {
        if (CollectionUtils.isEmpty(lightingScenes)) {
            return Lists.newArrayList();
        }
        LocalDate date = sunRiseAndSet.getDate();
        LocalDateTime dateBeginTime = sunRiseAndSet.getDate().atStartOfDay();
        List<LightingSceneDTO> scenes = new ArrayList<>();
        for (DemandSceneEntity lightingScene : lightingScenes) {
            LocalDateTime beginSceneTime = LocalDateTime.of(date,
                    lightingScene.getBeginTime().analyzeAbsoluteTime(sunRiseAndSet));
            LocalDateTime endSceneTime = LocalDateTime.of(date,
                    lightingScene.getEndTime().analyzeAbsoluteTime(sunRiseAndSet));
            // 当结束时间是当天起始时间时, 认为结束时间是到当天结束时间
            if (endSceneTime.isEqual(dateBeginTime)) {
                endSceneTime = dateBeginTime.plusDays(1);
            }
            // 结束时间小于或等于开始时间时视为无效场景
            if (!endSceneTime.isAfter(beginSceneTime)) {
                LogExecutor.project(projectId).warn(log(), logTag(), "无效的场景时间", Arrays.asList(
                        Pair.of("sunRiseAndSet", sunRiseAndSet),
                        Pair.of("lightingScene", lightingScene)
                ));
                continue;
            }
            LightingSceneDTO scene = demandLightingConverter.toLightingSceneDto(
                    lightingScene, beginSceneTime, endSceneTime);
            scenes.add(scene);
        }
        if (CollectionUtils.isEmpty(scenes)) {
            return Lists.newArrayList();
        }
        scenes.sort(Comparator.comparing(LightingSceneDTO::getBeginDateTime));
        LinkedList<LightingSceneDTO> result = new LinkedList<>();
        result.add(scenes.get(0));
        for (int i = 1; i < scenes.size(); i++) {
            handleSceneIntersectionTime(projectId, result, scenes.get(i));
        }
        return result;
    }

    /**
     * 处理场景重叠时间
     *
     * @param projectId 项目ID
     * @param source    数据源
     * @param target    目标源
     * @since 5.240402.0
     */
    private void handleSceneIntersectionTime(
            @NonNull String projectId,
            @NonNull LinkedList<LightingSceneDTO> source,
            @NonNull LightingSceneDTO target) {
        if (CollectionUtils.isEmpty(source)) {
            source.add(target);
            return;
        }
        LightingSceneDTO previous = source.getLast();
        Range<ChronoLocalDateTime<?>> previousTimeRange = Range.between(
                previous.getBeginDateTime(), previous.getEndDateTime().minusMinutes(1));
        Range<ChronoLocalDateTime<?>> currentTimeRange = Range.between(
                target.getBeginDateTime(), target.getEndDateTime().minusMinutes(1));
        // 上一个时间段包含当前时间段时, 直接使用上一个时间段
        if (previousTimeRange.containsRange(currentTimeRange)) {
            return;
        }
        // 当前时间段包含上一个时间段, 直接使用当前时间段
        if (currentTimeRange.containsRange(previousTimeRange)) {
            source.removeLast();
            source.add(target);
            return;
        }
        // 时间范围交叉时
        if (currentTimeRange.isOverlappedBy(previousTimeRange)) {
            // 当前时间段开始时间为上一个时段的结束时间
            LocalDateTime newBeginDateTime = LocalDateTime.from(previous.getEndDateTime());
            // 结束时间小于或等于开始时间时视为无效场景
            if (!target.getEndDateTime().isAfter(newBeginDateTime)) {
                LogExecutor.project(projectId).warn(log(), logTag(), "无效的场景开始时间", Arrays.asList(
                        Pair.of("current", target),
                        Pair.of("previous", previous)
                ));
                return;
            }
            // 将当前时间段开始时间为上一个时段的结束时间
            target.setBeginDateTime(newBeginDateTime);
            // 保留上一个时段并添加当前时间段
            source.add(target);
            return;
        }
        source.add(target);
    }

    /**
     * 根据分钟偏移量解析时间
     *
     * @param target        目标源
     * @param offsetMinutes 分钟偏移量
     * @return 计算后的目标源
     * @since 5.240402.0
     */
    private LocalDateTime computeTimeByOffsetMinutes(@NonNull LocalDateTime target, @Nullable Integer offsetMinutes) {
        if (Objects.isNull(offsetMinutes)) {
            return target;
        }
        LocalDate date = target.toLocalDate();
        int minuteOfDay = target.get(ChronoField.MINUTE_OF_DAY);
        minuteOfDay = minuteOfDay + offsetMinutes;
        int minMinuteOfDay = LocalTime.MIN.get(ChronoField.MINUTE_OF_DAY);
        int maxMinuteOfDay = LocalTime.MAX.get(ChronoField.MINUTE_OF_DAY);
        if (minuteOfDay < minMinuteOfDay) {
            // 当计算后的分钟小于每日最小分钟时, 将分钟重置为每日最小分钟
            minuteOfDay = minMinuteOfDay;
        } else if (minuteOfDay > maxMinuteOfDay) {
            // 当计算后的分钟大于每日最大分钟时, 认为是到当天结束时间, 即第二天
            date = date.plusDays(1);
            minuteOfDay = minMinuteOfDay;
        }
        return LocalDateTime.of(date, LocalTime.ofSecondOfDay(minuteOfDay * 60));
    }

    /**
     * 查询照明回路信息点和定义
     *
     * @return 照明回路信息点 -> [信息点编码 -> 信息点名称]
     * @since 5.240501.0
     */
    private Map<String, Map<String, String>> mapCircuitInfoCodeAndDefined() {
        Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> infoCodeAndDataSources =
                dtpKnowledgeClientService.mapInfoCodeAndDataSources(RWD_QUERY_CACHE, true,
                        DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        if (MapUtils.isEmpty(infoCodeAndDataSources)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, String>> result = new HashMap<>(Math.max(infoCodeAndDataSources.size(), 16));
        infoCodeAndDataSources.forEach((infoCode, dataSources) -> {
            for (DtpKnowledgeInfoCodeDataSourceDTO dataSource : dataSources) {
                MapExecutor.dynamicPut(result, infoCode, dataSource.getCode(), dataSource.getName(), LinkedHashMap::new);
            }
        });
        return result;
    }

    /**
     * 查询设备ID和低压末端配电柜映射
     *
     * @param projectId    项目ID
     * @param equipmentIds 设备ID, 为 {@code null} 时查询所有
     * @return 设备ID -> 关联照明回路
     * @since 5.240402.0
     */
    private Map<String, DtpRwdObjectDTO> mapEquipmentIdAndDistributionCabinet(
            @NonNull String projectId, @Nullable Collection<String> equipmentIds) {
        List<DtpRwdObjectDTO> distributionCabinets = dtpRwdClientService.listDistributionCabinetByEquipmentIds(
                RWD_QUERY_CACHE, false, projectId, equipmentIds, new DtpRwdObjectQueryParam());

        Map<String, DtpRwdObjectDTO> equipmentIdAndDistributionCabinet = new HashMap<>(DEFAULT_CIRCUIT_MAP_SIZE);
        for (DtpRwdObjectDTO distributionCabinet : distributionCabinets) {
            Set<String> relEquipmentIds = distributionCabinet.getRelObjIds();
            if (CollectionUtils.isEmpty(relEquipmentIds)) {
                continue;
            }
            for (String relCircuitId : relEquipmentIds) {
                equipmentIdAndDistributionCabinet.put(relCircuitId, distributionCabinet);
            }
        }
        return equipmentIdAndDistributionCabinet;
    }

    /**
     * 过滤照明回路
     *
     * @param target           目标源
     * @param filterCircuitIds 过滤照明回路ID, 为空时不进行过滤
     * @return 过滤后的照明回路
     * @since 5.240402.0
     */
    private List<DtpRwdCircuitDTO> filterCircuits(
            @NonNull List<DtpRwdCircuitDTO> target,
            @Nullable Collection<String> filterCircuitIds) {
        target.removeIf(item -> {
            String funcType = item.getFuncType();
            String serviceAreaType = item.getServiceAreaType();
            if (StringUtils.isBlank(funcType)) {
                return true;
            }
            if (StringUtils.isBlank(serviceAreaType)) {
                return true;
            }
            return CollectionUtils.isNotEmpty(filterCircuitIds) && !filterCircuitIds.contains(item.getId());
        });

        // 暂时不过滤控制模块的逻辑  夜景照明不过滤
        /*if (!templateType.isNightLighting()) {
            Set<String> ids = target.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
            //回路关联的控制模块
            List<DtpRwdObjectDTO> controllers = dtpRwdClientService.listCircuitControllerByCircuitIds(
                    false, false, ContextUtil.getProjectId(), ids,
                    new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id,
                            DtpRwdObjectDTO.Fields.relObjIds));
            //只取有控制模块的回路
            Map<String, Set<String>> mapRelObjIdAndIds = DtpRwdObjectDTO.mapRelObjIdAndIds(controllers);
            target.removeIf(item -> !mapRelObjIdAndIds.containsKey(item.getId()));
            
        }*/

        if (CollectionUtils.isEmpty(target)) {
            logProjectInstance().warn("无有效照明回路",
                    Collections.singletonList(Pair.of("circuitIds", filterCircuitIds)));
            return Lists.newArrayList();
        }
        return target;
    }

    /**
     * 根据照明回路查询楼层列表
     *
     * @param projectId  项目ID
     * @param floorIds   楼层ID, 为 {@code null} 时查询所有
     * @param circuitIds 照明回路ID, 为 {@code null} 时查询所有
     * @return 楼层列表
     * @since 24Q1 T3S2-5.18.0
     */
    private List<DtpRwdFloorDTO> queryFloorByCircuit(
            @NonNull String projectId,
            @Nullable Collection<String> floorIds,
            @NonNull Collection<String> circuitIds) {
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloorServiceByEquipmentId(
                RWD_QUERY_CACHE, false, projectId, circuitIds, new DtpRwdObjectQueryParam());
        if (CollectionUtils.isNotEmpty(floorIds)) {
            floors.removeIf(item -> !floorIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(floors)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "无有效照明回路服务楼层", Arrays.asList(
                    Pair.of("floorIds", floorIds),
                    Pair.of("circuitIds", circuitIds)
            ));
            return Lists.newArrayList();
        }
        return floors;
    }

    /**
     * 根据照明回路查询空间列表
     *
     * @param projectId      项目ID
     * @param circuitIds     照明回路ID, 为 {@code null} 时查询所有
     * @param filterSpaceIds 照明回路ID, 为空时不进行过滤
     * @return 空间列表
     * @since 24Q1 T3S2-5.18.0
     */
    @Override
    public List<DtpRwdSpaceDTO> querySpaceByCircuit(
            @NonNull String projectId,
            @NonNull Collection<String> circuitIds,
            @Nullable Collection<String> filterSpaceIds,
            Collection<String> withColumns) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().addWithColumn(DtpRwdSpaceDTO.Fields.class);
        if (CollectionUtils.isNotEmpty(withColumns)) {
            queryParam.setWithColumns(withColumns);
            queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.roomFuncType);
        }
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceServiceByEquipmentId(false, circuitIds, queryParam);

        spaces.removeIf(item -> CollectionUtils.isNotEmpty(filterSpaceIds) && !filterSpaceIds.contains(item.getId()));
        if (CollectionUtils.isEmpty(spaces)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "无有效照明回路服务空间", Arrays.asList(
                    Pair.of("circuitIds", circuitIds),
                    Pair.of("filterSpaceIds", filterSpaceIds)
            ));
            return Lists.newArrayList();
        }
        return spaces;
    }

    @Override
    public Map<String, List<DemandLightingFuncTypeSettingDTO>> spaceSettingsBuilding(String projectId,
                                                                                     DemandTemplateTypeEnum demandTemplateType,
                                                                                     Map<String, DemandConfigLightingDataDTO> spaceId2originalConfig) {

        if (CollUtil.isEmpty(spaceId2originalConfig)) {
            return Collections.emptyMap();
        }

        // 2. 查询项目中所有回路
        List<DtpRwdCircuitDTO> dtpRwdCircuitList = this.listCircuits(projectId, demandTemplateType, null);
        if (CollectionUtils.isEmpty(dtpRwdCircuitList)) {
            return Collections.emptyMap();
        }

        // 定义结果集
        Map<String, List<DemandLightingFuncTypeSettingDTO>> spaceId2finalConfig = new HashMap<>(16);

        // 回路对应的回路功能类型
        Map<String, String> circuitId2funcType = dtpRwdCircuitList.stream().collect(Collectors.toMap(DtpRwdCircuitDTO::getId, DtpRwdCircuitDTO::getFuncType));

        // 3、查询回路服务空间
        Set<String> circuitIds = dtpRwdCircuitList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> dtpRwdSpaceList = this.querySpaceByCircuit(projectId, circuitIds, null, null);

        // 组装
        for (DtpRwdSpaceDTO space : dtpRwdSpaceList) {
            Set<String> relCircuitIds = space.getRelObjIds();
            if (!spaceId2originalConfig.containsKey(space.getId()) || CollectionUtils.isEmpty(relCircuitIds)) {
                continue;
            }

            DemandConfigLightingDataDTO demandConfigLighting = spaceId2originalConfig.get(space.getId());
            spaceId2finalConfig.put(space.getId(), configBuild(relCircuitIds, processSpaceSettings(demandConfigLighting, space), circuitId2funcType));
        }

        return spaceId2finalConfig;
    }


    /**
     * 根据给定的空间配置数据和空间信息，处理并返回相应的功能类型设置。
     * 如果空间设置为空，则返回全局设置；否则，根据房间功能类型匹配空间功能类型设置。
     *
     * @param demandConfigLighting 光照需求配置数据
     * @param space                DTP-RWD空间数据对象，包含房间功能类型信息。
     * @return 功能类型设置列表，可能为空。
     */
    private List<DemandLightingFuncTypeSettingDTO> processSpaceSettings(DemandConfigLightingDataDTO demandConfigLighting, DtpRwdSpaceDTO space) {
        if (null == demandConfigLighting) {
            return Collections.emptyList();
        }
        // 获取空间设置列表
        List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings = demandConfigLighting.getSpaceSettings();

        // 根据空间类型查匹配配置
        if (CollectionUtils.isNotEmpty(spaceSettings)) {
            // 遍历空间设置，寻找与当前空间功能类型匹配的设置
            for (DemandLightingSpaceFuncTypeSettingDTO spaceSetting : spaceSettings) {
                // 检查空间功能类型设置是否存在
                if (spaceSetting.getSpaceFuncTypes() == null) {
                    continue;
                }
                for (BaseEnumVO spaceFuncType : spaceSetting.getSpaceFuncTypes()) {
                    // 如果找到匹配的功能类型，则返回对应的函数类型设置
                    if (spaceFuncType.getKey().equals(space.getRoomFuncType())) {
                        return spaceSetting.getFuncTypeSettings();
                    }
                }
            }
        }
        // 如果没有找到匹配的空间功能类型设置，返回全局设置或空列表
        return demandConfigLighting.getGlobalSettings() != null ? demandConfigLighting.getGlobalSettings() : Collections.emptyList();
    }


    /**
     * 配置构建方法，用于根据给定的相关电路ID集合、需求照明功能类型设置列表和电路ID到功能类型的映射，
     * 来筛选和构建需求照明功能类型设置。
     *
     * @param relCircuitIds                     相关电路ID集合，用于筛选电路设置。
     * @param demandLightingFuncTypeSettingList 需求照明功能类型设置列表，需要根据relCircuitIds进行筛选和更新。
     * @param circuitId2funcType                电路ID到功能类型的映射，用于查找未在列表中配置的电路对应的功能类型设置。
     */
    private List<DemandLightingFuncTypeSettingDTO> configBuild(Set<String> relCircuitIds
            , List<DemandLightingFuncTypeSettingDTO> demandLightingFuncTypeSettingList
            , Map<String, String> circuitId2funcType) {
        // 用于存储已保存的电路ID
        HashSet<String> savedCircuitId = new HashSet<>();
        // 用于功能类型到设置的映射，以便快速查找特定功能类型的设置
        HashMap<String, DemandLightingFuncTypeSettingDTO> funcType2setting = new HashMap<>();

        // 确保DemandLightingFuncTypeSettingList不为空
        demandLightingFuncTypeSettingList = null == demandLightingFuncTypeSettingList ? new ArrayList<>() : demandLightingFuncTypeSettingList;

        List<DemandLightingFuncTypeSettingDTO> list = BeanUtil.copyToList(demandLightingFuncTypeSettingList, DemandLightingFuncTypeSettingDTO.class);
        // 遍历需求照明功能类型设置列表，进行筛选和更新
        for (int i = 0; i < list.size(); i++) {
            DemandLightingFuncTypeSettingDTO funcTypeSetting = list.get(i);
            List<DemandLightingCircuitSettingDTO> circuitSettings = funcTypeSetting.getCircuitSettings();
            // 反馈调节等功能类型可能无回路明细, 按空处理走下方移除逻辑
            if (null == circuitSettings) {
                circuitSettings = new ArrayList<>();
            }
            // 遍历当前功能类型的电路设置，进行筛选
            for (int i1 = 0; i1 < circuitSettings.size(); i1++) {
                if (!relCircuitIds.contains(circuitSettings.get(i1).getCircuitId())) {
                    // 如果当前电路ID不在相关电路ID集合中，移除该电路设置
                    circuitSettings.remove(i1);
                    i1--;
                } else {
                    // 如果当前电路ID在相关电路ID集合中，将其保存
                    savedCircuitId.add(circuitSettings.get(i1).getCircuitId());
                }
            }

            // 如果当前功能类型的电路设置为空，则从列表中移除该功能类型设置
            if (circuitSettings.isEmpty()) {
                list.remove(i);
                i--;
            } else {
                // 如果当前功能类型的电路设置不为空，则将其映射到funcType2setting中
                funcType2setting.put(funcTypeSetting.getFuncType().getValue(), funcTypeSetting);
            }
        }

        // 找出在相关电路ID集合中，但未在已保存的电路ID集合中的电路ID
        Collection<String> subtract = CollUtil.subtract(relCircuitIds, savedCircuitId);

        // 遍历这些未配置的电路ID，为它们添加相应的功能类型设置
        for (String relCircuitId : subtract) {
            String funcType = circuitId2funcType.get(relCircuitId);
            DemandLightingFuncTypeSettingDTO funcTypeSetting = funcType2setting.get(funcType);

            // 如果当前功能类型设置为空，则创建一个新的设置，并添加到列表中
            if (null == funcTypeSetting) {
                funcTypeSetting = new DemandLightingFuncTypeSettingDTO();
                funcTypeSetting.setFuncType(DtpRwdCircuitFuncTypeEnum.analyze(funcType));
                funcTypeSetting.setSettingMode(DemandLightingTypeSettingModeEnum.NOT_CONFIGURED);
                funcTypeSetting.setCircuitSettings(new ArrayList<>());
                list.add(funcTypeSetting);
            }

            // 为当前电路ID添加相应的电路设置
            funcTypeSetting.getCircuitSettings().add(new DemandLightingCircuitSettingDTO(relCircuitId, null, DemandLightingCircuitSettingModeEnum.getSettingMode(funcTypeSetting.getSettingMode(), null), null));
        }

        return list;
    }


    /**
     * 查询空间
     *
     * @param projectId      项目ID
     * @param filterSpaceIds 过滤空间ID, 为空时查询所有
     * @return 空间
     * @since 5.240402.0
     */
    private List<DtpRwdSpaceDTO> querySpaces(@NonNull String projectId, @Nullable Collection<String> filterSpaceIds) {
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(
                RWD_QUERY_CACHE, false, projectId, new DtpRwdObjectQueryParam());
        if (CollectionUtils.isNotEmpty(filterSpaceIds)) {
            spaces.removeIf(item -> !filterSpaceIds.contains(item.getId()));
        }
        if (CollectionUtils.isEmpty(spaces)) {
            LogExecutor.project(projectId).warn(log(), logTag(), "无有效空间");
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 构建灯具类型和功能类型
     *
     * @param projectId 项目ID
     * @param type      模版类型
     * @param spaceList 空间列表
     * @param floorMap  楼层数据
     */
    private List<DtpRwdTreeLightingObjectDTO> buildLampFuncTypeAndFuncType(String projectId,
                                                                           DemandTemplateTypeEnum type,
                                                                           List<DtpRwdSpaceDTO> spaceList,
                                                                           Map<String, DtpRwdFloorDTO> floorMap) {
        List<DtpRwdTreeLightingObjectDTO> list = new ArrayList<>();
        // 获取项目下的照明回路信息
        List<DtpRwdCircuitDTO> circuits = listCircuits(projectId, type, null);
        Map<String, String> funcTypeCode2name = mapCircuitInfoCodeAndName(DtpRwdConvert.INFO_CODE_FUNC_TYPE);
        Map<String, String> lampFuncTypeCode2name = mapCircuitInfoCodeAndName(DtpRwdConvert.INFO_CODE_LAMP_FUNC_TYPE);
        // 填充空间的灯具类型信息
        spaceList.forEach(s -> {
            DtpRwdTreeLightingObjectDTO sVo = BeanUtil.toBean(s, DtpRwdTreeLightingObjectDTO.class);
            List<String> funcType = new ArrayList<>();
            List<String> lampFuncType = new ArrayList<>();
            Map<String, Set<String>> lampFuncType2FuncType = new HashMap<>();
            DtpRwdFloorDTO floor = floorMap.get(s.getFloorId());

            if (CollectionUtils.isNotEmpty(sVo.getRelObjIds())) {
                circuits.forEach(c -> {
                    if (sVo.getRelObjIds().contains(c.getId()) && StringUtils.isNotBlank(c.getFuncType())) {
                        funcType.add(c.getFuncType());
                        if (StringUtils.isNotBlank(c.getLampFuncType())) {
                            lampFuncType.add(c.getLampFuncType());
                            Set<String> orDefault = lampFuncType2FuncType.getOrDefault(c.getLampFuncType(), new HashSet<>());
                            orDefault.add(c.getFuncType());
                            lampFuncType2FuncType.put(c.getLampFuncType(), orDefault);
                        }
                    }
                });
            }
            sVo.setLampFuncTypeList(listInfoCodeAndDataSources(lampFuncTypeCode2name, lampFuncType));
            // 补充扩展字段 将对应的功能类型放入扩展字段 ，key 为 lampFuncType value 为 lampFuncTypeName
            sVo.getLampFuncTypeList().forEach(lampFuncTypeEnumVO -> {
                if (lampFuncType2FuncType.containsKey((String) lampFuncTypeEnumVO.getKey())) {
                    HashMap<String, String> addAdditionalValue = new HashMap<>();
                    lampFuncType2FuncType.get((String) lampFuncTypeEnumVO.getKey())
                            .forEach(funcTypeNode -> addAdditionalValue.put(funcTypeNode, funcTypeCode2name.get(funcTypeNode)));
                    lampFuncTypeEnumVO.setAdditionalMapValue(addAdditionalValue);
                }
            });
            sVo.setFuncTypeList(listInfoCodeAndDataSources(funcTypeCode2name, funcType));

            // 转换为map 构建额外字段 BeanUtil.beanToMap 可能有风险 后续优化一下
            Map<String, Object> benMap = BeanUtil.beanToMap(s);
            sVo.setInstanceType(DemandInstanceTypeEnum.create(BaseEnum.analyze(
                    sVo.getClassCode(), DtpRwdClassCodeEnum.class, false)));
            if (MapUtils.isNotEmpty(s.getUndefinedProperties())) {
                benMap.putAll(s.getUndefinedProperties());
            }
            // 填充楼层的 名称信息
            if (floor != null) {
                benMap.put("floorName", floor.getName());
                benMap.put("floorLocalId", floor.getLocalId());
            }
            sVo.buildExtraParameter(benMap);

            list.add(sVo);
        });
        return list;
    }


    public Set<BaseEnumVO> listInfoCodeAndDataSources(Map<String, String> infoCodeAndVo, List<String> cods) {
        if (MapUtils.isEmpty(infoCodeAndVo)) {
            return Sets.newLinkedHashSet();
        }
        Set<BaseEnumVO> r = new LinkedHashSet<>();
        for (String cod : cods) {
            r.add(new BaseEnumVO(cod, infoCodeAndVo.get(cod)));
        }
        return r;
    }

    /**
     * 构建父子结构
     *
     * @param buildingList   建筑列表
     * @param buildingObjMap 建筑ID -> 楼层列表
     * @param floorObjMap    楼层ID -> 空间列表
     * @return 照明对象树DTO
     */
    private List<DtpRwdTreeLightingObjectDTO> buildTreeObject(List<DtpRwdBuildingDTO> buildingList,
                                                              Map<String, List<DtpRwdFloorDTO>> buildingObjMap,
                                                              Map<String, List<DtpRwdObjectDTO>> floorObjMap) {
        List<DtpRwdTreeLightingObjectDTO> list = new ArrayList<>();
        // 遍历出父子结构
        buildingList.forEach(b -> {
            DtpRwdTreeLightingObjectDTO building = BeanUtil.toBean(b, DtpRwdTreeLightingObjectDTO.class);
            building.buildInstanceType();
            // 第二层 添加建筑下的楼层
            buildingObjMap.forEach((bk, bv) -> {
                // 楼层按floorSequenceId倒序排序
                List<DtpRwdFloorDTO> sortedList = bv.stream()
                        .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId).reversed())
                        .collect(Collectors.toList());
                List<DtpRwdTreeLightingObjectDTO> floor = BeanUtil.copyToList(sortedList, DtpRwdTreeLightingObjectDTO.class);
                if (StringUtils.equals(bk, b.getId())) {
                    List<DtpRwdTreeLightingObjectDTO> childrenList = new ArrayList<>();
                    for (DtpRwdTreeLightingObjectDTO f : floor) {
                        f.buildInstanceType();
                        // 第三层添加 楼层下的空间
                        floorObjMap.forEach((fk, dv) -> {
                            if (StringUtils.equals(fk, f.getId())) {
                                List<DtpRwdTreeLightingObjectDTO> space = BeanUtil.copyToList(dv, DtpRwdTreeLightingObjectDTO.class);
                                f.setChildrenList(space);
                                childrenList.add(f);
                            }
                        });
                    }
                    // 存在子节点才添加
                    if (CollectionUtils.isNotEmpty(childrenList)) {
                        building.setChildrenList(childrenList);
                    }
                }
            });
            // 存在子节点才添加
            if (CollectionUtils.isNotEmpty(building.getChildrenList())) {
                // 添加第一层 建筑
                list.add(building);
            }
        });
        return list;
    }

    @Override
    public List<? extends ExternalDemandConfigResultVO> externalListConfigResultsByInstance(ExternalDemandConfigResultQueryParam queryParam) {
        // 校验场景时间是必选的
        ParameterCheck.isNull(queryParam.getSceneTime(), LocaleMessageUtil.getMessage("key_d4dd9d7111614c3299b055be52bd3b0d"));
        ParameterCheck.isNull(queryParam.getTemplateType(), LocaleMessageUtil.getMessage("key_8356b8ff6af845ad99d6033c92e55c61"));
        List<ExternalDemandConfigResultLightingVO> voList = new ArrayList<>();
        Long demandTemplateId;
        Long demandLightingSceneId;
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(queryParam.getTemplateType());
        templateParam.setDate(queryParam.getSceneTime().toLocalDate());
        DemandTemplate template = templateParam.queryAndGetTemplate();
        // 当前时间没有模版直接返回
        if (template == null) {
            LogExecutor.project().warn(log(), logTag(), "没有获取到模版");
            return voList;
        }
        Optional<DemandSceneEntity> lightingScene = demandSceneGateway.getValidSceneByTime(queryParam.getTemplateType(),
                queryParam.getSceneTime());
        // 没有场景直接返回
        if (!lightingScene.isPresent()) {
            LogExecutor.project().warn(log(), logTag(), "没有获取到场景");
            return voList;
        }
        demandTemplateId = lightingScene.get().getRelTemplateId();
        demandLightingSceneId = lightingScene.get().getId();

        // 通过场景获取配置结果表的数据
        DemandConfigLightingResultQueryDaoParam param = new DemandConfigLightingResultQueryDaoParam();
        param.setRelDemandTemplateId(demandTemplateId);
        param.setRelDemandLightingSceneId(demandLightingSceneId);
        List<DemandConfigLightingResult> cfgAll = demandConfigLightingResultService.list(ContextUtil.getProjectId(),
                param);

        // 把数据转换为结果
        cfgAll.forEach(c -> {
            ExternalDemandConfigResultLightingVO vo = new ExternalDemandConfigResultLightingVO();
            vo.setCircuitId(c.getCircuitId());
            // 除了反馈调节 非开的场景都是关
            if (!DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(c.getConfigData().getCircuitSettingMode()) &&
                    !DemandLightingCircuitSettingModeEnum.OPEN.equals(c.getConfigData().getCircuitSettingMode())) {
                vo.setSettingMode(DemandLightingCircuitSettingModeEnum.CLOSE);
            } else {
                vo.setSettingMode(c.getConfigData().getCircuitSettingMode());
            }
            voList.add(vo);
        });

        return voList;
    }
}
