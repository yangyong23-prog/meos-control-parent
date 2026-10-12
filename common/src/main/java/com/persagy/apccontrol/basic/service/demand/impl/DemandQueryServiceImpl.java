package com.persagy.apccontrol.basic.service.demand.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.model.vo.demand.*;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.service.demand.DemandQueryService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandCustomizedInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandCustomizedInstanceDTO;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.cls.PropertyDescriptorUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.vo.demand.DemandTreeSpaceRoomFuncTypeVO.buildTreeSpaceRoomFuncType;
import static com.persagy.apccontrol.basic.model.vo.demand.DemandTreeSpaceRoomFuncTypeVO.pruneTree;

/**
 * 需求查询服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandQueryServiceImpl implements DemandQueryService, LogComponent {

    private static final String LOG_TAG = "需求服务";

    private final DemandConverter demandConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final MidPermissionClientService midPermissionClientService;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandConfigGateway<BaseDemandConfig> demandConfigGateway;
    private final DemandInstanceGateway demandInstanceGateway;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;

    @Override
    public String getTemplateTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull BaseDemandTemplateParam param) {
        param.checkAndSet();
        String templateTag = param.getTemplateTag();
        LocalDate date = param.getDate();

        if (Objects.nonNull(templateTag)) {
            return templateTag;
        }
        if (Objects.nonNull(date)) {
            DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(templateType, date);
            if (Objects.nonNull(dateSetting)) {
                return dateSetting.getRelTemplate().getTemplateTag();
            }
        }
        return null;
    }

    @Override
    public DemandTreeControlledObjectVO demandTree(@NonNull DemandTreeQueryParam param) {
        //数据校验
        param.check();
        DemandTreeControlledObjectVO objectVO = new DemandTreeControlledObjectVO(param);
        //构建空间树结构
        DemandStrategy strategy = param.getTemplateType().toDemandTypeEnum().demandStrategy();
        ProjectDTO project = midPermissionClientService.getProject(false, true, ContextUtil.getProjectId());
        // 添加顶级项目信息节点并构建其子节点（空间树结构）
        DemandTreeObjectVO vo = buildProjectNode(project);
        List<DtpRwdSpaceDTO> dtpRwdSpaceList = getDtpRwdSpaceDTOS(strategy, param);
        List<DemandTreeObjectVO> children = buildSpaceTreeChildren(dtpRwdSpaceList);
        vo.setChildren(children);
        List<DemandTreeObjectVO> volist = new ArrayList<>();
        volist.add(vo);
        if (param.getAllMsg() && children != null) {
            setDemandTreeObjectCfgVO(param, volist, dtpRwdSpaceList);
            volist.forEach(i -> i.getInstanceTag().buildTotalCustomizedSetting(i));
            setCustomizedTags(param, volist);
        }
        objectVO.setTree(volist.get(0));
        return objectVO;
    }

    @Override
    public R<DemandTreeInstanceProjectVO> treeDemandInstance(@NonNull DemandTreeInstanceQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        boolean querySpace = param.isQuerySpace();
        Set<String> serviceSystemIds = param.getServiceSystemIds();
        boolean queryCustomizedTag = param.isQueryCustomizedTag();
        boolean filterInShieldedSpace = param.isFilterInShieldedSpace();
        boolean filterInBlacklistedSpace = param.isFilterInBlacklistedSpace();
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns =
                fillDefaultWithColumns(param.getInstanceTypeAndWithColumns());

        ProjectDTO project = midPermissionClientService.getProject();
        Set<String> floorIds = Collections.emptySet();
        List<DtpRwdSpaceDTO> spaces = Collections.emptyList();
        if (querySpace) {
            DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
            DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
            queryParam.setWithColumns(instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.SPACE));
            if (CollectionUtils.isNotEmpty(serviceSystemIds)) {
                queryParam.setFilterSpaceServiceSystemIds(serviceSystemIds);
            }
            spaces = demandStrategy.listDemandSpace(queryParam);
            if (CollectionUtils.isNotEmpty(spaces)) {
                if (filterInShieldedSpace || filterInBlacklistedSpace) {
                    Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                    Set<String> toBeDelSpaceIds = querySpaceIdsInInstanceConfig(templateType, spaceIds,
                            new DemandInstanceConfigValueObject()
                                    .setShielded(filterInShieldedSpace ? true : null)
                                    .setInBlacklisted(filterInBlacklistedSpace ? true : null));
                    spaces.removeIf(item -> toBeDelSpaceIds.contains(item.getId()));
                }
                if (CollectionUtils.isNotEmpty(serviceSystemIds)) {
                    DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
                    spaceQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjFrom(serviceSystemIds));
                    spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                    Map<String, Set<String>> spaceIdAndServiceSystemIds = DtpRwdSpaceDTO.mapIdAndRelObjIds(
                            dtpRwdClientService.listSpace(spaceQueryParam));
                    spaces.forEach(item -> {
                        item.setRelObjIds(null);
                        item.setUndefinedProperties(DtpRwdSpaceDTO.CUSTOM_INFO_CODE_SERVICE_SYSTEM_IDS,
                                spaceIdAndServiceSystemIds.getOrDefault(item.getId(), Collections.emptySet()));
                    });
                }
                floorIds = spaces.stream().map(DtpRwdSpaceDTO::getFloorId).collect(Collectors.toSet());
            }
        } else {
            floorIds = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                            .addWithColumn(DtpRwdObjectDTO.Fields.id)).stream()
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(floorIds)) {
            return R.success(DemandTreeInstanceProjectVO.ofEmpty(project));
        }
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                .setId(floorIds).setWithColumns(instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.FLOOR)));
        Set<String> buildingIds = floors.stream().map(DtpRwdFloorDTO::getBuildingId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(buildingIds)) {
            return R.success(DemandTreeInstanceProjectVO.ofEmpty(project));
        }
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(new DtpRwdObjectQueryParam()
                .setId(buildingIds).setWithColumns(instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.BUILDING)));
        if (CollectionUtils.isEmpty(buildings)) {
            return R.success(DemandTreeInstanceProjectVO.ofEmpty(project));
        }
        if (queryCustomizedTag) {
            Set<DemandInstanceValueObject> allInstances = spaces.stream()
                    .map(item -> new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, item.getId()))
                    .collect(Collectors.toSet());
            allInstances.addAll(floors.stream()
                    .map(item -> new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR, item.getId()))
                    .collect(Collectors.toSet()));
            allInstances.addAll(buildings.stream()
                    .map(item -> new DemandInstanceValueObject(DemandInstanceTypeEnum.BUILDING, item.getId()))
                    .collect(Collectors.toSet()));
            DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
            List<DemandInstanceCustomizedTagDTO> demandInstanceCustomizedTags = demandStrategy
                    .mapDemandInstanceCustomizedTagNameAndValue(templateType, allInstances);
            if (CollectionUtils.isNotEmpty(demandInstanceCustomizedTags)) {
                Map<String, Map<String, Object>> instanceIdAndTags = demandInstanceCustomizedTags.stream()
                        .collect(Collectors.toMap(item -> item.getInstance().getInstanceId(),
                                DemandInstanceCustomizedTagDTO::getCustomizedTag));
                spaces.forEach(item -> Optional.ofNullable(instanceIdAndTags.get(item.getId()))
                        .ifPresent(tags -> tags.forEach(item::setUndefinedProperties)));
                floors.forEach(item -> Optional.ofNullable(instanceIdAndTags.get(item.getId()))
                        .ifPresent(tags -> tags.forEach(item::setUndefinedProperties)));
                buildings.forEach(item -> Optional.ofNullable(instanceIdAndTags.get(item.getId()))
                        .ifPresent(tags -> tags.forEach(item::setUndefinedProperties)));
            }
        }
        return R.success(DemandTreeInstanceProjectVO.of(project, buildings, floors, spaces));
    }

    @Override
    public R<Collection<DemandInstanceConfigProjectVO>> listInstanceConfig(
            @NonNull DemandInstanceConfigViewQueryParam param) {
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns =
                fillDefaultWithColumns(param.getInstanceTypeAndWithColumns());
        DemandInstanceConfigQueryParam instanceConfigQueryParam = new DemandInstanceConfigQueryParam();
        instanceConfigQueryParam.setDate(param.getDate());
        instanceConfigQueryParam.setTemplateType(param.getTemplateType());
        instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        List<DemandInstanceConfigEntity> instanceConfigs = demandInstanceGateway
                .listInstanceConfigs(instanceConfigQueryParam);
        if (CollectionUtils.isEmpty(instanceConfigs)) {
            return R.success(null);
        }
        Set<String> spaceIds = instanceConfigs.stream()
                .map(item -> item.getIdentifier().getInstance().getInstanceId())
                .collect(Collectors.toSet());
        List<DtpRwdObjectDTO> spaces = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setId(spaceIds).setWithColumns(instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.SPACE)));
        if (CollectionUtils.isEmpty(spaces)) {
            return R.success(null);
        }
        List<DemandInstanceConfigProjectVO> result = DemandInstanceConfigProjectVO.of(
                null, null, null, spaces, instanceConfigs);
        return R.successMulti(result, (long) result.size());
    }

    @Override
    public R<Collection<DemandSpaceInFloorVO>> listDemandSpaceInFloor(@NonNull DemandSpaceInFloorQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String floorId = param.getFloorId();

        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns = new HashMap<>();
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.SPACE, param.getWithColumns());
        Set<String> withColumns = fillDefaultWithColumns(instanceTypeAndWithColumns).get(DemandInstanceTypeEnum.SPACE);

        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpaceByInstance(templateType,
                new DemandInstanceValueObject(DemandInstanceTypeEnum.FLOOR, floorId), withColumns.toArray(new String[0]));
        if (CollectionUtils.isEmpty(spaces)) {
            return R.successMulti(Collections.emptyList());
        }
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Set<String> inBlacklistedSpaceIds = querySpaceIdsInInstanceConfig(templateType, spaceIds,
                new DemandInstanceConfigValueObject().setInBlacklisted(true));
        if (CollectionUtils.isNotEmpty(inBlacklistedSpaceIds)) {
            spaces.removeIf(item -> inBlacklistedSpaceIds.contains(item.getId()));
        }
        List<DemandSpaceInFloorVO> result = spaces.stream()
                .map(demandConverter::toSpaceInFloorVo)
                .collect(Collectors.toList());
        return R.successMulti(result, (long) result.size());
    }

    @Override
    public R<Collection<DemandCustomizedConfigInstanceVO>> listCustomizedConfigInstance(
            @NonNull DemandCustomizedConfigInstanceQueryParam param) {
        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            return R.successMulti(Collections.emptyList());
        }
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns =
                fillDefaultWithColumns(param.getInstanceTypeAndWithColumns());
        DemandCustomizedInstanceQueryParam queryParam = new DemandCustomizedInstanceQueryParam();
        queryParam.setDate(param.getDate());
        queryParam.setTemplateId(template.getId());
        queryParam.setSceneId(param.getSceneId());
        List<DemandCustomizedInstanceDTO> customizedInstances = demandConfigGateway.listCustomizedInstances(queryParam);
        if (CollectionUtils.isEmpty(customizedInstances)) {
            return R.successMulti(Collections.emptyList());
        }
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndIds = new TreeMap<>(
                Comparator.comparing(DemandInstanceTypeEnum::getLevel));
        Map<String, DemandInstanceTypeEnum> instanceIdAndType = new HashMap<>();
        for (DemandCustomizedInstanceDTO customizedInstance : customizedInstances) {
            instanceIdAndType.put(customizedInstance.getInstanceId(), customizedInstance.getInstanceType());
            MapExecutor.dynamicPut(instanceTypeAndIds, customizedInstance.getInstanceType(),
                    customizedInstance.getInstanceId(), HashSet::new);
        }
        List<DemandCustomizedConfigInstanceVO> result = new ArrayList<>();
        for (Map.Entry<DemandInstanceTypeEnum, Set<String>> entry : instanceTypeAndIds.entrySet()) {
            DemandInstanceTypeEnum instanceType = entry.getKey();
            Set<String> instanceIds = entry.getValue();

            List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                    .setId(instanceIds).setWithColumns(instanceTypeAndWithColumns.get(instanceType)));
            result.addAll(objects.stream()
                    .map(item -> demandConverter.toCustomizedConfigInstanceVo(item, instanceIdAndType.get(item.getId())))
                    .collect(Collectors.toList()));
        }
        return R.successMulti(result, (long) result.size());
    }

    @Override
    public R<Collection<DemandReferenceParentSpaceInFloorVO>> listDemandReferenceParentSpaceByInFloor(
            @NonNull DemandReferenceParentSpaceInFloorQueryParam param) {
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns = new HashMap<>();
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.SPACE, param.getWithColumns());
        param.setWithColumns(fillDefaultWithColumns(instanceTypeAndWithColumns).get(DemandInstanceTypeEnum.SPACE));
        List<DtpRwdSpaceDTO> referenceParentSpaces = demandConfigGateway.listReferenceParentSpaces(param);
        List<DemandReferenceParentSpaceInFloorVO> result = referenceParentSpaces.stream()
                .map(demandConverter::toReferenceParentSpaceInFloorVo)
                .collect(Collectors.toList());
        return R.successMulti(result, (long) result.size());
    }

    @Override
    public List<DtpRwdObjectDTO> listControlledObject(@NonNull DemandObjectQueryParam param) {
        List<String> column = param.getWithColumns();

        DtpRwdObjectQueryParam newQueryParam = new DtpRwdObjectQueryParam();
        newQueryParam.setWithColumns(column);
        DemandStrategy strategy = param.getTemplateType().toDemandTypeEnum().demandStrategy();
        if (CollectionUtils.isNotEmpty(column) &&
                (!column.contains(DtpRwdSpaceDTO.Fields.floorId) || !column.contains(DtpRwdSpaceDTO.Fields.buildingId))) {
            column.add(DtpRwdSpaceDTO.Fields.floorId);
            column.add(DtpRwdSpaceDTO.Fields.buildingId);
        }
        // 先查询空间信息
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(param.getTemplateType());
        queryParam.queryOnlyControlled();
        queryParam.addWithColumn(CollectionUtils.isNotEmpty(column) ? column.toArray(new String[0]) : null);
        List<DtpRwdSpaceDTO> dtpRwdSpaceList = strategy.listDemandSpace(queryParam);
        if (dtpRwdSpaceList.isEmpty()) {
            return new ArrayList<>();
        }
        // 遍历出建筑楼层 id
        Set<String> floorIds = dtpRwdSpaceList.stream()
                .map(DtpRwdSpaceDTO::getFloorId)
                .collect(Collectors.toSet());
        Set<String> buildingIds = dtpRwdSpaceList.stream()
                .map(DtpRwdSpaceDTO::getBuildingId)
                .collect(Collectors.toSet());

        DtpRwdObjectQueryParam queryParamForObjects = new DtpRwdObjectQueryParam();
        queryParamForObjects.setWithColumns(column);

        if (CollectionUtils.isNotEmpty(param.getFilterInstanceList())) {
            return getDtpRwdObjectDTOAsync(param.getFilterInstanceList(), dtpRwdSpaceList, queryParamForObjects, floorIds, buildingIds);
        } else {
            // 全量查询
            List<DemandObjectQueryParam.FilterInstance> filterInstance = new ArrayList<>();
            filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.SPACE));
            filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.PROJECT));
            filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.BUILDING));
            filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.FLOOR));
            return getDtpRwdObjectDTOAsync(filterInstance, dtpRwdSpaceList, queryParamForObjects, floorIds, buildingIds);
        }
    }

    /**
     * 查询实例配置中的空间ID
     *
     * @param templateType 模版类型
     * @param spaceIds     空间ID, 为空时查询所有
     * @param filterConfig 过滤配置, 为空时查询所有
     * @return 实例配置中的空间ID
     * @since 5.241102.0
     */
    private Set<String> querySpaceIdsInInstanceConfig(
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Set<String> spaceIds,
            @Nullable DemandInstanceConfigValueObject filterConfig) {
        DemandInstanceConfigQueryParam instanceConfigQueryParam = new DemandInstanceConfigQueryParam();
        instanceConfigQueryParam.setTemplateType(templateType);
        instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        instanceConfigQueryParam.setInstanceIds(spaceIds);
        instanceConfigQueryParam.setFilterConfig(filterConfig);
        return demandInstanceGateway.listInstanceIds(instanceConfigQueryParam);
    }

    /**
     * 填充默认查询字段
     *
     * @since 5.241102.0
     */
    private Map<DemandInstanceTypeEnum, Set<String>> fillDefaultWithColumns(
            @Nullable Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns) {
        Supplier<Set<String>> defaultWithColumns = () -> Sets.newHashSet(
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName);
        if (MapUtils.isEmpty(instanceTypeAndWithColumns)) {
            instanceTypeAndWithColumns = new HashMap<>();
        }
        Set<String> projectWithColumns = instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.PROJECT);
        Set<String> buildingWithColumns = instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.BUILDING);
        Set<String> floorWithColumns = instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.FLOOR);
        Set<String> spaceWithColumns = instanceTypeAndWithColumns.get(DemandInstanceTypeEnum.SPACE);
        if (CollectionUtils.isEmpty(projectWithColumns)) {
            projectWithColumns = defaultWithColumns.get();
        }
        if (CollectionUtils.isEmpty(buildingWithColumns)) {
            buildingWithColumns = defaultWithColumns.get();
        }
        if (CollectionUtils.isEmpty(floorWithColumns)) {
            floorWithColumns = defaultWithColumns.get();
        }
        if (CollectionUtils.isEmpty(spaceWithColumns)) {
            spaceWithColumns = defaultWithColumns.get();
        }
        buildingWithColumns.add(DtpRwdBuildingDTO.Fields.buildingSequenceId);
        floorWithColumns.add(DtpRwdFloorDTO.Fields.buildingId);
        floorWithColumns.add(DtpRwdFloorDTO.Fields.floorSequenceId);
        spaceWithColumns.add(DtpRwdSpaceDTO.Fields.floorId);
        spaceWithColumns.add(DtpRwdSpaceDTO.Fields.buildingId);
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.PROJECT, projectWithColumns);
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.BUILDING, buildingWithColumns);
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.FLOOR, floorWithColumns);
        instanceTypeAndWithColumns.put(DemandInstanceTypeEnum.SPACE, spaceWithColumns);
        return instanceTypeAndWithColumns;
    }

    /**
     * 并发查询
     * 查询建筑楼层项目对象信息
     */
    public List<DtpRwdObjectDTO> getDtpRwdObjectDTOAsync(List<DemandObjectQueryParam.FilterInstance> filterInstance,
                                                         List<DtpRwdSpaceDTO> dtpRwdSpaceList,
                                                         DtpRwdObjectQueryParam queryParamForObjects,
                                                         Set<String> floorIds,
                                                         Set<String> buildingIds) {

        List<DtpRwdObjectDTO> dtpRwdObjectDTO = new ArrayList<>();
        CompletableFuture<List<DtpRwdObjectDTO>> futureResult = CompletableFuture.supplyAsync(() -> {
            for (DemandObjectQueryParam.FilterInstance filterList : filterInstance) {
                filterList.check();
                DemandInstanceTypeEnum instanceType = filterList.getFilterInstanceType();
                Set<String> filterInstanceIds = filterList.getFilterInstanceIds();
                switch (instanceType) {
                    case SPACE:
                        //按照 ID 筛选出空间
                        List<DtpRwdSpaceDTO> spaceList = dtpRwdSpaceList;
                        if (CollectionUtils.isNotEmpty(filterInstanceIds)) {
                            spaceList = dtpRwdSpaceList.stream().filter(i -> filterInstanceIds.contains(i.getId())).collect(Collectors.toList());
                        }
                        dtpRwdObjectDTO.addAll(spaceList);
                        break;
                    case PROJECT:
                        queryParamForObjects.setId(ContextUtil.getProjectId());
                        if (setIdQueryParam(queryParamForObjects, Collections.singleton(ContextUtil.getProjectId()), filterInstanceIds))
                            break;
                        DtpRwdObjectDTO project = dtpRwdClientService.getObject(false, false, ContextUtil.getProjectId(), queryParamForObjects);
                        dtpRwdObjectDTO.add(project);
                        break;
                    case BUILDING:
                        //取交集
                        if (setIdQueryParam(queryParamForObjects, buildingIds, filterInstanceIds)) break;
                        CompletableFuture<List<DtpRwdBuildingDTO>> buildingFuture = CompletableFuture.supplyAsync(() ->
                                        dtpRwdClientService.listBuilding(false, false, ContextUtil.getProjectId(), queryParamForObjects)
                                , BusinessThreadPoolExecutor.ttlExecutor());
                        try {
                            List<DtpRwdBuildingDTO> buildingList = buildingFuture.get();
                            dtpRwdObjectDTO.addAll(buildingList);
                        } catch (Exception e) {
                            logProjectInstance().error("建筑查询失败", e,
                                    Pair.of("queryParamForObjects", queryParamForObjects));
                        }
                        break;
                    case FLOOR:
                        if (setIdQueryParam(queryParamForObjects, floorIds, filterInstanceIds)) break;
                        CompletableFuture<List<DtpRwdFloorDTO>> floorFuture = CompletableFuture.supplyAsync(() ->
                                        dtpRwdClientService.listFloor(false, false, ContextUtil.getProjectId(), queryParamForObjects)
                                , BusinessThreadPoolExecutor.ttlExecutor());
                        try {
                            List<DtpRwdFloorDTO> floorList = floorFuture.get();
                            dtpRwdObjectDTO.addAll(floorList);
                        } catch (Exception e) {
                            logProjectInstance().error("楼层信息查询失败", e,
                                    Pair.of("queryParamForObjects", queryParamForObjects));
                        }
                        break;
                }
            }
            return dtpRwdObjectDTO;
        }, BusinessThreadPoolExecutor.ttlExecutor());

        try {
            return futureResult.get();
        } catch (Exception e) {
            logProjectInstance().error("查询建筑楼层项目对象信息失败", e,
                    Pair.of("queryParamForObjects", queryParamForObjects));
            return new ArrayList<>();
        }
    }

    private boolean setIdQueryParam(DtpRwdObjectQueryParam queryParamForObjects, Set<String> ids, Set<String> filterInstanceIds) {
        if (CollectionUtils.isNotEmpty(filterInstanceIds)) {
            //取并集
            Set<String> fId = ids.stream().filter(filterInstanceIds::contains).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(fId)) {
                queryParamForObjects.setId(fId);
            } else {
                return true;
            }
        }
        return false;
    }


    @Override
    public DemandTreeShieldedObjectVO treeShieldedObject(@NonNull DemandTreeShieldedObjectQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();

        DemandInstanceConfigQueryParam queryParam = new DemandInstanceConfigQueryParam();
        queryParam.setTemplateType(templateType);
        queryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        queryParam.setFilterConfig(new DemandInstanceConfigValueObject().setShielded(true));
        Set<String> spaceIds = demandInstanceGateway.listInstanceIds(queryParam);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return null;
        }
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        DemandInstanceValueObject instanceValueObject = new DemandInstanceValueObject(
                DemandInstanceTypeEnum.PROJECT, projectId);
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpaceByInstance(templateType, instanceValueObject,
                DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdSpaceDTO.Fields.buildingId, DtpRwdSpaceDTO.Fields.floorId,
                DtpRwdSpaceDTO.Fields.roomFuncType);
        spaces.removeIf(item -> !spaceIds.contains(item.getId()));
        if (CollectionUtils.isEmpty(spaces)) {
            return null;
        }
        Set<String> floorIds = new HashSet<>();
        Set<String> buildingIds = new HashSet<>();
        for (DtpRwdSpaceDTO space : spaces) {
            floorIds.add(space.getFloorId());
            buildingIds.add(space.getBuildingId());
        }
        Optional<DtpRwdObjectDTO> project = dtpRwdClientService.getObjectOpt(
                new DtpRwdObjectQueryParam().setId(projectId));
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(
                new DtpRwdObjectQueryParam().setId(buildingIds));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(
                new DtpRwdObjectQueryParam().setId(floorIds));
        return toTreeShieldedObjectVO(project.orElseThrow(() -> new ApplicationException(
                        RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_c4977d8389a3493fa04da6732a17209a"))),
                buildings, floors, spaces);
    }

    /**
     * 转换屏蔽对象树VO
     *
     * @param project   项目
     * @param buildings 建筑
     * @param floors    楼层
     * @param spaces    空间
     * @return 屏蔽对象树VO
     * @since 5.240902.0
     */
    @Nullable
    private DemandTreeShieldedObjectVO toTreeShieldedObjectVO(
            @NonNull DtpRwdObjectDTO project,
            @NonNull List<DtpRwdBuildingDTO> buildings,
            @NonNull List<DtpRwdFloorDTO> floors,
            @NonNull List<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(spaces)) {
            return null;
        }
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = floors.stream()
                .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
        Map<String, List<DtpRwdSpaceDTO>> floorIdAndSpaces = spaces.stream()
                .collect(Collectors.groupingBy(DtpRwdSpaceDTO::getFloorId));

        List<DemandTreeShieldedObjectVO> buildingVos = new ArrayList<>();
        for (DtpRwdBuildingDTO building : buildings) {
            List<DtpRwdFloorDTO> tempFloors = buildingIdAndFloors.get(building.getId());
            if (CollectionUtils.isEmpty(tempFloors)) {
                continue;
            }
            int buildingSpaceCount = 0;
            List<DemandTreeShieldedObjectVO> floorVos = new ArrayList<>();
            for (DtpRwdFloorDTO floor : tempFloors) {
                List<DtpRwdSpaceDTO> tempSpaces = floorIdAndSpaces.get(floor.getId());
                if (CollectionUtils.isEmpty(tempSpaces)) {
                    continue;
                }
                int floorSpaceCount = tempSpaces.size();
                List<DemandTreeShieldedObjectVO> spaceVos = tempSpaces.stream()
                        .map(demandConverter::toTreeShieldedObjectVo)
                        .collect(Collectors.toList());
                DemandTreeShieldedObjectVO floorVo = demandConverter.toTreeShieldedObjectVo(floor);
                floorVo.setSpaceCount(floorSpaceCount);
                floorVo.setChildren(spaceVos);
                floorVos.add(floorVo);
                buildingSpaceCount += floorSpaceCount;
            }
            DemandTreeShieldedObjectVO buildingVo = demandConverter.toTreeShieldedObjectVo(building);
            buildingVo.setSpaceCount(buildingSpaceCount);
            buildingVo.setChildren(floorVos);
            buildingVos.add(buildingVo);
        }
        DemandTreeShieldedObjectVO root = demandConverter.toTreeShieldedObjectVo(project);
        root.setSpaceCount(spaces.size());
        root.setChildren(buildingVos);
        return root;
    }

    @Override
    public List<DemandInstanceChildrenSpaceFuncTypeVO> listChildrenSpaceFuncTypes(
            @NonNull DemandInstanceChildrenSpaceFuncTypeQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        // 空间功能类型 -> 关联空间ID
        Map<DtpKnowledgeInfoCodeDataSourceDTO, List<String>> roomFuncTypeAndSpaceIds =
                demandInstanceGateway.mapChildrenRoomFuncTypeAndSpaceIds(templateType,
                        new DemandInstanceValueObject(instanceType, instanceId));
        if (MapUtils.isEmpty(roomFuncTypeAndSpaceIds)) {
            return Lists.newArrayList();
        }
        List<DemandInstanceChildrenSpaceFuncTypeVO> result = new ArrayList<>();
        roomFuncTypeAndSpaceIds.forEach((roomFuncType, spaceIds) -> {
            DemandInstanceChildrenSpaceFuncTypeVO resultItem = new DemandInstanceChildrenSpaceFuncTypeVO();
            resultItem.setFuncType(roomFuncType.getCode());
            resultItem.setFuncTypeName(roomFuncType.getName());
            resultItem.setRelSpaceIds(spaceIds);
            result.add(resultItem);
        });
        return result;
    }

    @Override
    public List<DemandConfigVO<BaseDemandConfig>> queryDemandConfig(DemandConfigQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        Set<String> instanceIds = param.getInstanceIds();
        Long sceneId = param.getSceneId();
        LocalDate date = param.getDate();
        boolean queryInheritConfig = param.isQueryInheritConfig();
        boolean queryInstanceTag = param.isQueryInstanceTag();
        Set<String> withColumns = param.getWithColumns();

        // 查询需求模版
        DemandTemplate demandTemplate = param.queryAndGetTemplate();
        if (Objects.isNull(demandTemplate)) {
            return Lists.newArrayList();
        }
        Long templateId = demandTemplate.getId();
        // 查询需求配置
        DemandConfigByInstanceBatchQueryParam instanceBatchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        instanceBatchQueryParam.setDate(date);
        instanceBatchQueryParam.setTemplateId(templateId);
        if (Objects.nonNull(sceneId)) {
            instanceBatchQueryParam.setSceneId(sceneId);
        }
        instanceBatchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(instanceType, instanceIds));
        if (CollectionUtils.isEmpty(instanceIds)) {
            instanceBatchQueryParam.setAutoFillEmptyInstanceIds(true);
        }
        instanceBatchQueryParam.setQueryInheritConfig(queryInheritConfig);
        List<DemandConfigAggregate<BaseDemandConfig>> configs = demandConfigGateway.listConfigsByInstances(instanceBatchQueryParam);
        if (CollectionUtils.isEmpty(configs)) {
            return new ArrayList<>();
        }
        Map<String, DemandInstanceConfigEntity> instanceIdAndConfig = Collections.emptyMap();
        Map<String, Map<String, Object>> instanceIdAndTags = Collections.emptyMap();
        if (queryInstanceTag) {
            Set<DemandInstanceValueObject> allInstances = configs.stream()
                    .map(item -> item.getIdentifier().getInstance())
                    .collect(Collectors.toSet());
            instanceIds = allInstances.stream().map(DemandInstanceValueObject::getInstanceId).collect(Collectors.toSet());
            // 查询实例配置
            DemandInstanceConfigQueryParam instanceConfigQueryParam = new DemandInstanceConfigQueryParam();
            instanceConfigQueryParam.setDate(date);
            instanceConfigQueryParam.setTemplateType(templateType);
            instanceConfigQueryParam.setInstanceType(instanceType);
            instanceConfigQueryParam.setInstanceIds(instanceIds);
            List<DemandInstanceConfigEntity> instanceConfigs = demandInstanceGateway.listInstanceConfigs(instanceConfigQueryParam);
            instanceIdAndConfig = instanceConfigs.stream().collect(Collectors.toMap(item ->
                    item.getIdentifier().getInstance().getInstanceId(), Function.identity()));
            // 查询实例自定义标签
            DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
            List<DemandInstanceCustomizedTagDTO> instanceCustomizedTags = demandStrategy
                    .mapDemandInstanceCustomizedTagNameAndValue(templateType, allInstances);
            instanceIdAndTags = instanceCustomizedTags.stream().collect(Collectors.toMap(item ->
                    item.getInstance().getInstanceId(), DemandInstanceCustomizedTagDTO::getCustomizedTag));
        }
        List<DemandConfigVO<BaseDemandConfig>> result = new ArrayList<>();
        List<PropertyDescriptor> descriptors = PropertyDescriptorUtils.getPropertyDescriptors(DemandConfigVO.class);
        for (DemandConfigAggregate<BaseDemandConfig> config : configs) {
            String instanceId = config.getIdentifier().getInstance().getInstanceId();
            DemandInstanceTagVO instanceTag = new DemandInstanceTagVO();
            instanceTag.fillDemandTag(config);
            instanceTag.fillInstanceTag(instanceIdAndConfig.get(instanceId), instanceIdAndTags.get(instanceId));
            DemandConfigVO<BaseDemandConfig> resultItem = new DemandConfigVO<>();
            resultItem.setConfig(config.getConfig());
            resultItem.setFinalConfig(config.getFinalConfig());
            resultItem.setInheritInstances(config.getInheritInstanceAndModes().keySet());
            resultItem.setParentConfig(config.getParentConfig());
            resultItem.setInstanceType(param.getInstanceType());
            resultItem.setInstanceId(instanceId);
            resultItem.setInstanceTag(instanceTag);
            if (CollectionUtils.isNotEmpty(withColumns)) {
                for (String fieldName : resultItem.cacheFieldNames()) {
                    if (!withColumns.contains(fieldName)) {
                        resultItem.writePropertyValue(fieldName, null);
                    }
                }
            }
            result.add(resultItem);
        }
        return result;
    }

    @Override
    public Collection<DemandTreeBuildingFloorSpaceVO> treeBuildingFloorSpace(
            @NonNull DemandTreeBuildingFloorSpaceQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        boolean filterInShieldedSpace = param.isFilterInShieldedSpace();
        boolean filterInBlacklistedSpace = param.isFilterInBlacklistedSpace();
        boolean queryFloor = param.isQueryFloor();
        boolean querySpace = param.isQuerySpace();

        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();

        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.setFilterInShieldedSpace(filterInShieldedSpace);
        queryParam.setFilterInBlacklistedSpace(filterInBlacklistedSpace);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
        queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.floorId);
        queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.buildingId);
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        Set<String> floorIds = new HashSet<>();
        Set<String> buildingIds = new HashSet<>();
        for (DtpRwdSpaceDTO space : spaces) {
            String floorId = space.getFloorId();
            String buildingId = space.getBuildingId();
            if (StringUtils.isNotBlank(floorId)) {
                floorIds.add(floorId);
            }
            if (StringUtils.isNotBlank(buildingId)) {
                buildingIds.add(buildingId);
            }
        }
        if (CollectionUtils.isEmpty(buildingIds)) {
            return Lists.newArrayList();
        }
        DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam().setId(buildingIds)
                .addWithColumn(DtpRwdBuildingDTO.Fields.class);
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(buildingQueryParam);
        if (CollectionUtils.isEmpty(buildings)) {
            return Lists.newArrayList();
        }
        List<DtpRwdFloorDTO> floors = Collections.emptyList();
        if (queryFloor && CollectionUtils.isNotEmpty(floorIds)) {
            DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam().setId(floorIds)
                    .addWithColumn(DtpRwdFloorDTO.Fields.class);
            floors = dtpRwdClientService.listFloor(floorQueryParam);
        }
        return DemandTreeBuildingFloorSpaceVO.toResult(buildings, floors, querySpace ? spaces : null);
    }

    /**
     * 设置自定义标签
     *
     * @param param  查询参数
     * @param voList 需求树节点集合
     * @since 5.241102.0
     */
    private void setCustomizedTags(
            @NonNull DemandTreeQueryParam param,
            @NonNull List<DemandTreeObjectVO> voList) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();

        Set<DemandInstanceValueObject> allInstances = new HashSet<>();
        allInstances = DemandTreeObjectVO.listAllDemandInstances(allInstances, voList);

        List<DemandInstanceCustomizedTagDTO> demandInstanceCustomizedTags = demandStrategy
                .mapDemandInstanceCustomizedTagNameAndValue(templateType, allInstances);
        if (CollectionUtils.isEmpty(demandInstanceCustomizedTags)) {
            return;
        }
        Map<String, Map<String, Object>> instanceIdAndTags = demandInstanceCustomizedTags.stream()
                .collect(Collectors.toMap(item -> item.getInstance().getInstanceId(),
                        DemandInstanceCustomizedTagDTO::getCustomizedTag));
        // 设置自定义标签
        deepSetCustomizedTags(voList, instanceIdAndTags);
    }

    /**
     * 设置自定义标签
     *
     * @param objects                     设置对象
     * @param instanceIdAndCustomizedTags 实例ID -> 自定义标签
     * @since 5.241102.0
     */
    private void deepSetCustomizedTags(
            @NonNull List<DemandTreeObjectVO> objects,
            @NonNull Map<String, Map<String, Object>> instanceIdAndCustomizedTags) {
        if (CollectionUtils.isEmpty(objects)) {
            return;
        }
        for (DemandTreeObjectVO object : objects) {
            String objectId = object.getId();
            DemandInstanceTagVO instanceTag = object.getInstanceTag();
            List<DemandTreeObjectVO> children = object.getChildren();
            Map<String, Object> tags = instanceIdAndCustomizedTags.get(objectId);
            if (MapUtils.isNotEmpty(tags)) {
                instanceTag = Optional.ofNullable(instanceTag).orElseGet(DemandInstanceTagVO::new);
                instanceTag.setCustomizedTag(tags);
                object.setInstanceTag(instanceTag);
            }
            if (CollectionUtils.isNotEmpty(children)) {
                deepSetCustomizedTags(children, instanceIdAndCustomizedTags);
            }
        }
    }

    /**
     * 设置标签信息 是否是管控空间 和 是否屏蔽
     *
     * @param param           查询参数
     * @param voList          节点树信息
     * @param dtpRwdSpaceList 空间信息
     */
    private void setDemandTreeObjectCfgVO(DemandTreeQueryParam param, List<DemandTreeObjectVO> voList, List<DtpRwdSpaceDTO> dtpRwdSpaceList) {
        //标签信息组装
        DemandTemplate demandTemplate = demandTemplateRepository.getByTemplateByTemplateTypeTag(true, param.getTemplateType(), param.getTemplateTag());
        if (demandTemplate == null) {
            return;
        }
        DemandInstanceConfigQueryParam qp = new DemandInstanceConfigQueryParam();
        qp.setDate(param.getDate());
        qp.setTemplateType(demandTemplate.getDemandTemplateType());
        qp.setInstanceType(DemandInstanceTypeEnum.SPACE);
        //取出所有空间节点
        Set<String> ids = new HashSet<>();
        dtpRwdSpaceList.forEach(i -> ids.add(i.getId()));
        qp.setInstanceIds(ids);
        List<DemandInstanceConfigEntity> cfgList = demandInstanceGateway.listInstanceConfigs(qp);
        Map<String, DemandInstanceConfigEntity> instanceCfg = new HashMap<>();
        if (cfgList != null) {
            //按照实例 ID 创建 MAP 数据
            cfgList.forEach(i -> instanceCfg.put(i.getIdentifier().getInstance().getInstanceId(), i));
        }
        //查询所有配置
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds = new HashMap<>();
        instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.SPACE, dtpRwdSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
        instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.FLOOR, dtpRwdSpaceList.stream().map(DtpRwdSpaceDTO::getFloorId).collect(Collectors.toSet()));
        instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.BUILDING, dtpRwdSpaceList.stream().map(DtpRwdSpaceDTO::getBuildingId).collect(Collectors.toSet()));
        instanceTypeAndInstanceIds.put(DemandInstanceTypeEnum.PROJECT, Sets.newHashSet(ContextUtil.getProjectId()));
        DemandConfigByInstanceBatchQueryParam queryParam = new DemandConfigByInstanceBatchQueryParam();
        queryParam.setDate(param.getDate());
        queryParam.setTemplateId(demandTemplate.getId());
        queryParam.setInstanceTypeAndInstanceIds(instanceTypeAndInstanceIds);
        List<DemandConfigAggregate<BaseDemandConfig>> aggregates = new ArrayList<>(demandConfigGateway.listConfigsByInstances(queryParam));
        //转换成 map
        Map<String, DemandConfigAggregate<BaseDemandConfig>> map = new HashMap<>();
        aggregates.forEach(i -> map.put(i.getIdentifier().getInstance().getInstanceId(), i));

        //设置标签信息
        voList.forEach(i -> traverseSetCfg(i, instanceCfg, map));
    }


    /**
     * 遍历设置配置信息
     *
     * @param node           节点
     * @param instanceCfgMap 标签配置
     * @param map            实例配置集合
     */
    private void traverseSetCfg(DemandTreeObjectVO node, Map<String, DemandInstanceConfigEntity> instanceCfgMap, Map<String, DemandConfigAggregate<BaseDemandConfig>> map) {
        if (node == null) {
            return;
        }
        //设置当前节点标签
        DemandInstanceTagVO tag = Optional.ofNullable(node.getInstanceTag()).orElseGet(DemandInstanceTagVO::new);
        //存在配置填充配置
        if (instanceCfgMap != null && instanceCfgMap.containsKey(node.getId())) {
            DemandInstanceConfigEntity cfg = instanceCfgMap.get(node.getId());
            tag.setShielded(cfg.getConfig().getShielded());
            tag.setInBlacklisted(cfg.getConfig().getInBlacklisted());
        }
        DemandConfigAggregate<BaseDemandConfig> cfg = map.get(node.getId());

        if (cfg != null) {
            tag.setCustomConfig(cfg.getConfig() != null);
            tag.setExistCfg(cfg.getFinalConfig() != null);
        }
        node.setInstanceTag(tag);

        if (node.getChildren() == null) {
            return;
        }
        //遍历子节点
        for (DemandTreeObjectVO child : node.getChildren()) {
            traverseSetCfg(child, instanceCfgMap, map);
        }
    }

    /**
     * 构建项目节点
     *
     * @param project 项目信息
     * @return DemandTreeObjectVO
     */
    private DemandTreeObjectVO buildProjectNode(ProjectDTO project) {
        DemandTreeObjectVO vo = new DemandTreeObjectVO();
        vo.setId(project.getProjectId());
        vo.setName(project.getProjectName());
        vo.setLocalName(project.getProjectLocalName());
        return vo;
    }

    /**
     * 构建树结构子节点
     *
     * @return
     */
    private List<DemandTreeObjectVO> buildSpaceTreeChildren(List<DtpRwdSpaceDTO> dtpRwdSpaceList) {

        if (dtpRwdSpaceList == null) return null;

        // 收集楼层和建筑 ID
        Set<String> floorIds = dtpRwdSpaceList.stream()
                .map(DtpRwdSpaceDTO::getFloorId)
                .collect(Collectors.toSet());
        Set<String> buildingIds = dtpRwdSpaceList.stream()
                .map(DtpRwdSpaceDTO::getBuildingId)
                .collect(Collectors.toSet());

        List<DemandObjectQueryParam.FilterInstance> filterInstance = new ArrayList<>();
        filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.BUILDING));
        filterInstance.add(DemandObjectQueryParam.FilterInstance.build(DemandInstanceTypeEnum.FLOOR));
        List<DtpRwdObjectDTO> list = getDtpRwdObjectDTOAsync(filterInstance, dtpRwdSpaceList, new DtpRwdObjectQueryParam(), floorIds, buildingIds);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<DtpRwdFloorDTO> floorList = list.stream()
                .filter(dto -> floorIds.contains(dto.getId()))
                .map(dto -> (DtpRwdFloorDTO) dto)
                .collect(Collectors.toList());

        List<DtpRwdBuildingDTO> buildingList = list.stream()
                .filter(dto -> buildingIds.contains(dto.getId()))
                .map(dto -> (DtpRwdBuildingDTO) dto)
                .collect(Collectors.toList());

        // 构建建筑楼层关系和楼层房间关系
        Map<String, Set<String>> buildingMap = new HashMap<>(64);
        Map<String, List<DtpRwdFloorDTO>> buildingObjMap = new HashMap<>(64);
        Map<String, List<DtpRwdObjectDTO>> floorObjMap = new HashMap<>(128);
        buildRelations(dtpRwdSpaceList, buildingMap, floorObjMap);

        // 转换建筑楼层关系为对象关系
        Map<String, DtpRwdFloorDTO> map = floorList.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity(), (o1, o2) -> o2));
        buildingMap.forEach((buildingId, floorId) -> buildingObjMap.put(buildingId, floorId.stream().map(map::get).collect(Collectors.toList())));

        // 构建父子结构
        return buildTreeObject(buildingList, buildingObjMap, floorObjMap);
    }

    /**
     * 获取空间信息
     */
    private List<DtpRwdSpaceDTO> getDtpRwdSpaceDTOS(DemandStrategy strategy, DemandTreeQueryParam param) {
        // 查询项目下的建筑、楼层和空间信息
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(param.getTemplateType());
        queryParam.fillByInstance(new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, ContextUtil.getProjectId()));
        // 空间树构建与配置标签需要全量空间字段, 固定查询
        queryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        List<DtpRwdSpaceDTO> dtpRwdSpaceList = strategy.listDemandSpace(queryParam);
        if (CollectionUtils.isEmpty(dtpRwdSpaceList)) {
            return new ArrayList<>();
        }
        return dtpRwdSpaceList;
    }

    /**
     * 关系处理
     */
    private void buildRelations(List<DtpRwdSpaceDTO> dtpRwdSpaceList, Map<String, Set<String>> buildingMap,
                                Map<String, List<DtpRwdObjectDTO>> floorObjMap) {
        dtpRwdSpaceList.forEach(s -> {
            // 处理楼层房间关系
            floorObjMap.computeIfAbsent(s.getFloorId(), key -> new ArrayList<>()).add(s);
            // 处理建筑楼层关系
            buildingMap.computeIfAbsent(s.getBuildingId(), key -> new HashSet<>()).add(s.getFloorId());
        });
    }


    /**
     * 构建父子结构
     *
     * @param buildingList   建筑列表
     * @param buildingObjMap 建筑ID -> 楼层列表
     * @param floorObjMap    楼层ID -> 空间列表
     * @return 对象树DTO
     */
    private List<DemandTreeObjectVO> buildTreeObject(List<DtpRwdBuildingDTO> buildingList,
                                                     Map<String, List<DtpRwdFloorDTO>> buildingObjMap,
                                                     Map<String, List<DtpRwdObjectDTO>> floorObjMap) {
        List<DemandTreeObjectVO> result = new ArrayList<>();
        for (DtpRwdBuildingDTO building : buildingList) {
            // 将建筑转换为对应的VO对象
            DemandTreeObjectVO buildingVo = BeanUtil.toBean(building, DemandTreeObjectVO.class);
            List<DemandTreeObjectVO> floors = new ArrayList<>();
            if (buildingObjMap.containsKey(building.getId())) {
                // 获取建筑下的楼层列表并排序
                List<DtpRwdFloorDTO> floorList = buildingObjMap.get(building.getId())
                        .stream()
                        .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId).reversed())
                        .collect(Collectors.toList());
                // 将楼层转换为对应的VO对象
                floors = BeanUtil.copyToList(floorList, DemandTreeObjectVO.class);
                for (DemandTreeObjectVO floor : floors) {
                    if (floorObjMap.containsKey(floor.getId())) {
                        // 获取楼层下的空间列表并转换为对应的VO对象，设置为楼层的子节点
                        List<DemandTreeObjectVO> spaces = BeanUtil.copyToList(floorObjMap.get(floor.getId()), DemandTreeObjectVO.class);
                        floor.setChildren(spaces);
                    }
                }
            }
            buildingVo.setChildren(floors);
            if (CollectionUtils.isNotEmpty(buildingVo.getChildren())) {
                result.add(buildingVo);
            }
        }
        return result;
    }

    @Override
    public List<DemandTreeSpaceRoomFuncTypeVO> treeSpaceRoomFuncTypes(DemandTreeSpaceRoomFuncTypeQueryParam param) {
        // 获取需求模板与策略
        DemandTemplateTypeEnum demandTemplateType = param.getTemplateType();
        DemandStrategy demandStrategy = demandTemplateType.toDemandTypeEnum().demandStrategy();

        // 构建查询参数
        DemandSpacesQueryParam querySpaceParam = new DemandSpacesQueryParam(demandTemplateType);
        querySpaceParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdSpaceDTO.Fields.roomFuncType);
        querySpaceParam.setFilterInBlacklistedSpace(param.getFilterBlacklistSpace());

        // 查询有效节点
        Set<String> validNodes = Optional.ofNullable(demandStrategy.listDemandSpace(querySpaceParam))
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.stream()
                        .map(DtpRwdSpaceDTO::getRoomFuncType)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        // 获取空间功能类型枚举
        List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources = Optional.ofNullable(
                        dtpKnowledgeClientService.getInfoCode(
                                false,
                                DtpRwdClassCodeEnum.SPACE.value(),
                                DtpRwdSpaceDTO.Fields.roomFuncType
                        )
                )
                .map(DtpKnowledgeInfoCodeDTO::getDataSources)
                .orElse(Collections.emptyList());

        // 构建并过滤树结构
        return pruneTree(
                buildTreeSpaceRoomFuncType(dataSources),
                validNodes
        );
    }

    @Override
    public List<DemandConfigGroupVO> listConfigGroups(DemandTemplateSceneParam param) {
        DemandTemplate template = param.queryAndGetTemplate();
        if (Objects.isNull(template)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }

        DemandCustomizedInstanceQueryParam queryParam = new DemandCustomizedInstanceQueryParam();
        queryParam.setTemplateId(template.getId());
        queryParam.setSceneId(param.getSceneId());
        queryParam.setFilterConfig(new DemandInstanceConfigValueObject().setInBlacklisted(true));
        // 查询符合需求模板的需求实例值对象集合
        List<DemandCustomizedInstanceDTO> customizedInstances = demandConfigGateway.listCustomizedInstances(queryParam);
        if (CollectionUtils.isEmpty(customizedInstances)) {
            return Collections.emptyList();
        }

        // 按实例类型分组TreeMap，用于存储实例类型与对应ID集合的映射关系
        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndIds = new TreeMap<>();

        // 将当前项目ID到PROJECT类型的实例集合中
        instanceTypeAndIds.put(DemandInstanceTypeEnum.PROJECT, Collections.singleton(ContextUtil.getProjectId()));

        // 查询需求配置对象
        DemandConfigByInstanceBatchQueryParam demandConfigByInstanceBatchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        demandConfigByInstanceBatchQueryParam.setTemplateId(param.getTemplateId());
        demandConfigByInstanceBatchQueryParam.setSceneId(param.getSceneId());
        demandConfigByInstanceBatchQueryParam.setInstanceTypeAndInstanceIds(instanceTypeAndIds);
        List<DemandConfigAggregate<BaseDemandConfig>> demandConfigAggregates =
                demandConfigGateway.listConfigsByInstances(demandConfigByInstanceBatchQueryParam);

        // 将线性集合处理为分组映射结构后返回
        LinkedList<DemandConfigGroupVO> demandConfigGroups = Lists.newLinkedList();
        processAnArrayIntoAGroupedMapCollection(customizedInstances, demandConfigGroups);
        if (CollectionUtils.isNotEmpty(demandConfigAggregates)) {
            processRoomFuncType(demandConfigAggregates.get(0), demandConfigGroups);
        }
        return demandConfigGroups;
    }

    @Override
    public @NonNull List<DemandConfigResultVO<BaseDemandConfig>> listConfigResultsByInstances(
            @NonNull DemandConfigResultBatchQueryParam param) {
        return this.demandConfigGateway.listConfigResultsByInstances(param);
    }



    /**
     * 处理项目，建筑，楼层，空间的需求配置
     *
     * @param customizedInstances 需求配置
     * @param demandConfigGroups  结果集
     */
    private void processAnArrayIntoAGroupedMapCollection(
            List<DemandCustomizedInstanceDTO> customizedInstances,
            List<DemandConfigGroupVO> demandConfigGroups) {
        // 获取楼层在哪个建筑下
        List<String> floorIds = customizedInstances.stream()
                .filter(x -> x.getInstanceType().equals(DemandInstanceTypeEnum.FLOOR))
                .map(DemandInstanceValueObject::getInstanceId)
                .collect(Collectors.toList());
        Map<String, String> floorIdAndBuildingIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(floorIds)) {
            List<DtpRwdBuildingDTO> dtpRwdBuildings = dtpRwdClientService.listBuildingByFloorId(
                    floorIds,
                    new DtpRwdObjectQueryParam()
                            .addWithColumn(
                                    DtpRwdObjectDTO.Fields.id,
                                    DtpRwdObjectDTO.Fields.localName,
                                    DtpRwdObjectDTO.Fields.relObjIds
                            )
            );
            if (CollectionUtils.isNotEmpty(dtpRwdBuildings)) {
                for (DtpRwdBuildingDTO dtpRwdBuilding : dtpRwdBuildings) {
                    Set<String> relObjIds = dtpRwdBuilding.getRelObjIds();
                    if (CollectionUtils.isNotEmpty(relObjIds)) {
                        relObjIds.forEach(relObjectId -> {
                            floorIdAndBuildingIdMap.put(relObjectId, dtpRwdBuilding.getLocalName());
                        });
                    }
                }
            }
        }

        Map<DemandConfigGroupVO.GroupKey, DemandConfigGroupVO> tempMap = new HashMap<>();
        for (DemandCustomizedInstanceDTO instance : customizedInstances) {
            // 提取基础信息
            DemandConfigGroupVO.DemandInstanceValueObjectVO demandInstanceValueObjectVO =
                    demandConverter.instanceValueToVo(instance, floorIdAndBuildingIdMap.get(instance.getInstanceId()));

            // 分组维度信息
            final DemandInstanceTypeEnum objectType = instance.getInstanceType();
            final DemandConfigGroupEntity configGroup = instance.getConfigGroup();

            // 构建分组键
            final Long groupId = configGroup != null ? configGroup.getIdentifier().getGroupId() : null;
            final String groupName = configGroup != null ?
                    configGroup.getGroupName() :
                    instance.getInstanceName();

            // 使用复合键避免重复
            DemandConfigGroupVO.GroupKey key = new DemandConfigGroupVO.GroupKey(objectType, groupId, groupName);

            // 存在则更新，不存在则创建
            if (tempMap.containsKey(key)) {
                tempMap.get(key).getRelInstances().add(demandInstanceValueObjectVO);
            } else {
                demandConfigGroups.add(new DemandConfigGroupVO(
                        groupId,
                        groupName,
                        objectType.getValue(),
                        new ArrayList<>(Collections.singletonList(demandInstanceValueObjectVO)),
                        instance.getCreateTime()
                ));
                tempMap.put(key, demandConfigGroups.get(demandConfigGroups.size() - 1));
            }
        }

    }

    /**
     * 处理项目维度的空间功能类型
     *
     * @param aggregate  项目配置
     * @param resultList 响应结果集
     */
    private void processRoomFuncType(DemandConfigAggregate<BaseDemandConfig> aggregate,
                                     List<DemandConfigGroupVO> resultList) {
        DemandConfigValueObject<BaseDemandConfig> config = aggregate.getConfig();
        Map<Long, DemandConfigGroupEntity> roomFuncTypeRelGroupIdAndConfigGroup =
                aggregate.getIdentifier().getRoomFuncTypeRelGroupIdAndConfigGroup();
        if (Objects.nonNull(config) && CollectionUtils.isNotEmpty(config.getSpaceFuncTypeConfigs())) {
            List<DemandSpaceFuncTypeConfig<BaseDemandConfig>> spaceFuncTypeConfigs = config.getSpaceFuncTypeConfigs();
            spaceFuncTypeConfigs.forEach(x -> {
                Long relGroupId = x.getRelConfigGroupId();
                List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = x.getFuncTypes();
                if (CollectionUtils.isEmpty(funcTypes)) {
                    return;
                }
                String funcTypeName = funcTypes.stream()
                        .map(DtpKnowledgeInfoCodeDataSourceDTO::getName)
                        .collect(Collectors.joining("、"));
                String roomFuncTypeName = MapUtils.isNotEmpty(roomFuncTypeRelGroupIdAndConfigGroup)
                        && roomFuncTypeRelGroupIdAndConfigGroup.containsKey(relGroupId)
                        ? roomFuncTypeRelGroupIdAndConfigGroup.get(relGroupId).getGroupName()
                        : funcTypeName;
                List<DemandConfigGroupVO.DemandInstanceValueObjectVO> relInstances = new ArrayList<>();
                funcTypes.forEach(funcType -> {
                    DemandConfigGroupVO.DemandInstanceValueObjectVO valueObject =
                            new DemandConfigGroupVO.DemandInstanceValueObjectVO(null);
                    valueObject.setInstanceId(funcType.getCode());
                    valueObject.setInstanceName(funcType.getName());
                    relInstances.add(valueObject);
                });
                resultList.add(
                        new DemandConfigGroupVO(
                                relGroupId,
                                roomFuncTypeName,
                                "ROOM_FUNC_TYPE",
                                relInstances,
                                null
                        )
                );
            });
        }
    }


    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
