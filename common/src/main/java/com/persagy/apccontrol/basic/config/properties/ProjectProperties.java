package com.persagy.apccontrol.basic.config.properties;

import com.persagy.apccontrol.basic.config.properties.debug.DebugInterceptorProperties;
import com.persagy.apccontrol.basic.config.properties.edge.EdgeProperties;
import com.persagy.apccontrol.basic.config.properties.gray.GrayProperties;
import com.persagy.apccontrol.basic.config.properties.saas.SaasProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 项目配置文件
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@ConfigurationProperties(prefix = ProjectProperties.KEY)
@RefreshScope
public class ProjectProperties {

    public static final String KEY = "project";

    /**
     * 调试拦截器配置文件
     */
    @NestedConfigurationProperty
    private DebugInterceptorProperties debugInterceptor = new DebugInterceptorProperties();

    /**
     * 请求统计配置文件
     */
    @NestedConfigurationProperty
    private RequestStatisticsProperties requestStatistics = new RequestStatisticsProperties();

    /**
     * 灰度配置文件
     */
    @NestedConfigurationProperty
    private GrayProperties gray = new GrayProperties();

    /**
     * Rabbit配置文件
     */
    @NestedConfigurationProperty
    private RabbitProperties rabbit = new RabbitProperties();

    /**
     * 云端配置文件
     */
    @NestedConfigurationProperty
    private SaasProperties saas = new SaasProperties();

    /**
     * 边缘端配置文件
     */
    @NestedConfigurationProperty
    private EdgeProperties edge = new EdgeProperties();

}
