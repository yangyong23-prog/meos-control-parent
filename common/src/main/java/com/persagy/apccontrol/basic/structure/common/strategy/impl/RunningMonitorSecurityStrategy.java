package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectModelViewRecordConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.service.security.impl.SecurityServiceImpl.*;

/**
 * 安防策略实现
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 16:54
 * @since v5.240801.0
 */
@Component(RunningMonitorSecurityStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorSecurityStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorSecurityStrategy";
    public static final String LOG_TAG = "运行监测-安防策略";
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final CacheObjectModelStatusService cacheObjectModelStatusService;
    private final CacheObjectRepository cacheObjectRepository;
    private final CacheObjectRelRepository cacheObjectRelRepository;
    private final CacheObjectModelViewRepository cacheObjectModelViewRepository;
    private final CacheObjectModelViewRecordConverter cacheObjectModelViewRecordConverter;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.SECURITY;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        String projectId = ContextUtil.getProjectId();
        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        String instanceId = instance.getInstanceId();
        List<DtpRwdSpaceDTO> rwdSpaceList = new ArrayList<>();
        if (instanceType == DemandInstanceTypeEnum.SPACE) {
            param.setId(instanceId);
        } else if (runningMonitorSecondType.equals(RunningMonitorSecondTypeEnum.SECURITY_ARMING)) {
            // 撤布防状态的空间直接返回空间id
            List<String> spaceIds = listEvacuationDefenseSpaceByEquipment(projectId);
            if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                instanceType = DemandInstanceTypeEnum.SPACE;
                param.setId(spaceIds);
            } else {
                switch (instanceType) {
                    case BUILDING:
                        rwdSpaceList = querySpaceInBuilding(projectId, Lists.newArrayList(instanceId), param);
                        break;
                    case FLOOR:
                        rwdSpaceList = querySpacesInFloor(projectId, Lists.newArrayList(instanceId), param);
                        break;
                }
                return rwdSpaceList.stream().filter(x -> spaceIds.contains(x.getId())).collect(Collectors.toList());
            }

        } else if (runningMonitorSecondType.equals(RunningMonitorSecondTypeEnum.SECURITY_ACCESS)) {
            if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                Set<String> spaceIds = mapSpaceAccessStatePartialQuantity(projectId);
                instanceType = DemandInstanceTypeEnum.SPACE;
                param.setId(spaceIds);
            } else {
                switch (instanceType) {
                    case BUILDING:
                        rwdSpaceList = querySpaceInBuilding(projectId, Lists.newArrayList(instanceId), param);
                        break;
                    case FLOOR:
                        rwdSpaceList = querySpacesInFloor(projectId, Lists.newArrayList(instanceId), param);
                        break;
                }
                Set<String> accessStateSpaceId =
                        mapSpaceAccessState(projectId, rwdSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
                return rwdSpaceList.stream().filter(x -> accessStateSpaceId.contains(x.getId())).collect(Collectors.toList());
            }
        }

        StopWatch stopWatch = StopWatch.create("安防空间查询");

        stopWatch.start("空间");
        if (ArrayUtils.isNotEmpty(withColumns)) {
            param.addWithColumn(withColumns);
        } else {
            param.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        }
        switch (instanceType) {
            case PROJECT:
                rwdSpaceList = querySpaceInProjectId(projectId, param);
                break;
            case BUILDING:
                rwdSpaceList = querySpaceInBuilding(projectId, Lists.newArrayList(instanceId), param);
                break;
            case FLOOR:
                rwdSpaceList = querySpacesInFloor(projectId, Lists.newArrayList(instanceId), param);
                break;
            case SPACE:
                rwdSpaceList = dtpRwdClientService.listSpace(false, true, projectId, param);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        stopWatch.stop();

        logProjectInstance(ContextUtil.getProjectId()).info("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return rwdSpaceList;
    }

    /**
     * 入侵报警防区控制器服务的空间
     * GeneralZone => MechForArch/Eq2Sp => SPIACU
     * 带有入侵报警的摄像头所在空间
     * 监控摄像头（信息点：是否有布撤防功能 hasArmingFunc）的所在空间
     * GeneralZone => MechInArch/Eq2Sp => SPVSCM(hasArmingFunc=1)
     *
     * @param projectId 项目id
     * @return 空间状态
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public List<String> listEvacuationDefenseSpaceByEquipment(String projectId) {
        StopWatch stopWatch = StopWatch.create("撤布防");
        stopWatch.start("设备");
        DtpRwdClassCodeEnum codeEnum = DtpRwdClassCodeEnum.SPIACU;
        String value = codeEnum.value();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, HAS_ARMING_FUNC_CODE);
        param.setClassCodeEnum(Lists.newArrayList(codeEnum, DtpRwdClassCodeEnum.SPVSCM));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentList = dtpRwdClientService.listEquipment(param);
        stopWatch.stop();
        if (CollectionUtils.isNotEmpty(dtpRwdEquipmentList)) {
            stopWatch.start("入侵报警防区控制器为设备服务的空间");

            // 入侵报警防区控制器为设备服务的空间
            List<String> intrusionAlertAreaList = dtpRwdEquipmentList.stream()
                    .filter(x -> x.getClassCode().equals(value))
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam()
                    .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
            List<DtpRwdSpaceDTO> rwdSpaceList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(intrusionAlertAreaList)) {
                rwdSpaceList.addAll(
                        dtpRwdClientService.listSpaceServiceByEquipmentId(
                                intrusionAlertAreaList, dtpRwdObjectQueryParam)
                );
            }
            stopWatch.stop();

            stopWatch.start("监控摄像头为撤布防状态的设备所在空间");
            // 监控摄像头为撤布防状态的设备所在空间
            List<String> surveillanceCameraIdList = dtpRwdEquipmentList.stream()
                    .filter(x -> "1".equals(getUnDefinedValue(x, HAS_ARMING_FUNC_CODE)))
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(surveillanceCameraIdList)) {
                rwdSpaceList.addAll(
                        dtpRwdClientService.listSpaceInByEquipmentId(
                                false, false, projectId, surveillanceCameraIdList, dtpRwdObjectQueryParam)
                );
            }
            if (CollectionUtils.isEmpty(rwdSpaceList)) {
                return new ArrayList<>();
            }
            stopWatch.stop();


            stopWatch.start("过滤空间撤布防规则");

            // 过滤空间撤布防规则
            String deploymentStatus = InfoCodeEnum.DEPLOYMENT_STATUS.value();
            List<String> infoCodeList = Lists.newArrayList(deploymentStatus);
            // 查信息点规则
            Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>(16);
            rwdSpaceList.forEach(space -> MapExecutor.dynamicPut(classCodeAndObjectIds, space.getClassCode(), space.getId(), HashSet::new));
            Map<String, Set<String>> ruleInfoCodeMap = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);
            // 移除不满足条件的键值对
            ruleInfoCodeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().stream().noneMatch(s -> s.equals(deploymentStatus)));
            Set<String> ruleSpace = ruleInfoCodeMap.keySet();
            stopWatch.stop();
            logProjectInstance(projectId).debug("响应时间记录",
                    Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
            return rwdSpaceList.stream().map(DtpRwdObjectDTO::getId).filter(ruleSpace::contains).distinct().collect(Collectors.toList());
        }
        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return new ArrayList<>();
    }


    /**
     * 查询某一视角下的空间
     *
     * @param projectId   项目id
     * @param spaceIdList 空间id
     * @return 空间通行状态的空间
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public Set<String> mapSpaceAccessState(@NonNull String projectId,
                                           @NonNull List<String> spaceIdList) {
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        dtpRwdObjectQueryParam.addWithColumn(SecurityDtpRwdEquipmentDTO.Fields.class);
        dtpRwdObjectQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(spaceIdList));
        dtpRwdObjectQueryParam.addInfoCode(MONITORING_SURVEILLANCE_STATUS_CODE, MONITORING_SURVEILLANCE_STATUS_LIST);
        List<SecurityDtpRwdEquipmentDTO> dtpRwdEquipmentList = new ArrayList<>(dtpRwdClientService.listSecurityEquipment(
                false, projectId, dtpRwdObjectQueryParam));

        HashMap<String, AccessDemandStatusEnum> spaceAccessDemandMap = new HashMap<>(16);

        // 使用Map<Object, List<String>>进行分组，key为所属空间id，value为设备id，即当前空间下的所有门
        Map<String, List<String>> mapAllEquipmentInSpaceBySpaceId = new HashMap<>(16);

        for (SecurityDtpRwdEquipmentDTO obj : dtpRwdEquipmentList) {
            Set<String> relObjIds = obj.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                continue;
            }
            relObjIds = relObjIds.stream().filter(spaceIdList::contains).collect(Collectors.toSet());
            for (String value : relObjIds) {
                mapAllEquipmentInSpaceBySpaceId
                        .computeIfAbsent(value, k -> new ArrayList<>())
                        .add(obj.getId());
            }
        }

        String trafficInfoCode = InfoCodeEnum.ACCESS_DEMAND.value();
        List<String> infoCodeList = Lists.newArrayList(trafficInfoCode);

        // 查信息点规则
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>(16);
        dtpRwdEquipmentList.forEach(equipment -> MapExecutor.dynamicPut(classCodeAndObjectIds, equipment.getClassCode(), equipment.getId(), HashSet::new));
        Map<String, Set<String>> ruleInfoCodeMap = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);

        Map<String, String> infoCodeValueByObjectIdMap = new HashMap<>();
        // 移除不满足条件的键值对
        ruleInfoCodeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().stream().noneMatch(s -> s.equals(trafficInfoCode)));
        Set<String> equipmentHasRuleIdList = ruleInfoCodeMap.keySet();
        if (MapUtils.isEmpty(ruleInfoCodeMap)) {
            return new HashSet<>();
        }

        // 查询信息点数据  规则过滤后未使用到
        List<String> equipmentIdList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        List<InfoCodeValueVO> trafficStatusInfoList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIdList, infoCodeList);
        if (CollectionUtils.isNotEmpty(trafficStatusInfoList)) {
            infoCodeValueByObjectIdMap = trafficStatusInfoList.stream()
                    .collect(Collectors.toMap(InfoCodeValueVO::getObjectId, obj -> String.valueOf(obj.getInfoCodeValue())));
        }

        // 空间下的设备通行状态与信息点规则->转为空间的通行状态
        infoCodeRuleTurnEnum(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
        return spaceAccessDemandMap.keySet();
    }

    /**
     * 项目下所有的空间通行状态
     *
     * @param projectId 项目id
     * @return 空间通行状态的空间
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public Set<String> mapSpaceAccessStatePartialQuantity(@NonNull String projectId) {
        StopWatch stopWatch = StopWatch.create("查询安防模型图mapSpaceAccessStatePartialQuantity");
        stopWatch.start("joinAll");

        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        param.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        param.addInfoCode(
                MONITORING_SURVEILLANCE_STATUS_CODE, MONITORING_SURVEILLANCE_STATUS_LIST);
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentList = dtpRwdClientService.listEquipment(param);

        if (CollectionUtils.isEmpty(dtpRwdEquipmentList)) {
            return new HashSet<>();
        }
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdSpaceDTO> rwdSpaceList = new ArrayList<>();
        List<String> equipmentList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(equipmentList)) {
            rwdSpaceList = dtpRwdClientService.listSpaceInByEquipmentId(equipmentList, dtpRwdObjectQueryParam);
        }
        if (CollectionUtils.isEmpty(rwdSpaceList)) {
            return new HashSet<>();
        }

        HashMap<String, AccessDemandStatusEnum> spaceAccessDemandMap = new HashMap<>(16);

        // 使用Map<Object, List<String>>进行分组，key为所属空间id，value为设备id，即当前空间下的所有门
        Map<String, Set<String>> mapAllEquipmentInSpaceBySpaceId = new HashMap<>(16);

        for (DtpRwdSpaceDTO obj : rwdSpaceList) {
            Set<String> relObjIds = obj.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                continue;
            }
            mapAllEquipmentInSpaceBySpaceId.put(obj.getId(), relObjIds);
        }

        String trafficInfoCode = InfoCodeEnum.ACCESS_DEMAND.value();
        List<String> infoCodeList = Lists.newArrayList(trafficInfoCode);

        // 查信息点规则
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>(16);
        dtpRwdEquipmentList.forEach(equipment -> MapExecutor.dynamicPut(classCodeAndObjectIds, equipment.getClassCode(), equipment.getId(), HashSet::new));
        Map<String, Set<String>> ruleInfoCodeMap = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodeList);

        Map<String, String> infoCodeValueByObjectIdMap = new HashMap<>();
        // 移除不满足条件的键值对
        ruleInfoCodeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().stream().noneMatch(s -> s.equals(trafficInfoCode)));

        Set<String> equipmentHasRuleIdList = ruleInfoCodeMap.keySet();
        if (MapUtils.isEmpty(ruleInfoCodeMap)) {
            infoCodeRuleTurnEnumPartialQuantity(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
            return spaceAccessDemandMap.keySet();
        }

        // 查询信息点数据  规则过滤后未使用到
        List<String> equipmentIdList = dtpRwdEquipmentList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        List<InfoCodeValueVO> trafficStatusInfoList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIdList, infoCodeList);
        if (CollectionUtils.isNotEmpty(trafficStatusInfoList)) {
            infoCodeValueByObjectIdMap = trafficStatusInfoList.stream()
                    .collect(Collectors.toMap(InfoCodeValueVO::getObjectId, obj -> String.valueOf(obj.getInfoCodeValue())));
        }

        // 空间下的设备通行状态与信息点规则->转为空间的通行状态
        infoCodeRuleTurnEnumPartialQuantity(mapAllEquipmentInSpaceBySpaceId, infoCodeValueByObjectIdMap, equipmentHasRuleIdList, spaceAccessDemandMap);
        stopWatch.stop();

        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return spaceAccessDemandMap.keySet();
    }


    /**
     * 查询项目下的空间
     *
     * @param projectId 项目id
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    public List<DtpRwdSpaceDTO> querySpaceInProjectId(@NonNull String projectId, @NonNull DtpRwdObjectQueryParam param) {
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(
                false, false, projectId, param);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 查询建筑物下的空间
     *
     * @param projectId         项目id
     * @param filterBuildingIds 过滤建筑ID
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    public List<DtpRwdSpaceDTO> querySpaceInBuilding(
            @NonNull String projectId,
            @Nullable Collection<String> filterBuildingIds,
            DtpRwdObjectQueryParam param) {
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByBuildingId(filterBuildingIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(projectId).warn("无有效建筑下的设备", Pair.of("filterBuildingIds", filterBuildingIds));
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 查询楼层下的空间
     *
     * @param projectId      项目ID
     * @param filterFloorIds 筛选楼层ID, 为空时查询所有
     * @return 设备列表, {@code relObjIds} 字段为关联楼层ID
     * @since 5.240702.0
     */
    public List<DtpRwdSpaceDTO> querySpacesInFloor(
            @NonNull String projectId,
            @Nullable Collection<String> filterFloorIds,
            @NonNull DtpRwdObjectQueryParam param) {
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByFloorId(filterFloorIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(projectId).warn("无有效楼层下的设备", Pair.of("filterFloorIds", filterFloorIds));
            return Lists.newArrayList();
        }
        return spaces;
    }

    @Override
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        return x -> x.stream().filter(item -> {
            // 空间异常事件，通过”风控目标“进行过滤”一级风控目标=“安全” & 二级风控目标=“空间安全隐患”
            boolean riskConditions = item.getObjectType().equals(ObjectTypeEnum.SPACE.value())
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel1Collect())
                    && item.getRiskMeasureAimLevel1Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel1.E2.getCode())::contains)
                    && CollectionUtils.isNotEmpty(item.getRiskMeasureAimLevel2Collect())
                    && item.getRiskMeasureAimLevel2Collect().stream().anyMatch(Lists.newArrayList(RiskMeasureAimLevel2.E33.getCode())::contains);
            return riskConditions || item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value());
        }).collect(Collectors.toList());
    }

    @Override
    public Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return Lists.newArrayList(RiskMeasureAimLevel2.E3);
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO>
     * @author wxf
     * @description 获取撤布防空间
     * @date 13:59 2024/12/30
     **/
    public List<DtpRwdSpaceDTO> listDeploymentSpace() {
        // 入侵报警防区控制器服务的空间
        // GeneralZone => MechForArch/Eq2Sp => SPIACU
        // 监控摄像头（信息点：是否有布撤防功能 hasArmingFunc）的所在空间
        // GeneralZone => MechInArch/Eq2Sp => SPVSCM(hasArmingFunc=1)
        StopWatch stopWatch = StopWatch.create("查询安防撤布防空间");
        stopWatch.start("获取设备");
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = new ArrayList<>();
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setClassCode(Arrays.asList(DtpRwdClassCodeEnum.SPIACU.value(), DtpRwdClassCodeEnum.SPVSCM.value()));
        dtpRwdObjectQueryParam.setWithColumns(Arrays.asList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, InfoCodeEnum.HAS_ARMING_FUNC.getCode()));

        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipment(dtpRwdObjectQueryParam);
        if (CollUtil.isEmpty(dtpRwdEquipmentDTOS)) {
            return dtpRwdSpaceDTOS;
        }

        List<String> spiacuIds = new ArrayList<>();
        List<String> spvscmIds = new ArrayList<>();

        dtpRwdEquipmentDTOS.forEach(dtpRwdEquipmentDTO -> {
            if (DtpRwdClassCodeEnum.SPIACU.value().equals(dtpRwdEquipmentDTO.getClassCode())) {
                spiacuIds.add(dtpRwdEquipmentDTO.getId());
            } else if (DtpRwdClassCodeEnum.SPVSCM.value().equals(dtpRwdEquipmentDTO.getClassCode())) {
                Map<String, Object> undefinedProperties = dtpRwdEquipmentDTO.getUndefinedProperties();
                if (MapUtils.isNotEmpty(undefinedProperties)) {
                    Object hasArmingFunc = undefinedProperties.get(InfoCodeEnum.HAS_ARMING_FUNC.getCode());
                    if (hasArmingFunc != null && "1".equals(hasArmingFunc.toString())) {
                        spvscmIds.add(dtpRwdEquipmentDTO.getId());
                    }
                }
            }
        });
        stopWatch.stop();

        stopWatch.start("获取入侵报警防区控制器空间");
        if (CollUtil.isNotEmpty(spiacuIds)) {
            DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
            spaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
            dtpRwdSpaceDTOS.addAll(dtpRwdClientService.listSpaceServiceByEquipmentId(spiacuIds, spaceQueryParam));
        }
        stopWatch.stop();

        stopWatch.start("获取监控摄像头空间");
        if (CollUtil.isNotEmpty(spvscmIds)) {
            DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
            spaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
            dtpRwdSpaceDTOS.addAll(dtpRwdClientService.listSpaceInByEquipmentId(spvscmIds, spaceQueryParam));
        }
        stopWatch.stop();

        logProjectInstance(ContextUtil.getProjectId()).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return dtpRwdSpaceDTOS;
    }

    /**
     * @return java.util.List<com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO>
     * @author wxf
     * @description 获取通行空间
     * @date 14:01 2024/12/30
     **/
    public List<DtpRwdSpaceDTO> listAccessSpace() {
        if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.SECURITY)) {
            return this.listAccessSpaceFromCache();
        }
        List<DtpRwdSpaceDTO> securitySpaceVOS = new ArrayList<>();
        StopWatch stopWatch = StopWatch.create("通行空间");
        stopWatch.start("获取设备");
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        param.setClassCodeEnum(DOOR_EQUIPMENT_CLASS);
        param.addInfoCode(MONITORING_SURVEILLANCE_STATUS_CODE, MONITORING_SURVEILLANCE_STATUS_LIST);
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentList = dtpRwdClientService.listEquipment(param);
        stopWatch.stop();
        if (CollUtil.isEmpty(dtpRwdEquipmentList)) {
            return securitySpaceVOS;
        }


        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        dtpRwdEquipmentList.forEach(equipment -> {
            String classCode = equipment.getClassCode();
            if (StringUtils.isNotBlank(classCode)) {
                classCodeAndObjectIds.computeIfAbsent(classCode, k -> new HashSet<>()).add(equipment.getId());
            }
        });

        stopWatch.start("获取规则");
        Map<String, Set<String>> objectIdToExistsRuleInfoCodesMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.ACCESS_DEMAND.getCode(), InfoCodeEnum.EQUIP_ON_SET.getCode()));
        Map<String, Boolean> accessDemandMap = new HashMap<>();
        Map<String, Boolean> equipOnSetMap = new HashMap<>();
        if (MapUtil.isNotEmpty(objectIdToExistsRuleInfoCodesMap)) {
            for (Map.Entry<String, Set<String>> entry : objectIdToExistsRuleInfoCodesMap.entrySet()) {
                if (entry.getValue().contains(InfoCodeEnum.ACCESS_DEMAND.getCode())) {
                    accessDemandMap.put(entry.getKey(), true);
                }

                if (entry.getValue().contains(InfoCodeEnum.EQUIP_ON_SET.getCode())) {
                    equipOnSetMap.put(entry.getKey(), true);
                }
            }
        }
        stopWatch.stop();

        stopWatch.start("获取空间");
        List<String> equipIds = dtpRwdEquipmentList.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        spaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        List<DtpRwdSpaceDTO> dtpRwdSpaceDTOS = dtpRwdClientService.listSpaceInByEquipmentId(equipIds, spaceQueryParam);
        stopWatch.stop();

        if (CollUtil.isEmpty(dtpRwdSpaceDTOS)) {
            return securitySpaceVOS;
        }


        // 每个空间的门至少有一个有通行需求状态accessDemand的孪生规则
        // 空间下的所有门的信息点"设备开设定equipOnSet"都没有孪生规则，就不在统计范围
        stopWatch.start("过滤空间");
        dtpRwdSpaceDTOS.forEach(space -> {
            Set<String> relObjIds = space.getRelObjIds();
            boolean accessDemandFlag = false;
            boolean equipOnSetFlag = false;
            if (CollUtil.isNotEmpty(relObjIds)) {
                for (String id : relObjIds) {
                    if (accessDemandMap.containsKey(id)) {
                        accessDemandFlag = true;
                    }

                    if (equipOnSetMap.containsKey(id)) {
                        equipOnSetFlag = true;
                    }
                    if (accessDemandFlag && equipOnSetFlag) {
                        break;
                    }
                }
                if (accessDemandFlag && equipOnSetFlag) {
                    securitySpaceVOS.add(space);
                }
            }
        });
        stopWatch.stop();

        logProjectInstance().debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return securitySpaceVOS;
    }

    /**
     * READY 后按静态缓存关系聚合通行空间，实时规则仍直接查询。
     *
     * @return 满足两类通行规则的空间
     * @since v7.260802.0
     */
    private @NonNull List<DtpRwdSpaceDTO> listAccessSpaceFromCache() {
        StopWatch stopWatch = StopWatch.create("通行空间缓存");
        stopWatch.start("静态候选、关系和模型视图聚合");
        try {
            List<SecurityDtpRwdEquipmentDTO> cachedEquipments = this.cacheObjectRepository
                    .listByCacheKey(
                            CacheObjectModelKeyConfig.SECURITY_LIST_EQUIPMENT,
                            SecurityDtpRwdEquipmentDTO.class,
                            Arrays.asList(
                                    DtpRwdObjectDTO.Fields.id,
                                    DtpRwdObjectDTO.Fields.classCode,
                                    MONITORING_SURVEILLANCE_STATUS_CODE));
            List<SecurityDtpRwdEquipmentDTO> doors = cachedEquipments.stream()
                    .filter(equipment -> DOOR_EQUIPMENT_CLASS_STRING.contains(
                            equipment.getClassCode()))
                    .filter(equipment -> MONITORING_SURVEILLANCE_STATUS_LIST.contains(
                            equipment.getUndefinedProperties().getOrDefault(
                                    MONITORING_SURVEILLANCE_STATUS_CODE, "").toString()))
                    .collect(Collectors.toList());
            if (doors.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, Set<String>> classCodeAndObjectIds = new LinkedHashMap<>();
            doors.forEach(equipment -> classCodeAndObjectIds
                    .computeIfAbsent(equipment.getClassCode(), key -> new LinkedHashSet<>())
                    .add(equipment.getId()));
            Map<String, Set<String>> rules = this.abstractIotDataCacheServer
                    .mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, Arrays.asList(
                            InfoCodeEnum.ACCESS_DEMAND.getCode(),
                            InfoCodeEnum.EQUIP_ON_SET.getCode()));
            Set<String> accessDemandIds = new HashSet<>();
            Set<String> equipOnSetIds = new HashSet<>();
            if (MapUtil.isNotEmpty(rules)) {
                rules.forEach((equipmentId, infoCodes) -> {
                    if (CollectionUtils.isNotEmpty(infoCodes)
                            && infoCodes.contains(InfoCodeEnum.ACCESS_DEMAND.getCode())) {
                        accessDemandIds.add(equipmentId);
                    }
                    if (CollectionUtils.isNotEmpty(infoCodes)
                            && infoCodes.contains(InfoCodeEnum.EQUIP_ON_SET.getCode())) {
                        equipOnSetIds.add(equipmentId);
                    }
                });
            }
            if (accessDemandIds.isEmpty() || equipOnSetIds.isEmpty()) {
                return Collections.emptyList();
            }

            Set<String> doorIds = doors.stream().map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            CacheObjectRelQueryDaoParam relationParam = new CacheObjectRelQueryDaoParam()
                    .setObjectIds(doorIds)
                    .setGraphCode(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getLeft())
                    .setRelCode(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE.value().getRight());
            relationParam.addColumns(CacheObjectRel::getObjectId, CacheObjectRel::getRelObjectId);
            List<CacheObjectRel> relations = this.cacheObjectRelRepository
                    .listForward(relationParam);
            Map<String, Set<String>> spaceDoorIds = new LinkedHashMap<>();
            relations.forEach(relation -> spaceDoorIds
                    .computeIfAbsent(relation.getRelObjectId(), key -> new LinkedHashSet<>())
                    .add(relation.getObjectId()));
            Set<String> matchingSpaceIds = spaceDoorIds.entrySet().stream()
                    .filter(entry -> entry.getValue().stream().anyMatch(accessDemandIds::contains)
                            && entry.getValue().stream().anyMatch(equipOnSetIds::contains))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (matchingSpaceIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<DtpRwdSpaceDTO> cachedSpaces = this.cacheObjectRepository.listByCacheKey(
                    CacheObjectModelKeyConfig.SECURITY_LIST_SPACE,
                    DtpRwdSpaceDTO.class,
                    CacheObjectExtensionFieldConfig.listObjectQueryFields(
                            DtpRwdClassCodeEnum.SPACE.value()));
            List<DtpRwdSpaceDTO> spaces = cachedSpaces.stream()
                    .filter(space -> matchingSpaceIds.contains(space.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (spaces.isEmpty()) {
                return spaces;
            }
            CacheObjectModelViewQueryDaoParam modelViewParam =
                    new CacheObjectModelViewQueryDaoParam();
            modelViewParam.setIds(spaces.stream().map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            Map<String, DtpRwdSpaceDTO> modelViewById = new LinkedHashMap<>();
            for (CacheObjectModelView modelView : this.cacheObjectModelViewRepository
                    .listModelViews(modelViewParam)) {
                DtpRwdSpaceDTO modelViewSpace = this.cacheObjectModelViewRecordConverter
                        .toRwdObject(modelView, DtpRwdSpaceDTO.class);
                if (modelViewSpace != null) {
                    modelViewById.putIfAbsent(modelViewSpace.getId(), modelViewSpace);
                }
            }
            spaces.forEach(space -> {
                space.setRelObjIds(new LinkedHashSet<>(spaceDoorIds
                        .getOrDefault(space.getId(), Collections.emptySet())));
                DtpRwdSpaceDTO modelView = modelViewById.get(space.getId());
                if (modelView != null) {
                    space.setBimId(modelView.getBimId());
                    space.setBimLocation(modelView.getBimLocation());
                    space.setOutline(modelView.getOutline());
                }
            });
            return spaces;
        } finally {
            stopWatch.stop();
            this.logProjectInstance().debug("响应时间记录",
                    Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        }
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
