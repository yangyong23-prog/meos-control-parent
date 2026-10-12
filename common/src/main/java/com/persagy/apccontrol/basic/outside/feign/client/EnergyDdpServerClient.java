package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @Description: 后评估报告
 * @Author: laixingqi
 * @Date: 2023/10/12 14:19
 * @param:
 * @return:
 **/
@FeignClient(name = "energy-ddp-server", url = "${url.cloud-energy-ddp-server:}")
public interface EnergyDdpServerClient {

    @PostMapping("/meetingReportConfig/queryEfficiencyResultByProjectId")
    String queryByProjectId(@RequestHeader String groupCode, @RequestBody JSONObject projectId);

}
