package com.persagy.apccontrol.common.server;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.framework.common.context.ContextUtil;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpPersistServerEdge extends AbstractDtpPersistServer implements BaseFeignClientService {

    private final DtpPersistServerClient dtpPersistServerClient;
    private final DtpTwinClientService dtpTwinClientService;

    @Override
    public List<EnergyDataResponse> listIpDataInPeriod(EnergyDataQueryParam param) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        return query(false, "分精度数据", param, () ->
                dtpPersistServerClient.listIpDataInPeriod(groupCode, projectId, param), ArrayList::new);
    }

    @Override
    public CustomerResponse<List<EnergyDataResponse>> listIpDataInPeriod(String groupCode, String projectId, EnergyDataQueryParam param) {
        return dtpPersistServerClient.listIpDataInPeriod(groupCode, projectId, param);
    }

    @Override
    public DmpResult<List<EnergyDataResponse>> queryIPDataOnMoment(String groupCode, String projectId, String appId, String userId, QueryIPDataInPeriodDTO criteria) {

        if (criteria == null || Collections.isEmpty(criteria.getObjects())) {
            return null;
        }
        List<QueryTwinDataOnMomentDTO> queryTwinDataOnMomentDTOS = new ArrayList<>();
        for (QueryIPDataInPeriodDTO.ObjectInfo objectInfo : criteria.getObjects()) {
            queryTwinDataOnMomentDTOS.add(new QueryTwinDataOnMomentDTO(projectId, objectInfo.getObjectId(), Sets.newHashSet(objectInfo.getInfoCodes())));
        }
        QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO(queryTwinDataOnMomentDTOS);
        List<DtpTwinIotData> iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(groupCode, projectId, null, queryTwinDataDTO);

        if (Collections.isEmpty(iotDataList)) {
            return null;
        }
        DmpResult<List<EnergyDataResponse>> r = new DmpResult<>();

        List<EnergyDataResponse> dataList = new ArrayList<>();
        iotDataList.forEach(iotData -> {
            try {
                EnergyDataResponse data = new EnergyDataResponse();
                data.setObjectId(iotData.getObjectId());
                data.setInfoCode(iotData.getInfoCode());
                data.setIpType("1min");
                data.setIpTime(iotData.getDataTime());
                data.setIpValue(new BigDecimal(iotData.getDataValue()));
                data.setDataStatus(iotData.getDataStatus());
                dataList.add(data);
            } catch (Exception e) {
                log.error("获取孪生数据异常转换异常", e);
            }
        });
        r.setData(dataList);
        return r;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "持久化服务边缘端实现";
    }
}
