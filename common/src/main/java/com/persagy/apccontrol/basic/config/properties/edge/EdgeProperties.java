package com.persagy.apccontrol.basic.config.properties.edge;

import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.edge.children.AiProperties;
import com.persagy.apccontrol.basic.config.properties.edge.children.task.TaskProperties;
import com.persagy.apccontrol.basic.config.properties.edge.children.task.WebsocketProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 边缘端配置文件
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ConfigurationProperties(prefix = EdgeProperties.KEY)
@RefreshScope
public class EdgeProperties {

    public static final String KEY = ProjectProperties.KEY + ".edge";

    /**
     * AI配置文件
     */
    @NestedConfigurationProperty
    private AiProperties ai = new AiProperties();

    /**
     * Websocket任务配置
     */
    @NestedConfigurationProperty
    private WebsocketProperties websocket = new WebsocketProperties();

    /**
     * 定时任务配置
     */
    @NestedConfigurationProperty
    private TaskProperties task = new TaskProperties();

}
