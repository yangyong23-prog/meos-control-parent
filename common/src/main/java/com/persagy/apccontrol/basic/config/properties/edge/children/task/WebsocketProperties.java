package com.persagy.apccontrol.basic.config.properties.edge.children.task;

import com.persagy.apccontrol.basic.config.properties.edge.EdgeProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Websocket配置文件
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = WebsocketProperties.KEY)
public class WebsocketProperties {

    public static final String KEY = EdgeProperties.KEY + ".websocket";

    /**
     * 是否启用
     */
    private boolean enabled = true;

}
