package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogNodeExtendMapper;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class LogNodeExtendRepositoryImpl extends ServiceImpl<LogNodeExtendMapper, LogNodeExtendPo> implements LogNodeExtendRepository {
    @Override
    public List<LogNodeExtendPo> listByNodeIds(Collection<Long> nodeIds) {
        LambdaQueryWrapper<LogNodeExtendPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(LogNodeExtendPo::getNodeId, nodeIds);
        return getBaseMapper().selectList(queryWrapper);
    }
}
