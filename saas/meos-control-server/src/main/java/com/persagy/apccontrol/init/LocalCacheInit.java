package com.persagy.apccontrol.init;

import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 15:41
 */
@Slf4j
@Component
@Order(-50)
public class LocalCacheInit implements CommandLineRunner {

    @Value("${update.system.graph:true}")
    private Boolean updateSystemGraph;
    
    @Autowired
    private CommonConfigService commonConfigService;
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Override
    public void run(String... args) {
        try {
            this.localRedisCacheHandler.resetGlobalLocalRedisCache(TerminalEnum.CLOUD, updateSystemGraph);
 
            this.commonConfigService.refreshCacheData();
        } catch (Exception e) {
            log.error("本地缓存刷新失败", e);
        }
    }

}
