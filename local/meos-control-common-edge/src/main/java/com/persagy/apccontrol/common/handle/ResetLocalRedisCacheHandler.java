package com.persagy.apccontrol.common.handle;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 边缘端缓存加载
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/16 15:07
 */
@Slf4j
@Component
public class ResetLocalRedisCacheHandler {

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    /**
     * 边缘端缓存加载
     */
    public void resetLocalRedisCache(boolean updateCache) {
        try {
            // 边缘端独有的系统缓存
            this.localRedisCacheHandler.resetSystemCache(projectCfg.getGroupCode(), projectCfg.getProjectId());

            this.localRedisCacheHandler.resetGlobalLocalRedisCache(TerminalEnum.EDGE, updateCache);

        } catch (Exception e) {
            log.error("项目启动加载缓存 异常", e);
        }
    }

}
