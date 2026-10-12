package com.persagy.apccontrol.basic.service.adapter;

import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;

import java.util.Map;

/**
 * 值班首页聚合接口适配器
 */
public interface HomePageCardIotDataAdapter {

    /**
     * @param queryParam         查询参数
     * @param RunningMonitorType 查询系统
     * @return 分组统计数据
     * @description 获取值班首页数据
     */
    DutyHomePageGroupDTO queryHomePageCardGroupIotData(Map<String, ObjectIdAndForceCodeQuery> queryParam, RunningMonitorTypeEnum RunningMonitorType);


}
