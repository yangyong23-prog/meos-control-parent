package com.persagy.apccontrol.basic.config.properties.edge.children.task;

import com.persagy.apccontrol.basic.config.properties.edge.EdgeProperties;
import com.persagy.apccontrol.basic.config.properties.edge.children.task.child.LightingFeedbackAdjustTaskProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 任务配置文件
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
@ConfigurationProperties(prefix = TaskProperties.KEY)
@RefreshScope
public class TaskProperties {

    public static final String KEY = EdgeProperties.KEY + ".task";

    /**
     * 照明反馈调节定时任务配置
     */
    @NestedConfigurationProperty
    private LightingFeedbackAdjustTaskProperties lightingFeedbackAdjust = new LightingFeedbackAdjustTaskProperties();

}
