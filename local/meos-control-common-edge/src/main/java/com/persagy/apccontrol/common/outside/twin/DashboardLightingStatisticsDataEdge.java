package com.persagy.apccontrol.common.outside.twin;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.adapter.DashboardLightingStatisticsDataAdapter;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 照明Dashboard设备统计
 *
 * @author : wangchenghong
 * @date : 2024/12/12 18:17
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardLightingStatisticsDataEdge implements DashboardLightingStatisticsDataAdapter {

    private final DtpTwinClient dtpTwinClient;

    @Override
    public Map<String, LightingStatisticsResponse> getLightingStatisticsData(List<String> ltltccEquipIds, List<String> ltltcuEquipIds) {
        Map<String, Map<String, Map<String, List<String>>>> queryParam = new HashMap<>();
        Map<String, List<String>> ltltccChildParam = new HashMap<>();
        ltltccChildParam.put("objectIds", ltltccEquipIds);
        Map<String, Map<String, List<String>>> typeChildParam = new HashMap<>();
        typeChildParam.put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue(), ltltccChildParam);
        Map<String, List<String>> ltltcuChildParam = new HashMap<>();
        ltltcuChildParam.put("objectIds", ltltcuEquipIds);
        typeChildParam.put(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.getValue(), ltltcuChildParam);
        queryParam.put(SubscribeTypeEnum.LIGHT_MONITORING.getValue(), typeChildParam);
        CustomerResponse<Map<String, Map<String, LightingStatisticsResponse>>> response = dtpTwinClient.runLtltcc(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
        if ("success".equals(response.getResult()) && null != response.getContent() && response.getContent().size() != 0) {
            Map<String, LightingStatisticsResponse> dataContent = response.getContent().get(SubscribeTypeEnum.LIGHT_MONITORING.getValue());
            if (null != dataContent && dataContent.size() != 0) {
                return dataContent;
            }
        }
        return new HashMap<>();
    }
}
