package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.dto.lighting.DifyDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DifyResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.Collection;
import java.util.List;

/**
 * @author weiyizhong
 * @Company yushu.biz
 * @date 2023/10/26 14:09
 * @description: dify 服务客户端
 */
@FeignClient(name = "dify-server", url = "${external-url.dify.dify-server.service:}")
public interface DifyServerClient {


    /**
     *
     * @param difyDTO
     * @return  http://localhost/triggers/webhook/s0iuq6dKTNvlwZWutvU2erjK
     */
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<?> webhook(@RequestBody DifyDTO difyDTO);



}
