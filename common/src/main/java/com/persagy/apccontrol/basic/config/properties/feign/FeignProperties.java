package com.persagy.apccontrol.basic.config.properties.feign;

import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.feign.children.FeignRequestStatisticsProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Feign配置文件
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@ConfigurationProperties(prefix = FeignProperties.KEY)
@RefreshScope
public class FeignProperties {

    public static final String KEY = ProjectProperties.KEY + ".feign";

    /**
     * 请求统计
     */
    @NestedConfigurationProperty
    private FeignRequestStatisticsProperties requestStatistics = new FeignRequestStatisticsProperties();

}
