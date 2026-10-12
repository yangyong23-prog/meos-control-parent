package com.persagy.apccontrol.common.outside.feign;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 类功能描述
 *
 * @author wangtaofang
 * @version 1.0.0
 * @company persagy
 */
@RefreshScope
@FeignClient(name = "chiller-plant", url = "${inner-url.bdtp.ai-au.service:}")
public interface AlgorithmChillerPlantFeignClient {

    /**
     * 检查ai策略是否靠谱
     * @param param
     * @return
     */
    @PostMapping(value = {"/ai-au-server-edge/model_eval_result"})
    CustomerResponse<Boolean> modelEvalResult(@RequestBody JSONObject param);

}
