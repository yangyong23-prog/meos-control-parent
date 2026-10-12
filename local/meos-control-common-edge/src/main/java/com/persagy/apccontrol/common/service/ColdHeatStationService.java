package com.persagy.apccontrol.common.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;

/**
 * 冷热站
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/1/29 14:42
 */
public interface ColdHeatStationService {

    /**
     * 获取系统的运行模式
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    JSONObject getSystemRunningModel(String groupCode, String projectId, String systemId);

    /**
     * 获取系统的运行模式
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    MidCalendarAcSeasonTypeEnum getAcciSystemRunningModel(String groupCode, String projectId, String systemId);

}
