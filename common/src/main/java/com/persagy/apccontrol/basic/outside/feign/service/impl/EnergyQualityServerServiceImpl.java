package com.persagy.apccontrol.basic.outside.feign.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.client.EnergyQualityServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.service.EnergyQualityServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 品质平衡服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/21 20:24
 */
@Service
public class EnergyQualityServerServiceImpl implements EnergyQualityServerService {

    @Autowired
    private EnergyQualityServerClient energyQualityServerClient;

    @Override
    public Object querySeason(String groupCode, String projectId, JSONObject param) {
        CustomerResponse<Object> customerResponse = this.energyQualityServerClient.querySeason(groupCode, projectId, param);
        return customerResponse == null ? null : customerResponse.getContent();
    }

    @Override
    public Object queryStandard(String groupCode, String projectId, JSONObject param) {
        CustomerResponse<Object> customerResponse = this.energyQualityServerClient.queryStandard(groupCode, projectId, param);
        return customerResponse == null ? null : customerResponse.getContent();
    }

}
