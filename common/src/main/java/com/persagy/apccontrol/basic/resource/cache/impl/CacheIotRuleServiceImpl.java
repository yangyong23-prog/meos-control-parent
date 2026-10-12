package com.persagy.apccontrol.basic.resource.cache.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.cache.CacheIotRuleMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.resource.cache.CacheIotRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 缓存-IOT规则服务实现
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheIotRuleServiceImpl
        extends BaseServiceImpl<CacheIotRuleMapper, CacheIotRule>
        implements CacheIotRuleService {

}
