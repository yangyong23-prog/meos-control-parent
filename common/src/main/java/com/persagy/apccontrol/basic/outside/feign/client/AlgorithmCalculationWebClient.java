package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationDeleteDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationUpdateDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmStartCalculationDTO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import feign.Headers;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description: 算法平台
 * @author: yancheng
 * @date: 2023/3/27
 * @version: V1.0
 */
@FeignClient(name = "strategic",
        url = "${inner-url.ai.ai-au.service:}"
)
@RefreshScope
public interface AlgorithmCalculationWebClient {

    /**
     * 注册专家策略
     * @param algorithmCalculationLogonDTO
     * @return
     */
    @PostMapping({"/project-info/create"})
    @Headers({"acceptEncoding: gzip", "contentType: application/json"})
    DmpResult<String> logonAlgorithmCalculation(@RequestBody AlgorithmStartCalculationDTO algorithmCalculationLogonDTO);

    /**
     * 修改专家策略
     * @param algorithmCalculationUpdateDTO
     * @return
     */
    @PostMapping({"/expert-experience/create"})
    @Headers({"acceptEncoding: gzip", "contentType: application/json"})
    DmpResult<String> updateAlgorithmCalculation(@RequestBody AlgorithmCalculationUpdateDTO algorithmCalculationUpdateDTO);

    /**
     * 删除专家策略
     * @param algorithmCalculationDeleteDTO
     * @return
     */
    @PostMapping({"/project-info/delete"})
    @Headers({"acceptEncoding: gzip", "contentType: application/json"})
    DmpResult<String> deleteAlgorithmCalculation(@RequestBody AlgorithmCalculationDeleteDTO algorithmCalculationDeleteDTO);

}
