package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipStatisticsEnum;
import com.persagy.apccontrol.basic.model.param.ventilation.VentilationEquipStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.AlarmControlVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.RunStatusVo;
import com.persagy.apccontrol.basic.outside.dto.ExhaustFumeStatisticsVo;
import com.persagy.apccontrol.basic.outside.dto.param.ExhaustFumeStatisticsParam;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.ventilation.AbstractVentilationIotService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VentilationIotServiceCloud extends AbstractVentilationIotService {
    
    @Resource
    protected NacosSwitchConfig nacosSwitchConfig;

    @Resource
    private DtpPersistServerClient dtpPersistServerClient;

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(VentilationEquipStatisticsParam param) {
        
        Map<VentilationEquipStatisticsEnum, Set<String>> equipGroupIdMap = queryEquipGroupId();
        
        if (MapUtils.isEmpty(equipGroupIdMap)) {
            return new HashMap<>();
        }
        Map<String, Set<String>> equipGroupId = equipGroupIdMap.entrySet().stream().collect(HashMap::new,
                (k, v) -> k.get(v.getKey().value()), HashMap::putAll);

        ExhaustFumeStatisticsParam statisticsParam = new ExhaustFumeStatisticsParam();
        statisticsParam.buildObjects(equipGroupId);
        statisticsParam.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()));

        CustomerResponse<Map<String, ExhaustFumeStatisticsVo>> customerResponse = dtpPersistServerClient
                .watchFanStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);

        Map<String, ExhaustFumeStatisticsVo> map = CustomerResponse.handleResult(customerResponse);
        
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = buildEmptyResult(equipGroupId);
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                KitChenRangeHoodEquipStatisticsVo vo = r.get(k);
                v.setTotal(equipGroupId.get(k).size());
                vo.setAlarmControl(AlarmControlVo.builder()
                        .autoStatus(v.getAutoTotal())
                        .localStatus(v.getLocalTotal())
                        .manualStatus(v.getManualTotal())
                        .offLine(v.getOffLineTotal())
                        .orderFailAlarm(v.getAlarmTotal())
                        .build());
                vo.setRunStatus(RunStatusVo.builder()
                        .off(v.getStopTotal())
                        .on(v.getRunTotal())
                        .unknown(v.getUnknown())
                        .build());
                r.put(k, vo);
            });
        }
        
        return r;

    }
    
    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param) {
        if (MapUtils.isEmpty(param)) {
            return new HashMap<>();
        }
        
        ExhaustFumeStatisticsParam statisticsParam = new ExhaustFumeStatisticsParam();
        statisticsParam.buildObjects(param);
        statisticsParam.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()));
        
        CustomerResponse<Map<String, ExhaustFumeStatisticsVo>> customerResponse = dtpPersistServerClient
                .watchFanStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);
        
        Map<String, ExhaustFumeStatisticsVo> map = CustomerResponse.handleResult(customerResponse);
        
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = buildEmptyResult(param);
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                KitChenRangeHoodEquipStatisticsVo vo = r.get(k);
                v.setTotal(param.get(k).size());
                vo.setAlarmControl(AlarmControlVo.builder()
                                                 .autoStatus(v.getAutoTotal())
                                                 .localStatus(v.getLocalTotal())
                                                 .manualStatus(v.getManualTotal())
                                                 .offLine(v.getOffLineTotal())
                                                 .orderFailAlarm(v.getAlarmTotal())
                                                 .build());
                vo.setRunStatus(RunStatusVo.builder()
                                           .off(v.getStopTotal())
                                           .on(v.getRunTotal())
                                           .unknown(v.getUnknown())
                                           .build());
                r.put(k, vo);
            });
        }
        
        return r;
        
    }
}
