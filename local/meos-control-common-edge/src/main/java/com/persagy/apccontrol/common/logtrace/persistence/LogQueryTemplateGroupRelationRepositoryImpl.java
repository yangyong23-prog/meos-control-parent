package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateGroupRelationMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupRelationPo;
import org.springframework.stereotype.Repository;

@Repository
public class LogQueryTemplateGroupRelationRepositoryImpl extends ServiceImpl<LogQueryTemplateGroupRelationMapper, LogQueryTemplateGroupRelationPo> implements LogQueryTemplateGroupRelationRepository{
}
