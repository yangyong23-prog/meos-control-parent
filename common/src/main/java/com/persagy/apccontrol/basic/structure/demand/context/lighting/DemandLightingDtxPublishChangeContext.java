package com.persagy.apccontrol.basic.structure.demand.context.lighting;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupDetailResource;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeNowService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.third.MiddlewareServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明需求DTX发布变更上下文
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
public class DemandLightingDtxPublishChangeContext implements LogComponent {

    // ------------------------- 入参 -------------------------

    /**
     * 事件消息
     */
    private final DtxPublishMessageEvent.Message eventMessage;
    private final DemandTemplateTypeEnum templateType;

    @JsonIgnore
    private final DemandLightingStrategy demandLightingStrategy = SpringUtil.getBean(DemandLightingStrategy.class);
    @JsonIgnore
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
    @JsonIgnore
    private final DemandInstanceGroupResource demandInstanceGroupResource = SpringUtil.getBean(DemandInstanceGroupResource.class);
    @JsonIgnore
    private final DemandInstanceGroupDetailResource demandInstanceGroupDetailResource = SpringUtil.getBean(DemandInstanceGroupDetailResource.class);
    @JsonIgnore
    private final DemandConfigLightingComputeNowService demandConfigLightingComputeNowService = SpringUtil.getBean(DemandConfigLightingComputeNowService.class);
    @JsonIgnore
    private final DemandConfigRepository demandConfigRepository = SpringUtil.getBean(DemandConfigRepository.class);
    @JsonIgnore
    private final DemandConfigService demandConfigService = SpringUtil.getBean(DemandConfigService.class);
    @JsonIgnore
    private final DemandSceneRepository demandSceneRepository = SpringUtil.getBean(DemandSceneRepository.class);
    @JsonIgnore
    private final DemandTemplateRepository demandTemplateRepository = SpringUtil.getBean(DemandTemplateRepository.class);
    @JsonIgnore
    private final DemandConfigGroupRepository demandConfigGroupRepository = SpringUtil.getBean(DemandConfigGroupRepository.class);

    // ------------------------- 过程 -------------------------

    // -------------------- 照明回路 --------------------

    /**
     * 照明回路ID
     */
    private List<DtpRwdCircuitDTO> circuits = new ArrayList<>();

    /**
     * 照明回路ID -> 照明回路
     */
    private Map<String, DtpRwdCircuitDTO> circuitIdAndData = Collections.emptyMap();


    // -------------------- 空间 --------------------

    /**
     * 照明回路服务的空间ID
     */
    private Set<String> circuitServiceSpaceIds = Collections.emptySet();

    /**
     * 支持反馈调节的空间
     */
    private Set<String> allowFeedbackAdjustSpaceIds = Collections.emptySet();

    /**
     * 实例ID -> 照明回路ID
     */
    private Map<String, Set<String>> instanceIdAndCircuitIds = new LinkedHashMap<>();

    /**
     * 实例ID -> 空间功能类型
     */
    private Map<String, Set<String>> instanceIdAndRoomFuncType = new LinkedHashMap<>();

    /**
     * 实例ID -> [空间功能类型 -> 照明回路ID]
     */
    private Map<String, Map<String, Set<String>>> instanceIdRoomFuncTypeAndCircuitIds = new LinkedHashMap<>();

    // -------------------- 楼层 --------------------

    /**
     * 照明回路服务的楼层ID
     */
    private Set<String> circuitServiceFloorIds = Collections.emptySet();

    /**
     * 楼层ID -> 空间ID
     */
    private Map<String, Set<String>> floorIdAndSpaceIds = Collections.emptyMap();

    // -------------------- 需求配置 --------------------

    /**
     * 模版ID -> [场景ID -> 场景下的配置]
     */
    private MultiKeyMap<Long, List<DemandConfig>> templateIdSceneIdAndConfigs = new MultiKeyMap<>();

    /**
     * 编组列表
     */
    private List<DemandInstanceGroupPO> instanceGroups = Collections.emptyList();

    /**
     * 编组id -> 编组
     */
    private Map<Long, DemandInstanceGroupPO> groupIdInstanceGroupMap = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    private StopWatch stopWatch = StopWatch.create("DTX发布变更-照明需求上下文");

    /**
     * 是否失败
     */
    private boolean failed = false;

    /**
     * 待删除的编组ID
     */
    private Set<Long> toBeDelGroupIds = Collections.emptySet();

    /**
     * 待删除的编组详情ID
     */
    private Set<Long> toBeDelGroupDetailIds = Collections.emptySet();

    /**
     * 待编辑的需求配置ID
     */
    private Map<Long, DemandConfig> toBeEditDemandConfigIdAndData = new LinkedHashMap<>();

    /**
     * 待删除的需求配置ID
     */
    private Map<Long, DemandConfig> toBeDelDemandConfigIdAndData = new LinkedHashMap<>();

    /**
     * 待删除的需求配置分组ID和配置ID
     */
    private Map<Long, Long> toBeDelDemandConfigGroupIdAndConfigId = new LinkedHashMap<>();

