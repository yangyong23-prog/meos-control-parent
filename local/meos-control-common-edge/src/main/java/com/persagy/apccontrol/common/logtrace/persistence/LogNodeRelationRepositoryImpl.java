package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.persagy.apccontrol.common.logtrace.mapper.LogNodeRelationMapper;
import com.persagy.apccontrol.common.logtrace.po.LogNodeRelationPo;
import org.springframework.stereotype.Repository;

@Repository
public class LogNodeRelationRepositoryImpl extends ServiceImpl<LogNodeRelationMapper, LogNodeRelationPo> implements LogNodeRelationRepository {
}
