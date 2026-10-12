package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 新增品质平衡服务的接口
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:22
 */
@FeignClient(name = "energy-quality-server", url = "${inner-url.energy.energy-quality.service:}")
public interface EnergyQualityServerClient {

    /**
     *
     * @param groupCode
     * @param projectId
     * @param param
     * {
     *     "groupCode": "TYXN",
     *     "firstZoneCode": "0",
     *     "secondZoneCode": "30"
     * }
     * @return
     */
    @PostMapping("/quality/common/querySeason")
    CustomerResponse<Object> querySeason(@RequestHeader(name = "groupCode") String groupCode, @RequestHeader(name = "projectId") String projectId,
                                         @RequestBody JSONObject param);

    /**
     *
     * @param groupCode
     * @param projectId
     * @param param
     * {
     *     "groupCode": "TYXN",
     *     "projectId": "Pj9909990007"
     * }
     * @return
     */
    @PostMapping("/quality/common/queryStandard")
    CustomerResponse<Object> queryStandard(@RequestHeader(name = "groupCode") String groupCode, @RequestHeader(name = "projectId") String projectId,
                                         @RequestBody JSONObject param);


}
