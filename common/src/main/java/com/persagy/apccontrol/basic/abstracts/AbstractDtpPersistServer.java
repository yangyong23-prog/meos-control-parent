package com.persagy.apccontrol.basic.abstracts;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractDtpPersistServer implements LogComponent {

    public abstract List<EnergyDataResponse> listIpDataInPeriod(EnergyDataQueryParam param);

    public abstract CustomerResponse<List<EnergyDataResponse>> listIpDataInPeriod(String groupCode, String projectId, EnergyDataQueryParam param);

    public abstract DmpResult<List<EnergyDataResponse>> queryIPDataOnMoment(String groupCode, String projectId, String appId, String userId, QueryIPDataInPeriodDTO criteria);

    /**
     * 查询历史iot数据，按Map返回
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param param     参数
     * @return {@code Map<objectId, Map<infoCode, List<EnergyDataResponse>>> }
     * @since 7.250501.0
     */
    public Map<String, Map<String, List<EnergyDataResponse>>> mapObjectIdAndInfoCodeAndIpDataInPeriod(String groupCode, String projectId, EnergyDataQueryParam param) {
        CustomerResponse<List<EnergyDataResponse>> response = listIpDataInPeriod(groupCode, projectId, param);
        List<EnergyDataResponse> energyDataResponses = CustomerResponse.handleResult(response);
        if (CollectionUtils.isEmpty(energyDataResponses)) {
            return new HashMap<>();
        }
        return energyDataResponses.stream().collect(Collectors.groupingBy(EnergyDataResponse::getObjectId, Collectors.groupingBy(EnergyDataResponse::getInfoCode)));
    }

    /**
     * 查询最新iot数据
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param appId     应用ID
     * @param userId    用户id
     * @param criteria  参数
     * @return {@code Map<objectId, Map<infoCode, List<EnergyDataResponse>>> }
     * @since 7.250501.0
     */
    public Map<String, Map<String, List<EnergyDataResponse>>> mapObjectIdAndInfoCodeAndIpDataOnMoment(String groupCode, String projectId, String appId, String userId, QueryIPDataInPeriodDTO criteria) {
        DmpResult<List<EnergyDataResponse>> response = queryIPDataOnMoment(groupCode, projectId, appId, userId, criteria);
        List<EnergyDataResponse> energyDataResponses = response.getData();
        if (CollectionUtils.isEmpty(energyDataResponses)) {
            return new HashMap<>();
        }
        return energyDataResponses.stream().collect(Collectors.groupingBy(EnergyDataResponse::getObjectId, Collectors.groupingBy(EnergyDataResponse::getInfoCode)));
    }
}
