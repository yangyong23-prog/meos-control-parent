package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 运维平台客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface ElectricityDataClientService extends BaseFeignClientService {

    /**
     * 查询电价数据
     * @param param
     * @return
     */
    List<EnergyDataResponse> listElectrovalenceData(@NonNull ElectricityPriceQueryParam param);

    /**
     * 查询碳因子数据
     * @param param
     * @return
     */
    List<CarbonCategoryDataResponse.CarBonData> listCarbonCategoryData(@NonNull CarbonCategoryDataParam param);

}
