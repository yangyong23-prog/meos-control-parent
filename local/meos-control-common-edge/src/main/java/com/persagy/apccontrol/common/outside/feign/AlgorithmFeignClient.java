package com.persagy.apccontrol.common.outside.feign;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.common.outside.model.param.HistoryStorageParam;
import com.persagy.apccontrol.common.outside.model.response.HistoryStorageResponse;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/21 11:00
 */
@RefreshScope
@FeignClient(name = "ai-au-server-edge", url = "${inner-url.bdtp.ai-au.service:}")
public interface AlgorithmFeignClient {

    /**
     * 边缘侧开关机计算
     *
     * @param param
     * @return
     */
    @PostMapping(value = {"/open-close/start"})
    JSONObject openCloseStart(@RequestBody JSONObject param);

    /*********************************************** 旧版服务接口 ***************************************************/

    /**
     * 获取蓄冷标签
     * startTime 开始时间   yyyyMMddHHmmss
     * endTime  结束时间    yyyyMMddHHmmss
     *
     * @param groupCode
     * @param projectId
     * @return time        string      yyyyMMddHHmmss
     * storageTag	number	    蓄冷标签，0：不蓄冷，1：蓄冷
     */
    @PostMapping(value = {"/cooling-supply/tag/getStorage"})
    CustomerResponse<List<JSONObject>> getCoolStorageTag(@RequestParam(name = "groupId") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 获取蓄热标签
     * startTime 开始时间   yyyyMMddHHmmss
     * endTime  结束时间    yyyyMMddHHmmss
     *
     * @param groupCode
     * @param projectId
     * @return time        string      yyyyMMddHHmmss
     * storageTag	number	    蓄热标签，0：不蓄热，1：蓄热
     */
    @PostMapping(value = {"/heating-supply/tag/getStorage"})
    CustomerResponse<List<JSONObject>> getHeatStorageTag(@RequestParam(name = "groupId") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 供冷季-获取空间降温难度系数与成本
     * startTime 开始时间   HH:mm
     * endTime  结束时间    HH:mm
     *
     * @param groupCode
     * @param projectId
     * @return spaceId    string
     * difficultyIndex	number	    难度系数
     * expense	number	    成本
     */
    @PostMapping(value = {"/cooling-supply/space/getCoolingDifficultyIndexAndExpenseEstimate"})
    CustomerResponse<List<JSONObject>> getCoolingDifficultyIndexAndExpenseEstimate(@RequestParam(name = "groupId") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 供热季-获取空间降温难度系数与成本
     * startTime 开始时间   HH:mm
     * endTime  结束时间    HH:mm
     *
     * @param groupCode
     * @param projectId
     * @return spaceId    string
     * difficultyIndex	number	    难度系数
     * expense	number	    成本
     */
    @PostMapping(value = {"/heating-supply/space/getHeatingDifficultyIndexAndExpenseEstimate"})
    CustomerResponse<List<JSONObject>> getHeatDifficultyIndexAndExpenseEstimate(@RequestParam(name = "groupId") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);


    /*********************************************** 新版服务接口 ***************************************************/

    /**
     * 获取提前开机时长
     * {
     * "spaces": ["Sp6516478"],
     * "baseTime": "HH:mm"
     * }
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    @PostMapping(value = {"/v1/spaces/bulk-pre-start-durations"})
    CustomerResponse<List<JSONObject>> queryBulkPreStartDurations(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 获取提前关机时长
     * {
     * "spaces": ["Sp6516478"],
     * "baseTime": "HH:mm"
     * }
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    @PostMapping(value = {"/v1/spaces/bulk-pre-shutdown-durations"})
    CustomerResponse<List<JSONObject>> queryBulkPreShutdownDurations(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 获取提前蓄冷时长
     * {
     * "spaces": ["Sp6516478"],
     * "baseTime": "HH:mm"
     * }
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    @PostMapping(value = {"/v1/spaces/bulk-storage-start-durations"})
    CustomerResponse<List<JSONObject>> queryBulkStorageStartDurations(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 获取难度系数
     * <p>
     * {
     * "spaces": ["Sp6516478"],
     * "baseTime": "HH:mm"
     * }
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    @PostMapping(value = {"/v1/spaces/bulk-air-conditioning-difficulty"})
    CustomerResponse<List<JSONObject>> queryBulkAirConditioningDifficulty(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 获取供冷成本
     * <p>
     * {
     * "spaces": ["Sp6516478"],
     * "baseTime": "HH:mm"
     * }
     *
     * @param groupCode
     * @param projectId
     * @param param
     * @return
     */
    @PostMapping(value = {"/v1/spaces/bulk-air-conditioning-costs"})
    CustomerResponse<List<JSONObject>> queryBulkAirConditioningCosts(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestBody JSONObject param);

    /**
     * 蓄冷/热标签，算法未实现，用旧版的
     *
     * @param groupCode
     * @param projectId
     * @param baseTime  HH:mm
     * @return 0:无，1:蓄冷，2:蓄热
     */
    @Deprecated
    @GetMapping(value = {"/v1/project/storage/tag"})
    CustomerResponse<JSONObject> queryStorageTag(@RequestParam(name = "groupCode") String groupCode, @RequestParam(name = "projectId") String projectId, @RequestParam(name = "baseTime") String baseTime);

    /**
     * 获取历史蓄冷蓄热标签
     *
     * @param param param
     * @return {@link CustomerResponse }<{@link List }<{@link HistoryStorageResponse }>>
     */
    @PostMapping("/tag/getHistoryStorage")
    CustomerResponse<List<HistoryStorageResponse>> getHistoryStorage(@RequestBody HistoryStorageParam param);

}
