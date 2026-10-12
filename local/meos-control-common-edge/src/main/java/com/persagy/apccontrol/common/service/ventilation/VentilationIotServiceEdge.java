package com.persagy.apccontrol.common.service.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipStatisticsEnum;
import com.persagy.apccontrol.basic.model.param.ventilation.VentilationEquipStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.service.ventilation.AbstractVentilationIotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VentilationIotServiceEdge extends AbstractVentilationIotService {
    
    private final DtpTwinClientService dtpTwinClientService;

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(VentilationEquipStatisticsParam param) {
        
        Map<VentilationEquipStatisticsEnum, Set<String>> equipGroupId = queryEquipGroupId();
        if (MapUtils.isEmpty(equipGroupId)) {
            return new HashMap<>();
        }
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = new HashMap<>();
        equipGroupId.forEach((k, v) -> {
            Map<String, Set<String>> p = new HashMap<>();
            p.put("ffscafObjIds", v);
            Map<String, KitChenRangeHoodEquipStatisticsVo> response =
                    dtpTwinClientService.ventilationEquipStatistics(p);
            if (MapUtils.isNotEmpty(response)) {
                r.put(k.value(), response.get("ffscafInfo"));
            }
        });
        return r;
    }
    
    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param) {
        if (MapUtils.isEmpty(param)) {
            return new HashMap<>();
        }
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = new HashMap<>();
        param.forEach((k, v) -> {
            Map<String, Set<String>> p = new HashMap<>();
            p.put("ffscafObjIds", v);
            Map<String, KitChenRangeHoodEquipStatisticsVo> response =
                    dtpTwinClientService.ventilationEquipStatistics(p);
            if (MapUtils.isNotEmpty(response)) {
                r.put(k, response.get("ffscafInfo"));
            }
        });
        return r;
    }
}
