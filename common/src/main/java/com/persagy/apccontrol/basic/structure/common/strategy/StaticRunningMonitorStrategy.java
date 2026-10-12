package com.persagy.apccontrol.basic.structure.common.strategy;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSystemQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 静态运行监测策略
 *
 * @author JiYinchuan
 * @since 7.250101.0
 */
@RequiredArgsConstructor
public class StaticRunningMonitorStrategy implements RunningMonitorStrategy {

    /**
     * 运行监测类型
     */
    private final RunningMonitorTypeEnum runningMonitorType;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return runningMonitorType;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Lists.newArrayList();
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(
            @NonNull RunningMonitorSpaceQueryParam param) {
        return Lists.newArrayList();
    }
    
    @Override
    public List<? extends DtpRwdObjectDTO> listDashboardSystems(
            @NonNull RunningMonitorSystemQueryParam param) {
        return Lists.newArrayList();
    }

    @Override
    public List<? extends DtpRwdSpaceVO> listDashboardSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Lists.newArrayList();
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {
        return Lists.newArrayList();
    }

    @Override
    public String logTag() {
        return "运行监测-" + runningMonitorType().getReasonPhrase() + "策略";
    }
}
