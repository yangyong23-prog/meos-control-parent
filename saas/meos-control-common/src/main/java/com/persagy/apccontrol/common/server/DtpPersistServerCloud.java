package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.abstracts.AbstractDtpPersistServer;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpPersistServerCloud extends AbstractDtpPersistServer implements BaseFeignClientService {

    private final DtpPersistServerClient dtpPersistServerClient;

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
        return dtpPersistServerClient.queryIPDataOnMoment(groupCode, projectId, appId, userId, criteria);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "持久化服务云端实现";
    }
}
