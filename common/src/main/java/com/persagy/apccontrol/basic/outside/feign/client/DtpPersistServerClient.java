package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.StatisManageTerminalDTO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ExhaustFumeStatisticsVo;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsDTO;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;
import com.persagy.apccontrol.basic.outside.dto.param.ExhaustFumeStatisticsParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate.DtpPersisWatchChillerStatisticsQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillPumpStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate.DtpPersisWatchChillerStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.wrapper.FeignParamPageExecutor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * BDTP 聚合服务--注意聚合服务的接口对项目ID不敏感，以 objectId 为准
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/3/10 14:16
 */
@FeignClient(name = "dtp-persist-server", url = "${url.cloud-dtp-persist-server:}")
public interface DtpPersistServerClient {


    @PostMapping({"/iot/ipcal/data/queryIPDataOnMoment"})
    DmpResult<List<EnergyDataResponse>> executeQueryIPDataOnMoment(@RequestParam("groupCode") String groupCode,
                                                                   @RequestParam("projectId") String projectId,
                                                                   @RequestParam("appId") String appId,
                                                                   @RequestParam("userId") String userId,
                                                                   @RequestBody QueryIPDataInPeriodDTO criteria);

    @PostMapping({"/iot/data/aggreg/watch/lightingStatistics"})
    CustomerResponse<LightingStatisticsResponse> lightingStatistics(@RequestParam("groupCode") String groupCode,
                                                                    @RequestParam("projectId") String projectId,
                                                                    @RequestBody LightingStatisticsDTO criteria);

    /**
     * 查询rwd 关系/对应数据接口
     * 对应的HBASE数据
     * 查找指定时间段的持久化数据
     *
     * @param param 查询参数
     * @return 能耗分精度
     * @since v5.1.0
     */
    @PostMapping("/iot/ipcal/data/queryIPDataInPeriod")
    CustomerResponse<List<EnergyDataResponse>> executeListIpDataInPeriod(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid EnergyDataQueryParam param);


    /**
     * 排油烟对象统计
     *
     * @param groupCode
     * @param projectId
     * @param param     code + objectIds
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/watch/exhaustFumeStatistics")
    CustomerResponse<Map<String, ExhaustFumeStatisticsVo>> watchExhaustFumeStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);


    /**
     * 通风统计
     *
     * @param groupCode
     * @param projectId
     * @param param     code + objectIds
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/watch/fanStatistics")
    CustomerResponse<Map<String, ExhaustFumeStatisticsVo>> watchFanStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);


    /**
     * 末端统计
     *
     * @param groupCode
     * @param projectId
     * @param param     code + objectIds
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/watch/terminalStatistics")
    CustomerResponse<Map<String, StatisManageTerminalDTO>> watchTerminalStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);

    /**
     * 冷机开启台数统计
     *
     * @param param 查询参数
     * @param body  请求体
     * @return 冷机开启台数
     * @since 7.250602.0
     */
    @PostMapping("/iot/data/aggreg/watch/chillerStatistics")
    CustomerResponse<Map<String, DtpPersisWatchChillerStatisticsResponse>> watchChillerStatistics(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid DtpPersisWatchChillerStatisticsQueryParam body);

    /**
     * 泵组开启台数统计
     *
     * @param param 查询参数
     * @param body  请求体
     * @return 泵组开启台数
     * @since 7.250602.0
     */
    @PostMapping("/iot/data/aggreg/watch/chillPumpStatistics")
    CustomerResponse<Map<String, DtpPersisWatchChillPumpStatisticsResponse>> watchChillPumpStatistics(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid DtpPersisWatchChillPumpStatisticsQueryParam body);


    //----------电价接口--------
    @PostMapping("/iot/ipcal/data/queryProjectElecPrice")
    CustomerResponse<List<EnergyDataResponse>> queryProjectElecPrice(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody @Valid ElectricityPriceQueryParam body);

    // 值班首页聚合接口开始 --------

    /**
     * 空调末端状态统计
     *
     * @param param
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/homepage/terminalObjectStatusStatistics")
    CustomerResponse<DutyHomePageGroupDTO> homepageTerminalObjectStatusStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);

    /**
     * 照明状态统计
     *
     * @param param
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/homepage/lightingStatusStatistics")
    CustomerResponse<DutyHomePageGroupDTO> homepageLightingStatusStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);

    /**
     * 通风状态统计
     *
     * @param param
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/homepage/fanEquipmentStatusStatistics")
    CustomerResponse<Map<String, DutyHomePageDTO>> homepageFanEquipmentStatusStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);


    /**
     * 厨房排油烟状态统计
     *
     * @param param
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/homepage/exhaustFumeStatusStatistics")
    CustomerResponse<DutyHomePageGroupDTO> homepageExhaustFumeStatusStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam param);


    /**
     * 冷热源状态统计
     *
     * @param param
     * @return 统计结果
     */
    @PostMapping("/iot/data/aggreg/homepage/coolHeatSysStatusStatistics")
    CustomerResponse<DutyHomePageGroupDTO> homepageCoolHeatSysStatusStatistics(
            @RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
            @RequestBody @Valid ExhaustFumeStatisticsParam.CoolHeatParam param);

    // 值班首页聚合接口结束 --------


    default DmpResult<List<EnergyDataResponse>> queryIPDataOnMoment(String groupCode,
                                                                    String projectId,
                                                                    String appId,
                                                                    String userId,
                                                                    QueryIPDataInPeriodDTO criteria) {
        if (Objects.nonNull(criteria)) {
            // 解决查询日分精度数据时缺失查询时间导致无法查出数据问题
            criteria.fillTodyTimeIf1d();
        }
        return FeignParamPageExecutor.execute(criteria, p1 ->
                executeQueryIPDataOnMoment(groupCode, projectId, appId, userId, p1)
        );

    }

    /**
     * 查询rwd 关系/对应数据接口
     * 对应的HBASE数据
     * 查找指定时间段的持久化数据
     *
     * @param param 查询参数
     * @return 能耗分精度
     * @since v5.1.0
     */
    default CustomerResponse<List<EnergyDataResponse>> listIpDataInPeriod(
            String groupCode, String projectId,
            EnergyDataQueryParam param) {

        return FeignParamPageExecutor.execute(param, p1 ->
                executeListIpDataInPeriod(groupCode, projectId, p1)
        );

    }
}
