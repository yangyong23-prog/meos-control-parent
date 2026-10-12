package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 人员服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:22
 */
@FeignClient(name = "fm-person-server", url = "${inner-url.fm.fm-person.service:}")
public interface SaasOrgPersonClient {

    @PostMapping("/personManage/employee/queryPersonInfo")
    CustomerResponse<List<JSONObject>> queryPersonInfo(@RequestHeader(name = "groupCode") String groupCode, @RequestBody JSONObject personUserId);

}
