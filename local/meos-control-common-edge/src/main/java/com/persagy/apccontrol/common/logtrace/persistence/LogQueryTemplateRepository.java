package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;

import java.util.Collection;
import java.util.List;

public interface LogQueryTemplateRepository extends IService<LogQueryTemplatePo> {
    List<LogQueryTemplatePo> queryAllTopQueryTemplate(Long groupId,String application);

    List<LogQueryTemplatePo> queryAllTopQueryTemplateByGroupId(Long groupId);
    List<LogQueryTemplatePo> queryAllQueryTemplate(Collection<Long> groupId,String application);
    List<LogQueryTemplatePo> queryTemplateByIds(Collection<Long> ids);
    List<LogQueryTemplatePo> queryTemplateByGroupIds(Collection<Long> groups);
}
