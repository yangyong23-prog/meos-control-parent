package com.persagy.apccontrol.basic.structure.demand.context.lighting;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupDetailQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-配置继承上下文
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
public class DemandLightingConfigInheritContext {

    // ------------------------- 入参 -------------------------

    /**
     * 聚合根
     */
    private final List<DemandConfigAggregate<DemandConfigLightingValueObject>> aggregates;

    private final DemandLightingStrategy demandLightingStrategy = SpringUtil.getBean(DemandLightingStrategy.class);
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
    private final DemandInstanceGroupResource demandInstanceGroupResource = SpringUtil.getBean(DemandInstanceGroupResource.class);
    private final RedisCacheTemplate redisCacheTemplate = SpringUtil.getBean(RedisCacheTemplate.class);
    private final ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

    // ------------------------- 过程 -------------------------

    /**
     * 照明回路ID -> 照明回路
     */
    private Map<String, DtpRwdCircuitDTO> circuitIdAndData = Collections.emptyMap();

    /**
     * 非夜景照明建筑/楼层/空间ID -> 建筑/楼层/空间下照明回路ID
     */
    private Map<String, Set<String>> instanceIdAndCircuitIds = Collections.emptyMap();

    /**
     * 非夜景照明建筑/楼层/空间ID -> 建筑/楼层/空间下空间功能类型
     */
    private Map<String, Set<String>> instanceIdAndRoomFuncTypes = Collections.emptyMap();

    /**
     * 空间功能类型 -> 照明回路ID
     */
    private Map<String, Set<String>> roomFuncTypeAndCircuitIds = Collections.emptyMap();

    /**
     * 照明回路ID -> 照明回路所在编组ID
     */
    private Map<String, Set<Long>> circuitIdAndGroupIds = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    // ------------------------- 构造器 -------------------------

    /**
     * 构造器
     *
     * @param aggregates 聚合根
     * @since 7.250801.0
     */
    public DemandLightingConfigInheritContext(
            @NonNull List<DemandConfigAggregate<DemandConfigLightingValueObject>> aggregates) {
        this.aggregates = aggregates;
        if (CollectionUtils.isEmpty(aggregates)) {
            this.failed = true;
        }
    }

