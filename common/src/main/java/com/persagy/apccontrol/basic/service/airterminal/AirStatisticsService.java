package com.persagy.apccontrol.basic.service.airterminal;

import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;

import java.util.List;

/**
 * 末端设备统计 云边都支持的接口
 */
public interface AirStatisticsService {


    /**
     * 首页统计
     *
     * @return
     */
    public List<AirHomePageEquipmentVO> homePageEquipment(AirHomePageEquipmentParam param);

}
