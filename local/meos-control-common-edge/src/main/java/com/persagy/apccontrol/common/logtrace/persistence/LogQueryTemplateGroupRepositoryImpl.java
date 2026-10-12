package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateGroupMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupPo;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Repository
public class LogQueryTemplateGroupRepositoryImpl extends ServiceImpl<LogQueryTemplateGroupMapper, LogQueryTemplateGroupPo> implements LogQueryTemplateGroupRepository {


    @Override
    public List<LogQueryTemplateGroupPo> getLogQueryGroupByIds(Collection<Long> ids) {
        return this.listByIds(ids);
    }

    @Override
    public List<LogQueryTemplateGroupPo> queryAllTopGroupByApp(String application) {
        return getBaseMapper().queryAllTopGroupByApp(application);
    }

    @Override
    public List<LogQueryTemplateGroupPo> queryAllTopGroup() {
        return getBaseMapper().queryAllTopGroup();
    }

    @Override
    public List<LogQueryTemplateGroupPo> queryAllGroup(String application) {
        LambdaQueryWrapper<LogQueryTemplateGroupPo> queryWrapper = new LambdaQueryWrapper<LogQueryTemplateGroupPo>();
        queryWrapper.eq(StringUtils.hasLength(application), LogQueryTemplateGroupPo::getApplication, application);
        return getBaseMapper().selectList(queryWrapper);

    }
}
