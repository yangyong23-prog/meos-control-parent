package com.persagy.apccontrol.basic.resource.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.StrategyCfgMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyCfg;
import com.persagy.apccontrol.basic.resource.StrategyCfgService;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyCfgQueryDaoParam;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 策略配置表服务实现
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Service
public class StrategyCfgServiceImpl extends ServiceImpl<StrategyCfgMapper, StrategyCfg> implements StrategyCfgService {

    @Override
    public StrategyCfg get(@NonNull StrategyCfgQueryDaoParam param) {
        return getBaseMapper().get(param);
    }

    @Override
    public List<Long> listIds(@NonNull StrategyCfgQueryDaoParam param) {
        return getBaseMapper().listIds(param);
    }

    @Override
    public List<StrategyCfg> list(@NonNull StrategyCfgQueryDaoParam param) {
        return getBaseMapper().list(param);
    }

    @Override
    public List<StrategyCfg> listNotSynced(@NonNull StrategyCfgQueryDaoParam param) {
        return getBaseMapper().listNotSynced(param);
    }
}
