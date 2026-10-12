package com.persagy.apccontrol.service;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.outside.energy.StatisticsTypeEnum;
import com.persagy.apccontrol.basic.model.vo.outside.energy.StatisticsVO;

import java.util.List;

public interface LightingStatisticsService extends LogComponent {

    List<DemandTemplateTypeEnum> ALL_LIGHTING_TYPE_LIST = Lists.newArrayList(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
            DemandTemplateTypeEnum.SIGN_LIGHTING,DemandTemplateTypeEnum.BUILDING_LIGHTING,DemandTemplateTypeEnum.LANDSCAPE_LIGHTING,
            DemandTemplateTypeEnum.PARKING_LOT_LIGHTING);
    StatisticsVO getLightingStatistics(StatisticsTypeEnum type);
}
