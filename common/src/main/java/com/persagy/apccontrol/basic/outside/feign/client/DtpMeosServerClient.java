//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.persagy.apccontrol.basic.outside.feign.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosCircuitDemandParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcCommandSendDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * dtp-twin-server-edge 服务
 */
@FeignClient(name = "dtp-meos-server-edge", url = "${inner-url.bdtp.dtp-meos.service:}")
@RefreshScope
public interface DtpMeosServerClient {

    /**
     * 手动进行指令下发
     */
    @PostMapping({"/running-monitor/setOperatingParameters"})
    void setOperatingParameters(@RequestBody TwcInstructionSend dto);

    /**
     * 操作-单点控制
     */
    @PostMapping({"/commandExecute/commandSend"})
    void commandSend(@RequestBody TwcCommandSendDTO dto);

    /**
     * 计算-手动触发照明需求计算
     *
     * @param body 请求参数
     * @since 7.260101.0
     */
    @PostMapping("/calculate/lighting/circuitDemand")
    JsonNode circuitDemand(@RequestBody DtpMeosCircuitDemandParam body);

}
