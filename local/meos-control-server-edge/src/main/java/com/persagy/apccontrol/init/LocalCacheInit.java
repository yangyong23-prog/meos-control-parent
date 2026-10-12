package com.persagy.apccontrol.init;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.properties.edge.children.task.WebsocketProperties;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.model.entity.WebsocketConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.IotMonitorConfig;
import com.persagy.apccontrol.basic.model.enumtype.FunctionTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.WebsocketConfigService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.common.config.SystemInitService;
import com.persagy.apccontrol.common.handle.ResetLocalRedisCacheHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地缓存 init，每次重启填充
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 9:51
 */
@Slf4j
@Component
@Order(-50)
public class LocalCacheInit implements SystemInitService {

    @Value("${update.system.graph:true}")
    private Boolean updateSystemGraph;
    
    @Autowired
    private WebsocketConfigService websocketConfigService;
    
    @Autowired
    private InfoCodeConfigResource infoCodeConfigResource;

    @Autowired
    private ResetLocalRedisCacheHandler resetLocalRedisCacheHandler;
    @Autowired
    private CommonConfigService commonConfigService;
    @Autowired
    private WebsocketProperties websocketProperties;
    
    @Override
    public void init() {
        // 1.更新系统相关缓存
        this.resetLocalRedisCacheHandler.resetLocalRedisCache(updateSystemGraph);
        this.commonConfigService.refreshCacheData();
        
        // 2.加载websocket 推送配置数据
        this.loadWebsocketConfig();
        
   
    }
    
    /**
     * 加载websocket推送配置数据
     */
    private void loadWebsocketConfig() {
        if (!websocketProperties.isEnabled()) {
            return;
        }
        List<WebsocketConfig> websocketConfigs = this.websocketConfigService.queryAllWebsocketConfigs(null);
        if (CollectionUtil.isEmpty(websocketConfigs)) {
            return;
        }
        
        for (WebsocketConfig websocketConfig : websocketConfigs) {
            FunctionTypeEnum functionType = websocketConfig.getFunctionType();
            if (functionType != null) {
                LocalCacheConstant.WEBSOCKET_CONFIG.computeIfAbsent(functionType.getCode(), v -> Sets.newHashSet())
                                                   .add(WebsocketConfig.getUniqueKey(websocketConfig.getClassCode(),
                                                           websocketConfig.getInfoCode()));
            }
        }
        
        // 添加模版配置相关数据
        List<IotMonitorConfig> infoCodeConfigList = infoCodeConfigResource.listIotMonitorConfig();
        for (IotMonitorConfig iotMonitorConfig : infoCodeConfigList) {
            RunningMonitorTypeEnum runningMonitorType = iotMonitorConfig.getRunningMonitorType();
            if (runningMonitorType != null) {
                LocalCacheConstant.WEBSOCKET_CONFIG.computeIfAbsent(runningMonitorType.getValue(),
                                          v -> Sets.newHashSet())
                                                   .add(WebsocketConfig.getUniqueKey(iotMonitorConfig.getClassCode(),
                                                           iotMonitorConfig.getInfoCode()));
            }
        }
    }
    
    
    @Override
    public String getName() {
        return LocaleMessageUtil.getMessage("key_c1729aaa07cd4ac69fa4ef827ccbddc4");
    }
}
