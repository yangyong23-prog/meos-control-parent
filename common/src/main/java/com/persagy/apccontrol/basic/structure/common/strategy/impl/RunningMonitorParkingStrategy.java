package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 停车场策略实现
 *
 * @author JiYinchuan
 * @since 5.240802.0
 */
@Component(RunningMonitorParkingStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorParkingStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorParkingLotStrategy";
    public static final String LOG_TAG = "运行监测-停车场";

    private final DtpRwdClientService dtpRwdClientService;

    /***空间类型*/
    public static final List<String> ROOM_FUNC_TYPE =
            Lists.newArrayList("SFT-04.01.00", "SFT-04.02.00", "SFT-04.03.00", "SFT-04.04.00", "SFT-04.05.00"
                    , "SFT-04.06.00", "SFT-04.07.00");

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.PARKING;
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
        DemandInstanceTypeEnum instanceType = instance.getInstanceType();
        String instanceId = instance.getInstanceId();
        String projectId = ContextUtil.getProjectId();
        List<DtpRwdSpaceDTO> spaces = new ArrayList<>();
        switch (instanceType) {
            case PROJECT:
                spaces = querySpaceInProjectId(projectId, null, ROOM_FUNC_TYPE);
                break;
            case BUILDING:
                spaces = querySpaceInBuilding(projectId, Lists.newArrayList(instanceId), ROOM_FUNC_TYPE);
                break;
            case FLOOR:
                spaces = querySpacesInFloor(projectId, Lists.newArrayList(instanceId), ROOM_FUNC_TYPE);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(instanceType.getClass().getSimpleName(), instanceType);
        }
        return spaces;

    }

    /**
     * 查询项目下的空间
     *
     * @param projectId         项目id
     * @param filterBuildingIds 过滤建筑ID
     * @return {@link List }<{@link DtpRwdEquipmentDTO }>
     * @since 5.240702.0
     */
    private List<DtpRwdSpaceDTO> querySpaceInProjectId(
            @NonNull String projectId, @Nullable Collection<String> filterBuildingIds,
            @NonNull List<String> roomFuncType) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.EQUALS_IN, roomFuncType);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(false, false,
                projectId, param);
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
    private List<DtpRwdSpaceDTO> querySpaceInBuilding(
            @NonNull String projectId, @Nullable Collection<String> filterBuildingIds, @NonNull List<String> roomFuncType) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.EQUALS_IN, roomFuncType);
        param.addWithColumn(DtpRwdEquipmentDTO.Fields.class);
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
    private List<DtpRwdSpaceDTO> querySpacesInFloor(
            @NonNull String projectId, @Nullable Collection<String> filterFloorIds, @NonNull List<String> roomFuncType) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.addInfoCode("roomFuncType", DtpRwdCriteriaSignEnum.EQUALS_IN, roomFuncType);
        param.addWithColumn(DtpRwdEquipmentDTO.Fields.class);
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceByFloorId(filterFloorIds, param);
        if (CollectionUtils.isEmpty(spaces)) {
            logProjectInstance(projectId).warn("无有效楼层下的设备", Pair.of("filterFloorIds", filterFloorIds));
            return Lists.newArrayList();
        }
        return spaces;
    }

    @Override
    public void setExceptionEventObjectQueryParam(InfoCodeObjectQueryParam infoCodeObjectQueryParam) {
        infoCodeObjectQueryParam.setClassCodes(Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.value(), DtpRwdClassCodeEnum.CFPKPS.value()));
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
