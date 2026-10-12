package com.persagy.apccontrol.basic.config.properties.saas;

import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 云端配置文件
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ConfigurationProperties(prefix = SaasProperties.KEY)
@RefreshScope
public class SaasProperties {

    public static final String KEY = ProjectProperties.KEY + ".saas";

    /**
     * 需求管理配置文件
     */
    @NestedConfigurationProperty
    private DemandProperties demand = new DemandProperties();

}
