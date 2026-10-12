package com.persagy.apccontrol.basic.resource.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.StrategyPlanCfgMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.apccontrol.basic.resource.StrategyPlanCfgService;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyPlanCfgQueryDaoParam;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 策略计划表服务实现
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Service
public class StrategyPlanCfgServiceImpl extends ServiceImpl<StrategyPlanCfgMapper, StrategyPlanCfg> implements StrategyPlanCfgService {

    @Override
    public int count(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().count(param);
    }

    @Override
    public StrategyPlanCfg get(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().get(param);
    }

    @Override
    public StrategyPlanCfg getTempPlan(@NonNull Long strategyId, @NonNull String controlFieldName) {
        return getBaseMapper().getTempPlan(strategyId, controlFieldName);
    }

    @Override
    public List<Long> listIds(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().listIds(param);
    }

    @Override
    public List<StrategyPlanCfg> list(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().list(param);
    }

    @Override
    public List<StrategyPlanCfg> listTempPlanAtNow(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().listTempPlanAtNow(param);
    }

    @Override
    public List<StrategyPlanCfg> listNotSynced(@NonNull StrategyPlanCfgQueryDaoParam param) {
        return getBaseMapper().listNotSynced(param);
    }
}
