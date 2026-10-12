package com.persagy.apccontrol.basic.service.demand.instancegroup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.DemandBelongInstanceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryByInstanceIdParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandTreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.rwd.TreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceGroupDetailVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceGroupVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeObjectVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorLightingStrategy;
import com.persagy.apccontrol.basic.structure.demand.converter.instance.group.DemandInstanceGroupConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.evnet.DemandInstanceGroupChangeEvent;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求实例编组Service
 *
 * @author wxf
 * @since v7.250601.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupServiceImpl implements DemandInstanceGroupService {

    private final InstanceService instanceService;
    private final DemandLightingStrategy demandLightingStrategy;
    private final RunningMonitorLightingStrategy runningMonitorLightingStrategy;
    private final DemandInstanceGroupDetailService demandInstanceGroupDetailService;
    private final DtpRwdClientService dtpRwdClientService;
    private final DemandInstanceGroupResource demandInstanceGroupResource;
    private final DemandInstanceGroupConverter demandInstanceGroupConverter;
    private final DemandInstanceGroupMessageService demandInstanceGroupMessageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 室内照明：公共、停车场
    private static final List<DemandTemplateTypeEnum> IN_DOOR_LIGHTING_TEMPLATE_TYPE = Lists.newArrayList(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
            DemandTemplateTypeEnum.PARKING_LOT_LIGHTING);
    // 室外照明：楼体、景观、标识
    private static final List<DemandTemplateTypeEnum> OUT_DOOR_LIGHTING_TEMPLATE_TYPE = Lists.newArrayList(DemandTemplateTypeEnum.BUILDING_LIGHTING,
            DemandTemplateTypeEnum.LANDSCAPE_LIGHTING, DemandTemplateTypeEnum.SIGN_LIGHTING);
    // 所有照明
    private static final List<DemandTemplateTypeEnum> LIGHTING_TEMPLATE_TYPE = CollectionUtils.collate(IN_DOOR_LIGHTING_TEMPLATE_TYPE, OUT_DOOR_LIGHTING_TEMPLATE_TYPE);

    @Override
    public DemandTreeObjectVO treeBuildingFloorInstances(DemandTreeBuildingFloorSpaceQueryParam param) {
        TreeBuildingFloorSpaceQueryParam treeParam = new TreeBuildingFloorSpaceQueryParam();
        treeParam.setIsQuerySpace(true);

        // RedisCacheTemplate.MethodCall<List<DtpRwdBuildingDTO>> methodToCache = () -> instanceService.treeBuildingFloorSpace(treeParam);
        // List<DtpRwdBuildingDTO> dtpRwdBuildingDTOS = redisCacheTemplate.asyncRefreshTemplate("treeBuildingFloorSpace", param, methodToCache);

        List<DtpRwdBuildingDTO> dtpRwdBuildingDTOS = instanceService.treeBuildingFloorSpace(treeParam);

        // 控制模块所在的空间进行楼层-建筑-项目的聚合
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = demandLightingStrategy.listControllersByInstance(ContextUtil.getProjectId(), DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
                DemandInstanceTypeEnum.PROJECT, ContextUtil.getProjectId());
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addInstanceGroup(DemandInstanceGroupParam param) {
        DemandInstanceGroup demandInstanceGroup = new DemandInstanceGroup()
                .setProjectId(ContextUtil.getProjectId())
                .setTemplateType(param.getTemplateType())
                .setName(param.getGroupName())
                .setPurpose(param.getPurpose())
                .setTag(param.getTag());
        demandInstanceGroup = demandInstanceGroupResource.add(demandInstanceGroup);
        Set<String> instanceIds = param.getInstanceIds();
        Boolean b = demandInstanceGroupDetailService.batchSaveDetail(demandInstanceGroup.getId(), instanceIds, param.getInstanceType());
        if (!b) {
            throw new ApplicationException(ResponseCode.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_453bb763a71f40ceae3e330d61fa526b"), LocaleMessageUtil.getMessage("key_453bb763a71f40ceae3e330d61fa526b"));
        }
        DemandInstanceGroupPO newGroup = queryGroupPo(demandInstanceGroup.getId());
        publishChangeEvent(param.getTemplateType(), null, newGroup, DataTypeEnum.ADD);

        // 发送消息 触发后续逻辑 新增没有关联配置 不触发计算
        /*demandInstanceGroupMessageService.sendIdMessage(demandInstanceGroup.getTemplateType().toRunningMonitorSecondTypeEnum(),
                demandInstanceGroup.getId());*/

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editInstanceGroup(DemandInstanceGroupParam param) {
        Long id = param.getId();
        DemandInstanceGroup data = demandInstanceGroupResource.getById(id);
        if (Objects.isNull(data)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_cba07f782d47473b9dc4262f60187742"), LocaleMessageUtil.getMessage("key_cba07f782d47473b9dc4262f60187742"));
        }
        data.clearUpdateParameter();
        if (StringUtils.isNotBlank(param.getGroupName())) {
            data.setName(param.getGroupName());
        }
        if (Objects.nonNull(param.getTemplateType())) {
            data.setTemplateType(param.getTemplateType());
        }
        data.setPurpose(param.getPurpose());
        data.setTag(param.getTag());

        DemandInstanceGroupPO oldGroup = queryGroupPo(id);
        boolean isSuccess = demandInstanceGroupResource.updateById(data);
        if (isSuccess && CollectionUtils.isNotEmpty(param.getInstanceIds())) {
            // 先删除详情
            demandInstanceGroupDetailService.deleteDetailByGroupId(data.getId());
            // 再插入新的详情
            demandInstanceGroupDetailService.batchSaveDetail(id, param.getInstanceIds(), param.getInstanceType());
        }
        DemandInstanceGroupPO newGroup = queryGroupPo(id);
        publishChangeEvent(param.getTemplateType(), oldGroup, newGroup, DataTypeEnum.EDIT);
        // 发送消息 触发后续逻辑
        demandInstanceGroupMessageService.sendIdMessage(param.getTemplateType().toRunningMonitorSecondTypeEnum(),
                param.getId());

        return isSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteInstanceGroup(DemandInstanceGroupParam param) {
        DemandInstanceGroup data = demandInstanceGroupResource.getById(param.getId());
        if (Objects.isNull(data)) {
            return true;
        }
        DemandInstanceGroupPO oldGroup = queryGroupPo(param.getId());
        // 先删除详情
        demandInstanceGroupDetailService.deleteDetailByGroupId(param.getId());
        boolean r = demandInstanceGroupResource.removeById(param.getId());
        publishChangeEvent(data.getTemplateType(), oldGroup, null, DataTypeEnum.DELETE);
        // 发送消息 触发后续逻辑
        demandInstanceGroupMessageService.sendIdMessage(data.getTemplateType().toRunningMonitorSecondTypeEnum(),
                param.getId());

        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copyInstanceGroup(DemandInstanceGroupParam param) {
        Long id = param.getId();
        DemandInstanceGroup data = demandInstanceGroupResource.getById(id);
        if (Objects.isNull(data)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_cba07f782d47473b9dc4262f60187742"), LocaleMessageUtil.getMessage("key_cba07f782d47473b9dc4262f60187742"));
        }
        // 复制分组信息
        DemandInstanceGroup newData = new DemandInstanceGroup();
        BeanUtils.copyProperties(data, newData);
        newData.setId(null);
        newData.setName(param.getGroupName());
        DemandInstanceGroup group = demandInstanceGroupResource.add(newData);
        // 复制分组明细信息
        List<DemandInstanceGroupDetail> demandInstanceGroupDetails = demandInstanceGroupDetailService.ListDetailByGroupId(id);
        Set<String> instanceIds = demandInstanceGroupDetails.stream().map(DemandInstanceGroupDetail::getInstanceId).collect(Collectors.toSet());
        demandInstanceGroupDetailService.batchSaveDetail(newData.getId(), instanceIds, demandInstanceGroupDetails.get(0).getInstanceType());
        DemandInstanceGroupPO newGroup = queryGroupPo(group.getId());
        publishChangeEvent(param.getTemplateType(), null, newGroup, DataTypeEnum.ADD);
        return true;
    }

    @Override
    public Boolean checkNameExists(DemandInstanceGroupParam param) {
        LambdaQueryWrapper<DemandInstanceGroup> qw = new LambdaQueryWrapper<>();
        qw.eq(DemandInstanceGroup::getName, param.getGroupName());
        qw.eq(DemandInstanceGroup::getProjectId, ContextUtil.getProjectId());
        qw.eq(DemandInstanceGroup::getTemplateType, param.getTemplateType());
        qw.eq(StringUtils.isNotBlank(param.getTag()), DemandInstanceGroup::getTag, param.getTag());
        qw.ne(Objects.nonNull(param.getId()), DemandInstanceGroup::getId, param.getId());
        return demandInstanceGroupResource.count(qw) > 0;
    }

    @Override
    public List<DemandInstanceGroupVO> listInstanceGroup(DemandInstanceGroupQueryParam param) {
        LambdaQueryWrapper<DemandInstanceGroup> qw = new LambdaQueryWrapper<>();
        qw.eq(Objects.nonNull(param.getTemplateType()), DemandInstanceGroup::getTemplateType, param.getTemplateType());
        qw.in(CollectionUtils.isNotEmpty(param.getTemplateTypes()), DemandInstanceGroup::getTemplateType, param.getTemplateTypes());
        qw.like(StringUtils.isNotBlank(param.getKeyword()), DemandInstanceGroup::getName, param.getKeyword());
        qw.in(CollectionUtils.isNotEmpty(param.getTags()), DemandInstanceGroup::getTag, param.getTags());
        qw.eq(DemandInstanceGroup::getProjectId, ContextUtil.getProjectId());
        List<DemandInstanceGroup> list = demandInstanceGroupResource.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = list.stream().map(DemandInstanceGroup::getId).collect(Collectors.toSet());
        List<DemandInstanceGroupDetail> allInstanceDetails = demandInstanceGroupDetailService.ListDetailByGroupIds(groupIds);
        Set<String> allInstanceIds = allInstanceDetails.stream().map(DemandInstanceGroupDetail::getInstanceId).collect(Collectors.toSet());
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setId(allInstanceIds);
        dtpRwdObjectQueryParam.filterMonitoringSurveillanceStatus();
        if (CollectionUtils.isNotEmpty(param.getAdditionalWithColumns())) {
            dtpRwdObjectQueryParam.addWithColumnWithDefault(param.getAdditionalWithColumns());
        } else {
            dtpRwdObjectQueryParam.addWithColumnWithSimple();
            dtpRwdObjectQueryParam.addWithColumn(DtpRwdEquipmentDTO.Fields.floorId, DtpRwdEquipmentDTO.Fields.buildingId);
        }
        // 查询所有对象，放入map中
        Map<String, DtpRwdEquipmentDTO> dtpRwdObjectMap = dtpRwdClientService.listEquipment(dtpRwdObjectQueryParam).stream()
                .collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity(), (v1, v2) -> v2));
        List<DemandInstanceGroupVO> groupList = new ArrayList<>();
        // 处理返回列表
        list.forEach(item -> {
            List<DemandInstanceGroupDetail> demandInstanceGroupDetails = demandInstanceGroupDetailService.ListDetailByGroupId(item.getId());
            if (CollectionUtils.isEmpty(demandInstanceGroupDetails)) {
                return;
            }
            DemandInstanceGroupVO demandInstanceGroupVO = demandInstanceGroupConverter.toVO(item);
            List<DemandInstanceGroupDetailVO> relObjects = getDemandInstanceGroupDetailVOS(demandInstanceGroupDetails, dtpRwdObjectMap);
            if (CollectionUtils.isEmpty(relObjects)) {
                return;
            }
            demandInstanceGroupVO.setRelObjects(relObjects);
            groupList.add(demandInstanceGroupVO);
        });
        List<DemandInstanceGroupVO> result;
        // 如果照明
        if ((Objects.nonNull(param.getTemplateType()) && LIGHTING_TEMPLATE_TYPE.contains(param.getTemplateType()))
                || (CollectionUtils.isNotEmpty(param.getTemplateTypes())
                && param.getTemplateTypes().stream().anyMatch(LIGHTING_TEMPLATE_TYPE::contains))) {
            result = handleLightingBelongInstance(param, groupList, allInstanceIds);
            // 排序,先按序列升序，再按楼层降序，再按时间升序
            result.sort(Comparator.comparingInt((DemandInstanceGroupVO e) -> e.getBelongInstance().getSequence())
                    .thenComparing(Comparator.comparingInt((DemandInstanceGroupVO e) -> {
                        Integer floorSequence = e.getBelongInstance().getFloorSequence();
                        return Objects.isNull(floorSequence) ? 0 : floorSequence;
                    }).reversed()).thenComparing(DemandInstanceGroupVO::getCreateTime));
        } else {
            result = groupList;
        }
        return result;
    }

    @NotNull
    private List<DemandInstanceGroupDetailVO> getDemandInstanceGroupDetailVOS(List<DemandInstanceGroupDetail> demandInstanceGroupDetails, Map<String, DtpRwdEquipmentDTO> dtpRwdObjectMap) {
        List<DemandInstanceGroupDetailVO> relObjects = Lists.newArrayList();
        demandInstanceGroupDetails.forEach(detail -> {
            DtpRwdEquipmentDTO dtpRwdEquipmentDTO = dtpRwdObjectMap.get(detail.getInstanceId());
            if (Objects.isNull(dtpRwdEquipmentDTO)) {
                return;
            }
            DemandInstanceGroupDetailVO demandInstanceGroupDetailVO = new DemandInstanceGroupDetailVO();
            BeanUtils.copyProperties(dtpRwdEquipmentDTO, demandInstanceGroupDetailVO);
            demandInstanceGroupDetailVO.setInstanceType(detail.getInstanceType());
            relObjects.add(demandInstanceGroupDetailVO);
        });
        return relObjects;
    }

    @Override
    public List<DemandInstanceGroupVO> listInstanceGroupByInstanceId(@NonNull DemandInstanceGroupQueryByInstanceIdParam queryParam) {
        Set<@NonNull String> instanceIds = queryParam.getInstanceIds();
        List<DemandInstanceGroupDetail> demandInstanceGroupDetails = demandInstanceGroupDetailService.ListDetailByInstanceIds(instanceIds);
        if (CollectionUtils.isEmpty(demandInstanceGroupDetails)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = demandInstanceGroupDetails.stream().map(DemandInstanceGroupDetail::getRelGroupId).collect(Collectors.toSet());
        DemandInstanceGroupQueryDaoParam param = new DemandInstanceGroupQueryDaoParam();
        param.setIds(groupIds);
        param.setQueryDetail(true);
        param.setTag(queryParam.getTag());
        param.setTemplateType(queryParam.getTemplateType());
        List<DemandInstanceGroupPO> list = demandInstanceGroupResource.list(param);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<String, DtpRwdEquipmentDTO> instanceIdRwdObjectMap = dtpRwdClientService.listEquipment(new DtpRwdObjectQueryParam().setId(instanceIds).addWithColumnWithSimple(queryParam.getWithColumns()))
                .stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity(), (v1, v2) -> v2));
        // 过滤其他实例
        list.forEach(group -> {
            group.getDetails().removeIf(detail -> !instanceIds.contains(detail.getInstanceId()));
        });
        List<DemandInstanceGroupVO> result = demandInstanceGroupConverter.toVO(list);
        result.forEach(group -> {
            group.setRelObjects(getDemandInstanceGroupDetailVOS(group.getDetails(), instanceIdRwdObjectMap));
        });
        return result;
    }

    /**
     * 查询编组PO
     *
     * @param id 编组ID
     * @return 编组PO
     * @since 7.250602.0
     */
    private @Nullable DemandInstanceGroupPO queryGroupPo(@NonNull Long id) {
        DemandInstanceGroupQueryDaoParam queryDaoParam = new DemandInstanceGroupQueryDaoParam();
        queryDaoParam.setId(id);
        queryDaoParam.setQueryDetail(true);
        List<DemandInstanceGroupPO> groups = demandInstanceGroupResource.list(queryDaoParam);
        return CollectionUtils.isNotEmpty(groups) ? IterableUtils.first(groups) : null;
    }

    /**
     * 发布变更事件
     *
     * @param templateType 模版类型
     * @param oldGroup     操作前的编组
     * @param newGroup     操作后的编组
     * @param type         操作类型
     * @since 7.250602.0
     */
    private void publishChangeEvent(
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable DemandInstanceGroupPO oldGroup,
            @Nullable DemandInstanceGroupPO newGroup,
            @NonNull DataTypeEnum type) {
        DemandInstanceGroupChangeEvent.Message message = new DemandInstanceGroupChangeEvent.Message();
        message.setTemplateType(templateType);
        message.setType(type);
        message.setOldGroup(oldGroup);
        message.setNewGroup(newGroup);
        applicationEventPublisher.publishEvent(new DemandInstanceGroupChangeEvent(message));
    }

    /**
     * 处理照明属于实例
     *
     * @param param                   参数
     * @param demandInstanceGroupList 需求实例组列表
     * @param allInstanceIds          所有实例id列表
     * @since 7.250602.0
     */
    private List<DemandInstanceGroupVO> handleLightingBelongInstance(DemandInstanceGroupQueryParam param, List<DemandInstanceGroupVO> demandInstanceGroupList, Set<String> allInstanceIds) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();

        // 分组id对应回路列表
        Map<Long, Set<String>> groupIdRelIdsMap = demandInstanceGroupList.stream().collect(Collectors.toMap(DemandInstanceGroupVO::getId,
                (v) -> v.getRelObjects().stream().map(DemandInstanceGroupDetailVO::getId).collect(Collectors.toSet())));
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(allInstanceIds);
        // 查询所有的照明模块
        List<DtpRwdObjectDTO> allCircuitControllers = dtpRwdClientService.listCircuitControllerByCircuitIds(false, false, ContextUtil.getProjectId(), allInstanceIds, null);
        List<DtpRwdObjectDTO> allDDCs = runningMonitorLightingStrategy.queryDDC(allInstanceIds, Lists.newArrayList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds));
        if (CollectionUtils.isNotEmpty(allDDCs)) {
            allCircuitControllers.addAll(allDDCs);
        }
        if (CollectionUtils.isEmpty(allCircuitControllers)) {
            return Collections.emptyList();
        }
        // 按照明回路id->照明模块List进行分组
        Map<String, List<DtpRwdObjectDTO>> circuitIdCircuitContrllersMap = DtpRwdObjectDTO.mapRelObjIdAndData(allCircuitControllers);
        // 拿到所有照明控制模块id
        Set<String> allCircuitControllerIds = allCircuitControllers.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        // 查询照明控制模块所在空间
        List<DtpRwdSpaceDTO> circuitControllerSpaceList = dtpRwdClientService.listSpaceInByEquipmentId(allCircuitControllerIds, null);
        // 如果不是夜景照明, 并且空中模块没有所在空间, 则为无效数据, 返回空
        if (!OUT_DOOR_LIGHTING_TEMPLATE_TYPE.contains(templateType) && CollectionUtils.isEmpty(circuitControllerSpaceList)) {
            return Collections.emptyList();
        }
        // 按照明模块id->空间List进行分组
        Map<String, List<DtpRwdSpaceDTO>> circuitContrllerIdSpaceMap = DtpRwdSpaceDTO.mapRelObjIdAndData(circuitControllerSpaceList);
        // 构建编组下的回路，回路所属模块，模块所属空间 Map<分组id，空间列表>
        Map<Long, Collection<DtpRwdSpaceDTO>> groupIdSpaceMap = new HashMap<>();
        for (Map.Entry<Long, Set<String>> entry : groupIdRelIdsMap.entrySet()) {
            List<DtpRwdSpaceDTO> allSpace = new ArrayList<>();
            for (String circuitId : entry.getValue()) {
                // 获取照明模块
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = circuitIdCircuitContrllersMap.get(circuitId);
                if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
                    continue;
                }
                // 遍历照明模块
                for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                    List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = circuitContrllerIdSpaceMap.get(dtpRwdObjectDTO.getId());
                    if (CollectionUtils.isEmpty(dtpRwdSpaceDTOS)) {
                        continue;
                    }
                    allSpace.addAll(dtpRwdSpaceDTOS);
                }
            }
            groupIdSpaceMap.put(entry.getKey(), allSpace);
        }
        Set<String> allFloorIds = groupIdSpaceMap.values().stream().flatMap(e -> e.stream().map(DtpRwdSpaceDTO::getFloorId)).collect(Collectors.toSet());
        Map<String, Integer> floorIdFloorSequenceIdMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(allFloorIds)) {
            floorIdFloorSequenceIdMap = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam().setId(allFloorIds)
                            .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdFloorDTO.Fields.floorSequenceId)).stream()
                    .collect(Collectors.toMap(DtpRwdFloorDTO::getId, DtpRwdFloorDTO::getFloorSequenceId));
        }
        for (DemandInstanceGroupVO demandInstanceGroupVO : demandInstanceGroupList) {
            Collection<DtpRwdSpaceDTO> spaces = groupIdSpaceMap.get(demandInstanceGroupVO.getId());
            if (CollectionUtils.isEmpty(spaces)) {
                continue;
            }
            // 填充附属对象
            DemandBelongInstanceDTO belongInstance = new DemandBelongInstanceDTO();
            Set<String> buildingIdSet = spaces.stream().map(DtpRwdSpaceDTO::getBuildingId).collect(Collectors.toSet());
            Set<String> floorIdSet = spaces.stream().map(DtpRwdSpaceDTO::getFloorId).collect(Collectors.toSet());
            // 如果跨建筑或者是室外照明，则进入项目分组下
            if (buildingIdSet.size() > 1 || OUT_DOOR_LIGHTING_TEMPLATE_TYPE.contains(demandInstanceGroupVO.getTemplateType())) {
                // 勾选的回路跨多个建筑,则进入项目分组下
                belongInstance.setInstanceType(InstanceTypeEnum.PROJECT);
                belongInstance.setInstanceId(ContextUtil.getProjectId());
                belongInstance.setSequence(1);
            } else if (floorIdSet.size() > 1) {
                // 勾选的回路跨多个楼层但未跨建筑则进入具体的某个建筑分组下
                belongInstance.setInstanceType(InstanceTypeEnum.BUILDING);
                belongInstance.setInstanceId(Lists.newArrayList(buildingIdSet).get(0));
                belongInstance.setSequence(2);
            } else {
                // 勾选的回路均归属于单个楼层下，则进入具体的楼层分组
                belongInstance.setInstanceType(InstanceTypeEnum.FLOOR);
                belongInstance.setInstanceId(Lists.newArrayList(floorIdSet).get(0));
                belongInstance.setSequence(3);
                belongInstance.setFloorSequence(floorIdFloorSequenceIdMap.get(belongInstance.getInstanceId()));
            }
            demandInstanceGroupVO.setBelongInstance(belongInstance);
        }
        demandInstanceGroupList.removeIf(e -> Objects.isNull(e.getBelongInstance()));
        List<DemandBelongInstanceDTO> filterBelongInstances = param.getBelongInstance();
        if (CollectionUtils.isEmpty(filterBelongInstances)) {
            return demandInstanceGroupList;
        }
        long count = filterBelongInstances.stream().filter(e -> e.getInstanceType() == InstanceTypeEnum.PROJECT).count();
        if (count > 0) {
            // 如果包含了项目，就全量返回
            return demandInstanceGroupList;
        }
        Set<String> buildingIds = filterBelongInstances.stream().filter(e -> e.getInstanceType() == InstanceTypeEnum.BUILDING)
                .map(DemandBelongInstanceDTO::getInstanceId).collect(Collectors.toSet());
        Set<String> floorIds = filterBelongInstances.stream().filter(e -> e.getInstanceType() == InstanceTypeEnum.FLOOR)
                .map(DemandBelongInstanceDTO::getInstanceId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(buildingIds) || CollectionUtils.isNotEmpty(floorIds)) {
            Map<String, Set<String>> buildingFloorIdsMap = dtpRwdClientService.listFloorByBuildingId(buildingIds, null).stream()
                    .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId, Collectors.mapping(DtpRwdFloorDTO::getId, Collectors.toSet())));
            Set<DemandInstanceGroupVO> demandInstanceGroupSet = Sets.newHashSet();
            for (DemandBelongInstanceDTO demandBelongInstanceDTO : filterBelongInstances) {
                String instanceId = demandBelongInstanceDTO.getInstanceId();
                demandInstanceGroupSet.addAll(demandInstanceGroupList.stream().filter(e -> {
                    if (demandBelongInstanceDTO.getInstanceType() == InstanceTypeEnum.FLOOR) {
                        return instanceId.equals(e.getBelongInstance().getInstanceId());
                    } else if (demandBelongInstanceDTO.getInstanceType() == InstanceTypeEnum.BUILDING) {
                        Set<String> floorIdSet = buildingFloorIdsMap.getOrDefault(instanceId, Sets.newHashSet());
                        return instanceId.equals(e.getBelongInstance().getInstanceId()) || floorIdSet.contains(e.getBelongInstance().getInstanceId());
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList()));
            }
            return Lists.newArrayList(demandInstanceGroupSet);
        }
        return Collections.emptyList();
    }


    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "需求实例编组Service";
    }
}
