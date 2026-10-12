//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.AirTerminalZoneControllerParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosAllControllerParameterQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosScheduleInfoCodeDefineQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.AirTerminalZoneControllerParameterResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.DtpMeosAllControllerParameterResponse;
import com.persagy.apccontrol.basic.structure.control.model.SimpleInfoCodeDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * dtp-twin-server 服务
 */
@FeignClient(name = "dtp-meos-server", url = "${inner-url.bdtp.dtp-meos.service:}")
@RefreshScope
public interface DtpMeosServerCloudClient {

    /**
     * 查询-获取所有控制器参数
     *
     * @param param 请求参数
     * @return 获取所有控制器参数
     * @since 7.260201.0
     */
    @PostMapping("/runConsole/allControllerParameter")
    CustomerResponse<List<DtpMeosAllControllerParameterResponse>> getAllControllerParameter(
            @RequestBody DtpMeosAllControllerParameterQueryParam param);

    /**
     * 查询-获取所有控制器参数
     *
     * @param param 查询参数
     * @return 获取所有控制器参数
     * @since 7.260201.0
     */
    @GetMapping("/demandStrategy/listScheduleInfoCodeDefine")
    CustomerResponse<Map<String, List<SimpleInfoCodeDTO>>> listScheduleInfoCodeDefine(
            @SpringQueryMap DtpMeosScheduleInfoCodeDefineQueryParam param);

    /**
     * 查询-获取所有控制器参数
     *
     * @param param 请求参数
     * @return 获取所有控制器参数
     */
    @PostMapping("/runConsole/airTerminalZoneControllerParameter")
    CustomerResponse<AirTerminalZoneControllerParameterResponse> airTerminalZoneControllerParameter(
            @RequestBody AirTerminalZoneControllerParam param);

}
