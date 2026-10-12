package com.persagy.apccontrol.basic.service.dashboard;

import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingEquipmentCountDataVo;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 15:54
 */
public interface DashboardLightingService {

    /**
     * 查询-Dashboard统计
     *
     * @param param
     * @return
     */
    LightingCountDataVo dashboard(DashBoardQueryCountParam param);

    /**
     * 查询-Dashboard设备统计
     *
     * @return
     */
    LightingEquipmentCountDataVo dashboardEquipmentStatistics();
    

}
