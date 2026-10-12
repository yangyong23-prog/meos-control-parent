package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.mapper.WebsocketConfigMapper;
import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.apccontrol.basic.resource.WebsocketConfigService;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * WEBSOCKET推送配置
 */
@Service
public class WebsocketConfigServiceImpl extends SuperServiceImpl<WebsocketConfigMapper, WebsocketConfig> implements WebsocketConfigService {

    @Override
    public List<WebsocketConfig> queryAllWebsocketConfigs(String functionType) {
        return this.lambdaQuery().eq(StrUtil.isNotBlank(functionType), WebsocketConfig::getFunctionType, functionType).list();
    }

}
