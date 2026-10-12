package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupPo;

import java.util.Collection;
import java.util.List;

public interface LogQueryTemplateGroupRepository extends IService<LogQueryTemplateGroupPo> {

    List<LogQueryTemplateGroupPo> getLogQueryGroupByIds(Collection<Long> ids);

    List<LogQueryTemplateGroupPo> queryAllTopGroupByApp(String application);
    List<LogQueryTemplateGroupPo> queryAllTopGroup();
    List<LogQueryTemplateGroupPo> queryAllGroup(String application);
}
