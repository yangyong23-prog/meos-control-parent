package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.framework.database.service.SuperService;

import java.util.List;

/**
 * WEBSOCKET推送配置
 */
public interface WebsocketConfigService extends SuperService<WebsocketConfig> {

    /**
     * 获取当前项目所有的信息点配置
     *
     * @param functionType
     * @return
     */
    List<WebsocketConfig> queryAllWebsocketConfigs(String functionType);

}
