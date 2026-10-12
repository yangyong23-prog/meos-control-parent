package com.persagy.apccontrol.basic.config.properties;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置文件
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@ConfigurationProperties(prefix = RabbitProperties.KEY)
@RefreshScope
@FieldNameConstants
public class RabbitProperties {

    public static final String KEY = ProjectProperties.KEY + ".rabbit";
    public static final String LISTENERS = KEY + "." + "listeners";

    public static final String LISTENER_LIGHTING_CONFIG_CHANGE = "lighting-config-change";

    /**
     * 监听者
     */
    private Map<String, Listener> listeners = new HashMap<>(16);

    /**
     * 监听器
     *
     * @author JiYinchuan
     * @since 5.240402.0
     */
    @Data
    public static class Listener {

        public static final Level DEFAULT_MESSAGE_LOG_LEVEL = Level.INFO;

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 队列名称
         */
        private String queue;

        /**
         * 交换机名称
         */
        private String exchange;

        /**
         * 路由名称
         */
        private String key;

        /**
         * 是否持久化
         */
        private boolean durable = true;

        /**
         * 是否自动删除
         */
        private boolean autoDelete = false;

        /**
         * 消息日志级别
         */
        private Level messageLogLevel = DEFAULT_MESSAGE_LOG_LEVEL;

    }
}
