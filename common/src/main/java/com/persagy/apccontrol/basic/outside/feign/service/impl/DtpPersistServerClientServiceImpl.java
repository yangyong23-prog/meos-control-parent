package com.persagy.apccontrol.basic.outside.feign.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillerStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillerStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpPersistServerClientService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Map;

/**
 * 持久化服务客户端实现
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpPersistServerClientServiceImpl implements DtpPersistServerClientService {

    private final DtpPersistServerClient dtpPersistServerClient;

    @Override
    public @NonNull Map<String, DtpPersisWatchChillerStatisticsResponse> watchChillerStatistics(
            @NonNull DtpPersisWatchChillerStatisticsQueryParam param) {
        DtpRwdCommonParam instance = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_066b1c514d034b3293dd2c1520801606"), param, () ->
                dtpPersistServerClient.watchChillerStatistics(instance, param), Collections::emptyMap);
    }

    @Override
    public @NonNull Map<String, DtpPersisWatchChillPumpStatisticsResponse> watchChillPumpStatistics(
            @NonNull DtpPersisWatchChillPumpStatisticsQueryParam param) {
        DtpRwdCommonParam instance = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_00915e344ffd442ba9c271a1aa657433"), param, () ->
                dtpPersistServerClient.watchChillPumpStatistics(instance, param), Collections::emptyMap);
    }

    @Override
    public String logTag() {
        return "持久化服务";
    }
}