    /**
     * 构造器
     *
     * @param eventMessage 事件消息
     * @param templateType 模版类型
     * @since 7.250601.0
     */
    public DemandLightingDtxPublishChangeContext(
            DtxPublishMessageEvent.@NonNull Message eventMessage,
            @NonNull DemandTemplateTypeEnum templateType) {
        this.eventMessage = eventMessage;
        this.templateType = templateType;
        String groupCode = eventMessage.getGroupCode();
        String projectId = eventMessage.getProjectId();
        if (StringUtils.isAnyBlank(groupCode, projectId)) {
            logProjectInstance().error("集团编码和项目ID不能为空", Pair.of("message", eventMessage));
            throw new ApplicationException(MiddlewareServerErrorEnum.MESSAGE_CONSUME_ERROR, LocaleMessageUtil.getMessage("key_766b3512102547f7b758092bfac8555f"));
        }
        ContextUtils.initGroupCodeProjectId(eventMessage);
    }

    /**
     * 填充照明回路相关
     *
     * @throws InterruptedException 线程中断
     * @since 7.250601.0
     */
    public void fillAboutCircuits() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed) {
            return;
        }
        this.stopWatch.start(String.format("查询照明回路相关-%s", this.templateType));
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.addWithColumn(InfoCodeEnum.REPLACED_BY_NAT_LIGHTING.getCode());
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuits(Lists.newArrayList(
                this.templateType), null, spaceQueryParam);
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(circuits)) {
            this.failed = true;
            return;
        }
        this.stopWatch.start(String.format("处理照明回路相关-%s", this.templateType));
        Map<String, DtpRwdSpaceDTO> spaceIdAndData = new HashMap<>();
        Set<String> circuitServiceSpaceIds = new HashSet<>();
        Set<String> allowFeedbackAdjustSpaceIds = new HashSet<>();
        Map<String, Set<String>> instanceIdAndCircuitIds = new LinkedHashMap<>();
        Map<String, Set<String>> instanceIdAndRoomFuncType = new LinkedHashMap<>();
        Map<String, Set<String>> circuitIdAndServiceSpaceFuncTypes = new HashMap<>();
        Map<String, Set<String>> roomFuncTypeAndCircuitIds = new HashMap<>();
        Map<String, Map<String, Set<String>>> instanceIdRoomFuncTypeAndCircuitIds = new LinkedHashMap<>();
        Map<String, DtpRwdCircuitDTO> circuitIdAndData = new HashMap<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            String circuitId = circuit.getId();
            circuitIdAndData.put(circuitId, circuit);
            List<DtpRwdSpaceDTO> relSpace = circuit.getServiceSpaces();
            if (CollectionUtils.isNotEmpty(relSpace)) {
                for (DtpRwdSpaceDTO space : relSpace) {
                    String spaceId = space.getId();
                    String roomFuncType = space.getRoomFuncType();
                    spaceIdAndData.put(spaceId, space);
                    circuitServiceSpaceIds.add(spaceId);
                    MapExecutor.dynamicPut(instanceIdAndCircuitIds, spaceId, circuitId, HashSet::new);
                    if (StringUtils.isNotBlank(roomFuncType)) {
                        MapExecutor.dynamicPut(instanceIdAndRoomFuncType, spaceId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(circuitIdAndServiceSpaceFuncTypes, circuitId, roomFuncType, HashSet::new);
                        MapExecutor.dynamicPut(roomFuncTypeAndCircuitIds, roomFuncType, circuitId, HashSet::new);
                        MapExecutor.dynamicPut(instanceIdRoomFuncTypeAndCircuitIds, spaceId,
                                roomFuncType, circuitId, HashMap::new, HashSet::new);
                    }
                }
            }
        }
        for (DtpRwdSpaceDTO space : spaceIdAndData.values()) {
            String spaceId = space.getId();
            Set<String> relCircuitIds = instanceIdAndCircuitIds.get(spaceId);
            if (CollectionUtils.isNotEmpty(relCircuitIds) && space.allowFeedbackAdjust(relCircuitIds.stream()
                    .map(circuitIdAndData::get).filter(Objects::nonNull).collect(Collectors.toList()))) {
                allowFeedbackAdjustSpaceIds.add(spaceId);
            }
        }
        instanceIdAndCircuitIds.put(ContextUtil.getProjectId(), circuitIdAndData.keySet());
        this.stopWatch.stop();
        this.circuits = circuits;
        this.circuitIdAndData = circuitIdAndData;
        this.circuitServiceSpaceIds = circuitServiceSpaceIds;
        this.allowFeedbackAdjustSpaceIds = allowFeedbackAdjustSpaceIds;
        this.instanceIdAndRoomFuncType = instanceIdAndRoomFuncType;
        this.instanceIdAndCircuitIds = instanceIdAndCircuitIds;
    }

    /**
     * 填充照明回路关联楼层
     *
     * @throws InterruptedException 线程中断
     * @since 7.250601.0
     */
    @SuppressWarnings("DuplicatedCode")
    public void fillCircuitRelFloor() throws InterruptedException {
        this.checkInterrupt();
        if (this.templateType.isNightLighting()
                || CollectionUtils.isEmpty(this.circuitServiceSpaceIds)
                || MapUtils.isEmpty(this.instanceIdAndCircuitIds)) {
            return;
        }
        this.stopWatch.start(String.format("查询照明回路关联楼层-%s", this.templateType));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloorBySpaceId(this.circuitServiceSpaceIds,
                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id)
                        .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds));
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(floors)) {
            return;
        }
        Map<String, Set<String>> floorIdAndSpaceIds = DtpRwdObjectDTO.mapIdAndRelObjIds(floors);
        Map<String, Set<String>> floorIdAndCircuitIds = new LinkedHashMap<>();
        Map<String, Map<String, Set<String>>> floorIdRoomFuncTypeAndCircuitIds = new LinkedHashMap<>();
        floorIdAndSpaceIds.forEach((floorId, spaceIds) -> {
            for (String spaceId : spaceIds) {
                Set<String> circuitIdsInSpace = this.instanceIdAndCircuitIds.get(spaceId);
                Set<String> roomFuncTypes = this.instanceIdAndRoomFuncType.get(spaceId);
                if (CollectionUtils.isEmpty(circuitIdsInSpace)) {
                    continue;
                }
                if (CollectionUtils.isNotEmpty(roomFuncTypes)) {
                    for (String roomFuncType : roomFuncTypes) {
                        MapExecutor.dynamicPutAll(floorIdRoomFuncTypeAndCircuitIds, floorId,
                                roomFuncType, circuitIdsInSpace, HashMap::new, HashSet::new);
                    }
                }
                MapExecutor.dynamicPutAll(floorIdAndCircuitIds, floorId, circuitIdsInSpace, HashSet::new);
            }
        });
        this.floorIdAndSpaceIds = floorIdAndSpaceIds;
        this.circuitServiceFloorIds = floorIdAndCircuitIds.keySet();
        this.instanceIdAndCircuitIds.putAll(floorIdAndCircuitIds);
        this.instanceIdRoomFuncTypeAndCircuitIds.putAll(floorIdRoomFuncTypeAndCircuitIds);
    }

    /**
     * 填充照明回路关联建筑
     *
     * @throws InterruptedException 线程中断
     * @since 7.250601.0
     */
    public void fillCircuitRelBuilding() throws InterruptedException {
        this.checkInterrupt();
        if (this.templateType.isNightLighting()
                || CollectionUtils.isEmpty(this.circuitServiceFloorIds)
                || MapUtils.isEmpty(this.floorIdAndSpaceIds)
                || MapUtils.isEmpty(this.instanceIdAndCircuitIds)) {
            return;
        }
        this.stopWatch.start(String.format("查询照明回路关联建筑-%s", this.templateType));
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuildingByFloorId(this.circuitServiceFloorIds,
                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id)
                        .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds));
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(buildings)) {
            return;
        }
        Map<String, Set<String>> buildingIdAndCircuitIds = new LinkedHashMap<>();
        Map<String, Map<String, Set<String>>> buildingIdRoomFuncTypeAndCircuitIds = new LinkedHashMap<>();
        DtpRwdObjectDTO.mapIdAndRelObjIds(buildings).forEach((buildingId, floorIds) -> {
            for (String floorId : floorIds) {
                Set<String> spaceIds = this.floorIdAndSpaceIds.get(floorId);
                if (CollectionUtils.isEmpty(spaceIds)) {
                    continue;
                }
                for (String spaceId : spaceIds) {
                    Set<String> roomFuncTypes = this.instanceIdAndRoomFuncType.get(spaceId);
                    Set<String> circuitIdsInSpace = this.instanceIdAndCircuitIds.get(spaceId);
                    if (CollectionUtils.isEmpty(circuitIdsInSpace)) {
                        continue;
                    }
                    if (CollectionUtils.isNotEmpty(roomFuncTypes)) {
                        for (String roomFuncType : roomFuncTypes) {
                            MapExecutor.dynamicPutAll(buildingIdRoomFuncTypeAndCircuitIds, buildingId,
                                    roomFuncType, circuitIdsInSpace, HashMap::new, HashSet::new);
                        }
                    }
                    MapExecutor.dynamicPutAll(buildingIdAndCircuitIds, buildingId, circuitIdsInSpace, HashSet::new);
                }
            }
        });
        this.instanceIdAndCircuitIds.putAll(buildingIdAndCircuitIds);
        this.instanceIdRoomFuncTypeAndCircuitIds.putAll(buildingIdRoomFuncTypeAndCircuitIds);
    }

    /**
     * 填充需求配置
     *
     * @throws InterruptedException 线程中断
     * @since 7.250601.0
     */
    public void fillDemandConfig() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed) {
            return;
        }
        this.stopWatch.start(String.format("查询需求模版-%s", this.templateType));
        List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(this.templateType);
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(templateIds)) {
            this.failed = true;
            return;
        }
        this.stopWatch.start(String.format("查询需求配置-%s", this.templateType));
        DemandConfigByInstanceRepositoryBatchQueryParam queryParam =
                new DemandConfigByInstanceRepositoryBatchQueryParam();
        queryParam.setTemplateIds(templateIds);
        List<DemandConfig> demandConfigs = demandConfigRepository.listByInstances(queryParam);
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(demandConfigs)) {
            this.failed = true;
            return;
        }
        this.templateIdSceneIdAndConfigs = DemandConfig.toTemplateSceneIdAndConfigs(demandConfigs);
    }

    /**
     * 填充需求实例编组
     *
     * @throws InterruptedException 线程中断
     * @since 7.250602.0
     */
    public void fillDemandInstanceGroup() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed) {
            return;
        }
        this.stopWatch.start(String.format("查询需求实例编组-%s", this.templateType));
        DemandInstanceGroupQueryDaoParam param = new DemandInstanceGroupQueryDaoParam();
        // 只查询公区和停车场
        param.setTemplateType(this.templateType);
        param.setQueryDetail(true);
        List<DemandInstanceGroupPO> list = demandInstanceGroupResource.list(param);
        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        this.instanceGroups = list;
        this.groupIdInstanceGroupMap = list.stream().collect(Collectors.toMap(DemandInstanceGroupPO::getId, Function.identity(), (v1, v2) -> v2));

    }

    /**
     * 处理需求实例编组
     *
     * @throws InterruptedException 线程中断
     * @since 7.250602.0
     */
    public void handleDemandInstanceGroup() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed || CollectionUtils.isEmpty(this.circuits) || CollectionUtils.isEmpty(this.instanceGroups)) {
            return;
        }
        // 待删除详情id
        Set<Long> toDeleteDetailIds = Sets.newHashSet();
        // 待删除编组id
        Set<Long> toDeleteGroupIds = Sets.newHashSet();
        // 先拿到所有回路
        Set<String> circuitIds = this.circuitIdAndData.keySet();
        this.stopWatch.start(String.format("处理编组，拿到待删除id-%s", this.templateType));
        Iterator<DemandInstanceGroupPO> groupIterator = this.instanceGroups.iterator();
        while (groupIterator.hasNext()) {
            DemandInstanceGroupPO instanceGroup = groupIterator.next();
            String tag = instanceGroup.getTag();
            List<@NonNull DemandInstanceGroupDetail> details = instanceGroup.getDetails();
            Iterator<@NonNull DemandInstanceGroupDetail> detailIterator = details.iterator();
            while (detailIterator.hasNext()) {
                DemandInstanceGroupDetail detail = detailIterator.next();
                String instanceId = detail.getInstanceId();
                // 先判断回路id是否存在，如不存在，则移除
                if (!circuitIds.contains(instanceId)) {
                    toDeleteDetailIds.add(detail.getId());
                    detailIterator.remove();
                    continue;
                }
                DtpRwdCircuitDTO circuit = this.circuitIdAndData.get(instanceId);
                // 如果功能类型变更了，则移除
                if (!circuit.getFuncType().equals(tag)) {
                    toDeleteDetailIds.add(detail.getId());
                    detailIterator.remove();
                }
            }
            if (CollectionUtils.isEmpty(details)) {
                this.groupIdInstanceGroupMap.remove(instanceGroup.getId());
                toDeleteGroupIds.add(instanceGroup.getId());
                groupIterator.remove();
            }
        }
        this.stopWatch.stop();
        this.toBeDelGroupIds = toDeleteGroupIds;
        this.toBeDelGroupDetailIds = toDeleteDetailIds;
    }

    /**
     * 处理需求配置
     *
     * @throws InterruptedException 线程中断
     * @since 7.250601.0
     */
    public void handleDemandConfig() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed || MapUtils.isEmpty(this.templateIdSceneIdAndConfigs)) {
            this.failed = true;
            return;
        }
        DemandConfigAnalyzeStrategy<DemandConfigLightingValueObject> configAnalyzeStrategy =
                this.templateType.demandConfigAnalyzeStrategy();
        this.stopWatch.start(String.format("处理需求配置-%s", this.templateType));
        logProjectInstance().debug("开始处理需求配置", Pair.of("templateType", this.templateType));
        for (List<DemandConfig> configs : this.templateIdSceneIdAndConfigs.values()) {
            for (DemandConfig config : configs) {
                Long configId = config.getId();
                String instanceId = config.getDemandInstanceId();
                Long relGroupId = config.getRelGroupId();
                DemandConfigValueObject<DemandConfigLightingValueObject> configData =
                        configAnalyzeStrategy.analyzeConfig(config.getConfigData());
                if (Objects.isNull(configData)) {
                    logProjectInstance().warn("配置为空, 跳过执行", Arrays.asList(
                            Pair.of("templateType", this.templateType),
                            Pair.of("configId", configId)
                    ));
                    return;
                }
                Set<String> relCircuitIds = this.instanceIdAndCircuitIds.get(instanceId);
                Map<String, Set<String>> roomFuncTypeAndCircuitIds =
                        this.instanceIdRoomFuncTypeAndCircuitIds.get(instanceId);
                if (CollectionUtils.isEmpty(relCircuitIds)) {
                    logProjectInstance().warn("实例下无照明回路, 删除配置", Arrays.asList(
                            Pair.of("templateType", this.templateType),
                            Pair.of("instanceId", instanceId)
                    ));
                    this.toBeDelDemandConfigIdAndData.put(configId, config);
                    if (Objects.nonNull(relGroupId)) {
                        this.toBeDelDemandConfigGroupIdAndConfigId.put(relGroupId, configId);
                    }
                    continue;
                }
                // 处理全局配置
                logProjectInstance().debug("开始处理实例需求配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("instanceId", instanceId)
                ));
                // 全局配置是否被编辑, 为 null 时表示无效配置
                Boolean globalEdited = handleGlobalConfig(configId, instanceId, relCircuitIds, configData.getGlobalConfig());
                // 处理空间功能类型配置
                logProjectInstance().debug("开始处理空间功能类型配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("instanceId", instanceId)
                ));
                // 空间功能类型配置是否被编辑, 为 null 时表示无效配置
                Boolean spaceFuncTypeEdited = false;
                List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs =
                        configData.getSpaceFuncTypeConfigs();
                if (MapUtils.isNotEmpty(roomFuncTypeAndCircuitIds)) {
                    spaceFuncTypeEdited = handleSpaceFuncTypeConfigs(configId, instanceId,
                            spaceFuncTypeConfigs, roomFuncTypeAndCircuitIds);
                    // 存在配置时, 处理后为空, 表示无效配置
                    if (MapUtils.isEmpty(roomFuncTypeAndCircuitIds)) {
                        spaceFuncTypeEdited = null;
                    }
                }
                if (ObjectUtils.allNull(globalEdited, spaceFuncTypeEdited)) {
                    logProjectInstance().info("无有效需求配置, 删除配置", Arrays.asList(
                            Pair.of("templateType", this.templateType),
                            Pair.of("configId", configId)
                    ));
                    this.toBeDelDemandConfigIdAndData.put(configId, config);
                    if (Objects.nonNull(relGroupId)) {
                        this.toBeDelDemandConfigGroupIdAndConfigId.put(relGroupId, configId);
                    }
                } else if (BooleanUtils.isTrue(globalEdited) || BooleanUtils.isTrue(spaceFuncTypeEdited)) {
                    config.fillConfigData(configData);
                    this.toBeEditDemandConfigIdAndData.put(configId, config);
                }
            }
        }
        this.stopWatch.stop();
    }

    /**
     * 更新数据库
     *
     * @throws InterruptedException 线程中断
     * @since 7.250602.0
     */
    public void updateDatabase() throws InterruptedException {
        this.checkInterrupt();
        if (this.failed) {
            return;
        }
        boolean hasUpdate = false;
        if (CollectionUtils.isNotEmpty(this.toBeDelGroupIds)) {
            this.stopWatch.start(String.format("删除编组配置-%s", this.templateType));
            logProjectInstance().info("开始删除编组配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("ids", this.toBeDelGroupIds)
            ));
            demandInstanceGroupResource.del(this.toBeDelGroupIds);
            this.stopWatch.stop();
            hasUpdate = true;
        }
        this.checkInterrupt();
        if (CollectionUtils.isNotEmpty(this.toBeDelGroupDetailIds)) {
            this.stopWatch.start(String.format("删除编组详情数据-%s", this.templateType));
            logProjectInstance().info("开始删除编组详情数据", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("ids", this.toBeDelGroupDetailIds)
            ));
            demandInstanceGroupDetailResource.del(this.toBeDelGroupDetailIds);
            this.stopWatch.stop();
            hasUpdate = true;
        }
        this.checkInterrupt();
        if (MapUtils.isNotEmpty(this.toBeEditDemandConfigIdAndData)) {
            this.stopWatch.start(String.format("编辑需求配置-%s", this.templateType));
            logProjectInstance().info("开始编辑需求配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("size", this.toBeEditDemandConfigIdAndData.size()),
                    Pair.of("ids", this.toBeEditDemandConfigIdAndData.keySet())
            ));
            demandConfigService.updateByIdValidate(this.toBeEditDemandConfigIdAndData.values());
            this.stopWatch.stop();
            hasUpdate = true;
        }
        this.checkInterrupt();
        if (MapUtils.isNotEmpty(this.toBeDelDemandConfigIdAndData)) {
            this.stopWatch.start(String.format("删除需求配置-%s", this.templateType));
            logProjectInstance().info("开始删除需求配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("size", this.toBeDelDemandConfigIdAndData.size()),
                    Pair.of("ids", this.toBeDelDemandConfigIdAndData.keySet())
            ));
            demandConfigRepository.onlyDelConfigById(this.toBeDelDemandConfigIdAndData.keySet());
            this.stopWatch.stop();
            hasUpdate = true;
        }
        this.checkInterrupt();
        if (MapUtils.isNotEmpty(this.toBeDelDemandConfigGroupIdAndConfigId)) {
            this.stopWatch.start(String.format("删除需求配置分组-%s", this.templateType));
            logProjectInstance().info("开始删除需求配置分组", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("size", this.toBeDelDemandConfigGroupIdAndConfigId.size()),
                    Pair.of("ids", this.toBeDelDemandConfigGroupIdAndConfigId.keySet())
            ));
            demandConfigGroupRepository.del(this.toBeDelDemandConfigGroupIdAndConfigId.keySet());
            this.stopWatch.stop();
            hasUpdate = true;
        }
        if (!hasUpdate) {
            logProjectInstance().info("无数据更新", Pair.of("templateType", this.templateType));
        }
    }

    /**
     * 重新计算照明回路结果
     *
     * @throws InterruptedException 线程中断
     * @since 7.250602.0
     */
    public void computeResult() throws InterruptedException {
        this.checkInterrupt();
        List<Long> templateIds = demandTemplateRepository.listTemplateIdsByTemplateType(this.templateType);
        if (CollectionUtils.isEmpty(templateIds)) {
            logProjectInstance().info("模版不存在, 跳过重选配置结果", Pair.of("templateType", templateType));
            return;
        }
        DemandSceneQueryDaoParam queryDaoParam = new DemandSceneQueryDaoParam();
        queryDaoParam.setRelTemplateIds(templateIds);
        queryDaoParam.addColumns(BaseAssignIdDO::getId, DemandScene::getRelTemplateId);
        Map<Long, Set<Long>> templateIdAndSceneIds = demandSceneRepository.list(queryDaoParam).stream()
                .collect(Collectors.groupingBy(DemandSceneEntity::getRelTemplateId,
                        Collectors.mapping(DemandSceneEntity::getId, Collectors.toSet())));
        if (MapUtils.isEmpty(templateIdAndSceneIds)) {
            logProjectInstance().info("模版下场景不存在, 跳过重选配置结果", Arrays.asList(
                    Pair.of("templateType", templateType),
                    Pair.of("templateIds", templateIds)
            ));
            return;
        }
        this.stopWatch.start(String.format("重算配置结果-%s", this.templateType));
        logProjectInstance().info("开始重算配置结果", Pair.of("templateType", templateType));
        for (Map.Entry<Long, Set<Long>> entry : templateIdAndSceneIds.entrySet()) {
            Long templateId = entry.getKey();
            Set<Long> sceneIds = entry.getValue();
            for (Long sceneId : sceneIds) {
                DemandConfigLightingComputeParam computeParam = new DemandConfigLightingComputeParam();
                computeParam.setTemplateId(templateId);
                computeParam.setSceneId(sceneId);
                demandConfigLightingComputeNowService.resultCompute(computeParam);
            }
        }
        this.stopWatch.stop();
    }

    /**
     * 检查线程是否中断
     *
     * @throws InterruptedException 线程中断
     * @since 7.250602.0
     */
    public void checkInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            logProjectInstance().warn("检测到线程已被中断", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("eventMessage", this.eventMessage)
            ));
            throw new InterruptedException();
        }
    }

    /**
     * 处理空间类型配置
     *
     * @param configId                  配置ID
     * @param instanceId                实例ID
     * @param spaceFuncTypeConfigs      空间功能类型配置
     * @param roomFuncTypeAndCircuitIds 当前实例关联照明回路
     * @return 是否修改
     * @since 7.250601.0
     */
    private Boolean handleSpaceFuncTypeConfigs(
            @NonNull Long configId,
            @NonNull String instanceId,
            @NonNull List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs,
            @NonNull Map<String, Set<String>> roomFuncTypeAndCircuitIds) {
        if (MapUtils.isEmpty(roomFuncTypeAndCircuitIds)) {
            logProjectInstance().info("空间功能类型下无照明回路, 清空空间功能类型配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("configId", configId)
            ));
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                spaceFuncTypeConfigs.stream()
                        .map(DemandSpaceFuncTypeConfig::getRelConfigGroupId)
                        .filter(Objects::nonNull)
                        .forEach(item -> this.toBeDelDemandConfigGroupIdAndConfigId.put(item, configId));
                spaceFuncTypeConfigs.clear();
            }
            return null;
        }
        if (CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
            return false;
        }
        boolean edited = false;
        Iterator<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>>
                spaceFuncTypeConfigIterator = spaceFuncTypeConfigs.iterator();
        while (spaceFuncTypeConfigIterator.hasNext()) {
            DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>
                    spaceFuncTypeConfig = spaceFuncTypeConfigIterator.next();
            List<DtpKnowledgeInfoCodeDataSourceDTO> roomFuncTypes = spaceFuncTypeConfig.getFuncTypes();
            DemandConfigLightingValueObject roomFuncTypeConfig = spaceFuncTypeConfig.getConfig();
            Long relConfigGroupId = spaceFuncTypeConfig.getRelConfigGroupId();
            if (CollectionUtils.isEmpty(roomFuncTypes)) {
                logProjectInstance().warn("空间功能类型为空, 删除当前配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId)
                ));
                if (Objects.nonNull(relConfigGroupId)) {
                    this.toBeDelDemandConfigGroupIdAndConfigId.put(relConfigGroupId, configId);
                }
                spaceFuncTypeConfigIterator.remove();
                edited = true;
                continue;
            }
            if (Objects.isNull(roomFuncTypeConfig)) {
                logProjectInstance().warn("空间功能类型配置为空, 删除当前配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("roomFuncTypes", roomFuncTypes)
                ));
                if (Objects.nonNull(relConfigGroupId)) {
                    this.toBeDelDemandConfigGroupIdAndConfigId.put(relConfigGroupId, configId);
                }
                spaceFuncTypeConfigIterator.remove();
                edited = true;
                continue;
            }
            logProjectInstance().debug("开始处理空间功能类型配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("configId", configId),
                    Pair.of("roomFuncTypes", roomFuncTypes)
            ));
            // 当前空间功能类型配置中所有的照明回路ID
            Set<String> allRelCircuitIds = new HashSet<>();
            Iterator<DtpKnowledgeInfoCodeDataSourceDTO> roomFuncTypeIterator = roomFuncTypes.iterator();
            while (roomFuncTypeIterator.hasNext()) {
                String roomFuncType = String.valueOf(roomFuncTypeIterator.next().getCode());
                // 空间功能类型下的所有照明回路
                Set<String> relCircuitIds = roomFuncTypeAndCircuitIds.get(roomFuncType);
                if (CollectionUtils.isEmpty(relCircuitIds)) {
                    logProjectInstance().info("空间功能类型配置中无照明回路, 从配置中删除空间功能类型配置", Arrays.asList(
                            Pair.of("templateType", this.templateType),
                            Pair.of("configId", configId),
                            Pair.of("roomFuncType", roomFuncType)
                    ));
                    roomFuncTypeIterator.remove();
                    edited = true;
                    continue;
                }
                allRelCircuitIds.addAll(relCircuitIds);
            }
            // 处理空间功能类型配置
            Boolean configEdited = handleGlobalConfig(configId, instanceId, allRelCircuitIds, roomFuncTypeConfig);
            if (Objects.isNull(configEdited)) {
                logProjectInstance().info("无有效空间功能类型配置, 删除当前配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("roomFuncTypes", roomFuncTypes)
                ));
                if (Objects.nonNull(relConfigGroupId)) {
                    this.toBeDelDemandConfigGroupIdAndConfigId.put(relConfigGroupId, configId);
                }
                spaceFuncTypeConfigIterator.remove();
                edited = true;
                continue;
            }
            edited = edited || configEdited;
        }
        if (CollectionUtils.isEmpty(spaceFuncTypeConfigs)) {
            return null;
        }
        return edited;
    }

    /**
     * 处理全局配置
     *
     * @param configId      配置ID
     * @param instanceId    实例ID
     * @param relCircuitIds 关联照明回路
     * @param globalConfig  全局配置
     * @return 处理后的全局配置
     * @since 7.250801.0
     */
    private Boolean handleGlobalConfig(
            @NonNull Long configId,
            @NonNull String instanceId,
            @NonNull Set<String> relCircuitIds,
            @Nullable DemandConfigLightingValueObject globalConfig) {
        if (Objects.isNull(globalConfig)) {
            return null;
        }
        List<DemandConfigLightingFuncTypeValueObject> globalSettings = globalConfig.getGlobalSettings();
        Map<String, List<DemandConfigLightingFuncTypeValueObject>> instanceIdAndFeedbackAdjustSettings =
                globalConfig.getInstanceIdAndFeedbackAdjustSettings();
        // 全局配置和空间反馈调节配置都为空时不处理
        if (Objects.isNull(globalSettings) && MapUtils.isEmpty(instanceIdAndFeedbackAdjustSettings)) {
            return false;
        }
        Boolean globalSettingEdited = false;
        Boolean feedbackAdjustingEdited = false;
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            globalSettingEdited = handleGlobalSettings(configId, globalSettings, relCircuitIds);
        }
        if (MapUtils.isNotEmpty(instanceIdAndFeedbackAdjustSettings)) {
            feedbackAdjustingEdited = handleSpaceIdAndFeedbackAdjustSettings(configId, instanceId,
                    instanceIdAndFeedbackAdjustSettings, relCircuitIds);
        }
        if (Objects.isNull(globalSettingEdited)) {
            globalConfig.setGlobalSettings(null);
        }
        if (Objects.isNull(feedbackAdjustingEdited)) {
            globalConfig.setInstanceIdAndFeedbackAdjustSettings(null);
        }
        // 两个配置任意一个不为空, 但处理后都为空时此配置无效时删除当前配置
        if (ObjectUtils.allNull(globalSettingEdited, feedbackAdjustingEdited)) {
            return null;
        }
        return BooleanUtils.isNotFalse(globalSettingEdited) || BooleanUtils.isNotFalse(feedbackAdjustingEdited);
    }

    /**
     * 处理空间反馈调节配置
     *
     * @param configId                         配置ID
     * @param spaceId                          空间ID
     * @param spaceIdAndFeedbackAdjustSettings 空间ID -> 反馈调节配置
     * @param relCircuitIds                    当前实例关联照明回路
     * @return 是否修改
     * @since 7.250801.0
     */
    private Boolean handleSpaceIdAndFeedbackAdjustSettings(
            @NonNull Long configId,
            @NonNull String spaceId,
            @Nullable Map<String, List<DemandConfigLightingFuncTypeValueObject>> spaceIdAndFeedbackAdjustSettings,
            @NonNull Set<String> relCircuitIds) {
        if (MapUtils.isEmpty(spaceIdAndFeedbackAdjustSettings)) {
            return false;
        }
        // 当前空间不支持反馈调节时, 清空反馈调节配置
        if (CollectionUtils.isEmpty(relCircuitIds)
                || CollectionUtils.isEmpty(this.allowFeedbackAdjustSpaceIds)
                || !this.allowFeedbackAdjustSpaceIds.contains(spaceId)) {
            logProjectInstance().info("无有效反馈调节配置, 删除当前配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("configId", configId),
                    Pair.of("spaceId", spaceId)
            ));
            spaceIdAndFeedbackAdjustSettings.clear();
            return true;
        }
        boolean edited = false;
        // 当前空间反馈调节配置中包含其他空间配置时, 同步移除不支持反馈调节的空间配置
        Iterator<Map.Entry<String, List<DemandConfigLightingFuncTypeValueObject>>> iterator =
                spaceIdAndFeedbackAdjustSettings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<DemandConfigLightingFuncTypeValueObject>> entry = iterator.next();
            if (!this.allowFeedbackAdjustSpaceIds.contains(entry.getKey())) {
                iterator.remove();
                edited = true;
            }
        }
        // 处理当前空间的反馈调节配置
        List<DemandConfigLightingFuncTypeValueObject> feedbackAdjustSettings =
                spaceIdAndFeedbackAdjustSettings.get(spaceId);
        if (CollectionUtils.isEmpty(feedbackAdjustSettings)) {
            return edited;
        }
        Boolean globalSettingsEdited = handleGlobalSettings(configId, feedbackAdjustSettings, relCircuitIds);
        // 当前空间反馈调节配置无效时, 清空该空间配置
        if (Objects.isNull(globalSettingsEdited)) {
            feedbackAdjustSettings.clear();
            return true;
        }
        return edited || globalSettingsEdited;
    }

    /**
     * 处理全局配置
     *
     * @param globalSettings 全局配置
     * @param configId       配置ID
     * @param relCircuitIds  当前实例关联照明回路
     * @return 是否修改
     * @since 7.250601.0
     */
    private Boolean handleGlobalSettings(
            @NonNull Long configId,
            @Nullable List<DemandConfigLightingFuncTypeValueObject> globalSettings,
            @NonNull Set<String> relCircuitIds) {
        if (CollectionUtils.isEmpty(relCircuitIds)) {
            logProjectInstance().info("实例下无照明回路, 清空全局配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("configId", configId)
            ));
            if (CollectionUtils.isNotEmpty(globalSettings)) {
                globalSettings.clear();
            }
            return null;
        }
        if (CollectionUtils.isEmpty(globalSettings)) {
            return false;
        }
        boolean edited = false;
        Iterator<DemandConfigLightingFuncTypeValueObject> iterator = globalSettings.iterator();
        while (iterator.hasNext()) {
            DemandConfigLightingFuncTypeValueObject globalSetting = iterator.next();
            String funcType = globalSetting.getFuncType();
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = globalSetting.getCircuitGroupSettings();
            List<DemandConfigLightingCircuitValueObject> circuitSettings = globalSetting.getCircuitSettings();
            logProjectInstance().debug("开始处理照明回路功能类型配置", Arrays.asList(
                    Pair.of("templateType", this.templateType),
                    Pair.of("configId", configId),
                    Pair.of("funcType", funcType)
            ));
            // 处理编组配置
            if (CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                Iterator<DemandLightingCircuitGroupSettingDTO> groupIterator = circuitGroupSettings.iterator();
                while (groupIterator.hasNext()) {
                    DemandLightingCircuitGroupSettingDTO groupSetting = groupIterator.next();
                    Long groupId = groupSetting.getGroupId();
                    DemandInstanceGroupPO group = this.groupIdInstanceGroupMap.get(groupId);
                    if (Objects.isNull(group)) {
                        logProjectInstance().info("编组已被删除, 从功能类型配置中删除编组配置", Arrays.asList(
                                Pair.of("templateType", this.templateType),
                                Pair.of("configId", configId),
                                Pair.of("funcType", funcType),
                                Pair.of("groupId", groupId)
                        ));
                        groupIterator.remove();
                        edited = true;
                    }
                }
            }
            // 处理照明回路配置
            if (CollectionUtils.isNotEmpty(circuitSettings)) {
                edited = edited | handleExistCircuitSettings(configId, funcType, circuitSettings, relCircuitIds);
            }
            try {
                globalSetting.checkAndSet();
            } catch (ValidationException e) {
                logProjectInstance().warn("配置参数校验失败, 移除配置", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("funcType", funcType),
                        Pair.of("error", e.getMessage())
                ));
                iterator.remove();
            }
        }
        if (CollectionUtils.isEmpty(globalSettings)) {
            return null;
        }
        return edited;
    }

    /**
     * 处理已存在的照明回路配置
     *
     * @param configId        配置ID
     * @param funcType        功能类型
     * @param circuitSettings 照明回路配置
     * @param relCircuitIds   当前实例关联的所有照明回路
     * @return 是否修改
     * @since 7.250601.0
     */
    private boolean handleExistCircuitSettings(
            @NonNull Long configId,
            @NonNull String funcType,
            @NonNull List<DemandConfigLightingCircuitValueObject> circuitSettings,
            @NonNull Set<String> relCircuitIds) {
        if (CollectionUtils.isEmpty(circuitSettings)) {
            return false;
        }
        boolean edited = false;
        Iterator<DemandConfigLightingCircuitValueObject> iterator = circuitSettings.iterator();
        while (iterator.hasNext()) {
            DemandConfigLightingCircuitValueObject circuitSetting = iterator.next();
            String circuitId = circuitSetting.getCircuitId();
            DtpRwdCircuitDTO circuit = this.circuitIdAndData.get(circuitId);
            // 当照明回路在最新交付中不存在时进行移除
            if (!relCircuitIds.contains(circuitId) || Objects.isNull(circuit)) {
                logProjectInstance().info("照明回路已在交付数据中删除", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("funcType", funcType),
                        Pair.of("circuitId", circuitId)
                ));
                iterator.remove();
                edited = true;
                continue;
            }
            String newFuncType = circuit.getFuncType();
            // 当前照明回路不属于这个功能类型时移除
            if (!StringUtils.equals(funcType, newFuncType)) {
                logProjectInstance().info("照明回路功能类型已变更", Arrays.asList(
                        Pair.of("templateType", this.templateType),
                        Pair.of("configId", configId),
                        Pair.of("funcType", funcType),
                        Pair.of("circuitId", circuitId),
                        Pair.of("newFuncType", newFuncType)
                ));
                iterator.remove();
                edited = true;
            }
        }
        return edited;
    }

    @Override
    public String logTag() {
        return "照明需求-DTX交付变更";
    }
}
