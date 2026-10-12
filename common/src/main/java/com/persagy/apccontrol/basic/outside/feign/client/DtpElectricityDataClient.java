package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.ElectrovalenceDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.ElectrovalenceDataResponse;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 查询电力数据
 */
@FeignClient(name = "dtp-electricity-server", url = "${inner-url.bdtp.dtp-electricity.service:}")
@RefreshScope
@Validated
public interface DtpElectricityDataClient {

    /**
     * 查询电价数据
     * @param param
     * @return
     */
    @PostMapping({"/electrovalence/queryElectrovalenceData"})
    DmpResult<List<ElectrovalenceDataResponse>> queryElectrovalenceData(@RequestBody ElectrovalenceDataParam param);

    /**
     * 查询碳因子数据
     * @param param
     * @return
     */
    @PostMapping({"/carbon/queryCategoryData"})
    DmpResult<CarbonCategoryDataResponse> queryCarbonCategoryData(@RequestBody CarbonCategoryDataParam param);
}
