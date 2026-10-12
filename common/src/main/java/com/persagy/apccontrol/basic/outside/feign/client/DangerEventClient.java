package com.persagy.apccontrol.basic.outside.feign.client;


import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RefreshScope
@Validated
@Primary
@Api(
        value = "DangerEventClient",
        hidden = true
)
@FeignClient(
        name = "${persagy.serviceName.yihengCompute:yiheng-compute-server}",
        //path = "${persagy.service.path.exp:}",
        contextId = "danger-event",
        url = "${inner-url.bdtp.yiheng-compute.service:}"
)
public interface DangerEventClient {

    /**
     * 查询风险事件列表
     *
     * @param body 请求体
     * @return 异常事件列表
     * @since 5.241101.0
     */
    @PostMapping("/rwd/open-api/risk-event-instance/list")
    CustomerResponse<List<DangerEventInstance>> listDangerEvents(@RequestBody DangerEventQueryParam body);

}
