package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateHeaderMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderPo;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class LogQueryTemplateHeaderRepositoryImpl extends ServiceImpl<LogQueryTemplateHeaderMapper, LogQueryTemplateHeaderPo> implements LogQueryTemplateHeaderRepository{

    @Override
    public List<LogQueryTemplateHeaderPo> getHeaderByIds(Collection<Long> ids) {
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return this.listByIds(ids);
    }

    @Override
    public List<LogQueryTemplateHeaderPo> getHeaderByTemplateId(Long templateId) {
        LambdaQueryWrapper<LogQueryTemplateHeaderPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogQueryTemplateHeaderPo::getQueryTemplateId,templateId);
        return getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<LogQueryTemplateHeaderPo> queryAllTopQueryTemplateHeaderByTemplateId(Long templateId) {

        return getBaseMapper().queryAllTopQueryTemplateHeaderByTemplateId(templateId);
    }

    @Override
    public List<LogQueryTemplateHeaderPo> getParentDynamicHeaderByIds(Collection<Long> ids) {
        return getBaseMapper().getParentHeadersByIds(ids);
    }
}
