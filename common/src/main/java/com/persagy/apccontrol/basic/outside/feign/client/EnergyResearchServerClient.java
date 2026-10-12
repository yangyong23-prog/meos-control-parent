package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.feign.model.param.energyresearch.EnergyModelItemParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.energyresearch.EnergyItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 能源调研
 * @author muanfu
 **/
@FeignClient(name = "energy-research-server", url = "${inner-url.energy.energy-research.service:}")
public interface EnergyResearchServerClient {

    /**
     * 查询能源模型
     *
     * @param param param
     * @return {@link String }
     */
    @PostMapping("/common/api/project/energy/model/item/tag/filter")
    CustomerResponse<List<EnergyItemResponse>> queryEnergyModelItem(@RequestBody EnergyModelItemParam param);

}
