package com.persagy.apccontrol.basic.service.wsd;

import com.persagy.apccontrol.basic.model.param.wsd.QueryFloorSpaceEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.wsd.SpaceEquipmentVO;

import java.util.List;

/**
 * 给排水设备相关服务
 */
public interface WaterSupplyDrainageService {


    public List<SpaceEquipmentVO> querySpaceEquipmentList(QueryFloorSpaceEquipmentParam param);


}
