package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistAirTerminalServiceSpaceRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空调末端服务的空间
 * 查询的是排除的空间 总空间去掉 末端的空间就是目标空间
 */
@Validated
@Component(DemandBlacklistAirTerminalStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistAirTerminalStrategy extends AbstractDemandBlacklistStrategy<DemandBlacklistAirTerminalServiceSpaceRuleDTO> {
    
    public static final String BEAN_NAME = "demandBlacklistAirTerminalStrategy";
    private final DtpRwdClientService dtpRwdClientService;
    
    @Override
    public Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType,
                                   @NonNull DemandBlacklistAirTerminalServiceSpaceRuleDTO configData) {
        //查询全量空间
        Set<String> result = getAllSpaceId();
        
        //查询空调末端服务的空间
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.AIR_TERMINAL);
        List<DtpRwdSpaceDTO> spaces = listEquipmentSpaces(spaceQueryParam);
        
        if (CollectionUtils.isEmpty(spaces)) {
            return result;
        }
        Set<String> ids = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
        result.removeAll(ids);
        return result;
    }
    
    /**
     * 查询末端服务的空间
     *
     * @param param
     * @return
     */
    public List<DtpRwdSpaceDTO> listEquipmentSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        // 查询指定设备类下所有对象ID
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        
        RunningMonitorEquipmentQueryParam spaceQueryParam = new RunningMonitorEquipmentQueryParam();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = RunningMonitorSecondTypeEnum.AIR_TERMINAL;
        spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<? extends DtpRwdEquipmentDTO> epList =
                runningMonitorSecondType.toRunningMonitorTypeEnum().getRunningMonitorStrategy().listEquipments(spaceQueryParam);
        
        if (CollectionUtils.isEmpty(epList)) {
            return Collections.emptyList();
        }
        
        Set<String> equipmentIds = epList.stream()
                                         .map(DtpRwdObjectDTO::getId)
                                         .collect(Collectors.toSet());
        
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Collections.emptyList();
        }
        
        DtpRwdObjectQueryParam serviceSpaceQueryParam = new DtpRwdObjectQueryParam();
        serviceSpaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        return param.queryObject(serviceSpaceQueryParam, (queryParam) ->
                dtpRwdClientService.listSpaceServiceByEquipmentId(equipmentIds, queryParam));
    }
    
    
}
