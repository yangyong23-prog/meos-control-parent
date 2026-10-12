package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillerStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillerStatisticsResponse;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.Map;

/**
 * 持久化服务客户端
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
public interface DtpPersistServerClientService extends BaseFeignClientService {

    /**
     * 冷机开启台数统计
     *
     * @param param 查询参数
     * @return 冷机开启台数
     * @since 7.250602.0
     */
    @NonNull
    Map<String, DtpPersisWatchChillerStatisticsResponse> watchChillerStatistics(
            @NonNull @Valid DtpPersisWatchChillerStatisticsQueryParam param);

    /**
     * 泵组开启台数统计
     *
     * @param param 查询参数
     * @return 泵组开启台数
     * @since 7.250602.0
     */
    @NonNull
    Map<String, DtpPersisWatchChillPumpStatisticsResponse> watchChillPumpStatistics(
            @NonNull @Valid DtpPersisWatchChillPumpStatisticsQueryParam param);

}