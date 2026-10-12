package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateRelationMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateRelationPo;
import org.springframework.stereotype.Repository;

@Repository
public class LogQueryTemplateRelationRepositoryImpl extends ServiceImpl<LogQueryTemplateRelationMapper, LogQueryTemplateRelationPo> implements LogQueryTemplateRelationRepository{
}
