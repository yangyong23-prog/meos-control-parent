package com.persagy.apccontrol.basic.outside.feign.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.dto.ElectricityPriceQueryParam;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.client.DtpElectricityDataClient;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData.CarbonCategoryDataParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData.CarbonCategoryDataResponse;
import com.persagy.apccontrol.basic.outside.feign.service.ElectricityDataClientService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.persagy.apccontrol.basic.model.entity.RcBasicsConfig.LOG_TAG;

/**
 * 运维平台客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
@Slf4j
public class ElectricityDataClientServiceImpl implements ElectricityDataClientService {

    @Autowired
    private DtpElectricityDataClient dtpElectricityDataClient;
    @Autowired
    private DtpPersistServerClient dtpPersistServerClient;

    @Override
    public List<EnergyDataResponse> listElectrovalenceData(@NonNull ElectricityPriceQueryParam param) {
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance();
        return query(false, LocaleMessageUtil.getMessage("key_3f1af6004d424cad9c030e1795af3055"), param, () -> dtpPersistServerClient
                .queryProjectElecPrice(commonParam, param), ArrayList::new);
    }


    @Override
    public List<CarbonCategoryDataResponse.CarBonData> listCarbonCategoryData(CarbonCategoryDataParam param) {
        DmpResult<CarbonCategoryDataResponse> result = dtpElectricityDataClient.queryCarbonCategoryData(param);
        if (result != null && result.getData() != null) {
            return result.getData().getDataList();
        }
        return null;
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
