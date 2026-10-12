package com.persagy.apccontrol.basic.resource.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.mapper.StrategyDateCfgMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.resource.StrategyDateCfgService;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyDateCfgQueryDaoParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 策略日期表服务实现
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Service
public class StrategyDateCfgServiceImpl extends ServiceImpl<StrategyDateCfgMapper, StrategyDateCfg> implements StrategyDateCfgService {

    @Override
    public int count(@NonNull StrategyDateCfgQueryDaoParam param) {
        return getBaseMapper().count(param);
    }

    @Override
    public List<LocalDate> statisticDateInValid(@NonNull Collection<Long> strategyIds) {
        if (CollectionUtils.isEmpty(strategyIds)) {
            return Lists.newArrayList();
        }
        return getBaseMapper().statisticDateInValid(strategyIds);
    }

    @Override
    public List<String> listIds(@NonNull StrategyDateCfgQueryDaoParam param) {
        return getBaseMapper().listIds(param);
    }

    @Override
    public List<StrategyDateCfg> list(@NonNull StrategyDateCfgQueryDaoParam param) {
        return getBaseMapper().list(param);
    }

    @Override
    public List<StrategyDateCfg> listNotSynced(@NonNull StrategyDateCfgQueryDaoParam param) {
        return getBaseMapper().listNotSynced(param);
    }

    @Override
    public int delByStrategyId(@NonNull Collection<Long> strategyIds) {
        return getBaseMapper().delByStrategyId(strategyIds);
    }
}
