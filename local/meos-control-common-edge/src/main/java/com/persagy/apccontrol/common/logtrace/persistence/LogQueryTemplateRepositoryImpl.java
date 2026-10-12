package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Repository
public class LogQueryTemplateRepositoryImpl extends ServiceImpl<LogQueryTemplateMapper, LogQueryTemplatePo> implements LogQueryTemplateRepository {

    @Override
    public List<LogQueryTemplatePo> queryAllTopQueryTemplate(Long groupId, String application) {
        return this.getBaseMapper().queryAllTopTemplate(groupId, application);
    }

    @Override
    public List<LogQueryTemplatePo> queryAllTopQueryTemplateByGroupId(Long groupId) {
        return this.getBaseMapper().queryAllTopTemplateByGroupId(groupId);
    }

    @Override
    public List<LogQueryTemplatePo> queryTemplateByIds(Collection<Long> ids) {
        return this.getBaseMapper().selectBatchIds(ids);
    }

    @Override
    public List<LogQueryTemplatePo> queryTemplateByGroupIds(Collection<Long> groupIds) {
        LambdaQueryWrapper<LogQueryTemplatePo> queryWrapper = new LambdaQueryWrapper<LogQueryTemplatePo>();
        queryWrapper.in(LogQueryTemplatePo::getGroupId, groupIds);
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<LogQueryTemplatePo> queryAllQueryTemplate(Collection<Long> groupIds, String application) {
        LambdaQueryWrapper<LogQueryTemplatePo> queryWrapper = new LambdaQueryWrapper<LogQueryTemplatePo>();
        queryWrapper.in(!CollectionUtils.isEmpty(groupIds),LogQueryTemplatePo::getGroupId,groupIds)
                .eq(StringUtils.hasLength(application),LogQueryTemplatePo::getApplication,application);
        return this.getBaseMapper().selectList(queryWrapper);
    }
}
