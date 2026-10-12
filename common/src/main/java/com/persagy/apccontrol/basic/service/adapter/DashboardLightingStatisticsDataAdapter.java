package com.persagy.apccontrol.basic.service.adapter;

import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;

import java.util.List;
import java.util.Map;

/**
 * 照明Dashboard设备统计接口适配器
 */
public interface DashboardLightingStatisticsDataAdapter {

    /**
     * @description 获取照明Dashboard设备统计数据
     */
    Map<String, LightingStatisticsResponse> getLightingStatisticsData(List<String> ltltccEquipIds, List<String> ltltcuEquipIds);

}
