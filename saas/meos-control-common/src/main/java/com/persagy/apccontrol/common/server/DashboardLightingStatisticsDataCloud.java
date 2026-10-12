package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsDTO;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.adapter.DashboardLightingStatisticsDataAdapter;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : wangchenghong
 * @date : 2024/12/12 17:51
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardLightingStatisticsDataCloud implements DashboardLightingStatisticsDataAdapter {

    private final DtpPersistServerClient dtpPersistServerClient;
    private final NacosSwitchConfig nacosSwitchConfig;
    @Override
    public Map<String, LightingStatisticsResponse> getLightingStatisticsData(List<String> ltltccEquipIds, List<String> ltltcuEquipIds) {
        Map<String, LightingStatisticsResponse> responseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(ltltccEquipIds)) {
            responseMap.put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue(), getLightingStatistics(ltltccEquipIds));
        }
        if (CollectionUtils.isNotEmpty(ltltcuEquipIds)) {
            responseMap.put(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.getValue(), getLightingStatistics(ltltcuEquipIds));
        }
        return responseMap;
    }

    private LightingStatisticsResponse getLightingStatistics(List<String> equipIds) {
        if (CollectionUtils.isEmpty(equipIds)) {
            return null;
        }
        LightingStatisticsDTO lightingStatisticsDTO = new LightingStatisticsDTO();
        lightingStatisticsDTO.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        lightingStatisticsDTO.setObjectIds(equipIds);
        CustomerResponse<LightingStatisticsResponse> resultList = dtpPersistServerClient.lightingStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), lightingStatisticsDTO);
        if ("success".equals(resultList.getResult()) && !Objects.isNull(resultList.getContent())) {
            return resultList.getContent();
        }
        return null;
    }

}
