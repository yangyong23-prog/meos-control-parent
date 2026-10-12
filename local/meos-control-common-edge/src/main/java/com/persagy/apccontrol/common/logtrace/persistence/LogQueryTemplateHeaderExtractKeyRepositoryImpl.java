package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateHeaderExtractKeyMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderExtractKeyPo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LogQueryTemplateHeaderExtractKeyRepositoryImpl extends ServiceImpl<LogQueryTemplateHeaderExtractKeyMapper, LogQueryTemplateHeaderExtractKeyPo> implements LogQueryTemplateHeaderExtractKeyRepository{


    @Override
    public List<LogQueryTemplateHeaderExtractKeyPo> getLogHeaderExtractByTemplateId(Long templateId) {
       return getBaseMapper().getLogHeaderExtractByTemplateId(templateId);
    }
}
