package com.persagy.apccontrol.basic.service.dashboard;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.CircuitStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.dashboard.ControllerStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingEquipmentCountDataVo;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;
import com.persagy.apccontrol.basic.service.adapter.DashboardLightingStatisticsDataAdapter;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 15:54
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardLightingServiceImpl implements DashboardLightingService {

    private final DashboardLightingStatisticsDataAdapter dashboardLightingStatisticsDataAdapter;

    private final DemandLightingStrategy demandLightingStrategy;

    private final AbstractLightingService lightingService;

    @Override
    public LightingCountDataVo dashboard(DashBoardQueryCountParam param) {
        return lightingService.dashboard(param);
    }

    @Override
    public LightingEquipmentCountDataVo dashboardEquipmentStatistics() {
        LightingEquipmentCountDataVo lightingEquipmentCountDataVo = new LightingEquipmentCountDataVo();
        // 照明回路对象id列表
        List<String> ltltccEquipIds = new ArrayList<>();
        // 照明控制模块对象id列表
        List<String> ltltcuEquipIds = new ArrayList<>();
        // 公共照明回路对象列表
        generateEquipIds(ltltccEquipIds, ltltcuEquipIds, DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING);
        // 停车场照明回路对象列表
        generateEquipIds(ltltccEquipIds, ltltcuEquipIds, DemandTemplateTypeEnum.PARKING_LOT_LIGHTING);
        CircuitStatisticsVo circuitStatisticsVo = new CircuitStatisticsVo();
        if (CollectionUtils.isNotEmpty(ltltccEquipIds)) {
            circuitStatisticsVo.setTotalCircuitCount(ltltccEquipIds.size());
        }
        // 照明回路统计数据
        Map<String, LightingStatisticsResponse> countDataMap = dashboardLightingStatisticsDataAdapter.getLightingStatisticsData(ltltccEquipIds, ltltcuEquipIds);
        LightingStatisticsResponse ltltccCountData = countDataMap.get(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue());
        if (!Objects.isNull(ltltccCountData)) {
            circuitStatisticsVo.setOfflineCount(ltltccCountData.getOffLineTotal());
            circuitStatisticsVo.setOrderFailAlarmCount(ltltccCountData.getAlarmTotal());
            circuitStatisticsVo.setAutoCount(ltltccCountData.getAutoTotal());
            circuitStatisticsVo.setManualCount(ltltccCountData.getManualTotal());
        }
        lightingEquipmentCountDataVo.setCircuitStatistics(circuitStatisticsVo);
        ControllerStatisticsVo controllerStatisticsVo = new ControllerStatisticsVo();
        if (CollectionUtils.isNotEmpty(ltltcuEquipIds)) {
            controllerStatisticsVo.setTotalControllerCount(ltltcuEquipIds.size());
        }
        // 照明控制统计数据
        LightingStatisticsResponse ltltcuCountData = countDataMap.get(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.getValue());
        if (!Objects.isNull(ltltcuCountData)) {
            controllerStatisticsVo.setOfflineCount(ltltcuCountData.getOffLineTotal());
        }
        lightingEquipmentCountDataVo.setControllerStatistics(controllerStatisticsVo);
        return lightingEquipmentCountDataVo;
    }
    
    private void generateEquipIds(List<String> ltltccEquipIds, List<String> ltltcuEquipIds, DemandTemplateTypeEnum demandTemplateTypeEnum) {
        // 照明回路
        List<DtpRwdCircuitDTO> dtpRwdCircuitDtoList = demandLightingStrategy.listCircuitsByInstance(
                ContextUtil.getProjectId(), demandTemplateTypeEnum, DemandInstanceTypeEnum.PROJECT, ContextUtil.getProjectId());
        List<String> ltltccObjIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dtpRwdCircuitDtoList)) {
            ltltccObjIds.addAll(dtpRwdCircuitDtoList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toList()));
            ltltccEquipIds.addAll(dtpRwdCircuitDtoList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toList()));
        }
        // 控制模块
        List<DtpRwdObjectDTO> dtpRwdObjectList = demandLightingStrategy.listControllersByCircuit(
                ContextUtil.getProjectId(), demandTemplateTypeEnum, ltltccObjIds, Lists.newArrayList());
        if (CollectionUtils.isNotEmpty(dtpRwdObjectList)) {
            ltltcuEquipIds.addAll(dtpRwdObjectList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        }
    }


}