    /**
     * 填充照明回路相关数据
     *
     * @since 7.250801.0
     */
    public void fillAboutCircuits() {
        if (this.failed || CollectionUtils.isEmpty(this.aggregates)) {
            return;
        }
        Set<DemandTemplateTypeEnum> templateTypes = new HashSet<>();
        for (DemandConfigAggregate<DemandConfigLightingValueObject> aggregate : this.aggregates) {
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandTemplateTypeEnum templateType = identifier.getTemplateType();
            templateTypes.add(templateType);
        }
        EquipmentQueryParam equipmentQueryParam = new EquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdCircuitDTO.Fields.funcType);
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdSpaceDTO.Fields.roomFuncType,
                DtpRwdSpaceDTO.Fields.floorId, DtpRwdSpaceDTO.Fields.buildingId);
        List<DtpRwdCircuitDTO> circuits = redisCacheTemplate.asyncRefreshTemplate("listCircuits",
                templateTypes.toString() + equipmentQueryParam + spaceQueryParam, () ->
                        demandLightingStrategy.listCircuits(templateTypes, equipmentQueryParam, spaceQueryParam));
        if (CollectionUtils.isEmpty(circuits)) {
            this.failed = true;
            return;
        }
        String projectId = ContextUtil.getProjectId();
        Map<String, DtpRwdCircuitDTO> circuitIdAndData = new HashMap<>();
        Map<String, Set<String>> instanceIdAndCircuitIds = new HashMap<>();
        Map<String, Set<String>> instanceIdAndRoomFuncTypes = new HashMap<>();
        Map<String, Set<String>> roomFuncTypeAndCircuitIds = new HashMap<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            String circuitId = circuit.getId();
            List<DtpRwdSpaceDTO> serviceSpaces = circuit.getServiceSpaces();
            if (CollectionUtils.isNotEmpty(serviceSpaces)) {
                for (DtpRwdSpaceDTO serviceSpace : serviceSpaces) {
                    String spaceId = serviceSpace.getId();
                    String floorId = serviceSpace.getFloorId();
                    String buildingId = serviceSpace.getBuildingId();
                    String roomFuncType = serviceSpace.getRoomFuncType();
                    MapExecutor.dynamicPut(instanceIdAndCircuitIds, spaceId, circuitId, HashSet::new);
                    MapExecutor.dynamicPut(instanceIdAndCircuitIds, floorId, circuitId, HashSet::new);
                    MapExecutor.dynamicPut(instanceIdAndCircuitIds, buildingId, circuitId, HashSet::new);
                    if (StringUtils.isNotBlank(roomFuncType)) {
                        MapExecutor.dynamicPut(instanceIdAndRoomFuncTypes, spaceId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(instanceIdAndRoomFuncTypes, floorId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(instanceIdAndRoomFuncTypes, buildingId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(instanceIdAndRoomFuncTypes, projectId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(roomFuncTypeAndCircuitIds, roomFuncType, circuitId, HashSet::new);
                    }
                }
            }
            circuitIdAndData.put(circuitId, circuit);
            MapExecutor.dynamicPut(instanceIdAndCircuitIds, projectId, circuitId, HashSet::new);
            instanceIdAndCircuitIds.put(circuitId, Sets.newHashSet(circuitId));
        }
        this.circuitIdAndData = circuitIdAndData;
        this.instanceIdAndCircuitIds = instanceIdAndCircuitIds;
        this.instanceIdAndRoomFuncTypes = instanceIdAndRoomFuncTypes;
        this.roomFuncTypeAndCircuitIds = roomFuncTypeAndCircuitIds;
    }

    /**
     * 处理最终配置
     *
     * @since 7.250801.0
     */
    public void handleFinalConfig() {
        if (this.failed || CollectionUtils.isEmpty(this.aggregates)) {
            this.failed = true;
            return;
        }
        for (DemandConfigAggregate<DemandConfigLightingValueObject> aggregate : aggregates) {
            DemandConfigIdentifier identifier = aggregate.getIdentifier();
            DemandTemplateTypeEnum templateType = identifier.getTemplateType();
            String instanceId = identifier.getInstance().getInstanceId();
            DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig = aggregate.getFinalConfig();
            if (Objects.isNull(finalConfig)) {
                continue;
            }
            // 当前对象下照明回路
            Set<String> circuitIds = this.instanceIdAndCircuitIds.get(instanceId);
            if (CollectionUtils.isEmpty(circuitIds)) {
                aggregate.setFinalConfig(copy(templateType, finalConfig));
                continue;
            }
            finalConfig = copy(templateType, finalConfig);
            aggregate.setFinalConfig(finalConfig);
            // 处理全局配置
            DemandConfigLightingValueObject globalConfig = this.handleGlobalConfig(
                    instanceId, circuitIds, finalConfig.getGlobalConfig());
            // 处理空间功能类型配置
            Set<String> roomFuncTypes = this.instanceIdAndRoomFuncTypes.get(instanceId);
            List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs =
                    this.handleSpaceFuncTypeConfig(instanceId, roomFuncTypes, finalConfig.getSpaceFuncTypeConfigs());
            // 无有效配置时无最终配置
            if (Objects.isNull(globalConfig) && CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
                aggregate.setFinalConfig(emptyConfig(templateType));
                continue;
            }
            finalConfig.setGlobalConfig(globalConfig);
            finalConfig.setSpaceFuncTypeConfigs(spaceFuncTypeConfigs);
        }
    }

    /**
     * 空配置
     *
     * @param templateType 模版类型
     * @return 空配置
     * @since 7.250801.0
     */
    private DemandConfigValueObject<DemandConfigLightingValueObject> emptyConfig(
            @NonNull DemandTemplateTypeEnum templateType) {
        DemandConfigValueObject<DemandConfigLightingValueObject> valueObject = new DemandConfigValueObject<>();
        valueObject.setGlobalConfig((DemandConfigLightingValueObject) templateType
                .demandConfigAnalyzeStrategy().emptyConfig().get());
        return valueObject;
    }

    /**
     * 填充回路编组相关数据
     *
     * @since 7.250801.0
     */
    public void fillAboutCircuitGroups() {
        if (this.failed || CollectionUtils.isEmpty(this.aggregates)) {
            this.failed = true;
            return;
        }
        Set<Long> circuitGroupIds = new HashSet<>();
        for (DemandConfigAggregate<DemandConfigLightingValueObject> aggregate : aggregates) {
            DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig = aggregate.getFinalConfig();
            if (Objects.isNull(finalConfig)) {
                continue;
            }
            circuitGroupIds.addAll(parseCircuitGroupIds(finalConfig.getGlobalConfig()));
            List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>>
                    spaceFuncTypeConfigs = finalConfig.getSpaceFuncTypeConfigs();
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                spaceFuncTypeConfigs.forEach(item ->
                        circuitGroupIds.addAll(parseCircuitGroupIds(item.getConfig())));
            }
        }
        if (CollectionUtils.isEmpty(circuitGroupIds)) {
            return;
        }
        DemandInstanceGroupDetailQueryDaoParam detailQueryDaoParam = new DemandInstanceGroupDetailQueryDaoParam();
        detailQueryDaoParam.setRelGroupIds(circuitGroupIds);
        detailQueryDaoParam.addColumns(DemandInstanceGroupDetail::getInstanceId, DemandInstanceGroupDetail::getRelGroupId);
        DemandInstanceGroupQueryDaoParam queryDaoParam = new DemandInstanceGroupQueryDaoParam();
        queryDaoParam.setIds(circuitGroupIds);
        queryDaoParam.setQueryDetail(true);
        queryDaoParam.setDetailQueryParam(detailQueryDaoParam);
        queryDaoParam.addColumns(BaseAssignIdDO::getId);
        List<DemandInstanceGroupPO> groups = demandInstanceGroupResource.list(queryDaoParam);
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        Map<String, Set<Long>> circuitIdAndGroupIds = new HashMap<>();
        for (DemandInstanceGroupPO group : groups) {
            List<DemandInstanceGroupDetail> details = group.getDetails();
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
            for (DemandInstanceGroupDetail detail : details) {
                Long relGroupId = detail.getRelGroupId();
                String instanceId = detail.getInstanceId();
                MapExecutor.dynamicPut(circuitIdAndGroupIds, instanceId, relGroupId, HashSet::new);
            }
        }
        this.circuitIdAndGroupIds = circuitIdAndGroupIds;
    }

    /**
     * 填充最终配置中照明回路配置
     *
     * @since 7.250801.0
     */
    @SneakyThrows
    public void fillFinalConfigCircuitSettings() {
        if (this.failed || CollectionUtils.isEmpty(this.aggregates)) {
            this.failed = true;
            return;
        }
        for (DemandConfigAggregate<DemandConfigLightingValueObject> aggregate : this.aggregates) {
            String instanceId = aggregate.getIdentifier().getInstance().getInstanceId();
            if (Objects.isNull(aggregate.getFinalConfig())) {
                continue;
            }
            Set<String> circuitIds = this.instanceIdAndCircuitIds.get(instanceId);
            if (CollectionUtils.isEmpty(circuitIds)) {
                continue;
            }
            DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig = objectMapper.readValue(
                    objectMapper.writeValueAsString(aggregate.getFinalConfig()),
                    new TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>>() {
                    });
            DemandConfigLightingValueObject globalConfig = finalConfig.getGlobalConfig();
            List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>>
                    spaceFuncTypeConfigs = finalConfig.getSpaceFuncTypeConfigs();
            this.fillGlobalConfigCircuitSettings(circuitIds, globalConfig);
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                for (DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                    List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
                    DemandConfigLightingValueObject config = spaceFuncTypeConfig.getConfig();

                    Set<String> circuitIdsByRoomFuncTypes = new HashSet<>();
                    for (DtpKnowledgeInfoCodeDataSourceDTO funcType : funcTypes) {
                        Set<String> circuitIdsByFuncTypes = this.roomFuncTypeAndCircuitIds.get(funcType.getCode());
                        if (CollectionUtils.isNotEmpty(circuitIdsByFuncTypes)) {
                            circuitIdsByRoomFuncTypes.addAll(circuitIdsByFuncTypes);
                        }
                    }
                    this.fillGlobalConfigCircuitSettings(circuitIdsByRoomFuncTypes, config);
                }
            }
            aggregate.setFinalConfig(finalConfig);
        }
    }

    /**
     * 解析照明回路编组ID
     *
     * @param config 配置
     * @return 照明回路编组ID
     * @since 7.250801.0
     */
    private Set<Long> parseCircuitGroupIds(@Nullable DemandConfigLightingValueObject config) {
        if (Objects.isNull(config)) {
            return Collections.emptySet();
        }
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = config.getGlobalSettings();
        if (CollectionUtils.isEmpty(globalSettings)) {
            return Collections.emptySet();
        }
        Set<Long> circuitGroupIds = new HashSet<>();
        for (DemandConfigLightingFuncTypeValueObject globalSetting : globalSettings) {
            DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
            if (settingMode != DemandLightingTypeSettingModeEnum.INSTANCE_GROUP) {
                continue;
            }
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = globalSetting.getCircuitGroupSettings();
            if (CollectionUtils.isEmpty(circuitGroupSettings)) {
                continue;
            }
            circuitGroupIds.addAll(circuitGroupSettings.stream()
                    .map(DemandLightingCircuitGroupSettingDTO::getGroupId)
                    .collect(Collectors.toList()));
        }
        return circuitGroupIds;
    }

    /**
     * 处理空间功能类型配置
     *
     * @param instanceId           实例ID
     * @param roomFuncTypes        当前对象下空间功能类型
     * @param spaceFuncTypeConfigs 空间功能类型配置
     * @return 处理后的空间功能类型配置
     * @since 7.250801.0
     */
    private List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> handleSpaceFuncTypeConfig(
            @NonNull String instanceId,
            @Nullable Set<String> roomFuncTypes,
            @Nullable List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs) {
        if (CollectionUtils.isEmpty(roomFuncTypes) || CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
            return null;
        }
        Iterator<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>>
                spaceFuncTypeConfigIterator = spaceFuncTypeConfigs.iterator();
        while (spaceFuncTypeConfigIterator.hasNext()) {
            DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>
                    spaceFuncTypeConfig = spaceFuncTypeConfigIterator.next();
            List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
            DemandConfigLightingValueObject config = spaceFuncTypeConfig.getConfig();
            if (CollectionUtils.isEmpty(funcTypes)) {
                spaceFuncTypeConfigIterator.remove();
                continue;
            }
            // 当前功能类型下照明回路
            Set<String> circuitIdsByFuncTypes = new HashSet<>();
            Iterator<DtpKnowledgeInfoCodeDataSourceDTO> funcTypesIterator = funcTypes.iterator();
            while (funcTypesIterator.hasNext()) {
                DtpKnowledgeInfoCodeDataSourceDTO funcType = funcTypesIterator.next();
                if (!roomFuncTypes.contains(funcType.getCode())) {
                    funcTypesIterator.remove();
                    continue;
                }
                Set<String> eachCircuitIdsByFuncTypes = this.roomFuncTypeAndCircuitIds.get(funcType.getCode());
                if (CollectionUtils.isNotEmpty(eachCircuitIdsByFuncTypes)) {
                    circuitIdsByFuncTypes.addAll(eachCircuitIdsByFuncTypes);
                }
            }
            if (CollectionUtils.isEmpty(funcTypes) || CollectionUtils.isEmpty(circuitIdsByFuncTypes)) {
                spaceFuncTypeConfigIterator.remove();
                continue;
            }
            DemandConfigLightingValueObject funcTypeConfig = handleGlobalConfig(
                    instanceId, circuitIdsByFuncTypes, config);
            if (Objects.isNull(funcTypeConfig)) {
                spaceFuncTypeConfigIterator.remove();
                continue;
            }
            spaceFuncTypeConfig.setConfig(funcTypeConfig);
        }
        if (CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
            return null;
        }
        return spaceFuncTypeConfigs;
    }

    /**
     * 填充全局配置照明回路配置
     *
     * @param circuitIds   当前对象下照明回路ID
     * @param globalConfig 全局配置
     * @since 7.250801.0
     */
    private void fillGlobalConfigCircuitSettings(
            @NonNull Set<String> circuitIds,
            @Nullable DemandConfigLightingValueObject globalConfig) {
        if (CollectionUtils.isEmpty(circuitIds) || Objects.isNull(globalConfig)) {
            return;
        }
        Map<String, List<DtpRwdCircuitDTO>> funcTypeAndCircuits = this.toFuncTypeAndCircuits(circuitIds);
        if (MapUtils.isEmpty(funcTypeAndCircuits)) {
            return;
        }
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = globalConfig.getGlobalSettings();
        // 填充全局配置中的照明回路配置
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            Map<Long, DemandLightingTypeSettingModeEnum> allGroupIdAndSettingMode = new HashMap<>();
            globalSettings.stream().filter(item ->
                    item.getSettingMode() == DemandLightingTypeSettingModeEnum.INSTANCE_GROUP
            ).forEach(item -> {
                allGroupIdAndSettingMode.putAll(DemandLightingCircuitGroupSettingDTO
                        .toGroupIdAndSettingMode(item.getCircuitGroupSettings()));
            });
            for (DemandConfigLightingFuncTypeValueObject globalSetting : globalSettings) {
                String funcType = globalSetting.getFuncType();
                DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
                List<DemandConfigLightingCircuitValueObject> circuitSettings = globalSetting.getCircuitSettings();
                List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = globalSetting.getCircuitGroupSettings();
                Map<Long, DemandLightingTypeSettingModeEnum> groupIdAndSettingMode =
                        DemandLightingCircuitGroupSettingDTO.toGroupIdAndSettingMode(circuitGroupSettings);

                List<DtpRwdCircuitDTO> circuits = funcTypeAndCircuits.get(funcType);
                if (CollectionUtils.isEmpty(circuits)) {
                    continue;
                }
                // 移除已处理的回路
                funcTypeAndCircuits.remove(funcType);
                // 解析照明回路ID和配置模式
                Map<String, DemandLightingCircuitSettingModeEnum> circuitIdAndSettingMode =
                        parseCircuitIdAndSettingMode(circuitSettings);
                List<DemandConfigLightingCircuitValueObject> finalCircuitSettings = new ArrayList<>();
                for (DtpRwdCircuitDTO circuit : circuits) {
                    String circuitId = circuit.getId();
                    DemandLightingCircuitSettingModeEnum circuitSettingMode =
                            DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
                    switch (settingMode) {
                        case ALL_OPEN:
                            circuitSettingMode = DemandLightingCircuitSettingModeEnum.OPEN;
                            break;
                        case ALL_CLOSE:
                            circuitSettingMode = DemandLightingCircuitSettingModeEnum.CLOSE;
                            break;
                        case PARTIAL_OPEN:
                        case FEEDBACK_ADJUSTMENT:
                            DemandLightingCircuitSettingModeEnum existsSettingMode =
                                    circuitIdAndSettingMode.get(circuitId);
                            if (Objects.nonNull(existsSettingMode)) {
                                circuitSettingMode = existsSettingMode;
                            }
                            break;
                        case INSTANCE_GROUP:
                            Set<Long> groupIds = this.circuitIdAndGroupIds.get(circuitId);
                            if (CollectionUtils.isNotEmpty(groupIds)) {
                                // 任意一个编组为全开, 则为开
                                DemandLightingTypeSettingModeEnum groupSettingMode = groupIds.stream()
                                        .map(allGroupIdAndSettingMode::get)
                                        .filter(Objects::nonNull)
                                        .anyMatch(item -> item == DemandLightingTypeSettingModeEnum.ALL_OPEN)
                                        ? DemandLightingTypeSettingModeEnum.ALL_OPEN
                                        : null;
                                // 所有编组没有全开时重新赋值
                                if (Objects.isNull(groupSettingMode)) {
                                    for (Long groupId : groupIds) {
                                        groupSettingMode = groupIdAndSettingMode.get(groupId);
                                        if (groupSettingMode == DemandLightingTypeSettingModeEnum.ALL_OPEN
                                                || groupSettingMode == DemandLightingTypeSettingModeEnum.ALL_CLOSE) {
                                            break;
                                        }
                                    }
                                }
                                if (Objects.nonNull(groupSettingMode)) {
                                    circuitSettingMode = groupSettingMode.toCircuitSettingMode();
                                }
                            }
                            break;
                        default:
                    }
                    // 重新设置找照明回路配置
                    DemandConfigLightingCircuitValueObject circuitSetting = new DemandConfigLightingCircuitValueObject();
                    circuitSetting.setCircuitId(circuitId);
                    circuitSetting.setSettingMode(circuitSettingMode);
                    finalCircuitSettings.add(circuitSetting);
                }
                globalSetting.setCircuitSettings(finalCircuitSettings);
            }
        }
        // 填充全局配置中剩余不存在的照明回路
        if (MapUtils.isNotEmpty(funcTypeAndCircuits)) {
            if (CollectionUtils.isEmpty(globalSettings)) {
                globalSettings = new ArrayList<>();
            }
            for (Map.Entry<String, List<DtpRwdCircuitDTO>> entry : funcTypeAndCircuits.entrySet()) {
                String funcType = entry.getKey();
                List<DtpRwdCircuitDTO> circuits = entry.getValue();
                List<DemandConfigLightingCircuitValueObject> circuitSettings = new ArrayList<>();
                for (DtpRwdCircuitDTO circuit : circuits) {
                    DemandConfigLightingCircuitValueObject circuitSetting = new DemandConfigLightingCircuitValueObject();
                    circuitSetting.setCircuitId(circuit.getId());
                    circuitSetting.setSettingMode(DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
                    circuitSettings.add(circuitSetting);
                }
                DemandConfigLightingFuncTypeValueObject tempGlobalSetting =
                        new DemandConfigLightingFuncTypeValueObject();
                tempGlobalSetting.setFuncType(funcType);
                tempGlobalSetting.setSettingMode(DemandLightingTypeSettingModeEnum.NOT_CONFIGURED);
                tempGlobalSetting.setCircuitSettings(circuitSettings);
                globalSettings.add(tempGlobalSetting);
            }
            globalConfig.setGlobalSettings(globalSettings);
        }
    }

    /**
     * 处理全局配置
     *
     * @param instanceId   实例ID
     * @param circuitIds   当前对象下照明回路ID
     * @param globalConfig 全局配置
     * @return 处理后的全局配置
     * @since 7.250801.0
     */
    private DemandConfigLightingValueObject handleGlobalConfig(
            @NonNull String instanceId,
            @NonNull Set<String> circuitIds,
            @Nullable DemandConfigLightingValueObject globalConfig) {
        if (CollectionUtils.isEmpty(circuitIds) || Objects.isNull(globalConfig)) {
            return null;
        }
        Map<String, List<DtpRwdCircuitDTO>> funcTypeAndCircuits = this.toFuncTypeAndCircuits(circuitIds);
        if (MapUtils.isEmpty(funcTypeAndCircuits)) {
            return null;
        }
        Map<String, List<DemandConfigLightingFuncTypeValueObject>> instanceIdAndFeedbackAdjustSettings =
                globalConfig.getInstanceIdAndFeedbackAdjustSettings();
        List<DemandConfigLightingFuncTypeValueObject> feedbackAdjustSettings =
                MapUtils.isNotEmpty(instanceIdAndFeedbackAdjustSettings)
                        ? instanceIdAndFeedbackAdjustSettings.get(instanceId)
                        : null;
        // 功能类型 -> 原始配置
        Map<String, DemandConfigLightingFuncTypeValueObject> funcTypeAndOriginGlobalSetting = Collections.emptyMap();
        // 全局配置内容
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = globalConfig.getGlobalSettings();
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            funcTypeAndOriginGlobalSetting = new LinkedHashMap<>();
            for (DemandConfigLightingFuncTypeValueObject globalSetting : globalSettings) {
                funcTypeAndOriginGlobalSetting.put(globalSetting.getFuncType(), globalSetting);
            }
        }
        // 存在反馈调节配置时, 将反馈调节配置赋值到全局配置中
        if (CollectionUtils.isNotEmpty(feedbackAdjustSettings)) {
            globalSettings = feedbackAdjustSettings;
            globalConfig.setGlobalSettings(globalSettings);
            globalConfig.setInstanceIdAndFeedbackAdjustSettings(null);
        }
        if (Objects.isNull(globalSettings)) {
            return null;
        }
        if (globalSettings.isEmpty()) {
            return globalConfig;
        }
        Iterator<DemandConfigLightingFuncTypeValueObject> globalSettingIterator = globalSettings.iterator();
        while (globalSettingIterator.hasNext()) {
            DemandConfigLightingFuncTypeValueObject globalSetting = globalSettingIterator.next();
            String funcType = globalSetting.getFuncType();
            // 当前功能类型下的照明回路
            List<DtpRwdCircuitDTO> circuitsByFuncType = funcTypeAndCircuits.get(funcType);
            if (CollectionUtils.isEmpty(circuitsByFuncType)) {
                globalSettingIterator.remove();
                continue;
            }
            // 根据功能类型处理配置
            DemandConfigLightingFuncTypeValueObject finalGlobalSetting = this.handleGlobalSettingByFuncType(
                    circuitsByFuncType, funcTypeAndOriginGlobalSetting.get(funcType), globalSetting);
            // 配置无效时删除
            if (Objects.isNull(finalGlobalSetting)) {
                globalSettingIterator.remove();
                continue;
            }
            // 有效配置时重新赋值
            BeanUtils.copyProperties(finalGlobalSetting, globalSetting);
        }
        if (CollectionUtils.isEmpty(globalSettings)) {
            return null;
        }
        return globalConfig;
    }

    /**
     * 根据照明回路功能类型处理全局配置内容
     *
     * @param circuits            当前功能类型下的照明回路
     * @param originGlobalSetting 当前功能类型的原始配置
     * @param globalSetting       当前功能类型的全局配置
     * @return 处理后的全局配置
     * @since 7.250801.0
     */
    private DemandConfigLightingFuncTypeValueObject handleGlobalSettingByFuncType(
            @NonNull List<DtpRwdCircuitDTO> circuits,
            @Nullable DemandConfigLightingFuncTypeValueObject originGlobalSetting,
            @NonNull DemandConfigLightingFuncTypeValueObject globalSetting) {
        if (CollectionUtils.isEmpty(circuits)) {
            return null;
        }
        Set<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
        List<DemandConfigLightingCircuitValueObject> circuitSettings = globalSetting.getCircuitSettings();
        // 处理部分开启回路配置
        if (settingMode == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN
                && CollectionUtils.isNotEmpty(circuitSettings)) {
            circuitSettings.removeIf(item -> !circuitIds.contains(item.getCircuitId()));
        }
        // 处理反馈调节回路配置
        if (settingMode == DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT
                && CollectionUtils.isNotEmpty(circuitSettings)) {
            circuitSettings.removeIf(item -> !circuitIds.contains(item.getCircuitId()));
        }
        switch (settingMode) {
            case PARTIAL_OPEN:
                if (CollectionUtils.isEmpty(circuitSettings)) {
                    return null;
                }
                break;
            case FEEDBACK_ADJUSTMENT:
                if (CollectionUtils.isEmpty(circuitSettings)) {
                    // 当反馈调节回路配置为空时, 存在原始配置则将原始配置处理一遍并返回
                    if (Objects.nonNull(originGlobalSetting)) {
                        return this.handleGlobalSettingByFuncType(circuits, null, originGlobalSetting);
                    }
                    return null;
                }
                break;
            default:
        }
        return globalSetting;
    }

    /**
     * 将照明回路ID转换为功能类型和照明回路映射
     *
     * @param circuitIds 照明回路ID
     * @return 照明回路功能类型 -> 功能类型下的照明回路
     * @since 7.250801.0
     */
    private Map<String, List<DtpRwdCircuitDTO>> toFuncTypeAndCircuits(@Nullable Set<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Collections.emptyMap();
        }
        Map<String, List<DtpRwdCircuitDTO>> funcTypeAndCircuits = new TreeMap<>();
        for (String circuitId : circuitIds) {
            DtpRwdCircuitDTO circuit = this.circuitIdAndData.get(circuitId);
            if (Objects.isNull(circuit)) {
                continue;
            }
            String funcType = circuit.getFuncType();
            if (StringUtils.isNotBlank(funcType)) {
                MapExecutor.dynamicPut(funcTypeAndCircuits, funcType, circuit, ArrayList::new);
            }
        }
        return funcTypeAndCircuits;
    }

    /**
     * 解析照明回路ID和配置模式
     *
     * @param circuitSettings 照明回路配置
     * @return 照明回路ID -> 配置模式
     * @since 7.250801.0
     */
    private Map<String, DemandLightingCircuitSettingModeEnum> parseCircuitIdAndSettingMode(
            @NonNull List<DemandConfigLightingCircuitValueObject> circuitSettings) {
        if (CollectionUtils.isEmpty(circuitSettings)) {
            return Collections.emptyMap();
        }
        return circuitSettings.stream().collect(Collectors.toMap(DemandConfigLightingCircuitValueObject::getCircuitId,
                DemandConfigLightingCircuitValueObject::getSettingMode,
                (o1, o2) -> o1));
    }

    /**
     * 复制配置
     *
     * @param templateType 模版类型
     * @param source       数据源
     * @return 目标数据
     * @since 7.250801.0
     */
    @SneakyThrows
    private @NonNull DemandConfigValueObject<DemandConfigLightingValueObject> copy(
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable DemandConfigValueObject<DemandConfigLightingValueObject> source) {
        if (Objects.isNull(source)) {
            return emptyConfig(templateType);
        }
        return objectMapper.readValue(objectMapper.writeValueAsString(source),
                new TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>>() {
                });
    }
}
