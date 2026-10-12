package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderPo;

import java.util.Collection;
import java.util.List;

public interface LogQueryTemplateHeaderRepository extends IService<LogQueryTemplateHeaderPo> {

   List<LogQueryTemplateHeaderPo> getHeaderByIds(Collection<Long> ids);

   List<LogQueryTemplateHeaderPo> getParentDynamicHeaderByIds(Collection<Long> ids);

   List<LogQueryTemplateHeaderPo> getHeaderByTemplateId(Long templateId);

   List<LogQueryTemplateHeaderPo> queryAllTopQueryTemplateHeaderByTemplateId(Long templateId);

}
