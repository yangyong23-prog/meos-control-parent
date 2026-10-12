package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateHeaderRelationMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderRelationPo;
import org.springframework.stereotype.Repository;

@Repository
public class LogQueryTemplateHeaderRelationRepositoryImpl extends ServiceImpl<LogQueryTemplateHeaderRelationMapper, LogQueryTemplateHeaderRelationPo> implements LogQueryTemplateHeaderRelationRepository{
}
