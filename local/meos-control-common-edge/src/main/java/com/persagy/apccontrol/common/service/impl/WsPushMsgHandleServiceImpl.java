package com.persagy.apccontrol.common.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.config.VelocityConfig;
import com.persagy.apccontrol.common.model.dto.PageBroadcastMsgDto;
import com.persagy.apccontrol.common.service.WsPushMsgHandleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WsPushMsgHandleServiceImpl implements WsPushMsgHandleService {

    @Autowired
    private RcBasicsConfigService rcBasicsConfigService;

    @Override
    public String getContent(PageBroadcastMsgDto msgDto) {
        //根据信息匹配对应得模板，拼接播报内容
        RcBasicsConfig config = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getType, msgDto.getOperateType())
                .eq(RcBasicsConfig::getSt, StateTypeEnum.VALID)
                .last(CommonConst.LIMIT_ONE)
                .one();
        if (config == null) {
            return null;
        }
        String content = null;
        try {
            content = VelocityConfig.getMessageByTemplate(msgDto, config.getResult());
        } catch (JsonProcessingException ex) {
            log.error("VelocityConfig.getMessageByTemplate error:{}", ExceptionUtils.getStackTrace(ex));
        }
        return content;
    }
}
