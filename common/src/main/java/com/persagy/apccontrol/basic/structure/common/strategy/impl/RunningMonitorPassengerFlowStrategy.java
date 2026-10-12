package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客流策略实现
 *
 * @author JiYinchuan
 * @since 5.240802.0
 */
@Component(RunningMonitorPassengerFlowStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorPassengerFlowStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorPassengerFlowStrategy";
    public static final String LOG_TAG = "运行监测-客流策略";

    private final AbstractCommonServer abstractCommonServer;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.PASSENGER_FLOW;
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
        // 获取绑点规则
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.SPACE.value())));
        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        String instanceId = instance.getInstanceId();
        String projectId = ContextUtil.getProjectId();
        List<DtpRwdSpaceDTO> spaces = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(objectsExistRules)) {
            List<String> existPeopleInfoCodeSpaceId = objectsExistRules.stream()
                    .filter(x -> x.getInfoCode().equals(InfoCodeEnum.ACCM_IN_PEOPLE_FLOW.value()))
                    .map(ObjectsExistRule::getObjectId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(existPeopleInfoCodeSpaceId)) {
                return spaces;
            }
            switch (instanceType) {
                case PROJECT:
                    spaces = querySpaceInProjectId(projectId, existPeopleInfoCodeSpaceId);
                    break;
                case BUILDING:
                    spaces = querySpaceInBuilding(projectId, Lists.newArrayList(instanceId), existPeopleInfoCodeSpaceId);
                    break;
                case FLOOR:
                    spaces = querySpacesInFloor(projectId, Lists.newArrayList(instanceId), existPeopleInfoCodeSpaceId);
                    break;
                case SPACE:
                    DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
                    param.setClassCode(DtpRwdClassCodeEnum.SPACE.getValue());
                    param.setId(instanceId);
                    spaces = dtpRwdClientService.listSpace(false, true, projectId, param);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
            }
        }
        return spaces;
    }

    @Override
    public Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        return x -> x.stream().filter(item ->
                item.getObjectType().equals(ObjectTypeEnum.EQUIPMENT.value())
        ).collect(Collectors.toList());
    }

    /**
     * 查询项目下的空间，仅限有规则的空间
     *
     * @param projectId 项目id
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    private List<DtpRwdSpaceDTO> querySpaceInProjectId(@NonNull String projectId, List<String> existPeopleInfoCodeSpaceId) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        param.setId(existPeopleInfoCodeSpaceId);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(
                false, false, projectId, param);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 查询建筑物下的空间，仅限有规则的空间
     *
     * @param projectId         项目id
     * @param filterBuildingIds 过滤建筑ID
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    private List<DtpRwdSpaceDTO> querySpaceInBuilding(
            @NonNull String projectId,
            @Nullable Collection<String> filterBuildingIds,
            @NonNull List<String> existPeopleInfoCodeSpaceId) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        param.setId(existPeopleInfoCodeSpaceId);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByBuildingId(filterBuildingIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(projectId).warn("无有效建筑下的设备", Pair.of("filterBuildingIds", filterBuildingIds));
            return Lists.newArrayList();
        }
        return spaces;
    }

    /**
     * 查询楼层下的空间，仅限有规则的空间
     *
     * @param projectId      项目ID
     * @param filterFloorIds 筛选楼层ID, 为空时查询所有
     * @return 设备列表, {@code relObjIds} 字段为关联楼层ID
     * @since 5.240702.0
     */
    private List<DtpRwdSpaceDTO> querySpacesInFloor(
            @NonNull String projectId,
            @Nullable Collection<String> filterFloorIds,
            @NonNull List<String> existPeopleInfoCodeSpaceId) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        param.setId(existPeopleInfoCodeSpaceId);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByFloorId(filterFloorIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(projectId).warn("无有效楼层下的设备", Pair.of("filterFloorIds", filterFloorIds));
            return Lists.newArrayList();
        }
        return spaces;
    }


    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
