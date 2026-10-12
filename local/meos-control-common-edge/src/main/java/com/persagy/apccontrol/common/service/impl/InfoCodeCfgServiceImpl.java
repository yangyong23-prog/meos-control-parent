package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import com.persagy.apccontrol.common.service.InfoCodeCfgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * @author shaohongbo
 * @date 2023/4/4 19:12
 */
@Slf4j
@Service
public class InfoCodeCfgServiceImpl implements InfoCodeCfgService {

    @Autowired
    private RcInfoCodeCfgService rcInfoCodeCfgService;


    @Override
    @Cacheable(unless = "#result == null", value = CacheKey.Dynamic.INFO_CODE_CFG, key = "#classCode+'-'+#infoCode", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER)
    public RcInfoCodeCfg queryOneByInfCode(@NotNull String classCode, @NotNull String infoCode) {
        return rcInfoCodeCfgService.lambdaQuery().eq(RcInfoCodeCfg::getClassCode, classCode).eq(RcInfoCodeCfg::getInfoCode, infoCode).last(CommonConst.LIMIT_ONE).one();
    }
}




