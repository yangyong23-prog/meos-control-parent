package com.persagy.apccontrol.basic.outside.feign.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 品质平衡服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/21 20:22
 */
public interface EnergyQualityServerService {

    /**
     * 查询气候区
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    Object querySeason(String groupCode, String projectId, JSONObject param);

    /**
     * 查询集团标准
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    Object queryStandard(String groupCode, String projectId, JSONObject param);

}
