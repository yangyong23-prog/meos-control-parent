package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderExtractKeyPo;

import java.util.List;

public interface LogQueryTemplateHeaderExtractKeyRepository extends IService<LogQueryTemplateHeaderExtractKeyPo> {

    List<LogQueryTemplateHeaderExtractKeyPo> getLogHeaderExtractByTemplateId(Long templateId);
}
