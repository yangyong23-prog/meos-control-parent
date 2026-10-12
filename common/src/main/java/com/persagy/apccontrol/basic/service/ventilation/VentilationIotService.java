package com.persagy.apccontrol.basic.service.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipStatisticsEnum;
import com.persagy.apccontrol.basic.model.param.ventilation.VentilationEquipStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;

import java.util.Map;
import java.util.Set;

/**
 * 通风 iot相关操作
 * 设计云边接口差异的接口
 */
public interface VentilationIotService {
    
    
    /**
     * 查询通风设备分组
     */
    Map<VentilationEquipStatisticsEnum, Set<String>> queryEquipGroupId();
    
    /**
     * 通风设备统计
     *
     * @param param
     */
    Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(VentilationEquipStatisticsParam param);
    
    
    /**
     * 通风设备统计
     *
     * @param param
     */
    Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param);
    
    
}
