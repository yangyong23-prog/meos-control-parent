package com.persagy.apccontrol.basic.config.properties.gray;

import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 灰度配置文件
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@ConfigurationProperties(prefix = GrayProperties.KEY)
@RefreshScope
public class GrayProperties {

    public static final String KEY = ProjectProperties.KEY + "." + "gray";

    /**
     * 是否启用灰度版本
     */
    private boolean enabled = false;

    /**
     * Nacos集群
     */
    private String nacosCluster;

}
