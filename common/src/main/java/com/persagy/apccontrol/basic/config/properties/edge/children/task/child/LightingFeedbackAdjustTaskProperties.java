package com.persagy.apccontrol.basic.config.properties.edge.children.task.child;

import com.persagy.apccontrol.basic.config.properties.edge.children.task.TaskProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 照明反馈调节下发任务配置文件
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
@ConfigurationProperties(prefix = LightingFeedbackAdjustTaskProperties.KEY)
public class LightingFeedbackAdjustTaskProperties {

    public static final String KEY = TaskProperties.KEY + ".lighting-feedback-adjust";

    /**
     * 是否启用
     */
    private boolean enabled = true;

}
