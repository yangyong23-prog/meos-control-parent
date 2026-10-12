package com.persagy.apccontrol.basic.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/4/28 15:33
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("executor.pool")
public class ExecutorProperties {

    /** sse 数据推送 所使用的线程池 */
    private Config sse = new Config();

    @Getter
    @Setter
    @ToString
    public class Config {

        private int corePoolSize = 5;

        private int maxPoolSize = 20;

        private int queueCapacity = 1024;

        private int keepAliveSeconds = 300;
    }

}
